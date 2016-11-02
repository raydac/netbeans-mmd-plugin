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
package com.igormaznitsa.nbmindmap.utils;

import java.io.File;
import javax.annotation.Nonnull;
import javax.swing.JComponent;
import javax.swing.filechooser.FileFilter;
import org.openide.filesystems.FileChooserBuilder;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;

public final class DialogProviderManager  {
  
  
  private static final class DialogProviderImpl implements DialogProvider {

    @Override
    public void msgError(final String text) {
      NbUtils.msgError(text);
    }

    @Override
    public void msgInfo(final String text) {
      NbUtils.msgInfo(text);
    }

    @Override
    public void msgWarn(final String text) {
      NbUtils.msgWarn(text);
    }

    @Override
    public boolean msgOkCancel(String title, JComponent component) {
      return NbUtils.msgComponentOkCancel(title, component);
    }

    @Override
    public boolean msgConfirmOkCancel(String title, String question) {
      return NbUtils.msgConfirmOkCancel(title, question);
    }

    @Override
    public boolean msgConfirmYesNo(String title, String question) {
      return NbUtils.msgConfirmYesNo(title, question);
    }

    @Override
    public Boolean msgConfirmYesNoCancel(String title, String question) {
      return NbUtils.msgConfirmYesNoCancel(title, question);
    }

    @Override
    public File msgSaveFileDialog(final String id, final String title, final File defaultFolder, final boolean fileOnly, final FileFilter fileFilter, final String approveButtonText) {
      return new FileChooserBuilder(id).setTitle(title).setDefaultWorkingDirectory(defaultFolder).setFilesOnly(fileOnly).setFileFilter(fileFilter).setApproveText(approveButtonText).showSaveDialog();
    }

    @Override
    public File msgOpenFileDialog(final String id, final String title, final File defaultFolder, final boolean fileOnly, final FileFilter fileFilter, final String approveButtonText) {
      return new FileChooserBuilder(id).setTitle(title).setDefaultWorkingDirectory(defaultFolder).setFilesOnly(fileOnly).setFileFilter(fileFilter).setApproveText(approveButtonText).showOpenDialog();
    }
    
  }
  
  private static final DialogProviderManager INSTANCE = new DialogProviderManager();
  private final DialogProvider PROVIDER_INSTANCE = new DialogProviderImpl();

  @Nonnull
  public static DialogProviderManager getInstance() {
    return INSTANCE;
  }

  @Nonnull
  public DialogProvider getDialogProvider() {
    return PROVIDER_INSTANCE;
  }
  
  
  private DialogProviderManager(){
    
  }
  
}
