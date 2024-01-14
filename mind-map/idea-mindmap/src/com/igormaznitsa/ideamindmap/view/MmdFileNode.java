/*
 * Copyright (C) 2015-2024 Igor A. Maznitsa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.igormaznitsa.ideamindmap.view;

import com.intellij.icons.AllIcons;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.roots.FileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.update.MergingUpdateQueue;
import com.intellij.util.ui.update.Update;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public final class MmdFileNode extends ProjectViewNode<VirtualFile> {

  private static final Key<Set<VirtualFile>> MMD_PROJECT_DIRS_KEY =
      Key.create("mmd.files.project.dirs");
  private static final String MMD_EXTENSION = "mmd";
  private final MergingUpdateQueue updateQueue;

  public MmdFileNode(@NotNull Project project,
                     @NotNull ViewSettings settings,
                     @NotNull VirtualFile rootDir,
                     @NotNull Disposable parentDisposable) {
    super(project, rootDir, settings);
    addAllMmds(project);
    setupMmdFilesRefresher(project, parentDisposable);
    updateQueue =
        new MergingUpdateQueue(MmdFileNode.class.getName(), 200, true, null, parentDisposable,
            null);
  }

  private MmdFileNode(@NotNull Project project,
                      @NotNull ViewSettings settings,
                      @NotNull VirtualFile file,
                      @NotNull MergingUpdateQueue updateQueue) {
    super(project, file, settings);
    this.updateQueue = updateQueue;
  }

  private void addAllMmds(@NotNull final Project project) {
    final Set<VirtualFile> mmdFiles = getMmdFiles(project);
    final VirtualFile projectDir = ProjectUtil.guessProjectDir(project);
    final Collection<VirtualFile> files =
        ReadAction.compute(() -> FilenameIndex.getAllFilesByExt(project, MMD_EXTENSION));

    for (VirtualFile file : files) {
      while (file != null && !file.equals(projectDir)) {
        mmdFiles.add(file);
        file = file.getParent();
      }
    }
  }

  @NotNull
  private Set<VirtualFile> getMmdFiles(@NotNull Project project) {
    Set<VirtualFile> files = project.getUserData(MMD_PROJECT_DIRS_KEY);
    if (files == null) {
      files = new HashSet<>();
      project.putUserData(MMD_PROJECT_DIRS_KEY, files);
    }
    return files;
  }

  private void setupMmdFilesRefresher(@NotNull Project project,
                                      @NotNull Disposable parentDisposable) {
    project.getMessageBus().connect(parentDisposable)
        .subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {
          @Override
          public void after(@NotNull List<? extends VFileEvent> events) {
            boolean hasAnyMmdUpdate = false;
            FileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();
            for (VFileEvent event : events) {
              VirtualFile file = event.getFile();
              if (file == null || !fileIndex.isInContent(file)) {
                continue;
              }
              String extension = file.getExtension();
              if (extension != null && extension.equalsIgnoreCase(MMD_EXTENSION)) {
                hasAnyMmdUpdate = true;
                break;
              }
            }
            if (hasAnyMmdUpdate) {
              updateQueue.queue(new Update("UpdateMMDs") {
                public void run() {
                  getMmdFiles(project).clear();
                  addAllMmds(project);
                  ApplicationManager.getApplication().invokeLater(() ->
                          ProjectView.getInstance(project)
                              .getProjectViewPaneById(MmdFilesViewPane.ID)
                              .updateFromRoot(true),
                      project.getDisposed()
                  );
                }
              });
            }
          }
        });
  }

  @Override
  public boolean contains(@NotNull VirtualFile file) {
    return file.equals(getVirtualFile());
  }

  @Override
  @NotNull
  public VirtualFile getVirtualFile() {
    return getValue();
  }

  @NotNull
  @Override
  public Collection<? extends AbstractTreeNode<?>> getChildren() {
    List<VirtualFile> files = new ArrayList<>();
    for (VirtualFile file : getValue().getChildren()) {
      if (getMmdFiles(myProject).contains(file)) {
        files.add(file);
      }
    }
    if (files.isEmpty()) {
      return Collections.emptyList();
    }
    ViewSettings settings = getSettings();
    return ContainerUtil.map(files,
        file -> new MmdFileNode(myProject, settings, file, updateQueue));
  }

  @Override
  protected void update(@NotNull PresentationData data) {
    data.setIcon(
        getValue().isDirectory() ? AllIcons.Nodes.Folder : getValue().getFileType().getIcon());
    data.setPresentableText(getValue().getName());
  }

  @Override
  public boolean canNavigate() {
    return !getValue().isDirectory();
  }

  @Override
  public boolean canNavigateToSource() {
    return canNavigate();
  }

  @Override
  public void navigate(boolean requestFocus) {
    FileEditorManager.getInstance(myProject).openFile(getValue(), false);
  }

  @Override
  public int getTypeSortWeight(boolean sortByType) {
    return getVirtualFile().isDirectory() ? 1 : 0;
  }

}