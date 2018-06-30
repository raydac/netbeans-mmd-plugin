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
package com.igormaznitsa.mindmap.ide.commons;

import java.util.Locale;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

/**
 * Misc functions and flags.
 *
 * @since 1.4.2
 */
public final class Misc {

  private Misc() {
  }

  public static final String FILELINK_ATTR_OPEN_IN_SYSTEM = "useSystem"; //NOI18N
  public static final String FILELINK_ATTR_LINE = "line"; //NOI18N

  /**
   * Session key to keep last selected folder for added file into mind map node.
   * Object is File.
   */
  public static final String SESSIONKEY_ADD_FILE_LAST_FOLDER = "file.add.last.folder";

  /**
   * Session key to keep last selected flag to open in system viewer for added
   * file into mind map node. Object is Boolean
   */
  public static final String SESSIONKEY_ADD_FILE_OPEN_IN_SYSTEM = "file.add.open_in_system";

  /**
   * Create pattern from string.
   *
   * @param text text to be converted into pattern.
   * @param patternFlags flags to be used
   * @return formed pattern
   */
  @Nonnull
  public static Pattern string2pattern(@Nonnull final String text, final int patternFlags) {
    final StringBuilder result = new StringBuilder();

    for (final char c : text.toCharArray()) {
      result.append("\\u"); //NOI18N
      final String code = Integer.toHexString(c).toUpperCase(Locale.ENGLISH);
      result.append("0000", 0, 4 - code.length()).append(code); //NOI18N
    }

    return Pattern.compile(result.toString(), patternFlags);
  }
}
