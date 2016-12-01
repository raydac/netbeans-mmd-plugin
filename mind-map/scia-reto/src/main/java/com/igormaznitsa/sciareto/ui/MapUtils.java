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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
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
  public static Pattern string2pattern(@Nonnull final String text, final int patternFlags){
    final StringBuilder result = new StringBuilder();
    
    for(final char c : text.toCharArray()){
      result.append("\\u");
      final String code = Integer.toHexString(c).toUpperCase(Locale.ENGLISH);
      result.append("0000",0,4-code.length()).append(code);
    }
    
    return Pattern.compile(result.toString(), patternFlags);
  }
  
  @Nonnull
  @MustNotContainNull
  public static List<File> extractAllFileLinks(@Nullable final File baseFolder, @Nonnull final MindMap map) {
    final List<File> result = new ArrayList<File>();
    for(final Topic t : map){
      final ExtraFile file = (ExtraFile) t.getExtras().get(Extra.ExtraType.FILE);
      if (file != null){
        result.add(file.getAsURI().asFile(baseFolder));
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
