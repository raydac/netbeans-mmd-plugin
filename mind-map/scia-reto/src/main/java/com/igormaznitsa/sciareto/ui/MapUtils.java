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
package com.igormaznitsa.sciareto.ui;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.ExtraFile;
import com.igormaznitsa.mindmap.model.MMapURI;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.Topic;

public final class MapUtils {

  private MapUtils() {

  }

  @Nonnull
  @MustNotContainNull
  public static List<MMapURI> extractAllFileLinks(@Nonnull final MindMap map) {
    final List<MMapURI> result = new ArrayList<>();
    for(final Topic t : map){
      final ExtraFile file = (ExtraFile) t.getExtras().get(Extra.ExtraType.FILE);
      if (file != null){
        result.add(file.getAsURI());
      }
    }
    return result;
  }
  
  @Nonnull
  @MustNotContainNull
  public static List<Topic> findTopicsRelatedToFile(@Nullable final File baseFolder, @Nonnull final File file, @Nonnull final MindMap map) {

    final List<Topic> result = new ArrayList<>();

    final Path theFile = file.isAbsolute() ? file.toPath() : new File(baseFolder, file.getAbsolutePath()).toPath();

    final boolean folder = file.isDirectory();

    for (final Topic t : map) {
      final ExtraFile linkToFile = (ExtraFile) t.getExtras().get(Extra.ExtraType.FILE);
      if (linkToFile != null) {
        final MMapURI uri = linkToFile.getAsURI();
        final Path linkFile = uri.asFile(baseFolder).toPath();
        if (folder) {
          if (linkFile.startsWith(theFile)) {
            result.add(t);
          }
        } else if (linkFile.equals(theFile)) {
          result.add(t);
        }
      }
    }

    return result;
  }
}
