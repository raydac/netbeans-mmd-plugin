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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.tree.TreeNode;

public class FileTreeNode implements TreeNode, Transferable {

  protected final FileTreeNode parent;
  protected final File file;
  
  protected final List<FileTreeNode> children;
  protected final boolean folder;
  
  protected final String fileName;
  
  private static final DataFlavor [] DATA_FLAVOR = new DataFlavor[]{DataFlavor.javaFileListFlavor};
  
  public FileTreeNode(@Nullable final FileTreeNode parent, @Nullable final File file, final boolean fillChildren){
    this.parent = parent;
    this.file = file;
    this.fileName = file == null ? "." : file.getName();
    if (this.file == null){
      this.children = new ArrayList<>();
      this.folder = true;
    }else
    if (this.file.isDirectory()) {
      this.folder = true;
      this.children = new ArrayList<>();
    } else {
      this.children = Collections.EMPTY_LIST;
      this.folder = false;
    }
    refresh(fillChildren);
  }
  
  @Nullable
  public File getFile(){
    return this.file;
  }

  @Override
  public DataFlavor[] getTransferDataFlavors() {
    return new DataFlavor[]{DataFlavor.javaFileListFlavor};
  }

  @Override
  public boolean isDataFlavorSupported(final DataFlavor flavor) {
    return DataFlavor.javaFileListFlavor.equals(flavor);
  }

  @Override
  public Object getTransferData(final DataFlavor flavor) throws UnsupportedFlavorException, IOException {
    if (isDataFlavorSupported(flavor)){
      return this;
    } else {
      throw new UnsupportedFlavorException(flavor);
    }
  }
  
  protected final void refresh(final boolean deepRefresh){
    if (this.file != null && this.folder){
      this.children.clear();
      for (final File f : file.listFiles()) {
        this.children.add(new FileTreeNode(this, f, deepRefresh));
      }
    }
  }
  
  @Override
  @Nonnull
  public String toString(){
    return this.fileName;
  }
  
  @Override
  @Nonnull
  public TreeNode getChildAt(final int childIndex) {
    return this.children.get(childIndex);
  }

  @Override
  public int getChildCount() {
    return this.children.size();
  }

  @Override
  @Nullable
  public TreeNode getParent() {
    return this.parent;
  }

  @Override
  public int getIndex(@Nonnull final TreeNode node) {
    return this.children.indexOf(node);
  }

  @Override
  public boolean getAllowsChildren() {
    return this.folder;
  }

  @Override
  public boolean isLeaf() {
    return !this.folder;
  }

  @Override
  @Nonnull
  public Enumeration children() {
    final Iterator<FileTreeNode> iterator = this.children.iterator();
    return new Enumeration() {
      
      @Override
      public boolean hasMoreElements() {
        return iterator.hasNext();
      }

      @Override
      @Nonnull
      public Object nextElement() {
        return iterator.next();
      }
    };
  }
  
}
