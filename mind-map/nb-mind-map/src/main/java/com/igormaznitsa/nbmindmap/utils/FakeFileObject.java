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
package com.igormaznitsa.nbmindmap.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import org.apache.commons.io.FilenameUtils;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.util.Lookup;

public final class FakeFileObject extends FileObject {

  private static final long serialVersionUID = 4522181722467407052L;

  private final File wrappedFile;
  private final boolean folder;
  private final Date lastModified = new Date();
  
  private final static class FakeFileLock extends FileLock {
    private boolean locked = true;
    
    public FakeFileLock () {
    }

    @Override
    public void finalize () {
      if (isValid()){
        releaseLock();
      }
    }

    @Override
    public boolean isValid () {
      return this.locked;
    }

    @Override
    public void releaseLock () {
      this.locked = false;
    }
  }
  
  private static final Enumeration<String> EMPTY_ENUMERATION = new Enumeration<String>() {
    @Override
    public boolean hasMoreElements () {
      return false;
    }

    @Override
    public String nextElement () {
      throw new NoSuchElementException();
    }
  };
  
  public FakeFileObject(final File file, final boolean folder){
    super();
    this.wrappedFile = file;
    this.folder = folder;
  }

  @Override
  public String getPath () {
    return this.wrappedFile.getAbsolutePath().replace('\\', '/');
  }

  @Override
  public String getName () {
    return FilenameUtils.getBaseName(this.wrappedFile.getName());
  }

  @Override
  public String getExt () {
    return FilenameUtils.getExtension(this.wrappedFile.getName());
  }

  @Override
  public void rename (FileLock lock, String name, String ext) throws IOException {
  }

  @Override
  public FileSystem getFileSystem () throws FileStateInvalidException {
    return Lookup.getDefault().lookup(FileSystem.class);
  }

  @Override
  public FileObject getParent () {
    final File parent = this.wrappedFile.getParentFile();
    return parent == null ? null : new FakeFileObject(parent,true);
  }

  @Override
  public boolean isFolder () {
    return this.folder;
  }

  @Override
  public Date lastModified () {
    return this.lastModified;
  }

  @Override
  public boolean isRoot () {
    return this.folder && getParent() == null;
  }

  @Override
  public boolean isData () {
    return !this.folder;
  }

  @Override
  public boolean isValid () {
    return false;
  }

  @Override
  public void delete (final FileLock lock) throws IOException {
  }

  @Override
  public Object getAttribute (String attrName) {
    return null;
  }

  @Override
  public void setAttribute (String attrName, Object value) throws IOException {
  }

  @Override
  public Enumeration<String> getAttributes () {
    return EMPTY_ENUMERATION;
  }

  @Override
  public void addFileChangeListener (FileChangeListener fcl) {
  }

  @Override
  public void removeFileChangeListener (FileChangeListener fcl) {
  }

  @Override
  public long getSize () {
    return 0;
  }

  @Override
  public InputStream getInputStream () throws FileNotFoundException {
    throw new FileNotFoundException("It's a fake file object");
  }

  @Override
  public OutputStream getOutputStream (FileLock lock) throws IOException {
    throw new IOException("It's a fake file object");
 }

  @Override
  public FileLock lock () throws IOException {
    return new FakeFileLock();
  }

  @Override
  public void setImportant (boolean b) {
  }

  @Override
  public FileObject[] getChildren () {
    return new FileObject[0];
  }

  @Override
  public FileObject getFileObject (String name, String ext) {
    return null;
  }

  @Override
  public FileObject createFolder (String name) throws IOException {
    throw new IOException("It's a fake file object");
  }

  @Override
  public FileObject createData (String name, String ext) throws IOException {
    throw new IOException("It's a fake file object");
  }

  @Override
  public boolean isReadOnly () {
    return true;
  }
  
}
