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
package com.igormaznitsa.nbmindmap.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.io.FilenameUtils;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.util.Lookup;
import com.igormaznitsa.meta.annotation.MustNotContainNull;

public final class FakeFileObject extends FileObject {

  private static final long serialVersionUID = 4522181722467407052L;

  private final File wrappedFile;
  private final boolean folder;
  private final Date lastModified = new Date();

  private final static class FakeFileLock extends FileLock {

    private boolean locked = true;

    public FakeFileLock() {
    }

    @Override
    public void finalize() {
      try {
        if (isValid()) {
          releaseLock();
        }
      } finally {
        super.finalize();
      }
    }

    @Override
    public boolean isValid() {
      return this.locked;
    }

    @Override
    public void releaseLock() {
      this.locked = false;
    }
  }

  private static final Enumeration<String> EMPTY_ENUMERATION = new Enumeration<String>() {
    @Override
    public boolean hasMoreElements() {
      return false;
    }

    @Override
    public String nextElement() {
      throw new NoSuchElementException();
    }
  };

  public FakeFileObject(final File file, final boolean folder) {
    super();
    this.wrappedFile = file;
    this.folder = folder;
  }

  @Override
  @Nonnull
  public String getPath() {
    return this.wrappedFile.getAbsolutePath().replace('\\', '/');
  }

  @Override
  @Nonnull
  public String getName() {
    return FilenameUtils.getBaseName(this.wrappedFile.getName());
  }

  @Override
  @Nonnull
  public String getExt() {
    return FilenameUtils.getExtension(this.wrappedFile.getName());
  }

  @Override
  public void rename(@Nonnull FileLock lock, @Nonnull String name, @Nonnull String ext) throws IOException {
  }

  @Override
  public FileSystem getFileSystem() throws FileStateInvalidException {
    return Lookup.getDefault().lookup(FileSystem.class);
  }

  @Override
  @Nullable
  public FileObject getParent() {
    final File parent = this.wrappedFile.getParentFile();
    return parent == null ? null : new FakeFileObject(parent, true);
  }

  @Override
  public boolean isFolder() {
    return this.folder;
  }

  @Override
  @Nonnull
  public Date lastModified () {
    return this.lastModified;
  }

  @Override
  public boolean isRoot() {
    return this.folder && getParent() == null;
  }

  @Override
  public boolean isData() {
    return !this.folder;
  }

  @Override
  public boolean isValid() {
    return false;
  }

  @Override
  public void delete(@Nonnull final FileLock lock) throws IOException {
  }

  @Override
  @Nullable
  public Object getAttribute(@Nonnull String attrName) {
    return null;
  }

  @Override
  public void setAttribute(@Nonnull String attrName, @Nonnull Object value) throws IOException {
  }

  @Override
  @Nonnull
  public Enumeration<String> getAttributes() {
    return EMPTY_ENUMERATION;
  }

  @Override
  public void addFileChangeListener(@Nonnull final FileChangeListener fcl) {
  }

  @Override
  public void removeFileChangeListener(@Nonnull final FileChangeListener fcl) {
  }

  @Override
  public long getSize() {
    return 0;
  }

  @Override
  @Nonnull
  public InputStream getInputStream() throws FileNotFoundException {
    throw new FileNotFoundException("It's a fake file object");
  }

  @Override
  @Nonnull
  public OutputStream getOutputStream(@Nonnull final FileLock lock) throws IOException {
    throw new IOException("It's a fake file object");
  }

  @Override
  @Nonnull
  public FileLock lock() throws IOException {
    return new FakeFileLock();
  }

  @Override
  public void setImportant(boolean b) {
  }

  @Override
  @Nonnull
  @MustNotContainNull
  public FileObject[] getChildren() {
    return new FileObject[0];
  }

  @Override
  @Nullable
  public FileObject getFileObject(@Nonnull final String name, @Nonnull final String ext) {
    return null;
  }

  @Override
  @Nonnull
  public FileObject createFolder(@Nonnull final String name) throws IOException {
    throw new IOException("It's a fake file object");
  }

  @Override
  @Nonnull
  public FileObject createData(@Nonnull final String name, @Nonnull final String ext) throws IOException {
    throw new IOException("It's a fake file object");
  }

  @Override
  public boolean isReadOnly() {
    return true;
  }

}
