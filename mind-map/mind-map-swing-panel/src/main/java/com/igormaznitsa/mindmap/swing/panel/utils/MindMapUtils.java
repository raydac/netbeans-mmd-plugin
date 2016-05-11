/*
 * Copyright 2015 Igor Maznitsa.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.igormaznitsa.mindmap.swing.panel.utils;

import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.Topic;

import static com.igormaznitsa.mindmap.swing.panel.StandardTopicAttribute.*;

import java.awt.Color;
import java.io.File;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.filechooser.FileFilter;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.mindmap.swing.panel.Texts;

public final class MindMapUtils {

  private MindMapUtils() {
  }

  public static boolean isHidden(@Nullable final Topic topic) {
    if (topic == null) {
      return true;
    }
    final String collapsed = topic.findAttributeInAncestors(ATTR_COLLAPSED.getText());
    return collapsed != null && Boolean.parseBoolean(collapsed);
  }

  @Nullable
  public static Topic findFirstVisibleAncestor(@Nullable final Topic topic) {
    if (topic == null) {
      return null;
    }

    final Topic[] path = topic.getPath();

    Topic lastVisible = null;

    if (path.length > 0) {
      for (final Topic t : path) {
        lastVisible = t;
        final boolean collapsed = Boolean.parseBoolean(t.getAttribute(ATTR_COLLAPSED.getText()));
        if (collapsed) {
          break;
        }
      }
    }

    return lastVisible;
  }

  public static boolean ensureVisibility(@Nonnull final Topic topic) {
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

  public static boolean isCollapsed(@Nonnull final Topic topic) {
    return "true".equalsIgnoreCase(topic.getAttribute(ATTR_COLLAPSED.getText()));//NOI18N
  }

  public static boolean setCollapsed(@Nonnull final Topic topic, final boolean flag) {
    return topic.setAttribute(ATTR_COLLAPSED.getText(), flag ? "true" : null);//NOI18N
  }

  public static void removeCollapseAttributeFromTopicsWithoutChildren(@Nullable final MindMap map) {
    removeCollapseAttrIfNoChildren(map == null ? null : map.getRoot());
  }

  public static void removeCollapseAttr(@Nonnull final MindMap map) {
    _removeCollapseAttr(map.getRoot());
  }

  private static void _removeCollapseAttr(@Nullable final Topic topic) {
    if (topic != null) {
      topic.setAttribute(ATTR_COLLAPSED.getText(), null);
      if (topic.hasChildren()) {
        for (final Topic ch : topic.getChildren()) {
          _removeCollapseAttr(ch);
        }
      }
    }
  }

  public static void removeCollapseAttrIfNoChildren(@Nullable final Topic topic) {
    if (topic != null) {
      if (!topic.hasChildren()) {
        topic.setAttribute(ATTR_COLLAPSED.getText(), null);
      } else {
        for (final Topic t : topic.getChildren()) {
          removeCollapseAttrIfNoChildren(t);
        }
      }
    }
  }

  public static void copyColorAttributes(@Nonnull final Topic source, @Nonnull final Topic destination) {
    destination.setAttribute(ATTR_FILL_COLOR.getText(), source.getAttribute(ATTR_FILL_COLOR.getText()));
    destination.setAttribute(ATTR_BORDER_COLOR.getText(), source.getAttribute(ATTR_BORDER_COLOR.getText()));
    destination.setAttribute(ATTR_TEXT_COLOR.getText(), source.getAttribute(ATTR_TEXT_COLOR.getText()));
  }

  @Nonnull
  public static Color getBackgroundColor(@Nonnull final MindMapPanelConfig cfg, @Nonnull final Topic topic) {
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

  @Nonnull
  public static Color getTextColor(@Nonnull final MindMapPanelConfig cfg, @Nonnull final Topic topic) {
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

  @Nonnull
  public static Color getBorderColor(@Nonnull final MindMapPanelConfig cfg, @Nonnull final Topic topic) {
    final Color extracted = Utils.html2color(topic.getAttribute(ATTR_BORDER_COLOR.getText()), false);
    return extracted == null ? cfg.getElementBorderColor() : extracted;
  }

  @Nullable
  public static File selectFileToSaveForFileFilter(@Nonnull final MindMapPanel panel, @Nonnull final String title, @Nonnull final String dottedFileExtension, @Nonnull final String filterDescription, @Nonnull final String approveButtonText) {
    final File home = new File(System.getProperty("user.home"));//NOI18N

    final String lcExtension = dottedFileExtension.toLowerCase(Locale.ENGLISH);

    return panel.getController().getDialogProvider(panel).msgSaveFileDialog("user-dir", title, home, true, new FileFilter() { //NOI18N
      @Override
      public boolean accept(@Nonnull final File f) {
        return f.isDirectory() || (f.isFile() && f.getName().toLowerCase(Locale.ENGLISH).endsWith(lcExtension)); //NOI18N
      }

      @Override
      @Nonnull
      public String getDescription() {
        return filterDescription;
      }
    }, approveButtonText);
  }

  @Nullable
  public static File selectFileToOpenForFileFilter(@Nonnull final MindMapPanel panel, @Nonnull final String title, @Nonnull final String dottedFileExtension, @Nonnull final String filterDescription, @Nonnull final String approveButtonText) {
    final File home = new File(System.getProperty("user.home"));//NOI18N

    final String lcExtension = dottedFileExtension.toLowerCase(Locale.ENGLISH);

    return panel.getController().getDialogProvider(panel).msgOpenFileDialog("user-dir", title, home, true, new FileFilter() { //NOI18N
      @Override
      public boolean accept(@Nonnull final File f) {
        return f.isDirectory() || (f.isFile() && f.getName().toLowerCase(Locale.ENGLISH).endsWith(lcExtension)); //NOI18N
      }

      @Override
      @Nonnull
      public String getDescription() {
        return filterDescription;
      }
    }, approveButtonText);
  }

  @Nullable
  public static File checkFileAndExtension(@Nonnull final MindMapPanel panel, @Nullable final File file, @Nonnull final String dottedExtension) {
    if (file == null) {
      return null;
    }
    if (file.isDirectory()) {
      panel.getController().getDialogProvider(panel).msgError(String.format(Texts.getString("AbstractMindMapExporter.msgErrorItIsDirectory"), file.getAbsolutePath()));
      return null;
    }
    if (file.isFile()) {
      if (!panel.getController().getDialogProvider(panel).msgConfirmOkCancel(Texts.getString("AbstractMindMapExporter.titleSaveAs"), String.format(Texts.getString("AbstractMindMapExporter.msgAlreadyExistsWantToReplace"), file.getAbsolutePath()))) {
        return null;
      }
    } else if (!file.getName().toLowerCase(Locale.ENGLISH).endsWith(dottedExtension.toLowerCase(Locale.ENGLISH))) {
      if (panel.getController().getDialogProvider(panel).msgConfirmYesNo(Texts.getString("AbstractMindMapExporter.msgTitleAddExtension"), String.format(Texts.getString("AbstractMindMapExporter.msgAddExtensionQuestion"), dottedExtension))) {
        return new File(file.getParent(), file.getName() + dottedExtension);
      }
    }
    return file;
  }
}
