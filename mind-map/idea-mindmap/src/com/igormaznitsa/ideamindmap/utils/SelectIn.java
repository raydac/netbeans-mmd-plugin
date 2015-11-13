package com.igormaznitsa.ideamindmap.utils;

import com.igormaznitsa.ideamindmap.editor.MindMapDocumentEditor;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowId;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.NotNull;

public enum SelectIn {
  IDE,
  SYSTEM,;

  private static void projectFocusTo(final Project project,final VirtualFile file){
    final ProjectView view = ProjectView.getInstance(project);
    view.select(null, file, true);
    final ToolWindow toolwindow = ToolWindowManager.getInstance(project).getToolWindow(ToolWindowId.PROJECT_VIEW);
    if (toolwindow != null)
      toolwindow.activate(null);
  }

  public void open(@NotNull final MindMapDocumentEditor source, @NotNull final VirtualFile file) {
    switch (this) {
    case IDE: {
      if (file.isDirectory()) {
        projectFocusTo(source.getProject(),file);
      }
      else {
        projectFocusTo(source.getProject(),file);
        final ProjectView view = ProjectView.getInstance(source.getProject());
        view.select(null, file, true);
      }
    }
    break;
    case SYSTEM: {
      IdeaUtils.openInSystemViewer(source.getDialogProvider(), file);
    }
    break;
    }
  }
}
