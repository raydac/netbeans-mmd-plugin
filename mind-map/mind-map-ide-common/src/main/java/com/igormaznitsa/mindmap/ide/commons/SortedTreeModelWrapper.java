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

package com.igormaznitsa.mindmap.ide.commons;

import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.meta.common.utils.Assertions;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.MindMapModelEvent;
import com.igormaznitsa.mindmap.model.MindMapModelEventListener;
import com.igormaznitsa.mindmap.model.Topic;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.annotation.Nonnull;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public final class SortedTreeModelWrapper implements TreeModel, MindMapModelEventListener {
  private final MindMap model;

  private final Map<Object, List<Object>> sortedCache = new HashMap<>();
  private final Comparator<Object> comparator;
  private final List<TreeModelListener> treeListeners = new CopyOnWriteArrayList<>();

  public SortedTreeModelWrapper(@Nonnull final MindMap model, @Nonnull final Comparator<Object> comparator) {
    this.model = model;
    this.comparator = comparator;
    this.model.addMindMapModelEventListener(this);
  }

  @Override
  @Nonnull
  public Object getRoot() {
    return Assertions.assertNotNull(this.model.getRoot());
  }

  @Override
  @Nonnull
  public Object getChild(@Nonnull final Object parent, final int index) {
    return this.getChildrenFromCache(parent).get(index);
  }

  @Override
  public int getChildCount(@Nonnull final Object parent) {
    return this.model.getChildCount((Topic) parent);
  }

  @Override
  public boolean isLeaf(@Nonnull final Object node) {
    return this.model.isLeaf((Topic) node);
  }

  @Override
  public void valueForPathChanged(@Nonnull final TreePath path, @Nonnull final Object newValue) {
    this.model.valueForPathChanged((Topic[]) path.getPath(), (String) newValue);
    this.sortedCache.clear();
  }

  @Override
  public int getIndexOfChild(@Nonnull final Object parent, @Nonnull final Object child) {
    return getChildrenFromCache(parent).indexOf(child);
  }

  @Override
  public void addTreeModelListener(@Nonnull final TreeModelListener l) {
    this.treeListeners.add(l);
  }

  @Override
  public void removeTreeModelListener(@Nonnull final TreeModelListener l) {
    this.treeListeners.remove(l);
  }

  private void clear() {
    this.sortedCache.clear();
  }

  @Nonnull
  @MustNotContainNull
  private List<Object> getChildrenFromCache(@Nonnull final Object parent) {
    List<Object> result = this.sortedCache.get(parent);
    if (result == null) {
      result = new ArrayList<>();
      final int childCount = model.getChildCount((Topic) parent);
      for (int i = 0; i < childCount; i++) {
        result.add(model.getChild((Topic) parent, i));
      }
      if (this.comparator != null) {
        Collections.sort(result, this.comparator);
      }
      this.sortedCache.put(parent, result);
    }
    return result;
  }

  @Override
  public void onMindMapStructureChanged(@Nonnull final MindMapModelEvent event) {
    clear();
    final TreeModelEvent treeEvent = new TreeModelEvent(this, event.getPath());
    for (final TreeModelListener l : this.treeListeners) {
      l.treeStructureChanged(treeEvent);
    }
  }

  @Override
  public void onMindMapNodesChanged(@Nonnull final MindMapModelEvent event) {
    final TreeModelEvent treeEvent = new TreeModelEvent(this, event.getPath());
    for (final TreeModelListener l : this.treeListeners) {
      l.treeNodesChanged(treeEvent);
    }
  }

  public void dispose() {
    clear();
    this.model.removeMindMapModelEventListener(this);
  }

}
