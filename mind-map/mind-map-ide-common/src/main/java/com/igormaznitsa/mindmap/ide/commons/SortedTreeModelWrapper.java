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

import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.MindMapModelEvent;
import com.igormaznitsa.mindmap.model.MindMapModelListener;
import com.igormaznitsa.mindmap.model.Topic;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public final class SortedTreeModelWrapper implements TreeModel, MindMapModelListener {
  private final MindMap model;

  private final Map<Object, List<Object>> sortedCache = new HashMap<>();
  private final Comparator<Object> comparator;
  private final List<TreeModelListener> treeListeners = new CopyOnWriteArrayList<>();

  public SortedTreeModelWrapper(final MindMap model, final Comparator<Object> comparator) {
    this.model = model;
    this.comparator = comparator;
    this.model.addMindMapModelListener(this);
  }

  @Override
  public Object getRoot() {
    return Objects.requireNonNull(this.model.getRoot());
  }

  @Override
  public Object getChild(final Object parent, final int index) {
    return this.getChildrenFromCache(parent).get(index);
  }

  @Override
  public int getChildCount(final Object parent) {
    return ((Topic) parent).size();
  }

  @Override
  public boolean isLeaf(final Object node) {
    return ((Topic) node).isEmpty();
  }

  @Override
  public void valueForPathChanged(final TreePath path, final Object newValue) {
    final Topic [] topicPath = (Topic[]) path.getPath();
    if (topicPath.length > 0) {
      this.model.setTopicTextWithEvent(topicPath[topicPath.length - 1], (String) newValue);
      this.sortedCache.clear();
    }
  }

  @Override
  public int getIndexOfChild(final Object parent, final Object child) {
    return getChildrenFromCache(parent).indexOf(child);
  }

  @Override
  public void addTreeModelListener(final TreeModelListener l) {
    this.treeListeners.add(l);
  }

  @Override
  public void removeTreeModelListener(final TreeModelListener l) {
    this.treeListeners.remove(l);
  }

  private void clear() {
    this.sortedCache.clear();
  }

  private List<Object> getChildrenFromCache(final Object parent) {
    List<Object> result = this.sortedCache.get(parent);
    if (result == null) {
      result = new ArrayList<>();
      final int childCount = ((Topic) parent).size();
      for (int i = 0; i < childCount; i++) {
        result.add(((Topic) parent).getChildren().get(i));
      }
      if (this.comparator != null) {
        result.sort(this.comparator);
      }
      this.sortedCache.put(parent, result);
    }
    return result;
  }

  @Override
  public void onMindMapStructureChanged(final MindMapModelEvent event) {
    clear();
    final TreeModelEvent treeEvent = new TreeModelEvent(this, event.getPath());
    for (final TreeModelListener l : this.treeListeners) {
      l.treeStructureChanged(treeEvent);
    }
  }

  @Override
  public void onMindMapNodesChanged(final MindMapModelEvent event) {
    final TreeModelEvent treeEvent = new TreeModelEvent(this, event.getPath());
    for (final TreeModelListener l : this.treeListeners) {
      l.treeNodesChanged(treeEvent);
    }
  }

  public void dispose() {
    this.clear();
    this.model.removeMindMapModelListener(this);
  }

}
