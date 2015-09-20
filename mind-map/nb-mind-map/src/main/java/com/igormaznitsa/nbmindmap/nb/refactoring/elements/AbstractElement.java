/*
 * Copyright 2015 Igor Maznitsa.
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
package com.igormaznitsa.nbmindmap.nb.refactoring.elements;

import com.igormaznitsa.mindmap.model.MMapURI;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.nbmindmap.nb.refactoring.CannotUndoMindMapException;
import com.igormaznitsa.nbmindmap.nb.refactoring.MutableFileLink;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.text.PositionBounds;
import org.openide.util.Lookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractElement extends SimpleRefactoringElementImplementation {

  protected static final Logger logger = LoggerFactory.getLogger("MindMapRefactoringPlugin");

  protected final File projectFolder;
  protected final MMapURI processedFile;
  protected final MutableFileLink mindMapFile;

  protected volatile String oldMindMapText;

  public AbstractElement(final MutableFileLink mindMap, final File projectFolder, final MMapURI file) {
    super();
    this.projectFolder = projectFolder;
    this.processedFile = file;
    this.mindMapFile = mindMap;
  }

  private static void delay(final long delay) throws IOException {
    try {
      Thread.sleep(delay);
    }
    catch (InterruptedException ex) {
      throw new IOException("Interrupted", ex);
    }
  }

  protected static MindMap readMindMap(final File file) throws IOException {
    final FileObject fileObject = FileUtil.toFileObject(file);
    FileLock lock = null;
    while (true) {
      try {
        lock = fileObject.lock();
        break;
      }
      catch (FileAlreadyLockedException ex) {
        delay(500L);
      }
    }
    try {
      return new MindMap(new StringReader(fileObject.asText("UTF-8")));
    }
    finally {
      if (lock != null) {
        lock.releaseLock();
      }
    }
  }

  protected static void writeMindMap(final File file, final MindMap map) throws IOException {
    final FileObject fileObject = FileUtil.toFileObject(file);
    FileLock lock = null;
    while (true) {
      try {
        lock = fileObject.lock();
        break;
      }
      catch (FileAlreadyLockedException ex) {
        delay(500L);
      }
    }
    try {
      final OutputStream out = fileObject.getOutputStream(lock);
      try {
        IOUtils.write(map.packToString(), out, "UTF-8");
      }
      finally {
        IOUtils.closeQuietly(out);
      }
    }
    finally {
      if (lock != null) {
        lock.releaseLock();
      }
    }
  }

  @Override
  public void performChange() {
    try {
      this.oldMindMapText = FileUtils.readFileToString(this.mindMapFile.getFile(), "UTF-8");
    }
    catch (IOException ex) {
      logger.error("Can't load mind map file", ex);
      ErrorManager.getDefault().log(ErrorManager.ERROR, "Can't load mind map file during refactoring");
    }
  }

  @Override
  public void undoChange() {
    if (this.oldMindMapText != null) {
      try {
        FileUtils.writeStringToFile(this.mindMapFile.getFile(), this.oldMindMapText, "UTF-8");
      }
      catch (IOException ex) {
        logger.error("Can't undo old mind map text", ex);
        throw new CannotUndoMindMapException(this.mindMapFile.getFile());
      }
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
    return FileUtil.toFileObject(this.mindMapFile.getFile());
  }

  @Override
  public PositionBounds getPosition() {
    return null;
  }

}
