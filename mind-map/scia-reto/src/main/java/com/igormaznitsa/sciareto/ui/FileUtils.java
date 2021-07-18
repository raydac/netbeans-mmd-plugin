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
      if (path.isEmpty()) {
        result = true;
      } else if (org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS) {
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

    final int lastIndexUnix = path.lastIndexOf('/');
    final int lastIndexWindows = path.lastIndexOf('\\');
    final int lastIndex = Math.max(lastIndexUnix, lastIndexWindows);

    final int firstIndexUnix = path.indexOf('/');
    final int firstIndexWindows = path.indexOf('\\');
    final int firstIndex = Math.max(firstIndexUnix, firstIndexWindows);

    final File result;
    if (lastIndex == firstIndex) {
      result = new File(path.substring(0, lastIndex + 1));
    } else {
      if (lastIndex > firstIndex) {
        result = new File(path.substring(0, lastIndex));
      } else {
        result = file;
      }
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
