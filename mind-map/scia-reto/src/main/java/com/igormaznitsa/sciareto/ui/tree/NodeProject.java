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
import java.io.StringReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import com.igormaznitsa.meta.common.utils.Assertions;
import javax.annotation.Nullable;
import org.apache.commons.io.FileUtils;
import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.mindmap.model.MMapURI;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.sciareto.ui.MapUtils;
import com.igormaznitsa.sciareto.ui.SystemUtils;

public class NodeProject extends NodeFileOrFolder {

  private static final Logger LOGGER = LoggerFactory.getLogger(NodeProject.class);

  private volatile File folder = null;
  private volatile boolean knowledgeFolderPresented;
  
  public NodeProject(@Nonnull final NodeProjectGroup group, @Nonnull final File folder) {
    super(group, true, folder.getName(), !Files.isWritable(folder.toPath()));
    this.folder = folder;
    this.knowledgeFolderPresented = new File(folder,".projectKnowledge").isDirectory();
    reloadSubtree();
  }

  public boolean hasKnowledgeFolder(){
    return this.knowledgeFolderPresented;
  }
  
  @Override
  public void setName(@Nonnull final String name) {
    this.name = name;
    this.folder = new File(folder.getParentFile(), name);
    reloadSubtree();
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

  public void setFolder(@Nonnull final File folder) {
    Assertions.assertTrue("Must be directory", folder.isDirectory());
    this.folder = folder;
    reloadSubtree();
  }

  @Nonnull
  public NodeProjectGroup getGroup() {
    return (NodeProjectGroup) this.parent;
  }

  @Nonnull
  @MustNotContainNull
  public List<File> findAffectedFiles(@Nonnull final File changedFile) {
    final File baseFolder = makeFileForNode();
    final boolean folder = changedFile.isDirectory();
    
    final List<File> result = new ArrayList<>();
    for (final File mindMapFile : FileUtils.listFiles(baseFolder, new String[]{"mmd", "MMD"}, true)) {
      try {
        final MindMap map = new MindMap(null, new StringReader(FileUtils.readFileToString(mindMapFile, "UTF-8")));
        if (!MapUtils.findTopicsRelatedToFile(baseFolder, changedFile, map).isEmpty()) {
          result.add(mindMapFile);
        }
      } catch (IOException ex) {
        LOGGER.error("Can't process mind map file", ex);
      }
    }
    return result;
  }

  @Nonnull
  @MustNotContainNull
  public List<File> deleteAllLinksToFile(@Nonnull @MustNotContainNull final List<File> listOfFilesToProcess, @Nonnull final File fileToRemove) {
    final List<File> affectedFiles = new ArrayList<>();

    final File baseFolder = makeFileForNode();
    final MMapURI fileURI = new MMapURI(baseFolder, fileToRemove, null);

    for (final File file : listOfFilesToProcess) {
      if (file.isFile()) {
        try {
          final MindMap map = new MindMap(null, new StringReader(FileUtils.readFileToString(file, "UTF-8")));
          if (map.deleteAllLinksToFile(baseFolder, fileURI)) {
            SystemUtils.saveUTFText(file, map.packToString());
            affectedFiles.add(file);
          }
        } catch (IOException ex) {
          LOGGER.error("Can't process mind map file", ex);
        }
      }
    }

    return affectedFiles;
  }

  @Override
  public void reloadSubtree() {
    super.reloadSubtree();
    this.knowledgeFolderPresented = new File(this.folder,".projectKnowledge").isDirectory();
  }

  @Nonnull
  @MustNotContainNull
  public List<File> replaceAllLinksToFile(@Nonnull @MustNotContainNull final List<File> listOfFilesToProcess, @Nonnull final File oldFile, @Nonnull final File newFile) {
    final List<File> affectedFiles = new ArrayList<>();

    final File baseFolder = makeFileForNode();
    final MMapURI oldFileURI = new MMapURI(baseFolder, oldFile, null);
    final MMapURI newFileURI = new MMapURI(baseFolder, newFile, null);

    for (final File file : listOfFilesToProcess) {
      if (file.isFile()) {
        try {
          final MindMap map = new MindMap(null, new StringReader(FileUtils.readFileToString(file, "UTF-8")));
          if (map.replaceAllLinksToFile(baseFolder, oldFileURI, newFileURI)) {
            SystemUtils.saveUTFText(file, map.packToString());
            affectedFiles.add(file);
          }
        } catch (IOException ex) {
          LOGGER.error("Can't process mind map file", ex);
        }
      }
    }

    return affectedFiles;
  }
}
