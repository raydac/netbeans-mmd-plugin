package com.igormaznitsa.ideamindmap.swing;

import com.igormaznitsa.ideamindmap.editor.MindMapDocumentEditor;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditor;

import javax.annotation.Nonnull;

public class FindInMindMapAction extends AnAction {

  private static final Logger LOGGER = LoggerFactory.getLogger(FindInMindMapAction.class);

  @Override
  public void actionPerformed(@Nonnull AnActionEvent anActionEvent) {
    final FileEditor fileEditor = (FileEditor) anActionEvent.getDataContext().getData("fileEditor");
    if (fileEditor instanceof MindMapDocumentEditor) {
      ((MindMapDocumentEditor) fileEditor).activateTextSearchPanel();
    }
  }

  @Override
  public void update(@Nonnull final AnActionEvent anActionEvent) {
    final FileEditor fileEditor = (FileEditor) anActionEvent.getDataContext().getData("fileEditor");
    anActionEvent.getPresentation().setEnabled(fileEditor instanceof MindMapDocumentEditor);
  }
}
