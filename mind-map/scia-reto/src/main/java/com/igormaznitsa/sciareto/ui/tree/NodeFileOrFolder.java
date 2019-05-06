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
package com.igormaznitsa.sciareto.ui.tree;

import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.meta.annotation.ReturnsOriginal;
import com.igormaznitsa.meta.common.utils.ArrayUtils;
import com.igormaznitsa.meta.common.utils.Assertions;
import com.igormaznitsa.meta.common.utils.IOUtils;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.sciareto.Context;
import com.igormaznitsa.sciareto.preferences.PrefUtils;
import com.igormaznitsa.sciareto.ui.MainFrame;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

public class NodeFileOrFolder implements TreeNode, Comparator<NodeFileOrFolder>, Iterable<NodeFileOrFolder> {

  private static final Logger LOGGER = LoggerFactory.getLogger(NodeFileOrFolder.class);

  protected final NodeFileOrFolder parent;

  protected final List<NodeFileOrFolder> children;
  protected final boolean folderFlag;

  protected volatile String name;
  private volatile boolean noAccess;
  private final boolean readonly;

  private volatile boolean disposed = false;

  public NodeFileOrFolder(
          @Nullable final NodeFileOrFolder parent,
          final boolean folder,
          @Nullable final String name,
          final boolean showHiddenFiles,
          final boolean readOnly
  ) {
    this.parent = parent;
    this.name = name;

    if (folder) {
      this.children = Collections.synchronizedList(new ArrayList<>());
      this.folderFlag = true;
    } else {
      this.children = Collections.EMPTY_LIST;
      this.folderFlag = false;
    }

    this.readonly = readOnly;
  }

  public boolean isLoading() {
    return false;
  }

  public int size() {
    if (this.folderFlag) {
      int counter = 1;
      for (final NodeFileOrFolder f : this.children) {
        counter += f.size();
      }
      return counter;
    } else {
      return 1;
    }
  }

  public boolean isReadOnly() {
    return this.readonly;
  }

  public boolean isProjectKnowledgeFolder() {
    return !this.isLeaf() && Context.KNOWLEDGE_FOLDER.equals(this.name); //NOI18N
  }

  @Nullable
  public NodeProject findProject() {
    NodeFileOrFolder path = this;
    while (path != null) {
      if (path instanceof NodeProject) {
        return (NodeProject) path;
      }
      path = path.getNodeParent();
    }
    return null;
  }

  @Override
  public int compare(@Nonnull final NodeFileOrFolder o1, @Nonnull final NodeFileOrFolder o2) {
    final String name1 = o1.name;
    final String name2 = o2.name;

    if (o1.isLeaf() == o2.isLeaf()) {
      if (!o1.isLeaf()) {
        if (Context.KNOWLEDGE_FOLDER.equals(name1)) {
          return -1;
        } else if (Context.KNOWLEDGE_FOLDER.equals(name2)) {
          return 1;
        }
      }
      return name1.compareTo(name2);
    } else {
      return o1.isLeaf() ? 1 : -1;
    }
  }

  @Nonnull
  public NodeFileOrFolder addFile(@Nonnull final File file, final boolean showHiddenFiles) throws IOException {
    Assertions.assertTrue("Unexpected state!", this.folderFlag && file.getParentFile().equals(this.makeFileForNode())); //NOI18N
    final NodeFileOrFolder result = new NodeFileOrFolder(this, file.isDirectory(), file.getName(), showHiddenFiles, !Files.isWritable(file.toPath()));
    this.children.add(0, result);
    Collections.sort(this.children, this);
    return result;
  }

  public void setName(@Nonnull final String name) throws IOException {
    this.name = name;
    readSubtree(PrefUtils.isShowHiddenFilesAndFolders()).subscribeOn(MainFrame.PARALLEL_SCHEDULER).subscribe();
  }

  private void clearChildren() {
    try {
      this.children.forEach(NodeFileOrFolder::dispose);
    } finally {
      this.children.clear();
    }
  }

  protected static boolean isFileHidden(@Nonnull final Path path) {
    try {
      return Files.isHidden(path);
    } catch (IOException ex) {
      throw Exceptions.propagate(ex);
    }
  }

  @Nonnull
  public Mono<NodeFileOrFolder> readSubtree(final boolean addHiddenFilesAndFolders) {
      if (this.folderFlag) {
          final boolean parentIsProjectGroup = this.parent instanceof NodeProjectGroup;
          return Flux.using(() -> {
              this.clearChildren();
              final File nodeFile = makeFileForNode();
              try {
                  return Files.newDirectoryStream(nodeFile.toPath());
              } catch (Exception ex) {
                  LOGGER.warn("Error '" + ex.getClass().getCanonicalName() + "' during access to path: " + nodeFile.getPath());
                  this.noAccess = true;
                  return new DirectoryStream<Path>() {
                      @Override
                      public Iterator<Path> iterator() {
                          return Collections.emptyIterator();
                      }

                      @Override
                      public void close() throws IOException {
                      }
                  };
              }
          }, Flux::fromIterable, IOUtils::closeQuetly)
                  .parallel()
                  .doOnError(error -> {
                      LOGGER.warn("Error during path " + makeFileForNode().getName() + " opening: " + error.getMessage());
                      this.noAccess = true;
                  })
                  .filter(f -> {
                      if (parentIsProjectGroup) {
                          return addHiddenFilesAndFolders || !isFileHidden(f) || Context.KNOWLEDGE_FOLDER.equals(f.getFileName().toString());
                      } else {
                          return addHiddenFilesAndFolders || !isFileHidden(f);
                      }
                  })
                  .map(f -> {
                      NodeFileOrFolder newItem = new NodeFileOrFolder(this, Files.isDirectory(f), f.getFileName().toString(), addHiddenFilesAndFolders, !Files.isWritable(f));
                      this.children.add(newItem);
                      return newItem;
                  })
                  .flatMap(f -> f.readSubtree(addHiddenFilesAndFolders))
                  .reduce((x, y) -> {
                      return this;
                  })
                  .doFinally(signalType -> {
                      if (signalType == SignalType.ON_COMPLETE) {
                          this.children.sort(this);
                      }
                  });
      } else {
          return Mono.empty();
      }
  }

