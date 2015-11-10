package com.igormaznitsa.ideamindmap.editor;

import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.fileChooser.FileSaverDialog;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileWrapper;

import javax.swing.filechooser.FileFilter;
import java.io.File;

public class MindMapDialogProvider implements DialogProvider {
  private final Project project;
  private VirtualFile base;

  public MindMapDialogProvider(final Project project) {
    this.project = project;
  }

  @Override
  public void msgError(final String text) {
    Messages.showErrorDialog(this.project, text, "Error");
  }

  @Override
  public void msgInfo(final String text) {
    Messages.showInfoMessage(this.project, text, "Info");
  }

  @Override
  public void msgWarn(final String text) {
    Messages.showWarningDialog(this.project, text, "Warning");
  }

  @Override
  public boolean msgConfirmOkCancel(final String title, final String text) {
    return Messages.showOkCancelDialog(this.project, text, title, Messages.getQuestionIcon()) == Messages.OK;
  }

  @Override
  public boolean msgConfirmYesNo(final String title, final String text) {
    return Messages.showYesNoDialog(this.project, text, title, Messages.getQuestionIcon()) == Messages.YES;
  }

  @Override
  public Boolean msgConfirmYesNoCancel(final String title, final String text) {
    final int result = Messages.showYesNoCancelDialog(this.project, text, title, Messages.getQuestionIcon());
    return result == Messages.CANCEL ? null : result == Messages.YES;
  }

  @Override
  public File msgSaveFileDialog(final String id, final String title, final File defaultFolder, final boolean fileOnly, final FileFilter fileFilter,
    final String approveButtonText) {
    final FileSaverDescriptor descriptor = new FileSaverDescriptor(title, fileFilter.getDescription());

    final FileSaverDialog dialog = FileChooserFactory.getInstance().createSaveFileDialog(descriptor, this.project);

    final VirtualFileWrapper wrapper = dialog.save(this.base, null);

    if (wrapper != null) {
      final File folder = wrapper.getFile().getParentFile();
      if (folder != null) {
        base = new VirtualFileWrapper(folder).getVirtualFile();
      }
    }

    return wrapper == null ? null : wrapper.getFile();
  }
}
