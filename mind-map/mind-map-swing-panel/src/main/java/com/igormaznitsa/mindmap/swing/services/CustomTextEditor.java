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

package com.igormaznitsa.mindmap.swing.services;

import javax.swing.JComponent;
import javax.swing.event.UndoableEditListener;

/**
 * Describes wrapper which can provide text edit component.
 *
 * @see UIComponentFactory
 * @since 1.6.6
 */
public interface CustomTextEditor {

  JComponent getComponent();

  void setWrapStyleWord(boolean flag);

  void setLineWrap(boolean flag);

  void replaceSelection(String text);

  String getText();

  void setText(String text);

  String getSelectedText();

  void addCaretPosChangeListener(CaretPosChangeListener caretListener);

  void removeCaretPosChangeListener(CaretPosChangeListener caretListener);

  void addUndoableEditListener(UndoableEditListener listener);

  void removeUndoableEditListener(UndoableEditListener listener);

  int getCaretPos();

  void setCaretPos(int pos);

  int getCaretRow();

  int getCaretColumn();

  void setRowsColumns(int rows, int columns);

  @FunctionalInterface
  interface CaretPosChangeListener {
    void onCaretPosChange(int caretPos);
  }


}
