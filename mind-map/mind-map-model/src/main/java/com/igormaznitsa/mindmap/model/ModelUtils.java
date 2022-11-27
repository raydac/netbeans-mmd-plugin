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

package com.igormaznitsa.mindmap.model;

import static java.util.Locale.ENGLISH;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Auxiliary methods to work with mind map model.
 */
public final class ModelUtils implements Constants {

  private static final Pattern UNESCAPE_BR = Pattern.compile("(?i)\\<\\s*?br\\s*?\\/?\\>");
  private static final Pattern MD_ESCAPED_PATTERN =
      Pattern.compile("(\\\\[\\\\`*_{}\\[\\]()#<>+-.!])");
  private static final String MD_ESCAPED_CHARS = "\\`*_{}[]()#<>+-.!";
  private static final Pattern URI_QUERY_PARAMETERS = Pattern.compile("\\&?([^=]+)=([^&]*)");


  private ModelUtils() {
  }

  /**
   * Count number of chars found as prefix.
   *
   * @param chr  char to be checked
   * @param text text to be processed
   * @return number of chars met ias prefix of the string
   */
  public static int countPrefixChars(final char chr, final String text) {
    int result = 0;
    for (int i = 0; i < text.length(); i++) {
      if (text.charAt(i) == chr) {
        result++;
      } else {
        break;
      }
    }
    return result;
  }

  /**
   * Make PRE block with escaped text
   *
   * @param text text to be escaped and presented as PRE block, must not be null
   * @return pre block from text, must not be null
   */
  public static String makePreBlock(final String text) {
    return "<pre>" + escapeTextForPreBlock(text) + "</pre>";
  }

  /**
   * Escape text to be used in a PRE block.
   *
   * @param text text to be escaped, must not be null
   * @return escaped text, must not be null
   */
  public static String escapeTextForPreBlock(final String text) {
    final int length = text.length();
    final StringBuilder result = new StringBuilder(length);

    for (int i = 0; i < length; i++) {
      final char chr = text.charAt(i);

      switch (chr) {
        case '\"':
          result.append("&quot;");
          break;
        case '&':
          result.append("&amp;");
          break;
        case '<':
          result.append("&lt;");
          break;
        case '>':
          result.append("&gt;");
          break;
        default: {
          result.append(chr);
        }
        break;
      }
    }

    return result.toString();
  }

  /**
   * Make Markdown code block from text
   *
   * @param text text to be wrapped as code block, must not be null
   * @return wrapped text, must not be null
   */
  public static String makeMDCodeBlock(final String text) {
    final int maxQuotes = calcMaxLengthOfBacktickQuotesSubstr(text) + 1;
    final StringBuilder result = new StringBuilder(text.length() + 16);
    repeatChar(result, '`', maxQuotes);
    result.append(text);
    repeatChar(result, '`', maxQuotes);
    return result.toString();
  }

