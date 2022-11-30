/*
 * Copyright (C) 2015-2022 Igor A. Maznitsa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.igormaznitsa.mindmap.swing.panel.utils;

import static com.igormaznitsa.mindmap.swing.panel.StandardTopicAttribute.ATTR_BORDER_COLOR;
import static com.igormaznitsa.mindmap.swing.panel.StandardTopicAttribute.ATTR_COLLAPSED;
import static com.igormaznitsa.mindmap.swing.panel.StandardTopicAttribute.ATTR_FILL_COLOR;
import static com.igormaznitsa.mindmap.swing.panel.StandardTopicAttribute.ATTR_TEXT_COLOR;
import static javax.swing.SwingUtilities.windowForComponent;

import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.plugins.api.PluginContext;
import com.igormaznitsa.mindmap.swing.i18n.MmdI18n;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.swing.filechooser.FileFilter;

public final class MindMapUtils {

  private MindMapUtils() {
  }

  public static boolean isHidden(final Topic topic) {
    if (topic == null) {
      return true;
    }
    final String collapsed = topic.findAttributeInAncestors(ATTR_COLLAPSED.getText());
    return Boolean.parseBoolean(collapsed);
  }

  public static List<Color> findAllTopicColors(final MindMap map, final ColorType colorType) {
    final Set<Color> result = new HashSet<>();
    for (final Topic topic : map) {
      final Color color;
      switch (colorType) {
        case BORDER:
          color = Utils.html2color(topic.getAttribute(ATTR_BORDER_COLOR.getText()), false);
          break;
        case FILL:
          color = Utils.html2color(topic.getAttribute(ATTR_FILL_COLOR.getText()), false);
          break;
        case TEXT:
          color = Utils.html2color(topic.getAttribute(ATTR_TEXT_COLOR.getText()), false);
          break;
        default:
          throw new Error("Unexpected color type: " + colorType);
      }
      if (color != null) {
        result.add(color);
      }
    }
    return Arrays.asList(result.toArray(new Color[0]));
  }

  public static Topic findFirstVisibleAncestor(final Topic topic) {
    if (topic == null) {
      return null;
    }

    final Topic[] path = topic.getPath();

    Topic lastVisible = null;

    for (final Topic t : path) {
      lastVisible = t;
      final boolean collapsed = Boolean.parseBoolean(t.getAttribute(ATTR_COLLAPSED.getText()));
      if (collapsed) {
        break;
      }
    }

    return lastVisible;
  }

  public static boolean isTopicVisible(final Topic topic) {
    boolean result = true;

    Topic current = topic.getParent();
    while (current != null) {
      if (isCollapsed(current)) {
        result = false;
        break;
      }
      current = current.getParent();
    }
    return result;
  }

  public static boolean ensureVisibility(final Topic topic) {
    boolean result = false;

    Topic current = topic.getParent();
    while (current != null) {
      if (isCollapsed(current)) {
        result |= setCollapsed(current, false);
      }
      current = current.getParent();
    }
    return result;
  }

  public static boolean isCollapsed(final Topic topic) {
    return "true".equalsIgnoreCase(topic.getAttribute(ATTR_COLLAPSED.getText()));
  }

  public static boolean foldOrUnfoldChildren(final Topic topic, final boolean fold,
                                             final int levelCount) {
    boolean result = false;
    if (levelCount > 0 && !topic.isEmpty()) {
      for (final Topic c : topic) {
        result |= foldOrUnfoldChildren(c, fold, levelCount - 1);
      }
      result |= setCollapsed(topic, fold);
    }
    return result;
  }

  public static boolean setCollapsed(final Topic topic, final boolean fold) {
    return topic.putAttribute(ATTR_COLLAPSED.getText(), fold ? "true" : null);
  }

  public static void removeCollapseAttributeFromTopicsWithoutChildren(final MindMap map) {
    removeCollapseAttrIfNoChildren(map == null ? null : map.getRoot());
  }

  public static void removeCollapseAttr(final MindMap map) {
    _removeCollapseAttr(map.getRoot());
  }

  private static void _removeCollapseAttr(final Topic topic) {
    if (topic != null) {
      topic.putAttribute(ATTR_COLLAPSED.getText(), null);
      if (!topic.isEmpty()) {
        for (final Topic ch : topic.getChildren()) {
          _removeCollapseAttr(ch);
        }
      }
    }
  }

  public static void removeCollapseAttrIfNoChildren(final Topic topic) {
    if (topic != null) {
      if (topic.isEmpty()) {
        topic.putAttribute(ATTR_COLLAPSED.getText(), null);
      } else {
        for (final Topic t : topic.getChildren()) {
          removeCollapseAttrIfNoChildren(t);
        }
      }
    }
  }

  public static void copyColorAttributes(final Topic source, final Topic destination) {
    destination.putAttribute(ATTR_FILL_COLOR.getText(),
        source.getAttribute(ATTR_FILL_COLOR.getText()));
    destination.putAttribute(ATTR_BORDER_COLOR.getText(),
        source.getAttribute(ATTR_BORDER_COLOR.getText()));
    destination.putAttribute(ATTR_TEXT_COLOR.getText(),
        source.getAttribute(ATTR_TEXT_COLOR.getText()));
  }

  public static Color getBackgroundColor(final MindMapPanelConfig cfg, final Topic topic) {
    final Color extracted = Utils.html2color(topic.getAttribute(ATTR_FILL_COLOR.getText()), false);
    final Color result;
    if (extracted == null) {
      switch (topic.getTopicLevel()) {
        case 0: {
          result = cfg.getRootBackgroundColor();
        }
        break;
        case 1: {
          result = cfg.getFirstLevelBackgroundColor();
        }
        break;
        default: {
          result = cfg.getOtherLevelBackgroundColor();
        }
        break;
      }
    } else {
      result = extracted;
    }
    return result;
  }

  public static Color getTextColor(final MindMapPanelConfig cfg, final Topic topic) {
    final Color extracted = Utils.html2color(topic.getAttribute(ATTR_TEXT_COLOR.getText()), false);
    final Color result;
    if (extracted == null) {
      switch (topic.getTopicLevel()) {
        case 0: {
          result = cfg.getRootTextColor();
        }
        break;
        case 1: {
          result = cfg.getFirstLevelTextColor();
        }
        break;
        default: {
          result = cfg.getOtherLevelTextColor();
        }
        break;
      }
    } else {
      result = extracted;
    }
    return result;
  }

  public static Color getBorderColor(final MindMapPanelConfig cfg, final Topic topic) {
    final Color extracted =
        Utils.html2color(topic.getAttribute(ATTR_BORDER_COLOR.getText()), false);
    return extracted == null ? cfg.getElementBorderColor() : extracted;
  }

  public static File selectFileToSaveForFileFilter(final MindMapPanel panel,
                                                   final PluginContext pluginContext,
                                                   final String dialogId, final String title,
                                                   final File defaultFolder,
                                                   final String dottedFileExtension,
                                                   final String filterDescription,
                                                   final String approveButtonText) {
    final String lcExtension = dottedFileExtension.toLowerCase(Locale.ENGLISH);
    return panel.getController().getDialogProvider(panel).msgSaveFileDialog(
        windowForComponent(panel),
        pluginContext,
        dialogId,
        title,
        defaultFolder,
        true,
        new FileFilter[] {new FileFilter() {
          @Override
          public boolean accept(final File f) {
            return f.isDirectory() || (f.isFile() &&
                f.getName().toLowerCase(Locale.ENGLISH).endsWith(lcExtension));
          }

          @Override
          public String getDescription() {
            return filterDescription;
          }
        }}, approveButtonText);
  }

  public static File selectFileToOpenForFileFilter(final MindMapPanel panel,
                                                   final PluginContext pluginContext,
                                                   final String dialogId, final String title,
                                                   final File defaultFolder,
                                                   final String dottedFileExtension,
                                                   final String filterDescription,
                                                   final String approveButtonText) {
    final String lcExtension = dottedFileExtension.toLowerCase(Locale.ENGLISH);

    return panel.getController().getDialogProvider(panel).msgOpenFileDialog(
        windowForComponent(panel),
        pluginContext,
        dialogId,
        title,
        defaultFolder,
        true,
        new FileFilter[] {new FileFilter() {
          @Override
          public boolean accept(final File f) {
            return f.isDirectory() || (f.isFile() &&
                f.getName().toLowerCase(Locale.ENGLISH).endsWith(lcExtension));
          }

          @Override
          public String getDescription() {
            return filterDescription;
          }
        }}, approveButtonText);
  }

  public static File checkFileAndExtension(final MindMapPanel panel, final File file,
                                           final String dottedExtension) {
    if (file == null) {
      return null;
    }
    if (file.isDirectory()) {
      panel.getController().getDialogProvider(panel).msgError(null,
          String.format(MmdI18n.getInstance().findBundle().getString("AbstractMindMapExporter.msgErrorItIsDirectory"),
              file.getAbsolutePath()));
      return null;
    }
    if (file.isFile()) {
      if (!panel.getController().getDialogProvider(panel)
          .msgConfirmOkCancel(null, MmdI18n.getInstance().findBundle().getString("AbstractMindMapExporter.titleSaveAs"),
              String.format(
                  MmdI18n.getInstance().findBundle().getString("AbstractMindMapExporter.msgAlreadyExistsWantToReplace"),
                  file.getAbsolutePath()))) {
        return null;
      }
    } else if (!file.getName().toLowerCase(Locale.ENGLISH).endsWith(dottedExtension.toLowerCase(Locale.ENGLISH))) {
      if (panel.getController().getDialogProvider(panel).msgConfirmYesNo(null, MmdI18n.getInstance().findBundle().getString("AbstractMindMapExporter.msgTitleAddExtension"), String.format(
          MmdI18n.getInstance().findBundle().getString("AbstractMindMapExporter.msgAddExtensionQuestion"), dottedExtension))) {
        return new File(file.getParent(), file.getName() + dottedExtension);
      }
    }
    return file;
  }

  /**
   * Remove duplications and successors for presented topics in array.
   *
   * @param topics array to be processed
   * @return resulted array
   * @since 1.3.1
   */
  public static Topic[] removeSuccessorsAndDuplications(final Topic... topics) {
    final List<Topic> result = new ArrayList<>();

    for (final Topic t : topics) {
      result.removeIf(listed -> listed == t || listed.hasAncestor(t));
      result.add(t);
    }
    return result.toArray(new Topic[0]);
  }

  /**
   * Generate subtree for whitespace offsets in text lines.
   *
   * @param topic topic to be root for generated text
   * @param text  text source to make topics
   * @return same root topic provided as argument
   * @since 1.4.7
   */
  public static Topic makeSubTreeFromText(final Topic topic, final String text) {
    final String[] lines = Utils.breakToLines(text);

    if (lines.length == 0) {
      return topic;
    }

    int ignoredLeadingSpaces = Integer.MAX_VALUE;
    for (String s : lines) {
      if (s.trim().isEmpty()) {
        continue;
      }
      s = s.replace("\t", "    ");
      final int leadingSpacesNumber = s.length() - Utils.strip(s, true).length();
      ignoredLeadingSpaces = Math.min(leadingSpacesNumber, ignoredLeadingSpaces);
    }

    int maxLineLength = 0;
    for (int i = 0; i < lines.length; i++) {
      final String old = lines[i];
      if (old.trim().isEmpty()) {
        lines[i] = null;
      } else {
        lines[i] = old.substring(ignoredLeadingSpaces);
        maxLineLength = Math.max(lines[i].length(), maxLineLength);
      }
    }

    final Topic[] topics = new Topic[lines.length];
    final Set<Integer> justAddedSibling = new HashSet<>();
    for (int checkCharPosition = 0; checkCharPosition < maxLineLength; checkCharPosition++) {
      justAddedSibling.clear();
      for (int index = 0; index < lines.length; index++) {
        final String textStr = lines[index];
        if (textStr == null) {
          continue;
        }
        if (!Character.isWhitespace(textStr.charAt(checkCharPosition))) {
          lines[index] = null;
          Topic mostCloseParentTopic = null;
          for (int off = 1; index - off >= 0; off++) {
            if (!justAddedSibling.contains(index - off)) {
              if (topics[index - off] != null) {
                mostCloseParentTopic = topics[index - off];
                break;
              }
            }
          }
          if (mostCloseParentTopic == null) {
            for (int off = 1; index + off < topics.length; off++) {
              if (!justAddedSibling.contains(index + off)) {
                if (topics[index + off] != null) {
                  mostCloseParentTopic = topics[index + off];
                  break;
                }
              }
            }
          }

          final Topic newTopic = mostCloseParentTopic == null ? topic.makeChild(textStr.trim(), null) : mostCloseParentTopic.makeChild(textStr.trim(), null);
          topics[index] = newTopic;
          justAddedSibling.add(index);
        }
      }
    }

    return topic;
  }

  public enum ColorType {
    BORDER,
    FILL,
    TEXT
  }
}
