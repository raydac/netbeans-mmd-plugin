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

package com.igormaznitsa.nbmindmap.nb.refactoring;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;

public final class FileObjectLocks {

  private static final int MAX_ATTEMPTS = 20;
  private static final long RETRY_DELAY_MS = 500L;

  private FileObjectLocks() {
  }

  public static FileLock lock(final FileObject fileObject) throws IOException {
    requireNonNull(fileObject, "fileObject");

    FileAlreadyLockedException lastLockFailure = null;
    for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
      FileObjectLocks.requireNotInterrupted();
      try {
        return fileObject.lock();
      } catch (final FileAlreadyLockedException ex) {
        lastLockFailure = ex;
        FileObjectLocks.delay(RETRY_DELAY_MS);
      }
    }

    throw new IOException("Timed out waiting for file lock: " + fileObject.getPath(),
        lastLockFailure);
  }

  private static void requireNotInterrupted() throws IOException {
    if (Thread.currentThread().isInterrupted()) {
      throw new IOException("Interrupted while waiting for file lock");
    }
  }

  private static void delay(final long millis) throws IOException {
    try {
      Thread.sleep(millis);
    } catch (final InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new IOException("Interrupted while waiting for file lock", ex);
    }
  }
}
