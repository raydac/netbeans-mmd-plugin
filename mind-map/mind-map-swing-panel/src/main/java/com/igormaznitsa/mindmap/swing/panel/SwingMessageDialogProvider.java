/*
 * Copyright (C) 2015-2023 Igor A. Maznitsa
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

package com.igormaznitsa.mindmap.swing.panel;

import com.igormaznitsa.mindmap.plugins.api.PluginContext;
import java.awt.Component;
import java.awt.Window;
import java.io.File;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

public class SwingMessageDialogProvider implements DialogProvider {

  @Override
  public void msgError(final Component parentComponent, final String text) {
    JOptionPane
        .showMessageDialog(parentComponent, text, "Error", JOptionPane.ERROR_MESSAGE);
  }

  @Override
  public void msgInfo(final Component parentComponent, final String text) {
    JOptionPane
        .showMessageDialog(parentComponent, text, "Info", JOptionPane.INFORMATION_MESSAGE);
  }

  @Override
  public void msgWarn(final Component parentComponent, final String text) {
    JOptionPane.showMessageDialog(parentComponent, text, "Warning", JOptionPane.WARNING_MESSAGE);
  }

  @Override
  public boolean msgConfirmOkCancel(final Component parentComponent, final String title, final String question) {
    return JOptionPane.showConfirmDialog(parentComponent, question, title, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION;
  }

  @Override
  public boolean msgOkCancel(final Component parentComponent, final String title,
                             final JComponent component) {
    return JOptionPane
        .showConfirmDialog(parentComponent,
            component, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null) ==
        JOptionPane.OK_OPTION;
  }

  @Override
  public boolean msgConfirmYesNo(final Component parentComponent,
                                 final String title, final String question) {
    return JOptionPane
        .showConfirmDialog(parentComponent,
            question, title, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
  }

  @Override
  public Boolean msgConfirmYesNoCancel(final Component parentComponent,
                                       final String title,
                                       final String question) {
    final int result = JOptionPane
        .showConfirmDialog(parentComponent,
            question, title, JOptionPane.YES_NO_CANCEL_OPTION);
    if (result == JOptionPane.CANCEL_OPTION) {
      return null;
    } else {
      return result == JOptionPane.YES_OPTION;
    }
  }

  @Override
  public File msgSaveFileDialog(final Component parentComponent,
                                final PluginContext pluginContext,
                                final String id,
                                final String title,
                                final File defaultFolder,
                                final boolean filesOnly,
                                final FileFilter[] fileFilters,
                                final String approveButtonText) {
    final JFileChooser fileChooser = new JFileChooser(defaultFolder);
    fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
    fileChooser.setDialogTitle(title);
    fileChooser.setApproveButtonText(approveButtonText);
    fileChooser.setAcceptAllFileFilterUsed(true);
    for (final FileFilter f : fileFilters) {
      fileChooser.addChoosableFileFilter(f);
    }
    if (fileFilters.length != 0) {
      fileChooser.setFileFilter(fileFilters[0]);
    }
    fileChooser.setMultiSelectionEnabled(false);

    File result = null;
    if (fileChooser.showDialog(parentComponent,approveButtonText) == JFileChooser.APPROVE_OPTION) {
      result = fileChooser.getSelectedFile();
    }

    return result;
  }

  @Override
  public File msgOpenFileDialog(final Component parentComponent,
                                final PluginContext pluginContext,
                                final String id,
                                final String title,
                                final File defaultFolder,
                                final boolean filesOnly,
                                final FileFilter[] fileFilters,
                                final String approveButtonText) {
    final JFileChooser fileChooser = new JFileChooser(defaultFolder);
    fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
    fileChooser.setDialogTitle(title);
    fileChooser.setApproveButtonText(approveButtonText);
    for (final FileFilter f : fileFilters) {
      fileChooser.addChoosableFileFilter(f);
    }
    if (fileFilters.length != 0) {
      fileChooser.setFileFilter(fileFilters[0]);
    }
    fileChooser.setAcceptAllFileFilterUsed(true);
    fileChooser.setMultiSelectionEnabled(false);

    File result = null;
    if (fileChooser.showDialog(parentComponent,
        approveButtonText) == JFileChooser.APPROVE_OPTION) {
      result = fileChooser.getSelectedFile();
    }

    return result;
  }

}
