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
package com.igormaznitsa.nbmindmap.nb.refactoring;

import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.nbmindmap.nb.MMDDataObject;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import org.apache.commons.io.IOUtils;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MindMapLink {

  private static final Logger logger = LoggerFactory.getLogger(MindMapLink.class);

  private final DataObject dataObject;
  private volatile FileObject theFile;

  private volatile MindMap model;
  
  public MindMapLink(final FileObject file) {
    this.theFile = file;
    this.dataObject = findDataObject(file);
  }

  private static DataObject findDataObject(final FileObject fileObj) {
    DataObject doj = null;
    if (fileObj != null) {
      try {
        doj = DataObject.find(fileObj);
      }
      catch (DataObjectNotFoundException ex) {
        logger.warn("Can't find data object for file " + fileObj);
      }
    }
    return doj;
  }

  public FileObject getFile() {
    DataObject doj = this.dataObject == null ? findDataObject(this.theFile) : this.dataObject;
    return doj == null ? this.theFile : doj.getPrimaryFile();
  }

  public File asFile(){
    File result = null;
    final FileObject fo = getFile();
    if (fo != null){
      result = FileUtil.toFile(fo);
    }else{
      logger.warn("Can't find file object ["+this.dataObject+"; "+this.theFile+']');
    }
    return result;
  }
  
  private void delay(final long time){
    try{
      Thread.sleep(time);
    }catch(InterruptedException ex){
      logger.warn("Delay has been interrupted");
    }
  }
  
  private FileLock lock(final FileObject fo) throws IOException {
    if (fo!=null){
      FileLock lock = null;
      while(!Thread.currentThread().isInterrupted()){
        try{
          lock = fo.lock();
          break;
        }catch(FileAlreadyLockedException ex){
          delay(500L);
        }
      }
      return lock;
    }else{
      return null;
    }
  }
  
  public void writeUTF8Text(final String text) throws IOException {
    final FileObject foj = getFile();
    final FileLock flock = lock(foj);
    try{
      final OutputStream out  = foj.getOutputStream(flock);
      try{
        IOUtils.write(text, out, "UTF-8");
      }finally{
        IOUtils.closeQuietly(out);
      }
    }finally{
      flock.releaseLock();
    }
    
    final DataObject doj = DataObject.find(foj);
    if (doj!=null && doj instanceof MMDDataObject){
      logger.info("Notify about change primary file");
      ((MMDDataObject)doj).firePrimaryFileChanged();
    }
  }
  
  public String readUTF8Text() throws IOException {
    final FileObject foj = getFile();
    final FileLock flock = lock(foj);
    try{
      return foj.asText("UTF-8");
    }finally{
      flock.releaseLock();
    }
  }
  
  public synchronized MindMap asMindMap() throws IOException {
    if (this.model == null){
      this.model = new MindMap(new StringReader(readUTF8Text()));
    }
    return this.model;
  }

  public synchronized void writeMindMap() throws IOException {
    if (this.model != null){
      writeUTF8Text(this.model.packToString());
    }
  }

}
