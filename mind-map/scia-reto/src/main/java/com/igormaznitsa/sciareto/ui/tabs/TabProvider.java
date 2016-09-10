/*
 * Copyright 2016 Igor Maznitsa.
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
package com.igormaznitsa.sciareto.ui.tabs;

import java.io.File;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.swing.JComponent;
import javax.swing.filechooser.FileFilter;
import com.igormaznitsa.meta.annotation.Weight;
import com.igormaznitsa.sciareto.ui.editors.EditorType;

public interface TabProvider {


  void focusToEditor();

  @Weight(Weight.Unit.NORMAL)
  @Nonnull
  TabTitle getTabTitle();
  
  @Weight(Weight.Unit.LIGHT)
  @Nonnull
  JComponent getMainComponent();
  boolean saveDocument() throws IOException;
  boolean saveDocumentAs() throws IOException;
  @Nonnull
  FileFilter getFileFilter();
  void dispose();
  void updateConfiguration();
  void loadContent(@Nonnull File file) throws IOException;
  
  @Nonnull
  EditorType getContentType();
  
  boolean isEditable();

  boolean isSaveable();

  boolean isRedo();
  boolean isUndo();

  boolean redo();
  boolean undo();
  
}
