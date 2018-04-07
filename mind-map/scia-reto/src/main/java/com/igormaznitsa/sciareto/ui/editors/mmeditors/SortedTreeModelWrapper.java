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
package com.igormaznitsa.sciareto.ui.editors.mmeditors;

import com.igormaznitsa.mindmap.model.MindMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.meta.common.utils.Assertions;

public final class SortedTreeModelWrapper implements TreeModel, TreeModelListener {
  private final MindMap model;
  
  private final Map<Object,List<Object>> sortedCache = new HashMap<>();
  private final Comparator<Object> comparator;
  
  public SortedTreeModelWrapper(@Nonnull final MindMap model, @Nonnull final Comparator<Object> comparator){
    this.model = model;
    this.comparator = comparator;
    this.model.addTreeModelListener(this);
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
    return this.model.getChildCount(parent);
  }

  @Override
  public boolean isLeaf(@Nonnull final Object node) {
    return this.model.isLeaf(node);
  }

  @Override
  public void valueForPathChanged(@Nonnull final TreePath path, @Nonnull final Object newValue) {
    this.model.valueForPathChanged(path, newValue);
    this.sortedCache.clear();
  }

  @Override
  public int getIndexOfChild(@Nonnull final Object parent, @Nonnull final Object child) {
    return getChildrenFromCache(parent).indexOf(child);
  }

  @Override
  public void addTreeModelListener(@Nonnull final TreeModelListener l) {
    this.model.addTreeModelListener(l);
  }

  @Override
  public void removeTreeModelListener(@Nonnull final TreeModelListener l) {
    this.model.removeTreeModelListener(l);
  }
  
  private void clear(){
    this.sortedCache.clear();
  }
  
  @Nonnull
  @MustNotContainNull
  private List<Object> getChildrenFromCache(@Nonnull final Object parent){
    List<Object> result = this.sortedCache.get(parent);
    if (result == null){
      result = new ArrayList<>();
      final int childCount = model.getChildCount(parent);
      for(int i=0;i<childCount;i++){
        result.add(model.getChild(parent, i));
      }
      if (this.comparator!=null){
        Collections.sort(result, this.comparator);
      }
      this.sortedCache.put(parent, result);
    }
    return result;
  }

  @Override
  public void treeNodesChanged(@Nonnull final TreeModelEvent e) {
    
  }

  @Override
  public void treeNodesInserted(@Nonnull final TreeModelEvent e) {
    clear();
  }

  @Override
  public void treeNodesRemoved(@Nonnull final TreeModelEvent e) {
    clear();
  }

  @Override
  public void treeStructureChanged(@Nonnull final TreeModelEvent e) {
    clear();
  }
   
  public void dispose(){
    clear();
    this.model.removeTreeModelListener(this);
  }
  
}
