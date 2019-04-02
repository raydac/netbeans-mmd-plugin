/* 
 * Copyright (C) 2018 Igor Maznitsa.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package com.igormaznitsa.sciareto.ui.misc;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import javax.annotation.Nonnull;
import com.igormaznitsa.meta.common.utils.Assertions;

public class FileExaminator {

  private final File file;

  public FileExaminator(@Nonnull final File file) {
    this.file = file;
  }

  public boolean doesContainData(@Nonnull final byte[] dataBuffer, @Nonnull final byte[] dataArr1, @Nonnull final byte[] dataArr2) throws IOException {
    Assertions.assertTrue("Length must be the same", dataArr1.length == dataArr2.length);

    boolean result = false;

    final int len = dataArr1.length;

    if (len != 0 && this.file.length() >= len) {
      final ByteBuffer buffer = ByteBuffer.wrap(dataBuffer);
      final int[] counters = new int[len];

      try (final FileChannel channel = FileChannel.open(this.file.toPath(), StandardOpenOption.READ)) {
        buffer.clear();

        while (buffer.hasRemaining() && !Thread.currentThread().isInterrupted()) {
          int read = channel.read(buffer);
          if (read < 0) {
            break;
          }
        }

        if (dataArr1.length == 1) {
          for (int i = 0; i < buffer.position() && !Thread.currentThread().isInterrupted(); i++) {
            final byte b = buffer.get(i);
            if (b == dataArr1[0] || b == dataArr2[0]) {
              result = true;
              break;
            }
          }
        } else {
          mainLoop:
          for (int i = 0; i < buffer.position() && !Thread.currentThread().isInterrupted(); i++) {
            final byte b = buffer.get(i);
            for (int j = 0; j < len; j++) {
              int c = counters[j];
              if (c == 0) {
                continue;
              }
              if (b == dataArr1[c] || b == dataArr2[c]) {
                c++;
                if (c == len) {
                  result = true;
                  break mainLoop;
                }
                counters[j] = c;
              } else {
                counters[j] = 0;
              }
            }
            if (b == dataArr1[0] || b == dataArr2[0]) {
              for (int j = 0; j < len; j++) {
                if (counters[j] == 0) {
                  counters[j] = 1;
                  break;
                }
              }
            }
          }
        }
      }
    }
    return result;
  }
}
