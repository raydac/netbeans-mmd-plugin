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
package com.igormaznitsa.nbmindmap.utils;

import com.igormaznitsa.mindmap.plugins.api.PluginContext;
import java.awt.Component;
import java.io.File;
import javax.annotation.Nonnull;
import javax.swing.JComponent;
import javax.swing.filechooser.FileFilter;
import org.openide.filesystems.FileChooserBuilder;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;

public final class DialogProviderManager {

  private static final class DialogProviderImpl implements DialogProvider {

    @Override
    public void msgError(final Component parentComponent, final String text) {
      NbUtils.msgError(parentComponent, text);
    }

    @Override
    public void msgInfo(final Component parentComponent, final String text) {
      NbUtils.msgInfo(parentComponent, text);
    }

    @Override
    public void msgWarn(final Component parentComponent, final String text) {
      NbUtils.msgWarn(parentComponent, text);
    }

    @Override
    public boolean msgOkCancel(final Component parentComponent, String title, JComponent component) {
      return NbUtils.msgComponentOkCancel(parentComponent, title, component);
    }

    @Override
    public boolean msgConfirmOkCancel(final Component parentComponent, String title, String question) {
      return NbUtils.msgConfirmOkCancel(parentComponent, title, question);
    }

    @Override
    public boolean msgConfirmYesNo(final Component parentComponent, String title, String question) {
      return NbUtils.msgConfirmYesNo(parentComponent, title, question);
    }

    @Override
    public Boolean msgConfirmYesNoCancel(final Component parentComponent, String title, String question) {
      return NbUtils.msgConfirmYesNoCancel(parentComponent, title, question);
    }

    @Override
    public File msgSaveFileDialog(final Component parentComponent, 
        final PluginContext pluginContext,
        final String id, final String title, final File defaultFolder, final boolean fileOnly, final FileFilter[] fileFilters, final String approveButtonText) {
      final FileChooserBuilder builder = new FileChooserBuilder(id)
              .setTitle(title)
              .setDefaultWorkingDirectory(defaultFolder)
              .setFilesOnly(fileOnly)
              .setApproveText(approveButtonText);

      for (final FileFilter filter : fileFilters) {
        builder.addFileFilter(filter);
      }

      if (fileFilters.length != 0) {
        builder.setFileFilter(fileFilters[0]);
      }

      return builder.showSaveDialog();
    }

    @Override
    public File msgOpenFileDialog(final Component parentComponent, 
        final PluginContext pluginContext,
        final String id, final String title, final File defaultFolder, final boolean fileOnly, final FileFilter[] fileFilters, final String approveButtonText) {
      final FileChooserBuilder builder = new FileChooserBuilder(id)
              .setTitle(title)
              .setDefaultWorkingDirectory(defaultFolder)
              .setFilesOnly(fileOnly)
              .setApproveText(approveButtonText);

      for (final FileFilter filter : fileFilters) {
        builder.addFileFilter(filter);
      }

      if (fileFilters.length != 0) {
        builder.setFileFilter(fileFilters[0]);
      }

      return builder.showOpenDialog();
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

  private DialogProviderManager() {

  }

}
