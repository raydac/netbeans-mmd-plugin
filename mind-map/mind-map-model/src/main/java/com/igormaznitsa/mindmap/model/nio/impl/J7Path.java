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

import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.mindmap.model.nio.AbstractPath;
import com.igormaznitsa.mindmap.model.nio.Path;
import java.io.File;
import java.util.Iterator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class J7Path extends AbstractPath {

  private final java.nio.file.Path nioPath;

  private J7Path(@Nonnull final java.nio.file.Path path) {
    super();
    this.nioPath = path;
  }

  public J7Path(@Nonnull final File file) {
    super(file);
    this.nioPath = file.toPath();
  }

  public J7Path(@Nonnull final String first, @Nonnull @MustNotContainNull final String... items) {
    super(first, items);
    this.nioPath = java.nio.file.Paths.get(first, items);
  }

  @Nullable
  private static J7Path makePathIfNotNull(@Nullable final java.nio.file.Path basePath) {
    return basePath == null ? null : new J7Path(basePath);
  }

  @Override
  public boolean startsWith(@Nonnull final Path basePath) {
    final J7Path thatPath = (J7Path) basePath;
    return this.nioPath.startsWith(thatPath.nioPath);
  }

  @Override
  public boolean isAbsolute() {
    return this.nioPath.isAbsolute();
  }

  @Override
  @Nullable
  public Path relativize(@Nonnull final Path filePath) {
    final J7Path thatPath = (J7Path) filePath;
    return makePathIfNotNull(this.nioPath.relativize(thatPath.nioPath));
  }

  @Override
  @Nullable
  public Path getRoot() {
    return makePathIfNotNull(this.nioPath.getRoot());
  }

  @Override
  @Nonnull
  public File toFile() {
    return this.nioPath.toFile();
  }

  @Override
  @Nonnull
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
      @Nullable
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
  public boolean equals(@Nonnull final Object obj) {
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
  @Nonnull
  public String toString() {
    return this.nioPath.toString();
  }

}
