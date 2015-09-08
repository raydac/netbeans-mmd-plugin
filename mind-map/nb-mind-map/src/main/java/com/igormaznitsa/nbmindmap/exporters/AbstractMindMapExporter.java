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
package com.igormaznitsa.nbmindmap.exporters;

import com.igormaznitsa.nbmindmap.mmgui.MindMapPanel;
import com.igormaznitsa.nbmindmap.utils.NbUtils;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import javax.swing.ImageIcon;

public abstract class AbstractMindMapExporter {

  public AbstractMindMapExporter() {
  }

  public abstract void doExport(final MindMapPanel currentPanel) throws IOException;

  public abstract String getName();

  public abstract String getReference();

  public abstract ImageIcon getIcon();

  public static File checkFile(final File file, final String dottedExtension) {
    if (file == null) {
      return null;
    }
    if (file.isDirectory()) {
      NbUtils.msgError(String.format(java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18n/Bundle").getString("AbstractMindMapExporter.msgErrorItIsDirectory"), file.getAbsolutePath()));
      return null;
    }
    if (file.isFile()) {
      if (!NbUtils.msgConfirmOkCancel(java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18n/Bundle").getString("AbstractMindMapExporter.titleSaveAs"), String.format(java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18n/Bundle").getString("AbstractMindMapExporter.msgAlreadyExistsWantToReplace"), file.getAbsolutePath()))) {
        return null;
      }
    }
    else {
      if (!file.getName().toLowerCase(Locale.ENGLISH).endsWith(dottedExtension.toLowerCase(Locale.ENGLISH))) {
        if (NbUtils.msgConfirmYesNo(java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18n/Bundle").getString("AbstractMindMapExporter.msgTitleAddExtension"), String.format(java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18n/Bundle").getString("AbstractMindMapExporter.msgAddExtensionQuestion"), dottedExtension))) {
          return new File(file.getParent(), file.getName() + dottedExtension);
        }
      }
    }
    return file;
  }
}
