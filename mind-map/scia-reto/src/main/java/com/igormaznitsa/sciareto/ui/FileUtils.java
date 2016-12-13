/*
 * Copyright 2016 Igor Maznitsa.
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
package com.igormaznitsa.sciareto.ui;

import java.io.File;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import org.apache.commons.io.FilenameUtils;

public final class FileUtils {

  private FileUtils() {

  }

  public static boolean isRootFile(@Nonnull final File file) {
    boolean result = false;

    for (final File f : File.listRoots()) {
      if (f.equals(file)) {
        result = true;
        break;
      }
    }

    if (!result) {
      final String path = FilenameUtils.normalizeNoEndSeparator(file.getAbsolutePath());
      if (org.apache.commons.lang.SystemUtils.IS_OS_WINDOWS) {
        result = path.length() == 3 && path.endsWith(":\\"); //NOI18N
      } else {
        result = path.equals("/"); //NOI18N
      }
    }

    return result;
  }

  @Nonnull
  public static File removeLastElementInPath(@Nonnull final File file) {
    final String path = FilenameUtils.normalizeNoEndSeparator(file.getAbsolutePath());

    final int indexUnix = path.lastIndexOf('/');
    final int indexWindows = path.lastIndexOf('\\');
    final int index = Math.max(indexUnix, indexWindows);

    final File result;
    if (index > 0) {
      result = new File(path.substring(0, index));
    } else {
      result = file;
    }
    return result;
  }

  @Nonnull
  public static File replaceParentInPath(@Nonnull final File oldParent, @Nonnull final File newParent, @Nonnull final File file) {
    final Path filePath = file.toPath();
    final Path oldParentPath = oldParent.toPath();
    final Path newParentPath = newParent.toPath();

    final Path relativePathInOld = oldParentPath.relativize(filePath);
    final Path newPath = newParentPath.resolve(relativePathInOld);

    return newPath.toFile();
  }
}
