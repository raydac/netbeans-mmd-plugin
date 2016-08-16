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
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.apache.commons.io.FilenameUtils;
import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.meta.common.utils.ArrayUtils;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.model.nio.Path;
import com.igormaznitsa.mindmap.model.nio.Paths;
import com.igormaznitsa.sciareto.Context;
import com.igormaznitsa.sciareto.ui.DialogProviderManager;
import com.igormaznitsa.sciareto.ui.UiUtils;

public class NodeProjectGroup extends NodeFileOrFolder implements TreeModel, Iterable<NodeProject> {

  protected final String groupName;
  protected final List<TreeModelListener> listeners = new CopyOnWriteArrayList<>();
  private final Context context;

  public static final Pattern FILE_NAME = Pattern.compile("^[^\\+\\*\\?\\{\\}\\&\\|\\;\\:\\\\\\/]+$");

  private static final Logger LOGGER = LoggerFactory.getLogger(NodeProjectGroup.class);

  public NodeProjectGroup(@Nonnull final Context context, @Nonnull final String name) {
    super(null, true, ".", false);
    this.groupName = name;
    this.context = context;
  }

  @Override
  @Nullable
  public File makeFileForNode() {
    return null;
  }

  @Nullable
  public NodeProject findForFolder(@Nonnull final File folder) {
    NodeProject result = null;
    for (final NodeFileOrFolder n : this.children) {
      if (folder.equals(((NodeProject) n).getFolder())) {
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
    String newFileName = String.valueOf(newValue);
    
    if (FILE_NAME.matcher(newFileName).matches()) {
      final Object last = path.getLastPathComponent();
      if (last instanceof NodeFileOrFolder) {
        final NodeFileOrFolder editedNode = (NodeFileOrFolder) last;

        final String oldName = editedNode.toString();
        
        if (!oldName.equals(newFileName)) {
          final File origFile = ((NodeFileOrFolder) last).makeFileForNode();
          final String oldExtension = FilenameUtils.getExtension(oldName);
          final String newExtension = FilenameUtils.getExtension(newFileName);
          
          if (!oldExtension.equals(newExtension)){
            if (DialogProviderManager.getInstance().getDialogProvider().msgConfirmYesNo("Changed extension", String.format("You have changed extension! Restore old extension '%s'?",oldExtension))){
              newFileName = FilenameUtils.getBaseName(newFileName)+(oldExtension.isEmpty() ? "" : '.'+oldExtension);
            }
          }
          
          if (origFile != null) {
            final File newFile = new File(origFile.getParentFile(), newFileName);

            if (!editedNode.isLeaf() && !context.safeCloseEditorsForFile(origFile)) {
              return;
            }

            try {

              boolean doIt = true;

              List<File> affectedFiles = null;
              
              final NodeProject project = editedNode.findProject();
              if (project != null) {
                affectedFiles = project.findAffectedFiles(origFile);
                if (!affectedFiles.isEmpty()) {
                  affectedFiles = UiUtils.showSelectAffectedFiles(affectedFiles);
                  if (affectedFiles == null) {
                    doIt = false;
                  } else {
                      affectedFiles = project.replaceAllLinksToFile(affectedFiles, origFile, newFile);
                  }
                }
              }

              if (doIt) {
                Files.move(origFile.toPath(), newFile.toPath());
                editedNode.setName(newFile.getName());

                editedNode.fireNotifySubtreeChanged(this, listeners);

                this.context.notifyFileRenamed(affectedFiles, origFile, newFile);
              }
            } catch (IOException ex) {
              LOGGER.error("Can't rename file", ex);
              DialogProviderManager.getInstance().getDialogProvider().msgError("Can't rename file to '" + newValue + "\'");
            }
          }
        }
      }
    } else {
      DialogProviderManager.getInstance().getDialogProvider().msgError("Inapropriate file name '" + newFileName + "'!");
    }
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
      final File projectFolder = ((NodeProject) t).getFolder();
      if (filepath.startsWith(Paths.toPath(projectFolder))) {
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
      nodeProject.reloadSubtree();
      nodeProject.fireNotifySubtreeChanged(this, this.listeners);
    }
  }

  public boolean fireNotificationThatNodeDeleted(@Nonnull final NodeFileOrFolder node) {
    final NodeFileOrFolder parentNode = node.getNodeParent();
    if (parentNode != null) {
      final TreeModelEvent event = new TreeModelEvent(this, parentNode.makeTreePath(), new int[]{node.getIndexAtParent()}, new Object[]{node});
      if (parentNode.deleteChild(node)) {
        for (final TreeModelListener l : this.listeners) {
          l.treeNodesRemoved(event);
        }
        return true;
      }
    }
    return false;
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
      public void remove() {
        result.remove();
      }

      @Override
      @Nonnull
      public NodeProject next() {
        return (NodeProject) result.next();
      }
    };
  }

  public void addChild(@Nonnull final NodeFileOrFolder folder, @Nonnull final File childFile) {
    final NodeFileOrFolder newNode = folder.addFile(childFile);
    final TreeModelEvent event = new TreeModelEvent(this, folder.makeTreePath(), new int[]{newNode.getIndexAtParent()}, new Object[]{newNode});
    for (final TreeModelListener l : this.listeners) {
      l.treeNodesInserted(event);
    }
  }

  @Nonnull
  @MustNotContainNull
  public List<NodeFileOrFolder> findForNamePattern(@Nullable final Pattern namePattern) {
    final List<NodeFileOrFolder> result = new ArrayList<>();

    if (namePattern != null) {
      for (final NodeFileOrFolder f : this.children) {
        f.fillAllMatchNamePattern(namePattern, result);
      }
    }

    return result;
  }

}
