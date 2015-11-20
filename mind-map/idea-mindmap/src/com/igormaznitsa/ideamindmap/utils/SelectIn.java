/*
 * Copyright 2015 Igor Maznitsa.
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
package com.igormaznitsa.ideamindmap.utils;

import com.igormaznitsa.ideamindmap.editor.MindMapDocumentEditor;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.fileEditor.FileEditorManager;
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
        FileEditorManager.getInstance(source.getProject()).openFile(file,true);
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
