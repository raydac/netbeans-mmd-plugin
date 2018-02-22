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

        mainLoop:
        for (int i = 0; i < buffer.position(); i++) {
          if (Thread.currentThread().isInterrupted()) {
            break;
          }
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
    return result;
  }
}
