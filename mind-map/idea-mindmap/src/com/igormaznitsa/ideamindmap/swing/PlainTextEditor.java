package com.igormaznitsa.ideamindmap.swing;

import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.FileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.ui.EditorTextField;

public class PlainTextEditor extends EditorTextField {

  public PlainTextEditor(final Project project, final String text) {
    super(text, project, FileTypes.PLAIN_TEXT);
    setOneLineMode(false);
    setAutoscrolls(true);
  }

  @Override
  protected EditorEx createEditor() {
    final EditorEx result = super.createEditor();
    result.setVerticalScrollbarVisible(true);
    result.setHorizontalScrollbarVisible(true);
    result.getCaretModel().moveToOffset(0);
    return result;
  }
}
