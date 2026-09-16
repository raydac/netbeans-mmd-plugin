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

package com.igormaznitsa.nbmindmap.nb.refactoring.elements;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

import com.igormaznitsa.mindmap.model.MMapURI;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.nbmindmap.nb.refactoring.CannotUndoMindMapException;
import com.igormaznitsa.nbmindmap.nb.refactoring.FileObjectLocks;
import com.igormaznitsa.nbmindmap.nb.refactoring.MindMapLink;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ResourceBundle;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.text.PositionBounds;
import org.openide.util.Lookup;

public abstract class AbstractElement extends SimpleRefactoringElementImplementation {
  protected static final ResourceBundle BUNDLE =
      ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18n/Bundle");
  protected static final Logger LOGGER = LoggerFactory.getLogger("MindMapRefactoringPlugin");
  //NOI18N

  protected final File projectFolder;
  protected final MMapURI processedFile;
  protected final MindMapLink mindMapFile;

  protected volatile String oldMindMapText;

  public AbstractElement(final MindMapLink mindMap, final File projectFolder, final MMapURI file) {
    super();
    this.projectFolder = projectFolder;
    this.processedFile = file;
    this.mindMapFile = mindMap;
  }

  protected static void writeMindMap(final File file, final MindMap map) throws IOException {
    requireNonNull(file, "file");
    requireNonNull(map, "map");

    final FileObject fileObject = FileUtil.toFileObject(file);
    if (fileObject == null) {
      throw new IOException("Can't find file object for " + file);
    }

    final FileLock lock = FileObjectLocks.lock(fileObject);
    try (OutputStream out = fileObject.getOutputStream(lock)) {
      IOUtils.write(map.asString(), out, UTF_8);
    } finally {
      lock.releaseLock();
    }
  }

  protected final void rewriteLinks(final MindMapChange change) {
    try {
      change.apply();
    } catch (final RuntimeException ex) {
      this.mindMapFile.discardModel();
      throw ex;
    } catch (final Exception ex) {
      this.mindMapFile.discardModel();
      LOGGER.error("Error during mind map refactoring", ex); //NOI18N
      throw new IllegalStateException("Can't process mind map during refactoring", ex);
    }
  }

  @Override
  public void performChange() {
    final File snapshotFile = this.mindMapFile.asFile();
    if (snapshotFile == null) {
      throw new IllegalStateException("Mind map file is not a local file");
    }

    try {
      this.oldMindMapText = FileUtils.readFileToString(snapshotFile, UTF_8);
    } catch (final IOException ex) {
      LOGGER.error("Can't load mind map file", ex); //NOI18N
      throw new IllegalStateException("Can't load mind map file during refactoring", ex);
    }
  }

  @Override
  public void undoChange() {
    if (this.oldMindMapText == null) {
      return;
    }

    final File snapshotFile = this.mindMapFile.asFile();
    try {
      FileUtils.writeStringToFile(snapshotFile, this.oldMindMapText, UTF_8);
    } catch (final IOException ex) {
      LOGGER.error("Can't undo old mind map text", ex); //NOI18N
      throw new CannotUndoMindMapException(snapshotFile);
    }
  }

  @Override
  public String getDisplayText() {
    return this.getText();
  }

  @Override
  public Lookup getLookup() {
    return Lookup.EMPTY;
  }

  @Override
  public FileObject getParentFile() {
    return this.mindMapFile.getFile();
  }

  @Override
  public PositionBounds getPosition() {
    return null;
  }

  @FunctionalInterface
  protected interface MindMapChange {
    void apply() throws Exception;
  }

}
