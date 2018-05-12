/*
 * Copyright 2015-2018 Igor Maznitsa.
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
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.nbmindmap.nb.refactoring.CannotUndoMindMapException;
import com.igormaznitsa.nbmindmap.nb.refactoring.MindMapLink;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ResourceBundle;
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

public abstract class AbstractElement extends SimpleRefactoringElementImplementation {
  protected static final ResourceBundle BUNDLE = ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18n/Bundle");
  protected static final Logger LOGGER = LoggerFactory.getLogger("MindMapRefactoringPlugin"); //NOI18N

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

  private static void delay(final long delay) throws IOException {
    try {
      Thread.sleep(delay);
    }
    catch (final InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new IOException("Interrupted", ex); //NOI18N
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
        IOUtils.write(map.packToString(), out, "UTF-8"); //NOI18N
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
      this.oldMindMapText = FileUtils.readFileToString(this.mindMapFile.asFile(), "UTF-8"); //NOI18N
    }
    catch (IOException ex) {
      LOGGER.error("Can't load mind map file", ex); //NOI18N
      ErrorManager.getDefault().log(ErrorManager.ERROR, "Can't load mind map file during refactoring"); //NOI18N
    }
  }

  @Override
  public void undoChange() {
    if (this.oldMindMapText != null) {
      try {
        FileUtils.writeStringToFile(this.mindMapFile.asFile(), this.oldMindMapText, "UTF-8"); //NOI18N
      }
      catch (IOException ex) {
        LOGGER.error("Can't undo old mind map text", ex); //NOI18N
        throw new CannotUndoMindMapException(this.mindMapFile.asFile());
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
    return this.mindMapFile.getFile();
  }

  @Override
  public PositionBounds getPosition() {
    return null;
  }

}
