/*
 * Copyright 2015 Igor Maznitsa.
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
package com.igormaznitsa.nbmindmap.nb.swing;

import com.igormaznitsa.mindmap.model.MindMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class SortedTreeModelWrapper implements TreeModel, TreeModelListener {
  private final MindMap model;
  
  private final Map<Object,List<Object>> sortedCache = new HashMap<>();
  private final Comparator<Object> comparator;
  
  public SortedTreeModelWrapper(final MindMap model, final Comparator<Object> comparator){
    this.model = model;
    this.comparator = comparator;
    this.model.addTreeModelListener(this);
  }

  @Override
  public Object getRoot() {
    return this.model.getRoot();
  }

  @Override
  public Object getChild(final Object parent, final int index) {
    return this.getChildrenFromCache(parent).get(index);
  }

  @Override
  public int getChildCount(final Object parent) {
    return this.model.getChildCount(parent);
  }

  @Override
  public boolean isLeaf(final Object node) {
    return this.model.isLeaf(node);
  }

  @Override
  public void valueForPathChanged(final TreePath path, final Object newValue) {
    this.model.valueForPathChanged(path, newValue);
    this.sortedCache.clear();
  }

  @Override
  public int getIndexOfChild(final Object parent, final Object child) {
    return getChildrenFromCache(parent).indexOf(child);
  }

  @Override
  public void addTreeModelListener(TreeModelListener l) {
    this.model.addTreeModelListener(l);
  }

  @Override
  public void removeTreeModelListener(TreeModelListener l) {
    this.model.removeTreeModelListener(l);
  }
  
  private void clear(){
    this.sortedCache.clear();
  }
  
  private List<Object> getChildrenFromCache(final Object parent){
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
  public void treeNodesChanged(final TreeModelEvent e) {
    
  }

  @Override
  public void treeNodesInserted(final TreeModelEvent e) {
    clear();
  }

  @Override
  public void treeNodesRemoved(final TreeModelEvent e) {
    clear();
  }

  @Override
  public void treeStructureChanged(final TreeModelEvent e) {
    clear();
  }
   
  public void dispose(){
    clear();
    this.model.removeTreeModelListener(this);
  }
  
}
