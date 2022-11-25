/*
 * Copyright (C) 2015-2022 Igor A. Maznitsa
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.igormaznitsa.sciareto.ui.editors;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

public final class TextFile {

  private final long length;
  private final byte[] hash;
  private final File file;
  private final AtomicReference<byte[]> contentRef = new AtomicReference<>();

  public TextFile(@Nonnull final File file) throws IOException {
    this.file = file;
    final byte[] content = FileUtils.readFileToByteArray(file);
    this.hash = DigestUtils.digest(DigestUtils.getSha256Digest(), content);
    this.length = content.length;
    this.contentRef.set(content);
  }

  public TextFile(@Nonnull final File file, final boolean setContentIntoRef, @Nonnull final byte [] content) {
    this.file = file;
    this.length = content.length;
    this.hash = DigestUtils.digest(DigestUtils.getSha256Digest(), content);
    if (setContentIntoRef) {
      this.contentRef.set(content);
    }
  }
  
  @Nonnull
  public File getFile() {
    return this.file;
  }
  
  @Nullable
  public byte[] getContent() {
    return this.contentRef.get();
  }
  
  @Nonnull
  public String readContentAsUtf8() {
    return new String(this.readContent(), StandardCharsets.UTF_8);
  }
  
  @Nonnull
  public byte[] readContent() {
    final byte[] result = this.contentRef.getAndSet(null);
    if (result == null) {
      throw new IllegalStateException("Content already read");
    }
    return result;
  }

  @Nonnull
  public byte[] getHash() {
    return this.hash.clone();
  }

  public long getLength() {
    return this.length;
  }

  public boolean hasSameContent(@Nonnull final File file) throws IOException {
    boolean result = false;
    if (this.length == file.length()) {
      final byte[] fileHash = DigestUtils.digest(DigestUtils.getSha256Digest(), file);
      result = Arrays.equals(fileHash, this.hash);
    }
    return result;
  }

}
