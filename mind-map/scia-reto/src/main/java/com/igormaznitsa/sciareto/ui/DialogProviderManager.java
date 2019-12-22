/*
 * Copyright (C) 2018 Igor Maznitsa.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

package com.igormaznitsa.sciareto.ui;

import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.meta.common.utils.GetUtils;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.panel.utils.PathStore;
import com.igormaznitsa.sciareto.Main;
import java.awt.Component;
import java.io.File;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

public final class DialogProviderManager {

  private static final DialogProviderManager INSTANCE = new DialogProviderManager();
  private static final DialogProvider PROVIDER = new DialogProvider() {
    private final PathStore cacheOpenFileThroughDialog = new PathStore();
    private final PathStore cacheSaveFileThroughDialog = new PathStore();

    @Override
    public void msgError(@Nullable final Component parentComponent, @Nonnull final String text) {
      JOptionPane.showMessageDialog(GetUtils.ensureNonNull(parentComponent, Main.getApplicationFrame()), text, "Error!", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void msgInfo(@Nullable final Component parentComponent, @Nonnull final String text) {
      JOptionPane.showMessageDialog(GetUtils.ensureNonNull(parentComponent, Main.getApplicationFrame()), text, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void msgWarn(@Nullable Component parentComponent, @Nonnull final String text) {
      JOptionPane.showMessageDialog(GetUtils.ensureNonNull(parentComponent, Main.getApplicationFrame()), text, "Warning!", JOptionPane.WARNING_MESSAGE);
    }

    @Override
    public boolean msgConfirmOkCancel(@Nullable Component parentComponent, @Nonnull final String title, @Nonnull final String question) {
      return JOptionPane.showConfirmDialog(GetUtils.ensureNonNull(parentComponent, Main.getApplicationFrame()), question, title, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION;
    }

    @Override
    public boolean msgOkCancel(@Nullable Component parentComponent, @Nonnull final String title, @Nonnull final JComponent component) {
      return JOptionPane.showConfirmDialog(GetUtils.ensureNonNull(parentComponent, Main.getApplicationFrame()), component, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null) == JOptionPane.OK_OPTION;
    }

    @Override
    public boolean msgConfirmYesNo(@Nullable final Component parentComponent, @Nonnull final String title, @Nonnull final String question) {
      return JOptionPane.showConfirmDialog(GetUtils.ensureNonNull(parentComponent, Main.getApplicationFrame()), question, title, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    @Override
    @Nullable
    public Boolean msgConfirmYesNoCancel(@Nullable Component parentComponent, @Nonnull final String title, @Nonnull final String question) {
      final int result = JOptionPane.showConfirmDialog(GetUtils.ensureNonNull(parentComponent, Main.getApplicationFrame()), question, title, JOptionPane.YES_NO_CANCEL_OPTION);
      if (result == JOptionPane.CANCEL_OPTION) {
        return null;
      } else {
        return result == JOptionPane.YES_OPTION;
      }
    }

    @Override
    @Nullable
    public File msgSaveFileDialog(@Nullable final Component parentComponent, @Nonnull final String id, @Nonnull final String title, @Nullable final File defaultFolder, final boolean filesOnly, @Nonnull @MustNotContainNull final FileFilter[] fileFilters, @Nonnull final String approveButtonText) {
      final File folderToUse;
      if (defaultFolder == null) {
        folderToUse = cacheSaveFileThroughDialog.find(id);
      } else {
        folderToUse = defaultFolder;
      }

      final JFileChooser fileChooser = new JFileChooser(folderToUse);
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
      if (fileChooser.showDialog(GetUtils.ensureNonNull(
          parentComponent == null ? null : SwingUtilities.windowForComponent(parentComponent),
          Main.getApplicationFrame()),
          approveButtonText) == JFileChooser.APPROVE_OPTION
      ) {
        result = cacheSaveFileThroughDialog.put(id, fileChooser.getSelectedFile());
      }

      return result;
    }

    @Override
    @Nullable
    public File msgOpenFileDialog(@Nullable final Component parentComponent, @Nonnull String id, @Nonnull String title, @Nullable File defaultFolder, boolean filesOnly, @Nonnull @MustNotContainNull FileFilter[] fileFilters, @Nonnull String approveButtonText) {
      final File folderToUse;
      if (defaultFolder == null) {
        folderToUse = cacheOpenFileThroughDialog.find(id);
      } else {
        folderToUse = defaultFolder;
      }

      final JFileChooser fileChooser = new JFileChooser(folderToUse);
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
      if (fileChooser.showDialog(GetUtils.ensureNonNull(
          parentComponent == null ? null : SwingUtilities.windowForComponent(parentComponent),
          Main.getApplicationFrame()),
          approveButtonText) == JFileChooser.APPROVE_OPTION) {
        result = cacheOpenFileThroughDialog.put(id, fileChooser.getSelectedFile());
      }

      return result;
    }
  };

  private DialogProviderManager() {
  }

  @Nonnull
  public static DialogProviderManager getInstance() {
    return INSTANCE;
  }

  @Nonnull
  public DialogProvider getDialogProvider() {
    return PROVIDER;
  }

}
