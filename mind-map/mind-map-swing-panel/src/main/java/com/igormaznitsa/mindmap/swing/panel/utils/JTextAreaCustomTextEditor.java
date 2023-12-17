/*
 * Copyright (C) 2015-2023 Igor A. Maznitsa
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

package com.igormaznitsa.mindmap.swing.panel.utils;

import static java.util.Objects.requireNonNull;

import com.igormaznitsa.mindmap.swing.services.CustomTextEditor;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Utilities;

/**
 * Auxiliary class implements a custom text editor based on JTextArea.
 *
 * @since 1.6.6
 */
public class JTextAreaCustomTextEditor implements CustomTextEditor {

  private final JTextArea textArea;
  private final List<CaretPosChangeListener> caretPosChangeListenerList =
      new CopyOnWriteArrayList<>();

  public JTextAreaCustomTextEditor(final JTextArea textArea) {
    this.textArea = requireNonNull(textArea);
    this.textArea.addCaretListener(
        e -> this.caretPosChangeListenerList.forEach(x -> x.onCaretPosChange(e.getDot())));
  }

  @Override
  public JComponent getComponent() {
    return this.textArea;
  }

  @Override
  public void setWrapStyleWord(final boolean flag) {
    this.textArea.setWrapStyleWord(flag);
  }

  @Override
  public void setLineWrap(final boolean flag) {
    this.textArea.setLineWrap(flag);
  }

  @Override
  public void replaceSelection(final String text) {
    this.textArea.replaceSelection(text);
  }

  @Override
  public String getSelectedText() {
    return this.textArea.getSelectedText();
  }

  @Override
  public void addCaretPosChangeListener(final CaretPosChangeListener caretListener) {
    this.caretPosChangeListenerList.add(caretListener);
  }

  @Override
  public void removeCaretPosChangeListener(final CaretPosChangeListener caretListener) {
    this.caretPosChangeListenerList.remove(caretListener);
  }

  @Override
  public void addUndoableEditListener(UndoableEditListener listener) {
    this.textArea.getDocument().addUndoableEditListener(listener);
  }

  @Override
  public void removeUndoableEditListener(UndoableEditListener listener) {
    this.textArea.getDocument().removeUndoableEditListener(listener);
  }

  @Override
  public int getCaretPos() {
    return this.textArea.getCaretPosition();
  }

  @Override
  public void setCaretPos(final int pos) {
    this.textArea.setCaretPosition(pos);
  }

  @Override
  public int getCaretRow() {
    final int caretPos = this.getCaretPos();
    int result = (caretPos == 0) ? 1 : 0;
    try {
      int offs = caretPos;
      while (offs > 0) {
        offs = Utilities.getRowStart(this.textArea, offs) - 1;
        result++;
      }
    } catch (BadLocationException e) {
      // ignore
    }
    return result;
  }

  @Override
  public int getCaretColumn() {
    final int caretPos = this.getCaretPos();
    try {
      return caretPos - Utilities.getRowStart(this.textArea, caretPos) + 1;
    } catch (BadLocationException e) {
      // ignore
    }
    return -1;
  }

  @Override
  public void setRowsColumns(final int rows, final int columns) {
    this.textArea.setColumns(columns);
    this.textArea.setRows(rows);
  }

  @Override
  public String getText() {
    return this.textArea.getText();
  }

  @Override
  public void setText(final String text) {
    this.textArea.setText(text);
  }


}
