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
package com.igormaznitsa.sciareto.ui.tree;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import com.igormaznitsa.meta.common.utils.ArrayUtils;
import com.igormaznitsa.mindmap.model.nio.Path;
import com.igormaznitsa.mindmap.model.nio.Paths;

public class NodeProjectGroup extends NodeFileOrFolder implements TreeModel, Iterable<NodeProject> {

  protected final String groupName;
  protected final List<TreeModelListener> listeners = new CopyOnWriteArrayList<>();

  public NodeProjectGroup(@Nonnull final String name) {
    super(null, null, false);
    this.groupName = name;
  }

  @Nullable
  public NodeProject findForFolder(@Nonnull final File folder) {
    NodeProject result = null;
    for (final NodeFileOrFolder n : this.children) {
      if (folder.equals(n.file)) {
        result = (NodeProject) n;
      }
    }
    return result;
  }

  public void removeProject(@Nonnull final NodeProject project) {
    int index = this.children.indexOf(project);
    if (index >= 0 && this.children.remove(project)) {
      final TreeModelEvent event = new TreeModelEvent(this, new Object[]{this}, new int[]{index}, new Object[]{project});
      for (final TreeModelListener l : this.listeners) {
        l.treeNodesRemoved(event);
      }
    }
  }

  @Nonnull
  public NodeProject addProjectFolder(@Nonnull final File folder) {
    NodeProject newProject = findForFolder(folder);
    if (newProject == null) {
      newProject = new NodeProject(this, folder);

      final int index = this.children.size();
      this.children.add(newProject);

      final TreeModelEvent event = new TreeModelEvent(this, new Object[]{this}, new int[]{index}, new Object[]{newProject});
      for (final TreeModelListener l : this.listeners) {
        l.treeNodesInserted(event);
      }
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
    return ((NodeFileOrFolder) parent).getChildAt(index);
  }

  @Override
  public int getChildCount(@Nonnull final Object parent) {
    return ((NodeFileOrFolder) parent).getChildCount();
  }

  @Override
  public boolean isLeaf(@Nonnull final Object node) {
    return ((NodeFileOrFolder) node).isLeaf();
  }

  @Override
  public void valueForPathChanged(@Nonnull final TreePath path, @Nonnull final Object newValue) {

  }

  @Override
  public int getIndexOfChild(@Nonnull final Object parent, @Nonnull final Object child) {
    return ((NodeFileOrFolder) parent).getIndex((NodeFileOrFolder) child);
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
  public NodeProject findProjectForFile(@Nonnull final File file) {
    final Path filepath = Paths.toPath(file);
    for (final NodeFileOrFolder t : this.children) {
      if (t.getFile() != null && filepath.startsWith(Paths.toPath(t.getFile()))) {
        return (NodeProject) t;
      }
    }
    return null;
  }

  @Override
  @Nullable
  public TreePath findPathToFile(@Nonnull final File file) {
    TreePath path = null;
    for (final NodeFileOrFolder p : this.children) {
      path = p.findPathToFile(file);
      if (path != null) {
        break;
      }
    }
    if (path != null) {
      path = new TreePath(ArrayUtils.joinArrays(new Object[]{this}, path.getPath()));
    }
    return path;
  }

  public void refreshProjectFolder(@Nonnull final NodeProject nodeProject) {
    final int index = this.getIndex(nodeProject);
    if (index >= 0) {
      nodeProject.refresh(true);
      final TreeModelEvent event = new TreeModelEvent(this, new Object[]{this}, new int[]{index}, new Object[]{nodeProject});
      for (final TreeModelListener l : this.listeners) {
        l.treeStructureChanged(event);
      }
    }
  }

  @Override
  @Nonnull
  public Iterator<NodeProject> iterator() {
    final List<NodeFileOrFolder> projects = new ArrayList<>(this.children);
    final Iterator<NodeFileOrFolder> result = projects.iterator();
    return new Iterator<NodeProject>() {
      @Override
      public boolean hasNext() {
        return result.hasNext();
      }

      @Override
      @Nonnull
      public NodeProject next() {
        return (NodeProject)result.next();
      }
    };
  }

}
