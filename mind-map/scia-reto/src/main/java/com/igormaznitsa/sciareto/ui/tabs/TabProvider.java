/*
 * Copyright (C) 2015-2022 Igor A. Maznitsa
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.igormaznitsa.sciareto.ui.tabs;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.swing.JComponent;
import javax.swing.filechooser.FileFilter;
import com.igormaznitsa.meta.annotation.Weight;
import com.igormaznitsa.sciareto.ui.FindTextScopeProvider;
import com.igormaznitsa.sciareto.ui.editors.AbstractEditor;
import com.igormaznitsa.sciareto.ui.editors.SelectCommand;
import javax.swing.JPanel;

public interface TabProvider {

  void focusToEditor(int line);

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
  AbstractEditor getEditor();
  
  boolean isEditable();

  boolean isSaveable();

  boolean isRedo();
  boolean isUndo();

  boolean redo();
  boolean undo();
  
  boolean doesSupportCutCopyPaste();
  boolean isCopyAllowed();
  boolean isPasteAllowed();
  boolean isCutAllowed();
  
  boolean isSelectCommandAllowed(@Nonnull SelectCommand command);
  void doSelectCommand(@Nonnull SelectCommand command);
  
  boolean doCopy();
  boolean doCut();
  boolean doPaste();
  
  boolean findNext(@Nonnull Pattern pattern, @Nonnull FindTextScopeProvider provider);
  boolean findPrev(@Nonnull Pattern pattern,@Nonnull FindTextScopeProvider provider);
  boolean doesSupportPatternSearch();

  boolean showSearchPane(@Nonnull JPanel searchPanel); 
}
