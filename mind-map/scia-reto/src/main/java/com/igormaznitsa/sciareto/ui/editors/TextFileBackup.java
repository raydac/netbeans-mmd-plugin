/*
 * Copyright (C) 2020 Igor Maznitsa.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boon,
 * MA 02110-1301  USA
 */

package com.igormaznitsa.sciareto.ui.editors;

import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class TextFileBackup {

  private static final Logger LOGGER = LoggerFactory.getLogger(TextFileBackup.class);

  private static final BackupContent END_WORK =
      new BackupContent(new File("/some/non-existing/fake/file"), null);
  private static final AtomicReference<TextFileBackup> instance = new AtomicReference<>();
  private final BlockingQueue<BackupContent> contentQueue = new ArrayBlockingQueue<>(32);

  private TextFileBackup() {

  }

  private static void writeLong(final long value, @Nonnull final OutputStream out)
      throws IOException {
    final byte[] splitted = new byte[8];
    long acc = value;
    for (int i = 0; i < 8; i++) {
      splitted[i] = (byte) acc;
      acc >>>= 8;
    }
    IOUtils.write(splitted, out);
  }

  @Nullable
  public static File findBackupForFile(@Nonnull final File file) {
    final File root = file.getParentFile();
    final File backup0 = new File(root, makeBackupFileName(file, 0));
    final File backup1 = new File(root, makeBackupFileName(file, 1));

    if (backup0.isFile()) {
      return backup0;
    }
    if (backup1.isFile()) {
      return backup1;
    }
    return null;
  }

  @Nonnull
  public static String makeBackupFileName(@Nonnull final File orig, final int index) {
    final String name = orig.getName();
    return String.format(".%s.%d.abk", name, index);
  }

  @Nonnull
  public static TextFileBackup getInstance() {
    if (instance.get() == null) {
      final TextFileBackup newInstance = new TextFileBackup();
      if (instance.compareAndSet(null, newInstance)) {
        newInstance.start();
      }
    }
    return instance.get();
  }

  public void finish() {
    this.add(END_WORK);
  }

  @Nonnull
  private byte[] prepareContent(@Nonnull final String content) {
    final ByteArrayOutputStream bao = new ByteArrayOutputStream(content.length() << 1);
    final byte[] textAsBytes = content.getBytes(StandardCharsets.UTF_8);
    final CRC32 crc32 = new CRC32();
    crc32.update(textAsBytes);
    final long crc32value = crc32.getValue();
    final long timestamp = System.currentTimeMillis();

    try {
      writeLong(timestamp, bao);
      writeLong(crc32value, bao);
      final ByteArrayOutputStream packedDataBuffer = new ByteArrayOutputStream(content.length());
      final DeflaterOutputStream zos = new DeflaterOutputStream(packedDataBuffer, new Deflater(2));
      IOUtils.write(textAsBytes, zos);
      zos.flush();
      zos.finish();
      final byte[] packedContent = packedDataBuffer.toByteArray();
      writeLong(textAsBytes.length, bao);
      writeLong(packedContent.length, bao);
      IOUtils.write(packedContent, bao);
      bao.flush();
      return bao.toByteArray();
    } catch (IOException ex) {
      LOGGER.error("Unexpected situation, can't pack array");
      return new byte[0];
    }
  }

  private void start() {
    final Thread thread = new Thread(this::run, "edit-text-content-backuper");
    thread.setDaemon(false);
    thread.start();
  }

  private void removeBackup(@Nonnull final File file) {
    final File root = file.getParentFile();
    final File backup0 = new File(root, makeBackupFileName(file, 0));
    final File backup1 = new File(root, makeBackupFileName(file, 1));
    FileUtils.deleteQuietly(backup0);
    FileUtils.deleteQuietly(backup1);
  }

  private void backup(@Nonnull final File file, @Nonnull final byte[] data) {
    if (data.length > 0) {
      final File root = file.getParentFile();
      final File backup0 = new File(root, makeBackupFileName(file, 0));
      final File backup1 = new File(root, makeBackupFileName(file, 1));

      try {
        if (backup1.isFile()) {
          FileUtils.forceDelete(backup1);
        }
        FileUtils.writeByteArrayToFile(backup1, data, false);
        if (backup0.isFile()) {
          FileUtils.forceDelete(backup0);
        }
        if (!backup1.renameTo(backup0)) {
          LOGGER.error("Can't rename backup file " + backup1 + " to " + backup0);
        }
      } catch (IOException ex) {
        LOGGER.error("Can't save backup file: " + file, ex);
      }

    }
  }

  private void run() {
    while (!Thread.currentThread().isInterrupted()) {
      try {
        final BackupContent item = this.contentQueue.poll(5, TimeUnit.SECONDS);
        if (item != null) {
          if (item == END_WORK) {
            break;
          }
          if (item.content == null) {
            removeBackup(item.originalFile);
          } else {
            backup(item.originalFile, prepareContent(item.content));
          }
        }
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
      }
    }

  }

  public void add(@Nonnull final BackupContent content) {
    try {
      final boolean placed = this.contentQueue.offer(content, 1, TimeUnit.SECONDS);
      if (!placed) {
        LOGGER.error("Can't place content into queue for timeout, content is " + content);
      }
    } catch (InterruptedException ex) {
      LOGGER.error("Can't place content into queue for interruption, content is " + content);
      Thread.currentThread().interrupt();
    }
  }

  public static class Restored {
    private final long timestamp;
    private final long crc32;
    private final int packedSize;
    private final int unpackedSize;
    private final byte[] content;

    public Restored(@Nonnull final File file) throws IOException {
      try (final InputStream inStream = new BufferedInputStream(Files.newInputStream(file.toPath()))) {
        this.timestamp = readLong(inStream);
        this.crc32 = readLong(inStream);
        this.unpackedSize = (int) readLong(inStream);
        this.packedSize = (int) readLong(inStream);
        this.content = new byte[unpackedSize];
        try (final InflaterInputStream zipIn = new InflaterInputStream(inStream)) {
          IOUtils.readFully(zipIn, this.content);
        }
        final CRC32 calcCrc32 = new CRC32();
        calcCrc32.update(content);
        if (crc32 != calcCrc32.getValue()) {
          throw new IOException("CRC32 error");
        }
      }
    }

    private static long readLong(@Nonnull final InputStream in) throws IOException {
      final byte[] splitted = new byte[8];
      IOUtils.readFully(in, splitted);
      long acc = 0L;
      for (int i = 0; i < 8; i++) {
        acc >>>= 8;
        acc |= ((long) splitted[i]) << 56;
      }
      return acc;
    }

    public int getPackedSize() {
      return this.packedSize;
    }

    public int getUnpackedSize() {
      return this.unpackedSize;
    }

    public long getTimestamp() {
      return this.timestamp;
    }

    public long getCrc32() {
      return this.crc32;
    }

    public byte[] asByteArray() {
      return this.content;
    }

    @Nonnull
    public TextFile asTextFile() {
      return new TextFile(new File("backup"), true, this.content);
    }

    @Nonnull
    public String asText() {
      return new String(this.content, 0, this.content.length, StandardCharsets.UTF_8);
    }
  }

  public static class BackupContent {

    private final File originalFile;
    private final String content;

    public BackupContent(@Nonnull final File file, @Nullable final String content) {
      this.originalFile = file;
      this.content = content;
    }
  }
}
