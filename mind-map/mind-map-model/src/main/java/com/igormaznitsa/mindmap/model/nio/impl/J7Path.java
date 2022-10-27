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

package com.igormaznitsa.mindmap.model.nio.impl;

import com.igormaznitsa.mindmap.model.nio.AbstractPath;
import com.igormaznitsa.mindmap.model.nio.Path;
import java.io.File;
import java.util.Iterator;

public class J7Path extends AbstractPath {

  private final java.nio.file.Path nioPath;

  private J7Path(final java.nio.file.Path path) {
    super();
    this.nioPath = path;
  }

  public J7Path(final File file) {
    super(file);
    this.nioPath = file.toPath();
  }

  public J7Path(final String first, final String... items) {
    super(first, items);
    this.nioPath = java.nio.file.Paths.get(first, items);
  }

  private static J7Path makePathIfNotNull(final java.nio.file.Path basePath) {
    return basePath == null ? null : new J7Path(basePath);
  }

  @Override
  public boolean startsWith(final Path basePath) {
    final J7Path thatPath = (J7Path) basePath;
    return this.nioPath.startsWith(thatPath.nioPath);
  }

  @Override
  public boolean isAbsolute() {
    return this.nioPath.isAbsolute();
  }

  @Override
  public Path relativize(final Path filePath) {
    final J7Path thatPath = (J7Path) filePath;
    return makePathIfNotNull(this.nioPath.relativize(thatPath.nioPath));
  }

  @Override
  public Path getRoot() {
    return makePathIfNotNull(this.nioPath.getRoot());
  }

  @Override
  public File toFile() {
    return this.nioPath.toFile();
  }

  @Override
  public Iterator<Path> iterator() {
    return new Iterator<Path>() {
      private final Iterator<java.nio.file.Path> wrappedIterator = nioPath.iterator();

      @Override
      public void remove() {
        this.wrappedIterator.remove();
      }

      @Override
      public boolean hasNext() {
        return this.wrappedIterator.hasNext();
      }

      @Override
      public Path next() {
        return makePathIfNotNull(this.wrappedIterator.next());
      }
    };
  }

  @Override
  public int hashCode() {
    return this.nioPath.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof J7Path) {
      final J7Path that = (J7Path) obj;
      return this.nioPath.equals(that.nioPath);
    }
    return false;
  }

  @Override
  public String toString() {
    return this.nioPath.toString();
  }

}
