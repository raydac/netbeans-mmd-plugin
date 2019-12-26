/*
 * Copyright 2015-2018 Igor Maznitsa.
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
package com.igormaznitsa.ideamindmap.editor;

import com.igormaznitsa.ideamindmap.utils.IdeaUtils;
import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.panel.utils.PathStore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.WindowManager;
import java.awt.Component;
import java.io.File;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

public class MindMapDialogProvider implements DialogProvider {

  private final Project project;

  private final PathStore cacheOpenFileThroughDialog = new PathStore();
  private final PathStore cacheSaveFileThroughDialog = new PathStore();

  public MindMapDialogProvider(final Project project) {
    this.project = project;
  }

  @Override
  public void msgError(@Nullable final Component parentComponent, @Nonnull final String text) {
    Messages.showErrorDialog(this.project, text, "Error");
  }

  @Override
  public void msgInfo(@Nullable final Component parentComponent, @Nonnull final String text) {
    Messages.showInfoMessage(this.project, text, "Info");
  }

  @Override
  public void msgWarn(@Nullable final Component parentComponent, @Nonnull final String text) {
    Messages.showWarningDialog(this.project, text, "Warning");
  }

  @Override
  public boolean msgConfirmOkCancel(@Nullable final Component parentComponent, @Nonnull final String title, @Nonnull final String text) {
    return Messages.showOkCancelDialog(this.project, text, title, Messages.getQuestionIcon()) == Messages.OK;
  }

  @Override
  public boolean msgOkCancel(@Nullable final Component parentComponent, @Nonnull final String title, @Nonnull final JComponent component) {
    return IdeaUtils.plainMessageOkCancel(this.project, title, component);
  }

  @Override
  public boolean msgConfirmYesNo(@Nullable final Component parentComponent, @Nonnull final String title, @Nonnull final String text) {
    return Messages.showYesNoDialog(this.project, text, title, Messages.getQuestionIcon()) == Messages.YES;
  }

  @Override
  public Boolean msgConfirmYesNoCancel(@Nullable final Component parentComponent, @Nonnull final String title, @Nonnull final String text) {
    final int result = Messages.showYesNoCancelDialog(this.project, text, title, Messages.getQuestionIcon());
    return result == Messages.CANCEL ? null : result == Messages.YES;
  }

  @Nullable
  @Override
  public File msgOpenFileDialog(@Nullable final Component parentComponent,
          @Nonnull final String id,
          @Nonnull final String title,
          @Nullable final File defaultFolder,
          final boolean fileOnly,
          @Nullable @MustNotContainNull final FileFilter[] fileFilters,
          @Nonnull final String approveButtonText) {

    final JFileChooser fileChooser = new JFileChooser(defaultFolder == null ? cacheOpenFileThroughDialog.find(null, id) : defaultFolder);
    fileChooser.setDialogTitle(title);
    for (final FileFilter f : fileFilters) {
      fileChooser.addChoosableFileFilter(f);
    }
    fileChooser.setAcceptAllFileFilterUsed(true);
    fileChooser.setMultiSelectionEnabled(false);
    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

    if (fileChooser.showOpenDialog(WindowManager.getInstance().findVisibleFrame()) == JFileChooser.APPROVE_OPTION) {
      return cacheOpenFileThroughDialog.put(id, fileChooser.getSelectedFile());
    } else {
      return null;
    }
  }

  @Override
  public File msgSaveFileDialog(@Nullable final Component parentComponent,
                                @Nonnull final String id,
                                @Nonnull final String title,
                                @Nullable final File defaultFolder,
                                final boolean fileOnly,
                                @Nonnull @MustNotContainNull final FileFilter[] fileFilters,
                                @Nonnull final String approveButtonText) {
    final JFileChooser fileChooser = new JFileChooser(defaultFolder == null ? cacheSaveFileThroughDialog.find(null, id) : defaultFolder);
    fileChooser.setDialogTitle(title);
    for (final FileFilter f : fileFilters) {
      fileChooser.addChoosableFileFilter(f);
    }
    fileChooser.setAcceptAllFileFilterUsed(true);
    fileChooser.setMultiSelectionEnabled(false);
    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

    if (fileChooser.showSaveDialog(WindowManager.getInstance().findVisibleFrame()) == JFileChooser.APPROVE_OPTION) {
      return cacheSaveFileThroughDialog.put(id, fileChooser.getSelectedFile());
    } else {
      return null;
    }
  }
}
