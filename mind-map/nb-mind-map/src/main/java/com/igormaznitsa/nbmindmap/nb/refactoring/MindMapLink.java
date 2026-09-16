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

package com.igormaznitsa.nbmindmap.nb.refactoring;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.nbmindmap.nb.editor.MMDDataObject;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import org.apache.commons.io.IOUtils;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

public class MindMapLink {

  private static final Logger LOGGER = LoggerFactory.getLogger(MindMapLink.class);

  private final DataObject dataObject;
  private volatile FileObject theFile;

  private volatile MindMap model;

  public MindMapLink(final FileObject file) {
    this.theFile = file;
    this.dataObject = MindMapLink.findDataObject(file);
  }

  private static DataObject findDataObject(final FileObject fileObj) {
    if (fileObj == null) {
      return null;
    }

    try {
      return DataObject.find(fileObj);
    } catch (final DataObjectNotFoundException ex) {
      LOGGER.warn("Can't find data object for file " + fileObj);
      return null;
    }
  }

  public FileObject getFile() {
    final DataObject doj = this.dataObject == null
        ? MindMapLink.findDataObject(this.theFile)
        : this.dataObject;
    return doj == null ? this.theFile : doj.getPrimaryFile();
  }

  public File asFile() {
    final FileObject fo = this.getFile();
    if (fo == null) {
      LOGGER.warn("Can't find file object [" + this.dataObject + "; " + this.theFile + ']');
      return null;
    }
    return FileUtil.toFile(fo);
  }

  public void writeUTF8Text(final String text) throws IOException {
    final FileObject foj = this.getFile();
    if (foj == null) {
      throw new IOException("Mind map file is missing");
    }

    final FileLock flock = FileObjectLocks.lock(foj);
    try (OutputStream out = foj.getOutputStream(flock)) {
      IOUtils.write(text, out, UTF_8);
    } finally {
      flock.releaseLock();
    }

    final DataObject doj = DataObject.find(foj);
    if (doj instanceof MMDDataObject) {
      LOGGER.info("Notify about change primary file");
      ((MMDDataObject) doj).firePrimaryFileChanged();
    }
  }

  public String readUTF8Text() throws IOException {
    final FileObject foj = this.getFile();
    if (foj == null) {
      throw new IOException("Mind map file is missing");
    }

    final FileLock flock = FileObjectLocks.lock(foj);
    try {
      return foj.asText(UTF_8.name());
    } finally {
      flock.releaseLock();
    }
  }

  public synchronized MindMap asMindMap() throws IOException {
    if (this.model == null) {
      this.model = new MindMap(new StringReader(this.readUTF8Text()));
    }
    return this.model;
  }

  public synchronized void writeMindMap() throws IOException {
    if (this.model != null) {
      this.writeUTF8Text(this.model.asString());
    }
  }

  public synchronized void discardModel() {
    this.model = null;
  }

}
