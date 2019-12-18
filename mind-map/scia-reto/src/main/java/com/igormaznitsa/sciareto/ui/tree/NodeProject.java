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
import com.igormaznitsa.meta.common.utils.Assertions;
import com.igormaznitsa.mindmap.model.MMapURI;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.sciareto.Context;
import com.igormaznitsa.sciareto.preferences.PrefUtils;
import com.igormaznitsa.sciareto.ui.MainFrame;
import com.igormaznitsa.sciareto.ui.MapUtils;
import com.igormaznitsa.sciareto.ui.SystemUtils;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.io.FileUtils;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

public class NodeProject extends NodeFileOrFolder {

  private static final Logger LOGGER = LoggerFactory.getLogger(NodeProject.class);

  private volatile File folder = null;
  private volatile boolean knowledgeFolderPresented;
  private final AtomicBoolean loading = new AtomicBoolean(true);
  private final AtomicReference<Disposable> loadDispose = new AtomicReference<>();
  
  public NodeProject(@Nonnull final NodeProjectGroup group, @Nonnull final File folder) throws IOException {
    super(group, true, folder.getName(), PrefUtils.isShowHiddenFilesAndFolders(), !Files.isWritable(folder.toPath()));
    this.folder = folder;
    this.knowledgeFolderPresented = new File(folder, Context.KNOWLEDGE_FOLDER).isDirectory();
  }

  @Override
  public boolean isLoading() {
    return this.loading.get();
  }

  public boolean hasKnowledgeFolder() {
    return this.knowledgeFolderPresented;
  }

  @Override
  public void setName(@Nonnull final String name) throws IOException {
    this.name = name;
    this.folder = new File(folder.getParentFile(), name);
    readSubtree(PrefUtils.isShowHiddenFilesAndFolders()).subscribeOn(MainFrame.REACTOR_SCHEDULER).subscribe();
  }

  @Override
  @Nullable
  public File makeFileForNode() {
    return this.folder;
  }

  @Nonnull
  public File getFolder() {
    return this.folder;
  }

  public void setFolder(@Nonnull final File folder) throws IOException {
    Assertions.assertTrue("Must be directory", folder.isDirectory()); //NOI18N
    this.folder = folder;
    readSubtree(PrefUtils.isShowHiddenFilesAndFolders()).subscribeOn(MainFrame.REACTOR_SCHEDULER).subscribe();
  }

  @Nonnull
  public NodeProjectGroup getGroup() {
    return (NodeProjectGroup) this.parent;
  }

  @Nonnull
  @MustNotContainNull
  public List<File> findAffectedFiles(@Nonnull final File changedFile) {
    final File baseFolder = makeFileForNode();
    final boolean isfolder = changedFile.isDirectory();

    final List<File> result = new ArrayList<>();
    FileUtils.listFiles(baseFolder, new String[]{"mmd", "MMD"}, true).forEach((mindMapFile) -> {
      try {
        final MindMap map = new MindMap(new StringReader(FileUtils.readFileToString(mindMapFile, StandardCharsets.UTF_8))); //NOI18N
        if (!MapUtils.findTopicsRelatedToFile(baseFolder, changedFile, map).isEmpty()) {
          result.add(mindMapFile);
        }
      } catch (IOException ex) {
        LOGGER.error("Can't process mind map file", ex); //NOI18N
      }
    });
    return result;
  }

  @Nonnull
  @MustNotContainNull
  public List<File> deleteAllLinksToFile(@Nonnull @MustNotContainNull final List<File> listOfFilesToProcess, @Nonnull final File fileToRemove) {
    final List<File> affectedFiles = new ArrayList<>();

    final File baseFolder = makeFileForNode();
    final MMapURI fileURI = new MMapURI(baseFolder, fileToRemove, null);

    listOfFilesToProcess.stream().filter((file) -> (file.isFile())).forEachOrdered((file) -> {
      try {
        final MindMap map = new MindMap(new StringReader(FileUtils.readFileToString(file, "UTF-8"))); //NOI18N
        if (map.deleteAllLinksToFile(baseFolder, fileURI)) {
          SystemUtils.saveUTFText(file, map.packToString());
          affectedFiles.add(file);
        }
      } catch (IOException ex) {
        LOGGER.error("Can't process mind map file", ex); //NOI18N
      }
    });

    return affectedFiles;
  }

  public void initLoading(@Nonnull final Disposable disposable) {
    this.loading.set(true);
    this.loadDispose.set(disposable);
    this.getGroup().notifyProjectStateChanged(this);
  }
  
  @Nonnull
  @Override
  public Mono<NodeFileOrFolder> readSubtree(final boolean addHiddenFilesAndFolders) {
    final AtomicLong time = new AtomicLong();
    final AtomicReference<NodeFileOrFolder> knowledgeFolder = new AtomicReference<>();
    
    return Mono.just(this)
            .doOnSubscribe(s -> {
              time.set(System.currentTimeMillis());
            })
            .flatMap(p -> super.readSubtree(addHiddenFilesAndFolders))
            .doFinally(signalType -> {
              Collections.sort(this.children, this);
              LOGGER.info(String.format("Project %s reloaded, spent %d ms", this.toString(), System.currentTimeMillis() - time.get()));
            })
            .doOnTerminate(() -> {
              this.loading.set(false);
              this.loadDispose.set(null);
              this.getGroup().notifyProjectStateChanged(this);
            });
  }

  @Nonnull
  @MustNotContainNull
  public List<File> replaceAllLinksToFile(@Nonnull @MustNotContainNull final List<File> listOfFilesToProcess, @Nonnull final File oldFile, @Nonnull final File newFile) {
    final List<File> affectedFiles = new ArrayList<>();

    final File baseFolder = makeFileForNode();
    final MMapURI oldFileURI = new MMapURI(baseFolder, oldFile, null);
    final MMapURI newFileURI = new MMapURI(baseFolder, newFile, null);

    listOfFilesToProcess.stream().filter((file) -> (file.isFile())).forEachOrdered((file) -> {
      try {
        final MindMap map = new MindMap(new StringReader(FileUtils.readFileToString(file, StandardCharsets.UTF_8)));
        if (map.replaceAllLinksToFile(baseFolder, oldFileURI, newFileURI)) {
          SystemUtils.saveUTFText(file, map.packToString());
          affectedFiles.add(file);
        }
      } catch (IOException ex) {
        LOGGER.error("Can't process mind map file", ex); //NOI18N
      }
    });

    return affectedFiles;
  }

  public void cancelLoading() {
    final Disposable disposable = this.loadDispose.getAndSet(null);
    if (disposable != null) {
      disposable.dispose();
      this.getGroup().notifyProjectStateChanged(this);
    }
  }
}
