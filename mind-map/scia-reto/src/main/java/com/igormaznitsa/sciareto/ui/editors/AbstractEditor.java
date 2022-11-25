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

package com.igormaznitsa.sciareto.ui.editors;

import static com.igormaznitsa.sciareto.ui.UiUtils.findTextBundle;

import com.igormaznitsa.meta.common.interfaces.Disposable;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.sciareto.SciaRetoStarter;
import com.igormaznitsa.sciareto.preferences.PreferencesManager;
import com.igormaznitsa.sciareto.preferences.SpecificKeys;
import com.igormaznitsa.sciareto.ui.DialogProviderManager;
import com.igormaznitsa.sciareto.ui.tabs.TabProvider;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

public abstract class AbstractEditor implements TabProvider, Disposable {

  private static final Map<String, ImageIcon> ICON_CACHE = new HashMap<>();
  protected final ResourceBundle bundle = findTextBundle();
  protected final Logger logger;
  private final AtomicBoolean disposeFlag = new AtomicBoolean();
  protected MindMapPanelConfig mindMapPanelConfig;

  public AbstractEditor() {
    super();
    this.logger = LoggerFactory.getLogger(this.getClass());
    this.mindMapPanelConfig = loadMindMapConfigFromPreferences();
  }

  @Nonnull
  protected static MindMapPanelConfig loadMindMapConfigFromPreferences() {
    final MindMapPanelConfig config = new MindMapPanelConfig();
    config.loadFrom(PreferencesManager.getInstance().getPreferences());
    return config;
  }

  @Nonnull
  protected static synchronized ImageIcon loadMenuIcon(@Nonnull final String name) {
    if (ICON_CACHE.containsKey(name)) {
      return ICON_CACHE.get(name);
    } else {
      final ImageIcon loaded =
          new javax.swing.ImageIcon(ClassLoader.getSystemResource("menu_icons/" + name + ".png"));
      ICON_CACHE.put(name, loaded);
      return loaded;
    }
  }

  @Override
  public void focusToEditor(int line) {
    throw new UnsupportedOperationException(
        "Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public final void updateConfiguration() {
    this.mindMapPanelConfig = loadMindMapConfigFromPreferences();
    this.doUpdateConfiguration();
  }

  public void doUpdateConfiguration() {

  }

  public boolean isZoomable() {
    return true;
  }

  public void doZoomIn() {

  }

  public void doZoomOut() {

  }

  public void doZoomReset() {

  }

  @Override
  public final boolean saveDocumentAs() throws IOException {
    final DialogProvider dialogProvider = DialogProviderManager.getInstance().getDialogProvider();
    final File file = this.getTabTitle().getAssociatedFile();
    File fileToSave = dialogProvider.msgSaveFileDialog(SciaRetoStarter.getApplicationFrame(),
        null,
        "save-as", "Save as", file, true, new FileFilter[] {getFileFilter()}, "Save");
    if (fileToSave != null) {
      if (!fileToSave.getName().contains(".")) {
        final Boolean result =
            dialogProvider.msgConfirmYesNoCancel(SciaRetoStarter.getApplicationFrame(),
                "Add extension",
                String.format("Add file extenstion '%s'?", this.getDefaultExtension()));
        if (result == null) {
          return false;
        }
        if (result) {
          fileToSave = new File(fileToSave.getParentFile(),
              fileToSave.getName() + '.' + this.getDefaultExtension());
        }
      }
      this.getTabTitle().setAssociatedFile(fileToSave);
      this.onSelectedSaveDocumentAs(fileToSave);
      this.getTabTitle().setChanged(true);
      return this.saveDocument();
    }
    return false;
  }

  protected boolean isOverwriteAllowed(@Nullable final TextFile textFile) throws IOException {
    return textFile == null
        || textFile.hasSameContent(textFile.getFile())
        || DialogProviderManager.getInstance().getDialogProvider().msgConfirmOkCancel(
        SciaRetoStarter.getApplicationFrame(),
        findTextBundle().getString("editorAbstract.msgConfirmOverwrite.title"),
        String.format(findTextBundle().getString("editorAbstract.msgConfirmOverwrite.text"),
            textFile.getFile().getName()));
  }

  protected void onSelectedSaveDocumentAs(@Nonnull final File selectedFile) {

  }

  protected boolean isAutoBackupAllowed() {
    final PreferencesManager manager = PreferencesManager.getInstance();
    return manager.getFlag(manager.getPreferences(),
        SpecificKeys.PROPERTY_BACKUP_LAST_EDIT_BEFORE_SAVE, false);
  }

  public void deleteBackup() {
    if (this.isEditable() && !this.isDisposed()) {
      final File associatedFile = this.getTabTitle().getAssociatedFile();
      if (isAutoBackupAllowed() && associatedFile != null) {
        TextFileBackup.getInstance().add(new TextFileBackup.BackupContent(associatedFile, null));
      }
    }
  }

  protected void backup(@Nullable final String text) {
    if (this.isEditable() && !this.isDisposed()) {
      if (text != null) {
        final File associatedFile = this.getTabTitle().getAssociatedFile();
        if (isAutoBackupAllowed() && associatedFile != null) {
          TextFileBackup.getInstance().add(new TextFileBackup.BackupContent(associatedFile, text));
        }
      }
    }
  }

  protected void backup() {
    this.backup(this.getContentAsText());
  }

  @Nullable
  protected abstract String getContentAsText();

  @Nonnull
  public abstract String getDefaultExtension();

  @Nonnull
  public abstract EditorContentType getEditorContentType();

  @Nonnull
  public abstract JComponent getContainerToShow();

  @Override
  public final void dispose() {
    if (disposeFlag.compareAndSet(false, true)) {
      if (this.isEditable()) {
        final File associatedFile = this.getTabTitle().getAssociatedFile();
        if (associatedFile != null) {
          TextFileBackup.getInstance().add(new TextFileBackup.BackupContent(associatedFile, null));
        }
      }
      try {
        final JComponent editComponent = this.getMainComponent();
        if (editComponent instanceof Disposable) {
          ((Disposable) editComponent).dispose();
        }
      } finally {
        this.doDispose();
      }
    }
  }

  protected void doDispose() {
  }

  @Override
  public boolean isDisposed() {
    return this.disposeFlag.get();
  }

  @Nullable
  protected TextFile restoreFromBackup(@Nonnull final File file) {
    if (!SwingUtilities.isEventDispatchThread()) {
      throw new Error("Must be called only from Swing UI thread");
    }

    final File backupFile = TextFileBackup.findBackupForFile(file);
    if (backupFile == null) {
      return null;
    }

    SciaRetoStarter.disposeSplash();

    if (DialogProviderManager.getInstance().getDialogProvider()
        .msgConfirmYesNo(this.getContainerToShow(), "Restore from backup?",
            String.format("Detected backup '%s', restore content?", backupFile.getName()))) {
      try {
        final TextFile result = new TextFileBackup.Restored(backupFile).asTextFile();
        DialogProviderManager.getInstance().getDialogProvider().msgWarn(this.getContainerToShow(),
            "Restored from backup file: " + backupFile.getName());
        return result;
      } catch (IOException ex) {
        DialogProviderManager.getInstance().getDialogProvider().msgError(this.getContainerToShow(),
            "Can't restore backup file for error: " + ex.getMessage());
        return null;
      }
    } else {
      return null;
    }
  }

  @Override
  public boolean showSearchPane(@Nonnull final JPanel searchPanel) {
    return false;
  }
}