  public static String escapeMarkdown(final String text) {
    final StringBuilder buffer = new StringBuilder(text.length() * 2);
    for (final char c : text.toCharArray()) {
      if (c == '\n') {
        buffer.append("<br/>");
        continue;
      } else if (Character.isISOControl(c)) {
        continue;
      } else if (MD_ESCAPED_CHARS.indexOf(c) >= 0) {
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

  /**
   * Append a char some number times
   *
   * @param out   output, must not be null
   * @param chr   char to be added
   * @param times number of times to repeat the char
   * @throws IOException if any IO error
   */
  public static void repeatChar(final Appendable out, final char chr, final int times)
      throws IOException {
    for (int i = 0; i < times; i++) {
      out.append(chr);
    }
  }

  /**
   * Append a char some number times
   *
   * @param out   output, must not be null
   * @param chr   char to be added
   * @param times number of times to repeat the char
   */
  public static void repeatChar(final StringBuilder out, final char chr, final int times) {
    for (int i = 0; i < times; i++) {
      out.append(chr);
    }
  }

  /**
   * Unescape Markdown text
   *
   * @param text text to be unescaped, must not be null
   * @return unescaped text, must not be null
   */
  public static String unescapeMarkdown(final String text) {
    String unescaped = UNESCAPE_BR.matcher(text).replaceAll(NEXT_LINE);
    final StringBuffer result = new StringBuffer(text.length());
    final Matcher escaped = MD_ESCAPED_PATTERN.matcher(unescaped);
    while (escaped.find()) {
      final String group = escaped.group(1);
      escaped.appendReplacement(result, Matcher.quoteReplacement(group.substring(1)));
    }
    escaped.appendTail(result);
    return result.toString();
  }

  /**
   * Make ellipsis from text
   *
   * @param text      text to be processed, must not be null
   * @param maxLength max allowed length of text, must be positive one
   * @return ellipsis if text longer than asked length
   */
  public static String makeEllipsis(String text, final int maxLength) {
    if (text.length() > maxLength) {
      text = text.substring(0, maxLength) + "...";
    }
    return text;
  }

  /**
   * Get number of lines in text
   *
   * @param text text, must not be null
   * @return found number of lines
   */
  public static int countLines(final String text) {
    int result = 1;
    for (int i = 0; i < text.length(); i++) {
      if (text.charAt(i) == NEXT_LINE_CHAR) {
        result++;
      }
    }
    return result;
  }

  /**
   * Break text to lines
   *
   * @param text text to be processed, must not be null
   * @return array of lines, must not be null
   */
  public static String[] breakToLines(final String text) {
    final int lineNum = countLines(text);
    final String[] result = new String[lineNum];
    final StringBuilder line = new StringBuilder();

    int index = 0;

    for (int i = 0; i < text.length(); i++) {
      if (text.charAt(i) == NEXT_LINE_CHAR) {
        result[index++] = line.toString();
        line.setLength(0);
      } else {
        line.append(text.charAt(i));
      }
    }
    result[index] = line.toString();
    return result;
  }

  /**
   * Generate URI query part from provided properties
   *
   * @param properties query parameters, must not be null
   * @return URI query formatted parameter string, must not be null
   */
  public static String makeQueryStringForURI(final Properties properties) {
    if (properties == null || properties.isEmpty()) {
      return "";
    }
    final StringBuilder buffer = new StringBuilder();

    final List<String> keysInOrder = new ArrayList<>(properties.stringPropertyNames());
    Collections.sort(keysInOrder);

    try {
      for (final String k : keysInOrder) {
        final String encodedKey = URLEncoder.encode(k, StandardCharsets.UTF_8.name());
        final String encodedValue =
            URLEncoder.encode(properties.getProperty(k), StandardCharsets.UTF_8.name());

        if (buffer.length() > 0) {
          buffer.append(URI_QUERY_PARAMETER_SEPARATOR);
        }
        buffer.append(encodedKey).append('=').append(encodedValue);
      }
    } catch (final UnsupportedEncodingException ex) {
      throw new Error("Unexpected error", ex);
    }
    return buffer.toString();
  }

  /**
   * Extract query parameters from URI
   *
   * @param uri uri to be processed, must not be null
   * @return properties contain found query parameters, must not be null
   */
  public static Properties extractQueryPropertiesFromURI(final URI uri) {
    final Properties result = new Properties();

    final String rawQuery = uri.getRawQuery();
    if (rawQuery != null) {
      final Matcher matcher = URI_QUERY_PARAMETERS.matcher(rawQuery);

      while (matcher.find()) {
        try {
          final String key = URLDecoder.decode(matcher.group(1), StandardCharsets.UTF_8.name());
          final String value = URLDecoder.decode(matcher.group(2), StandardCharsets.UTF_8.name());
          result.put(key, value);
        } catch (UnsupportedEncodingException ex) {
          throw new Error(ex);
        }
      }
    }

    return result;
  }

  /**
   * Char code as URI hex byte string
   *
   * @param ch char to be represented
   * @return result as '%' prefixed string, must not be null
   */
  private static String char2UriHexByte(final char ch) {
    final String s = Integer.toHexString(ch).toUpperCase(ENGLISH);
    return '%' + (s.length() < 2 ? "0" : "") + s;
  }

  /**
   * Encode string chars into format allowed for URI
   *
   * @param text string to be processed, must not be null
   * @return encoded string, must not be null
   */
  public static String encodeForURI(final String text) {
    final StringBuilder result = new StringBuilder();

    for (int i = 0; i < text.length(); i++) {
      final char c = text.charAt(i);
      if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || "-_.~".indexOf(c) >= 0) {
        result.append(c);
      } else {
        if (":/?#[]@!$^'()*+,;= ".indexOf(c) >= 0) {
          result.append(char2UriHexByte(c));
        } else {
          result.append(c);
        }
      }
    }

    return result.toString();
  }

  /**
   * Make file for path with prefix 'file:' awareness
   *
   * @param path path for file, can be null or empty
   * @return created file if path provided, null if path is null or empty
   * @throws URISyntaxException if URI of file path is malformed one
   */
  public static File makeFileForPath(final String path) throws URISyntaxException {
    if (path == null || path.isEmpty()) {
      return null;
    }
    if (path.startsWith("file:")) {
      return new File(new URI(normalizeFileURI(path)));
    } else {
      return new File(path);
    }
  }

  /**
   * Escape special URI chars in text
   *
   * @param text text to be escaped, must not be null
   * @return escaped text, must not be null
   */
  public static String escapeURIPath(final String text) {
    final String chars = "% :<>?";
    String result = text;
    for (int i = 0; i < chars.length(); i++) {
      final char ch = chars.charAt(i);
      result = result.replace(Character.toString(ch),
          '%' + Integer.toHexString(ch).toUpperCase(ENGLISH));
    }
    return result;
  }

  /**
   * Remove all ISO control codes from text
   *
   * @param text text to be processed, must not be null
   * @return cleared text, must not be null
   */
  public static String removeISOControls(final String text) {
    StringBuilder result = null;
    boolean detected = false;
    for (int i = 0; i < text.length(); i++) {
      final char ch = text.charAt(i);
      if (detected) {
        if (!Character.isISOControl(ch)) {
          result.append(ch);
        }
      } else {
        if (Character.isISOControl(ch)) {
          detected = true;
          result = new StringBuilder(text.length());
          result.append(text, 0, i);
        }
      }
    }
    return detected ? result.toString() : text;
  }

  /**
   * Normalize file URI string, remove scheme if presented and replace special chars
   *
   * @param fileUri file URI, must not be null
   * @return normalized value, must not be null
   */
  private static String normalizeFileURI(final String fileUri) {
    final int schemePosition = fileUri.indexOf(':');
    final String scheme =
        schemePosition < 0 ? "" : fileUri.substring(0, schemePosition + 1);
    final String chars = " :<>?";
    String result = fileUri.substring(scheme.length());
    for (int i = 0; i < chars.length(); i++) {
      final char ch = chars.charAt(i);
      result = result.replace(Character.toString(ch),
          "%" + Integer.toHexString(ch).toUpperCase(ENGLISH));
    }
    return scheme + result;
  }

  /**
   * Mak URI from Path
   *
   * @param path path to be processed, can be null
   * @return generated URI, can be null if path is null
   */
  public static URI toURI(final Path path) {
    if (path == null) {
      return null;
    }
    try {
      final StringBuilder buffer = new StringBuilder();

      final Path root = path.getRoot();
      if (root != null) {
        buffer.append(root.toString().replace('\\', '/'));
      }

      for (final Path p : path) {
        if (buffer.length() > 0 && buffer.charAt(buffer.length() - 1) != '/') {
          buffer.append('/');
        }
        buffer.append(encodeForURI(p.toFile().getName()));
      }

      if (path.isAbsolute()) {
        buffer.insert(0, "file://" + (root == null ? "/" : ""));
      }

      return new URI(buffer.toString());
    } catch (Exception ex) {
      throw new IllegalArgumentException("Can't convert path to URI: " + path, ex);
    }
  }

  /**
   * Make File from URI
   *
   * @param uri uri to be converted, must not be null
   * @return result file, must not be null
   */
  public static File toFile(final URI uri) {
    final List<String> pathItems = new ArrayList<>();

    final String authority = uri.getAuthority();
    if (authority != null && !authority.isEmpty()) {
      pathItems.add(authority);
    }

    final String[] splitPath = uri.getPath().split("\\/");
    boolean separator = false;
    if (splitPath.length == 0) {
      separator = true;
    } else {
      for (final String s : splitPath) {
        if (!s.isEmpty()) {
          pathItems.add(separator ? File.separatorChar + s : s);
          separator = false;
        } else {
          separator = true;
        }
      }
    }

    if (separator) {
      pathItems.add(File.separator);
    }

    final String[] fullArray = pathItems.toArray(new String[0]);
    final String[] next = Arrays.copyOfRange(fullArray, 1, fullArray.length);
    return Paths.get(fullArray[0], next).toFile();
  }
}
