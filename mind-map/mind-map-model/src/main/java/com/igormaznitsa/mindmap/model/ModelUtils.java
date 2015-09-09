/*
 * Copyright 2015 Igor Maznitsa.
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
package com.igormaznitsa.mindmap.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringEscapeUtils;

public enum ModelUtils {

  ;
  private static final Pattern UNESCAPE_BR = Pattern.compile("(?i)\\<\\s*?br\\s*?\\/?\\>"); //NOI18N
  private static final Pattern MD_ESCAPED_PATTERN = Pattern.compile("(\\\\[\\\\`*_{}\\[\\]()#<>+-.!])"); //NOI18N
  private static final String MD_ESCAPED_CHARS = "\\`*_{}[]()#<>+-.!"; //NOI18N

  public static boolean onlyFromChar(final String line, final char chr) {
    if (line.isEmpty()) {
      return false;
    }
    for (int i = 0; i < line.length(); i++) {
      if (line.charAt(i) != chr) {
        return false;
      }
    }
    return true;
  }

  public static String makePreBlock(final String text) {
    return "<pre>" + StringEscapeUtils.escapeHtml(text) + "</pre>"; //NOI18N
  }

  public static String makeMDCodeBlock(final String text) throws IOException {
    final int maxQuotes = calcMaxLengthOfBacktickQuotesSubstr(text) + 1;
    final StringBuilder result = new StringBuilder(text.length() + 16);
    writeChar(result, '`', maxQuotes);
    result.append(text);
    writeChar(result, '`', maxQuotes);
    return result.toString();
  }

  public static String escapeMarkdownStr(final String text) {
    final StringBuilder buffer = new StringBuilder(text.length() * 2);
    for (final char c : text.toCharArray()) {
      if (c == '\n') {
        buffer.append("<br/>"); //NOI18N
        continue;
      }
      else if (Character.isISOControl(c)) {
        continue;
      }
      else if (MD_ESCAPED_CHARS.indexOf(c) >= 0) {
        buffer.append('\\');
      }

      buffer.append(c);
    }
    return buffer.toString();
  }

  public static int calcMaxLengthOfBacktickQuotesSubstr(final String text) {
    int result = 0;
    if (text != null) {
      int pos = 0;
      while (pos >= 0) {
        pos = text.indexOf('`', pos);
        if (pos >= 0) {
          int found = 0;
          while (pos < text.length() && text.charAt(pos) == '`') {
            found++;
            pos++;
          }
          result = Math.max(result, found);
        }
      }
    }
    return result;
  }

  public static void writeChar(final Appendable out, final char chr, final int times) throws IOException {
    for (int i = 0; i < times; i++) {
      out.append(chr);
    }
  }

  public static String unescapeMarkdownStr(final String text) {
    String unescaped = UNESCAPE_BR.matcher(text).replaceAll("\n"); //NOI18N
    final StringBuffer result = new StringBuffer(text.length());
    final Matcher escaped = MD_ESCAPED_PATTERN.matcher(unescaped);
    while (escaped.find()) {
      final String group = escaped.group(1);
      escaped.appendReplacement(result, Matcher.quoteReplacement(group.substring(1)));
    }
    escaped.appendTail(result);
    return result.toString();
  }

  public static String makeShortTextVersion(String text, final int maxLength) {
    if (text.length() > maxLength) {
      text = text.substring(0, maxLength) + "..."; //NOI18N
    }
    return text;
  }

  public static int countLines(final String text) {
    int result = 1;
    for (int i = 0; i < text.length(); i++) {
      if (text.charAt(i) == '\n') {
        result++;
      }
    }
    return result;
  }

  public static void assertNotNull(final String message, final Object obj) {
    if (obj == null) {
      throw new NullPointerException(message);
    }
  }

  public static String[] breakToLines(final String text) {
    final int lineNum = countLines(text);
    final String[] result = new String[lineNum];
    final StringBuilder line = new StringBuilder();

    int index = 0;

    for (int i = 0; i < text.length(); i++) {
      if (text.charAt(i) == '\n') {
        result[index++] = line.toString();
        line.setLength(0);
      }
      else {
        line.append(text.charAt(i));
      }
    }
    result[index] = line.toString();
    return result;
  }

}
