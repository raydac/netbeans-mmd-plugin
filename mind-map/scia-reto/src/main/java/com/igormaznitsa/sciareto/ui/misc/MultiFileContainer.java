/*
 * Copyright (C) 2015-2024 Igor A. Maznitsa
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

package com.igormaznitsa.sciareto.ui.misc;

import com.igormaznitsa.meta.annotation.MustNotContainNull;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.compress.compressors.deflate.DeflateCompressorInputStream;
import org.apache.commons.compress.compressors.deflate.DeflateCompressorOutputStream;
import org.apache.commons.compress.compressors.deflate.DeflateParameters;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public final class MultiFileContainer {

  private static final MultiFileContainer INSTANCE = new MultiFileContainer();
  private static final int MAGIC = 75;

  private MultiFileContainer() {

  }

  @Nonnull
  public static MultiFileContainer getInstance() {
    return INSTANCE;
  }

  @Nonnull
  public static File findFolder() throws IOException {
    final CodeSource codeSource = MultiFileContainer.class.getProtectionDomain().getCodeSource();
    if (codeSource == null) {
      String folder = System.getProperty("user.dir");
      if (folder == null) {
        folder = System.getProperty("user.home");
      }
      if (folder == null) {
        folder = ".";
      }
      return new File(folder);
    } else {
      final URL url = codeSource.getLocation();
      try {
        final File urlFile = new File(url.toURI());
        return urlFile.isFile() ? urlFile.getParentFile() : urlFile;
      } catch (URISyntaxException ex) {
        throw new IOException(ex);
      }
    }
  }

  @Nonnull
  public Optional<File> write(@Nonnull final String fileName,
                              @Nonnull @MustNotContainNull final List<FileItem> fileItemList)
      throws IOException {
    if (fileItemList.size() > 0xFFFF) {
      throw new IllegalArgumentException("Too many file items, allowed only 0..65535");
    }
    final File folder = findFolder();
    if (folder.isDirectory()) {
      final File target = new File(folder, fileName);
      final DeflateParameters deflateParameters = new DeflateParameters();
      deflateParameters.setCompressionLevel(9);
      deflateParameters.setWithZlibHeader(false);

      try (final DeflateCompressorOutputStream outputStream = new DeflateCompressorOutputStream(new BufferedOutputStream(Files.newOutputStream(target.toPath())), deflateParameters)){
        outputStream.write(MAGIC);
        outputStream.write(fileItemList.size());
        outputStream.write(fileItemList.size() >> 8);
        for (final FileItem item : fileItemList) {
          item.write(outputStream);
        }
        outputStream.finish();
      }
      return Optional.of(target);
    } else {
      return Optional.empty();
    }
  }

  public void deleteFile(@Nonnull final String fileName) throws IOException {
    FileUtils.delete(new File(findFolder(), fileName));
  }

  public boolean isFileExists(@Nonnull final String fileName) throws IOException {
    return new File(findFolder(), fileName).isFile();
  }

  @Nonnull
  @MustNotContainNull
  public List<FileItem> read(@Nonnull final String fileName) throws IOException {
    final File file = new File(findFolder(), fileName);
    if (file.isFile()) {
      final DeflateParameters deflateParameters = new DeflateParameters();
      deflateParameters.setCompressionLevel(9);
      deflateParameters.setWithZlibHeader(false);

      try (final DeflateCompressorInputStream inputStream = new DeflateCompressorInputStream(new BufferedInputStream(Files.newInputStream(file.toPath())), deflateParameters)) {
        final int magic = inputStream.read();
        if (magic != MAGIC) {
          throw new IOException("Illegal file format error");
        }
        final int sizeLow = inputStream.read();
        final int sizeHigh = inputStream.read();
        if (sizeLow < 0 || sizeHigh < 0) {
          throw new IOException("Format error");
        }
        final int size = sizeHigh << 8 | sizeLow;
        final List<FileItem> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
          result.add(FileItem.read(inputStream));
        }
        return result;
      }
    } else {
      return List.of();
    }
  }

  public static final class FileItem {

    private final boolean changed;
    private final String position;
    @Nullable
    private final File file;
    @Nullable
    private final byte[] original;
    @Nullable
    private final byte[] current;

    private final List<byte[]> history;

    public FileItem(final boolean changed, @Nonnull final String position,
                    @Nullable final File file,
                    @Nullable final byte[] original,
                    @Nullable final byte[] current,
                    @Nonnull @MustNotContainNull List<byte[]> history) {
      this.changed = changed;
      this.position = Objects.requireNonNull(position);
      this.file = file;
      this.original = original;
      this.current = current;
      this.history = new ArrayList<>(history);
    }

    public boolean isChanged() {
      return this.changed;
    }

    @Nonnull
    @MustNotContainNull
    public List<byte[]> getHistory() {
      return this.history;
    }

    @Nonnull
    public static FileItem read(@Nonnull final InputStream inputStream) throws IOException {
      final DataInputStream dataInputStream = new DataInputStream(inputStream);
      boolean changed = dataInputStream.readBoolean();
      String position = dataInputStream.readUTF();
      String fileName = dataInputStream.readUTF();
      int lengthOriginal = dataInputStream.readInt();
      byte[] original = IOUtils.readFully(dataInputStream, lengthOriginal);
      int lengthCurrent = dataInputStream.readInt();
      byte[] current = IOUtils.readFully(dataInputStream, lengthCurrent);

      int historySize = dataInputStream.readInt();
      final List<byte[]> history = new ArrayList<>(historySize);
      for (int i = 0; i < historySize; i++) {
        final int length = dataInputStream.readInt();
        history.add(IOUtils.readFully(dataInputStream, length));
      }

      return new FileItem(changed, position, fileName.isEmpty() ? null : new File(fileName),
          original,
          current, history);
    }

    public void write(@Nonnull final OutputStream outputStream) throws IOException {
      final DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
      dataOutputStream.writeBoolean(this.changed);
      dataOutputStream.writeUTF(this.position);
      dataOutputStream.writeUTF(this.file == null ? "" : this.file.getAbsolutePath());
      dataOutputStream.writeInt(this.original == null ? 0 : this.original.length);
      dataOutputStream.write(this.original == null ? new byte[0] : this.original);
      dataOutputStream.writeInt(this.current == null ? 0 : this.current.length);
      dataOutputStream.write(this.current == null ? new byte[0] : this.current);

      dataOutputStream.writeInt(this.history.size());
      for (final byte[] bytes : this.history) {
        dataOutputStream.writeInt(bytes.length);
        dataOutputStream.write(bytes);
      }
    }

    @Nullable
    public byte[] getCurrent() {
      return this.current;
    }

    @Nullable
    public byte[] getOriginal() {
      return this.original;
    }

    @Nullable
    public File getFile() {
      return this.file;
    }

    @Nonnull
    public String getPosition() {
      return this.position;
    }
  }
}


