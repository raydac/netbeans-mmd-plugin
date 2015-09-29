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
package com.igormaznitsa.mindmap.exporters;

import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import static com.igormaznitsa.mindmap.swing.panel.StandardTopicAttribute.*;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileFilter;

public abstract class AbstractMindMapExporter {

  protected static final ResourceBundle BUNDLE = java.util.ResourceBundle.getBundle("com/igormaznitsa/mindmap/swing/panel/Bundle");

  public AbstractMindMapExporter() {
  }

  public abstract void doExport(MindMapPanel panel, OutputStream out) throws IOException;

  public abstract String getName();

  public abstract String getReference();

  public abstract ImageIcon getIcon();

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
    }
    else {
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
    }
    else {
      result = extracted;
    }
    return result;
  }

  public static Color getBorderColor(final MindMapPanelConfig cfg, final Topic topic) {
    final Color extracted = Utils.html2color(topic.getAttribute(ATTR_BORDER_COLOR.getText()), false);
    return extracted == null ? cfg.getElementBorderColor() : extracted;
  }

  public static File selectFileForFileFilter(final MindMapPanel panel, final String title, final String dottedFileExtension, final String filterDescription, final String approveButtonText) {
    final File home = new File(System.getProperty("user.home"));//NOI18N

    final String lcExtension = dottedFileExtension.toLowerCase(Locale.ENGLISH);

    return panel.getController().getDialogProvider(panel).msgSaveFileDialog("user-dir", title, home, true, new FileFilter() { //NOI18N
      @Override
      public boolean accept(File f) {
        return f.isDirectory() || (f.isFile() && f.getName().toLowerCase(Locale.ENGLISH).endsWith(lcExtension)); //NOI18N
      }

      @Override
      public String getDescription() {
        return filterDescription;
      }
    }, approveButtonText);
  }

  public static File checkFileAndExtension(final MindMapPanel panel, final File file, final String dottedExtension) {
    if (file == null) {
      return null;
    }
    if (file.isDirectory()) {
      panel.getController().getDialogProvider(panel).msgError(String.format(BUNDLE.getString("AbstractMindMapExporter.msgErrorItIsDirectory"), file.getAbsolutePath()));
      return null;
    }
    if (file.isFile()) {
      if (!panel.getController().getDialogProvider(panel).msgConfirmOkCancel(BUNDLE.getString("AbstractMindMapExporter.titleSaveAs"), String.format(BUNDLE.getString("AbstractMindMapExporter.msgAlreadyExistsWantToReplace"), file.getAbsolutePath()))) {
        return null;
      }
    }
    else {
      if (!file.getName().toLowerCase(Locale.ENGLISH).endsWith(dottedExtension.toLowerCase(Locale.ENGLISH))) {
        if (panel.getController().getDialogProvider(panel).msgConfirmYesNo(BUNDLE.getString("AbstractMindMapExporter.msgTitleAddExtension"), String.format(BUNDLE.getString("AbstractMindMapExporter.msgAddExtensionQuestion"), dottedExtension))) {
          return new File(file.getParent(), file.getName() + dottedExtension);
        }
      }
    }
    return file;
  }
}
