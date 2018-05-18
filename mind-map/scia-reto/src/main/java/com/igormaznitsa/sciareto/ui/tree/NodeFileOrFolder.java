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

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.meta.annotation.ReturnsOriginal;
import com.igormaznitsa.meta.common.utils.ArrayUtils;
import com.igormaznitsa.meta.common.utils.Assertions;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.sciareto.Context;
import com.igormaznitsa.sciareto.preferences.PrefUtils;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.concurrent.RecursiveTask;

public class NodeFileOrFolder implements TreeNode, Comparator<NodeFileOrFolder>, Iterable<NodeFileOrFolder> {

  private static final Logger LOGGER = LoggerFactory.getLogger(NodeFileOrFolder.class);

  protected interface Cancelable {

    void cancel();

    boolean isCanceled();
  }

  public static final Cancelable THREAD_CANCELABLE = new Cancelable() {
    @Override
    public boolean isCanceled() {
      return Thread.currentThread().isInterrupted();
    }

    @Override
    public void cancel() {
      throw new UnsupportedOperationException("Can't be called directy");
    }
  };

  protected static class ForkedLoadNodeTask extends RecursiveTask<NodeFileOrFolder> {

    private final NodeFileOrFolder parent;
    private final boolean isdir;
    private final String name;
    private final boolean addhidden;
    private final boolean writable;
    private final Cancelable cancelable;

    protected ForkedLoadNodeTask(@Nonnull final NodeFileOrFolder parent, @Nonnull final Cancelable cancelable, final boolean isdir, @Nonnull final String name, final boolean addhidden, final boolean writable) {
      this.parent = parent;
      this.name = name;
      this.addhidden = addhidden;
      this.writable = writable;
      this.isdir = isdir;
      this.cancelable = cancelable;
    }

    @Override
    @Nonnull
    protected NodeFileOrFolder compute() {
      try {
        final NodeFileOrFolder result = new NodeFileOrFolder(this.parent, this.isdir, this.name, this.addhidden, this.writable);
        result.reloadSubtree(this.addhidden, this.cancelable);
        return result;
      } catch (final IOException ex) {
        throw new RuntimeException(ex);
      }
    }
  }

  protected final NodeFileOrFolder parent;

  protected final List<NodeFileOrFolder> children;
  protected final boolean folderFlag;

  protected volatile String name;

  private final boolean readonly;

  public NodeFileOrFolder(@Nullable final NodeFileOrFolder parent, final boolean folder, @Nullable final String name, final boolean showHiddenFiles, final boolean readOnly) throws IOException {
    this.parent = parent;
    this.name = name;

    if (folder) {
      this.children = new ArrayList<>();
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
    reloadSubtree(PrefUtils.isShowHiddenFilesAndFolders(),THREAD_CANCELABLE);
  }

  public void reloadSubtree(final boolean addHiddenFilesAndFolders, @Nonnull final Cancelable cancelable) throws IOException {
    this.children.clear();
    this.children.addAll(_reloadSubtree(addHiddenFilesAndFolders, cancelable));
  }

  @Nonnull
  @MustNotContainNull
  protected List<NodeFileOrFolder> _reloadSubtree(final boolean addHiddenFilesAndFolders, @Nonnull final Cancelable cancelableObject) throws IOException {
    final List<NodeFileOrFolder> result;
    if (this.folderFlag) {
      result = new ArrayList<>();
      final File generatedFile = makeFileForNode();
      if (generatedFile != null && generatedFile.isDirectory()) {
        final List<ForkedLoadNodeTask> forks = new ArrayList<>();
        try (final DirectoryStream<Path> stream = Files.newDirectoryStream(generatedFile.toPath())) {
          for (final Path p : stream) {
            if (cancelableObject.isCanceled()) {
              break;
            }
            if (addHiddenFilesAndFolders || !Files.isHidden(p)) {
              forks.add(new ForkedLoadNodeTask(this, cancelableObject, Files.isDirectory(p), p.getFileName().toString(), addHiddenFilesAndFolders, !Files.isWritable(p)));
            }
          }
        }

        for (final ForkedLoadNodeTask f : forks) {
          if (cancelableObject.isCanceled()) {
            break;
          }
          f.fork();
        }

        if (!cancelableObject.isCanceled()) {
          Collections.reverse(forks);

          for (final ForkedLoadNodeTask f : forks) {
            if (cancelableObject.isCanceled()) {
              break;
            }
            try {
              result.add(f.join());
            } catch (final RuntimeException ex) {
              if (ex.getCause() instanceof IOException) {
                throw (IOException) ex.getCause();
              } else {
                throw new IOException(ex);
              }
            }
          }

          if (!cancelableObject.isCanceled()) {
            Collections.sort(result, this);
          }
        }
      }
    } else {
      result = Collections.emptyList();
    }
    return result;
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

  @Override
  public boolean isLeaf() {
    return this.isLoading() ? true : !this.folderFlag;
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
    return this.children.remove(child);
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

}
