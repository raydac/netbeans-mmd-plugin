/* 
 * Copyright (C) 2018 Igor Maznitsa.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package com.igormaznitsa.sciareto;

import java.io.File;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.sciareto.ui.tree.NodeProject;
import com.igormaznitsa.sciareto.ui.tabs.TabTitle;
import com.igormaznitsa.sciareto.ui.tree.NodeFileOrFolder;
import com.igormaznitsa.sciareto.ui.tree.NodeProjectGroup;

public interface Context {

  static String KNOWLEDGE_FOLDER = ".projectKnowledge";
  
  @Nullable
  NodeProject findProjectForFile(@Nonnull File file);

  boolean openFileAsTab(@Nonnull File file);

  boolean openProject(@Nonnull File folder, boolean enforceSeparatedItem);

  void closeTab(@Nonnull @MustNotContainNull TabTitle... title);

  void editPreferences();

  void onCloseProject(@Nonnull final NodeProject project);

  boolean focusInTree(@Nonnull TabTitle title);

  boolean focusInTree(@Nonnull File file);
  
  boolean safeCloseEditorsForFile(@Nonnull File file);

  void showFindTextPane(@Nullable String text);
  
  void hideFindTextPane();
  
  boolean showGraphMindMapFileLinksDialog(@Nullable File projectFolder, @Nullable File file, final boolean openIfSelected);
  
  @Nullable
  TabTitle getFocusedTab();
  
  @Nullable
  File createMindMapFile(@Nonnull File folder);

  void notifyReloadConfig();

  boolean deleteTreeNode(@Nonnull NodeFileOrFolder node);
  
  void notifyFileRenamed(@Nullable @MustNotContainNull List<File> affectedFiles, @Nonnull File oldFile, @Nonnull File newFile);
 
  void notifyUpdateRedoUndo();

  boolean hasUnsavedDocument();

  boolean centerRootTopicIfFocusedMMD();

  @Nonnull
  NodeProjectGroup getCurrentGroup();
}
