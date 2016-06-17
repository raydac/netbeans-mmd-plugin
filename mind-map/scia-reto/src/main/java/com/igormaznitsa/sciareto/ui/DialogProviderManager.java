/*
 * Copyright 2016 Igor Maznitsa.
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
package com.igormaznitsa.sciareto.ui;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.sciareto.Main;

public final class DialogProviderManager {
  
  private static final Map<String,File> CACHE_ID_FILE = Collections.synchronizedMap(new HashMap<String, File>());
  
  private static final DialogProviderManager INSTANCE = new DialogProviderManager();
  private static final DialogProvider PROVIDER = new DialogProvider() {
    @Override
    public void msgError(@Nonnull final String text) {
      JOptionPane.showMessageDialog(Main.getApplicationFrame(), text, "Error!", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void msgInfo(@Nonnull final String text) {
      JOptionPane.showMessageDialog(Main.getApplicationFrame(), text, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void msgWarn(@Nonnull final String text) {
      JOptionPane.showMessageDialog(Main.getApplicationFrame(), text, "Warning!", JOptionPane.WARNING_MESSAGE);
    }

    @Override
    public boolean msgConfirmOkCancel(@Nonnull final String title, @Nonnull final String question) {
      return JOptionPane.showConfirmDialog(Main.getApplicationFrame(), question, title, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION;
    }

    @Override
    public boolean msgOkCancel(@Nonnull final String title, @Nonnull final JComponent component) {
      return JOptionPane.showConfirmDialog(Main.getApplicationFrame(), component, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,null) == JOptionPane.OK_OPTION;
    }

    @Override
    public boolean msgConfirmYesNo(@Nonnull final String title, @Nonnull final String question) {
      return JOptionPane.showConfirmDialog(Main.getApplicationFrame(), question, title, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    @Override
    @Nullable
    public Boolean msgConfirmYesNoCancel(@Nonnull final String title, @Nonnull final String question) {
      final int result = JOptionPane.showConfirmDialog(Main.getApplicationFrame(), question, title, JOptionPane.YES_NO_CANCEL_OPTION);
      if (result == JOptionPane.CANCEL_OPTION) {
        return null;
      } else {
        return result == JOptionPane.YES_OPTION;
      }
    }

    @Override
    @Nullable
    public File msgSaveFileDialog(@Nonnull final String id, @Nonnull final String title, @Nullable final File defaultFolder, final boolean filesOnly, @Nonnull final FileFilter fileFilter, @Nonnull final String approveButtonText) {
      final File folderToUse;
      if (defaultFolder == null){
        folderToUse = CACHE_ID_FILE.get(id);
      } else {
        folderToUse = defaultFolder;
      }

      final JFileChooser fileChooser = new JFileChooser(folderToUse);
      fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
      fileChooser.setDialogTitle(title);
      fileChooser.setApproveButtonText(approveButtonText);
      fileChooser.setAcceptAllFileFilterUsed(true);
      fileChooser.setFileFilter(fileFilter);
      fileChooser.setMultiSelectionEnabled(false);
      
      File result = null;
      if (fileChooser.showDialog(Main.getApplicationFrame(), approveButtonText) == JFileChooser.APPROVE_OPTION){
        result = fileChooser.getSelectedFile();
        if(result!=null){
          CACHE_ID_FILE.put(id, result);
        }
      }
      
      return result;
    }

    @Override
    @Nullable
    public File msgOpenFileDialog(@Nonnull String id, @Nonnull String title, @Nullable File defaultFolder, boolean filesOnly, @Nonnull FileFilter fileFilter, @Nonnull String approveButtonText) {
      final File folderToUse;
      if (defaultFolder == null) {
        folderToUse = CACHE_ID_FILE.get(id);
      } else {
        folderToUse = defaultFolder;
      }

      final JFileChooser fileChooser = new JFileChooser(folderToUse);
      fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
      fileChooser.setDialogTitle(title);
      fileChooser.setApproveButtonText(approveButtonText);
      fileChooser.setFileFilter(fileFilter);
      fileChooser.setAcceptAllFileFilterUsed(true);
      fileChooser.setMultiSelectionEnabled(false);

      File result = null;
      if (fileChooser.showDialog(Main.getApplicationFrame(), approveButtonText) == JFileChooser.APPROVE_OPTION) {
        result = fileChooser.getSelectedFile();
        if (result != null) {
          CACHE_ID_FILE.put(id, result);
        }
      }

      return result;
    }
  };
  
  private DialogProviderManager(){
  }

  @Nonnull
  public static DialogProviderManager getInstance(){
    return INSTANCE;
  }
  
  @Nonnull
  public DialogProvider getDialogProvider(){
    return PROVIDER;
  }
  
}
