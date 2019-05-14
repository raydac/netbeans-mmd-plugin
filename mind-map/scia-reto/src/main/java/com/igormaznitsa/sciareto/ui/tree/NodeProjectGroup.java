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
import com.igormaznitsa.meta.annotation.UiThread;
import com.igormaznitsa.meta.common.utils.ArrayUtils;
import com.igormaznitsa.meta.common.utils.Assertions;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.model.nio.Path;
import com.igormaznitsa.mindmap.model.nio.Paths;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import com.igormaznitsa.sciareto.Context;
import com.igormaznitsa.sciareto.Main;
import com.igormaznitsa.sciareto.preferences.PrefUtils;
import com.igormaznitsa.sciareto.ui.DialogProviderManager;
import com.igormaznitsa.sciareto.ui.MainFrame;
import com.igormaznitsa.sciareto.ui.UiUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
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
import reactor.core.publisher.Mono;

public class NodeProjectGroup extends NodeFileOrFolder implements TreeModel {

  protected final String groupName;
  protected final List<TreeModelListener> listeners = new CopyOnWriteArrayList<>();
  private final Context context;

  public static final Pattern FILE_NAME = Pattern.compile("^[^\\+\\*\\?\\{\\}\\&\\|\\;\\:\\\\\\/]+$"); //NOI18N

  private static final Logger LOGGER = LoggerFactory.getLogger(NodeProjectGroup.class);

  public NodeProjectGroup(@Nonnull final Context context, @Nonnull final String name) throws IOException {
    super(null, true, ".", PrefUtils.isShowHiddenFilesAndFolders(), false); //NOI18N
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

  @UiThread
  public void removeProject(@Nonnull final NodeProject project) {
    int index = this.children.indexOf(project);
    if (index >= 0 && this.children.remove(project)) {
      final TreeModelEvent event = new TreeModelEvent(this, new Object[]{this}, new int[]{index}, new Object[]{project});
      this.listeners.forEach((l) -> {
        l.treeNodesRemoved(event);
      });
    }
    project.dispose();
  }

  @Nonnull
  @Override
  public Mono<NodeFileOrFolder> readSubtree(final boolean addHiddenFilesAndFolders) {
    this.children.forEach(proj -> {
      ((NodeProject)proj).initLoading(proj.readSubtree(addHiddenFilesAndFolders).subscribeOn(MainFrame.REACTOR_SCHEDULER).subscribe());
    });
    return Mono.just(this);
  }

  @Nonnull
  public NodeProject addProjectFolder(@Nonnull final File folder) throws IOException {
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

          if (!oldExtension.equals(newExtension)) {
            if (DialogProviderManager.getInstance().getDialogProvider().msgConfirmYesNo(Main.getApplicationFrame(), "Changed extension", String.format("You have changed extension! Restore old extension '%s'?", oldExtension))) {
              newFileName = FilenameUtils.getBaseName(newFileName) + (oldExtension.isEmpty() ? "" : '.' + oldExtension); //NOI18N
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

                final TreeModelEvent renamedEvent = new TreeModelEvent(this, editedNode.makeTreePath());
                for (final TreeModelListener l : listeners) {
                  l.treeNodesChanged(renamedEvent);
                }

                editedNode.fireNotifySubtreeChanged(this, listeners);

                this.context.notifyFileRenamed(affectedFiles, origFile, newFile);
              }
            } catch (IOException ex) {
              LOGGER.error("Can't rename file", ex); //NOI18N
              DialogProviderManager.getInstance().getDialogProvider().msgError(Main.getApplicationFrame(), "Can't rename file to '" + newValue + "\'");
            }
          }
        }
      }
    } else {
      DialogProviderManager.getInstance().getDialogProvider().msgError(Main.getApplicationFrame(), "Inapropriate file name '" + newFileName + "'!");
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

  void notifyProjectStateChanged(@Nonnull final NodeProject project) {
    Assertions.assertTrue("Must belong the group", project.getGroup() == this);

    final TreeModelEvent event = new TreeModelEvent(this, new TreePath(new Object[]{this, project}));

    Utils.safeSwingCall(new Runnable() {
      @Override
      public void run() {
        for (final TreeModelListener l : listeners) {
          l.treeStructureChanged(event);
        }
      }
    });
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

  public void startProjectFolderRefresh(@Nonnull final NodeProject nodeProject, @Nullable @MustNotContainNull final Runnable... invokeLater) {
    final int index = this.getIndex(nodeProject);
    if (index >= 0) {
      Main.getApplicationFrame().asyncReloadProject(nodeProject, ArrayUtils.joinArrays(invokeLater, new Runnable[]{new Runnable() {
        @Override
        public void run() {
          nodeProject.fireNotifySubtreeChanged(NodeProjectGroup.this, listeners);
        }
      }}));
    }
  }

  public void cancelLoading(){
      this.children.forEach((p) -> {
          ((NodeProject)p).cancelLoading();
      });
  }
  
  public boolean deleteNode(@Nonnull final NodeFileOrFolder node, final boolean notifyListeners) {
    final NodeFileOrFolder parentNode = node.getNodeParent();
    if (parentNode != null) {
      final TreeModelEvent event = new TreeModelEvent(this, parentNode.makeTreePath(), new int[]{node.getIndexAtParent()}, new Object[]{node});
      if (parentNode.deleteChild(node)) {
        if (notifyListeners) {
          for (final TreeModelListener l : this.listeners) {
            l.treeNodesRemoved(event);
          }
        }
        return true;
      }
    }
    return false;
  }

  @Nonnull
  public NodeFileOrFolder addChild(@Nonnull final NodeFileOrFolder folder, final boolean showHiddenFiles, @Nonnull final File childFile) throws IOException {
    final NodeFileOrFolder newNode = folder.addFile(childFile, showHiddenFiles);
    final TreeModelEvent event = new TreeModelEvent(this, folder.makeTreePath(), new int[]{newNode.getIndexAtParent()}, new Object[]{newNode});
    for (final TreeModelListener l : this.listeners) {
      l.treeNodesInserted(event);
    }
    return newNode;
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
