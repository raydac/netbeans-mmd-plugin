/*
 * Copyright (C) 2015-2022 Igor A. Maznitsa
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

package com.igormaznitsa.nbmindmap.nb.lifecycle;

import static java.util.stream.Collectors.toList;

import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.nbmindmap.nb.editor.MMDDataObject;
import java.awt.EventQueue;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataLoaderPool;
import org.openide.loaders.DataObject;
import org.openide.modules.OnStart;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

@OnStart
public final class MmdFileTypeReclaimer implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(MmdFileTypeReclaimer.class);
  private static final Object LISTENER_LOCK = new Object();
  private static final RequestProcessor PROJECT_SCAN =
      new RequestProcessor(MmdFileTypeReclaimer.class);
  private static PropertyChangeListener editorCloseListener;

  static void unload() {
    MmdFileTypeReclaimer.detachEditorCloseListener();
    MmdFileTypeReclaimer.releaseUserMimeMapping();
  }

  private static void detachEditorCloseListener() {
    final Runnable detach = () -> {
      synchronized (LISTENER_LOCK) {
        if (editorCloseListener != null) {
          TopComponent.getRegistry().removePropertyChangeListener(editorCloseListener);
          editorCloseListener = null;
        }
      }
    };

    if (EventQueue.isDispatchThread()) {
      detach.run();
    } else {
      EventQueue.invokeLater(detach);
    }
  }

  private static void releaseUserMimeMapping() {
    if (MmdFileTypeRules.shouldClearUserMimeMapping(
        FileUtil.getMIMETypeExtensions(MMDDataObject.MIME))) {
      FileUtil.setMIMEType(MMDDataObject.MMD_EXT, null);
      LOGGER.info("Released user MIME mapping for ." + MMDDataObject.MMD_EXT);
    }
  }

  @Override
  public void run() {
    this.reclaimUserMimeMapping();
    WindowManager.getDefault().invokeWhenUIReady(this::reclaimStuckMindMapFiles);
  }

  private void reclaimUserMimeMapping() {
    FileUtil.setMIMEType(MMDDataObject.MMD_EXT, MMDDataObject.MIME);
    LOGGER.info("Reclaimed ." + MMDDataObject.MMD_EXT + " as " + MMDDataObject.MIME);
  }

  private void reclaimStuckMindMapFiles() {
    this.installEditorCloseListener();
    this.invalidateOpenEditorFiles();
    this.invalidateOpenProjectMapsInBackground();
  }

  private void installEditorCloseListener() {
    synchronized (LISTENER_LOCK) {
      if (editorCloseListener != null) {
        return;
      }

      editorCloseListener = evt -> {
        if (TopComponent.Registry.PROP_TC_CLOSED.equals(evt.getPropertyName())) {
          this.onEditorClosed(evt.getOldValue());
        }
      };
      TopComponent.getRegistry().addPropertyChangeListener(editorCloseListener);
    }
  }

  private void onEditorClosed(final Object closed) {
    if (!(closed instanceof TopComponent)) {
      return;
    }

    final DataObject dataObject = ((TopComponent) closed).getLookup().lookup(DataObject.class);
    if (dataObject == null) {
      return;
    }

    final FileObject file = dataObject.getPrimaryFile();
    if (this.isStuckMindMapFile(file)) {
      this.invalidateWrongDataObject(file);
    }
  }

  private void invalidateOpenEditorFiles() {
    this.collectOpenEditorFiles().stream()
        .filter(this::isStuckMindMapFile)
        .forEach(this::invalidateWrongDataObject);
  }

  private void invalidateOpenProjectMapsInBackground() {
    PROJECT_SCAN.post(() -> {
      final Collection<FileObject> maps = this.collectOpenProjectMaps();
      EventQueue.invokeLater(() -> maps.stream()
          .filter(this::isStuckMindMapFile)
          .forEach(this::invalidateWrongDataObject));
    });
  }

  private Collection<FileObject> collectOpenEditorFiles() {
    return TopComponent.getRegistry().getOpened().stream()
        .map(topComponent -> topComponent.getLookup().lookup(DataObject.class))
        .filter(Objects::nonNull)
        .map(DataObject::getPrimaryFile)
        .filter(Objects::nonNull)
        .collect(toList());
  }

  private Collection<FileObject> collectOpenProjectMaps() {
    return Arrays.stream(OpenProjects.getDefault().getOpenProjects())
        .map(Project::getProjectDirectory)
        .filter(Objects::nonNull)
        .flatMap(this::walkMindMapFiles)
        .collect(toList());
  }

  private Stream<FileObject> walkMindMapFiles(final FileObject folder) {
    if (folder == null || !folder.isFolder()
        || MmdFileTypeRules.isSkippedFolderName(folder.getName())) {
      return Stream.empty();
    }

    return Arrays.stream(folder.getChildren())
        .flatMap(child -> {
          if (child.isFolder()) {
            return this.walkMindMapFiles(child);
          }

          return MmdFileTypeRules.isMindMapExtension(child.getExt())
              ? Stream.of(child)
              : Stream.empty();
        });
  }

  private boolean isStuckMindMapFile(final FileObject file) {
    if (file == null || !file.isValid() || file.isFolder()) {
      return false;
    }

    try {
      return MmdFileTypeRules.shouldInvalidateWrongDataObject(
          file.getExt(),
          DataObject.find(file) instanceof MMDDataObject);
    } catch (final IOException ex) {
      LOGGER.error("Can't inspect mind map file " + file.getPath(), ex);
      return false;
    }
  }

  private void invalidateWrongDataObject(final FileObject file) {
    try {
      final DataObject dataObject = DataObject.find(file);
      if (dataObject instanceof MMDDataObject) {
        return;
      }

      this.clearPreferredLoader(file);
      dataObject.setValid(false);
      LOGGER.info("Invalidated text DataObject for " + file.getPath());
    } catch (final PropertyVetoException veto) {
      LOGGER.info("Mind map still open as text, will reclaim after close: " + file.getPath());
    } catch (final IOException ex) {
      LOGGER.error("Can't reclaim mind map file " + file.getPath(), ex);
    }
  }

  private void clearPreferredLoader(final FileObject file) {
    try {
      DataLoaderPool.setPreferredLoader(file, null);
    } catch (final IOException ex) {
      LOGGER.error("Can't clear preferred loader for " + file.getPath(), ex);
    }
  }
}