  @Nullable
  public File makeFileForNode() {
    if (this.parent == null) {
      return null;
    } else {
      return new File(this.parent.makeFileForNode(), this.name);
    }
  }

  void fireNotifySubtreeChanged(@Nonnull TreeModel model, @Nonnull @MustNotContainNull final List<TreeModelListener> listeners) {
    if (this.parent != null && this.folderFlag) {
      final Object[] childrenObject = new Object[children.size()];
      final int[] indexes = new int[children.size()];
      for (int i = 0; i < this.children.size(); i++) {
        final NodeFileOrFolder c = this.children.get(i);
        childrenObject[i] = c;
        indexes[i] = i;
        c.fireNotifySubtreeChanged(model, listeners);
      }
      final TreeModelEvent event = new TreeModelEvent(model, this.parent.makeTreePath(), indexes, childrenObject);
      for (final TreeModelListener l : listeners) {
        l.treeStructureChanged(event);
      }
    }
  }

  @Nonnull
  @MustNotContainNull
  @ReturnsOriginal
  public List<NodeFileOrFolder> findRelatedNodes(@Nonnull final File file, @Nonnull @MustNotContainNull final List<NodeFileOrFolder> list) {
    final File theFile = makeFileForNode();
    if (theFile != null) {
      if (file.equals(theFile) || theFile.toPath().startsWith(file.toPath())) {
        list.add(this);
        for (final NodeFileOrFolder f : this.children) {
          f.findRelatedNodes(file, list);
        }
      }
    }
    return list;
  }

  @Override
  @Nonnull
  public String toString() {
    return this.name;
  }

  @Override
  @Nonnull
  public TreeNode getChildAt(final int childIndex) {
    return this.children.get(childIndex);
  }

  @Override
  public int getChildCount() {
    return this.isLoading() ? 0 : this.children.size();
  }

  @Nullable
  public NodeFileOrFolder getNodeParent() {
    return this.parent;
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
    return this.folderFlag;
  }

  public boolean hasNoAccess() {
    return this.noAccess;
  }

  @Override
  public boolean isLeaf() {
    return this.isLoading() ? true : !(this.folderFlag || this.noAccess);
  }

  @Override
  @Nonnull
  public Enumeration children() {
    final Iterator<NodeFileOrFolder> iterator = this.children.iterator();
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

  @Nonnull
  public TreePath makeTreePath() {
    final List<Object> path = new ArrayList<>();
    TreeNode node = this;
    while (node != null) {
      path.add(0, node);
      node = node.getParent();
    }
    return new TreePath(path.toArray());
  }

  public int getIndexAtParent() {
    int result = -1;
    if (this.parent != null) {
      result = this.parent.getIndex(this);
    }
    return result;
  }

  @Nullable
  public TreePath findPathToFile(@Nonnull final File file) {
    final File generatedFile = makeFileForNode();
    if (file.equals(generatedFile)) {
      return new TreePath(new Object[]{this});
    }
    if (!this.isLeaf()) {
      for (final NodeFileOrFolder c : this.children) {
        final TreePath result = c.findPathToFile(file);
        if (result != null) {
          return new TreePath(ArrayUtils.joinArrays(new Object[]{this}, result.getPath()));
        }
      }
    }
    return null;
  }

  boolean deleteChild(@Nonnull final NodeFileOrFolder child) {
    boolean result = false;
    try {
      result = this.children.remove(child);
    } finally {
      child.dispose();
    }
    return result;
  }

  protected void fillAllMatchNamePattern(@Nonnull final Pattern namePattern, @Nonnull @MustNotContainNull final List<NodeFileOrFolder> resultList) {
    if (namePattern.matcher(this.name).matches()) {
      resultList.add(this);
    }
    if (!this.isLeaf()) {
      for (final NodeFileOrFolder c : this.children) {
        c.fillAllMatchNamePattern(namePattern, resultList);
      }
    }
  }

  public boolean isMindMapFile() {
    return !this.folderFlag && this.name.endsWith(".mmd"); //NOI18N
  }

  @Override
  @Nonnull
  public Iterator<NodeFileOrFolder> iterator() {
    final List<NodeFileOrFolder> projects = new ArrayList<>(this.children);
    final Iterator<NodeFileOrFolder> result = projects.iterator();
    return new Iterator<NodeFileOrFolder>() {
      @Override
      public boolean hasNext() {
        return result.hasNext();
      }

      @Override
      public void remove() {
        result.remove();
      }

      @Override
      @Nonnull
      public NodeFileOrFolder next() {
        return result.next();
      }
    };
  }

  public final boolean isDisposed() {
    return this.disposed;
  }

  public final void dispose() {
    if (!this.disposed) {
      this.disposed = true;
      try {
        this.onDispose();
      } finally {
        this.children.forEach((c) -> {
          c.dispose();
        });
      }
    }
  }

  protected void onDispose() {

  }
}
