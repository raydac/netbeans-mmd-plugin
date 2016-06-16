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
package com.igormaznitsa.sciareto.tree;

import java.io.File;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import com.igormaznitsa.mindmap.model.nio.Path;
import com.igormaznitsa.mindmap.model.nio.Paths;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;

public class ProjectGroupTree extends FileTreeNode implements TreeModel {

  protected final String groupName;
  protected final List<TreeModelListener> listeners = new CopyOnWriteArrayList<>();

  public ProjectGroupTree(@Nonnull final String name) {
    super(null, null, false);
    this.groupName = name;
  }

  @Nullable
  public ProjectTree findForFolder(@Nonnull final File folder) {
    ProjectTree result = null;
    for (final FileTreeNode n : this.children) {
      if (folder.equals(n.file)) {
        result = (ProjectTree) n;
      }
    }
    return result;
  }

  public void remove(@Nonnull final ProjectTree project){
    if (this.children.remove(project)){
      final ProjectGroupTree theInstance = this;
      Utils.safeSwingCall(new Runnable() {
        @Override
        public void run() {
          final TreeModelEvent event = new TreeModelEvent(theInstance, new Object[]{theInstance, project});
          for (final TreeModelListener l : listeners) {
            l.treeNodesRemoved(event);
          }
        }
      });
    }
  }
  
  @Nonnull
  public ProjectTree addFolder(@Nonnull final File folder) {
    ProjectTree newProject = findForFolder(folder);
    if (newProject == null) {
      newProject = new ProjectTree(this, folder);
      
      this.children.add(newProject);
      final ProjectGroupTree theInstance = this;

      Utils.safeSwingCall(new Runnable() {
        @Override
        public void run() {
          final TreeModelEvent event = new TreeModelEvent(theInstance, new Object[]{theInstance});
          for (final TreeModelListener l : listeners) {
            l.treeStructureChanged(event);
          }
        }
      });
    }
    return newProject;
  }

  @Override
  @Nonnull
  public Object getRoot() {
    return this;
  }

  @Override
  @Nonnull
  public Object getChild(@Nonnull final Object parent, final int index) {
    return ((FileTreeNode) parent).getChildAt(index);
  }

  @Override
  public int getChildCount(@Nonnull final Object parent) {
    return ((FileTreeNode)parent).getChildCount();
  }

  @Override
  public boolean isLeaf(@Nonnull final Object node) {
    return ((FileTreeNode) node).isLeaf();
  }

  @Override
  public void valueForPathChanged(@Nonnull final TreePath path, @Nonnull final Object newValue) {

  }

  @Override
  public int getIndexOfChild(@Nonnull final Object parent, @Nonnull final Object child) {
    return ((FileTreeNode) parent).getIndex((FileTreeNode) child);
  }

  @Override
  public void addTreeModelListener(@Nonnull final TreeModelListener l) {
    this.listeners.add(l);
  }

  @Override
  public void removeTreeModelListener(@Nonnull final TreeModelListener l) {
    this.listeners.remove(l);
  }

  @Nullable
  public ProjectTree findProjectForFile(@Nonnull final File file){
    final Path filepath = Paths.toPath(file);
    for(final FileTreeNode t : this.children){
      if (t.getFile()!=null && filepath.startsWith(Paths.toPath(t.getFile()))){
        return (ProjectTree)t;
      }
    }
    return null;
  }
  
}
