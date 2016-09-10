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
package com.igormaznitsa.sciareto;

import java.io.File;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.sciareto.ui.tree.NodeProject;
import com.igormaznitsa.sciareto.ui.tabs.TabTitle;
import com.igormaznitsa.sciareto.ui.tree.NodeFileOrFolder;

public interface Context {


  @Nullable
  NodeProject findProjectForFile(@Nonnull File file);

  boolean openFileAsTab(@Nonnull File file);

  boolean openProject(@Nonnull File folder, boolean enforceSeparatedItem);

  void closeTab(@Nonnull @MustNotContainNull TabTitle... title);

  void editPreferences();

  void onCloseProject(@Nonnull final NodeProject project);

  void focusInTree(@Nonnull TabTitle title);

  void focusInTree(@Nonnull File file);
  
  boolean safeCloseEditorsForFile(@Nonnull File file);

  @Nullable
  TabTitle getFocusedTab();
  
  @Nullable
  File createMindMapFile(@Nonnull File folder);

  void notifyReloadConfig();

  boolean deleteTreeNode(@Nonnull NodeFileOrFolder node);
  
  void notifyFileRenamed(@Nullable @MustNotContainNull List<File> affectedFiles, @Nonnull File oldFile, @Nonnull File newFile);
  void notifyUpdateRedoUndo();
}
