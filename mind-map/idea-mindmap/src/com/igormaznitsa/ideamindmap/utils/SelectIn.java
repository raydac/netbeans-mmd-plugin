package com.igormaznitsa.ideamindmap.utils;

import com.igormaznitsa.ideamindmap.editor.MindMapDocumentEditor;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowId;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.NotNull;

public enum SelectIn {
    IDE,
    SYSTEM,;

    public void open(@NotNull final MindMapDocumentEditor source, @NotNull final VirtualFile file) {
        switch (this) {
            case IDE: {
                if (file.isDirectory()) {
                    final ProjectView view = ProjectView.getInstance(source.getProject());
                    view.select(null, file, true);
                    final ToolWindow toolwindow = ToolWindowManager.getInstance(source.getProject()).getToolWindow(ToolWindowId.PROJECT_VIEW);
                    if (toolwindow != null) toolwindow.activate(null);
                } else {
                    new OpenFileDescriptor(source.getProject(), file).navigate(true);
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
