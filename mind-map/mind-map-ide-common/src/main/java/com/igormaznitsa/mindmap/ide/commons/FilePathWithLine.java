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

package com.igormaznitsa.mindmap.ide.commons;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilePathWithLine {

  private static final Pattern PATH_CONTAINS_LINE_NUMBER = Pattern.compile("^(.*):([\\d]+)$");
  private final String path;
  private final int line;

  public FilePathWithLine(final String filePath) {
    if (filePath == null) {
      this.path = null;
      this.line = -1;
    } else {
      final Matcher matcher = PATH_CONTAINS_LINE_NUMBER.matcher(filePath);
      if (matcher.find()) {
        this.path = matcher.group(1);
        int theLine;
        try {
          theLine = Integer.parseInt(matcher.group(2));
          if (theLine <= 0) {
            theLine = -1;
          }
        } catch (NumberFormatException ex) {
          theLine = -1;
        }
        this.line = theLine;
      } else {
        this.path = filePath;
        this.line = -1;
      }
    }
  }

  public static int strToLine(final String text) {
    int parsed = -1;
    if (text != null) {
      try {
        parsed = Integer.parseInt(text.trim());
        parsed = parsed <= 0 ? -1 : parsed;
      } catch (NumberFormatException ignored) {
      }
    }
    return parsed;
  }

  public String getPathOrEmpty() {
    return this.path == null ? "" : this.path;
  }

  public boolean isPathEmpty() {
    return this.path == null || this.path.length() == 0;
  }

  public String getPath() {
    return this.path;
  }

  public int getLine() {
    return this.line;
  }

  @Override
  public String toString() {
    if (this.line > 0) {
      return this.getPathOrEmpty() + ':' + this.line;
    } else if (this.getPathOrEmpty().indexOf(':') >= 0) {
      return PATH_CONTAINS_LINE_NUMBER.matcher(this.getPathOrEmpty()).matches() ?
          this.getPathOrEmpty() + ":0" : this.getPathOrEmpty();
    } else {
      return this.getPathOrEmpty();
    }
  }

  public boolean isEmptyOrOnlySpaces() {
    return this.path == null || this.path.trim().length() == 0;
  }
}
