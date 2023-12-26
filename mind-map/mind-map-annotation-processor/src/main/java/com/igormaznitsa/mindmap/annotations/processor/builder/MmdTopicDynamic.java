/*
 * Copyright (C) 2015-2023 Igor A. Maznitsa
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

package com.igormaznitsa.mindmap.annotations.processor.builder;

import static java.util.Objects.requireNonNullElse;

import com.igormaznitsa.mindmap.annotations.Direction;
import com.igormaznitsa.mindmap.annotations.MmdColor;
import com.igormaznitsa.mindmap.annotations.MmdEmoticon;
import com.igormaznitsa.mindmap.annotations.MmdTopic;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.text.StringEscapeUtils;

@SuppressWarnings("ClassExplicitlyAnnotation")
public final class MmdTopicDynamic implements MmdTopic {
  public static final Map<String, Object> CONFIG_DEFAULT;
  private static final String[] EMPTY_STRINGS = new String[0];

  static {
    final Map<String, Object> map = new HashMap<>();
    map.put("uid", "");
    map.put("fileuid", "");
    map.put("path", EMPTY_STRINGS);
    map.put("emoticon", MmdEmoticon.EMPTY);
    map.put("filelink", "");
    map.put("anchor", true);
    map.put("jumpto", "");
    map.put("note", "");
    map.put("uri", "");
    map.put("colortext", MmdColor.Default);
    map.put("colorfill", MmdColor.Default);
    map.put("colorborder", MmdColor.Default);
    map.put("collapse", false);
    map.put("direction", Direction.AUTO);
    map.put("title", "");
    CONFIG_DEFAULT = Collections.unmodifiableMap(map);
  }

  public final String uid;
  public final String fileUid;
  public final String[] path;
  public final MmdEmoticon emoticon;
  public final String fileLink;
  public final boolean anchor;
  public final String jumpTo;
  public final String note;
  public final String uri;
  public final MmdColor colorText;
  public final MmdColor colorFill;
  public final MmdColor colorBorder;
  public final boolean collapse;
  public final Direction direction;
  public final String title;
  private final long position;
  private final long line;

  private MmdTopicDynamic(
      final long line,
      final long position,
      final String uid,
      final String fileUid,
      final String[] path,
      final MmdEmoticon emoticon,
      final String fileLink,
      final boolean anchor,
      final String jumpTo,
      final String note,
      final String uri,
      final MmdColor colorText,
      final MmdColor colorFill,
      final MmdColor colorBorder,
      final boolean collapse,
      final Direction direction,
      final String title) {
    this.line = line;
    this.position = position;
    this.uid = uid;
    this.fileUid = fileUid;
    this.path = path;
    this.emoticon = emoticon;
    this.fileLink = fileLink;
    this.anchor = anchor;
    this.jumpTo = jumpTo;
    this.note = note;
    this.uri = uri;
    this.colorText = colorText;
    this.colorFill = colorFill;
    this.colorBorder = colorBorder;
    this.collapse = collapse;
    this.direction = direction;
    this.title = title;
  }

  public static MmdTopicDynamic of(final long line, final long position,
                                   final String title) {
    return new MmdTopicDynamic(
        line,
        position,
        "",
        "",
        EMPTY_STRINGS,
        MmdEmoticon.EMPTY,
        "",
        true,
        "",
        "",
        "",
        MmdColor.Default,
        MmdColor.Default,
        MmdColor.Default,
        false,
        Direction.AUTO,
        requireNonNullElse(title, "")
    );
  }

  private static String removePrefixTillDot(final String value) {
    final int lastDot = value.lastIndexOf('.');
    return lastDot >= 0 ? value.substring(lastDot + 1) : value;
  }

  private static String safeUnescapeJava(final String text) {
    try {
      return StringEscapeUtils.unescapeJava(text);
    } catch (Exception ex) {
      return text;
    }
  }

  private static String unescapeAsJavaString(final String javaString) {
    String str = javaString.trim();
    final boolean processEscaped;
    if (str.startsWith("\"")) {
      str = str.substring(1);
      if (str.endsWith("\"")) {
        str = str.substring(0, str.length() - 1);
      }
      processEscaped = true;
    } else {
      processEscaped = false;
    }

    if (processEscaped) {
      return safeUnescapeJava(str);
    } else {
      return str;
    }
  }

  public static MmdTopicDynamic of(
      final long line,
      final long position,
      final String args,
      final String others
  ) {

    final Map<String, Object> config = new HashMap<>(CONFIG_DEFAULT);

    final StringReader reader = new StringReader(args);
    while (true) {
      final Map.Entry<String, String> nextArg = nextKeyValuePair(reader);
      if (nextArg == null) {
        break;
      }

      final String value = unescapeAsJavaString(nextArg.getValue());
      final String loweCasedKey = nextArg.getKey().toLowerCase(Locale.ENGLISH);

      switch (loweCasedKey) {
        case "uid":
        case "fileuid":
        case "filelink":
        case "jumpto":
        case "note":
        case "uri":
        case "title": {
          config.put(loweCasedKey, value);
        }
        break;
        case "colortext":
        case "colorborder":
        case "colorfill": {
          config.put(loweCasedKey,
              MmdColor.findForName(removePrefixTillDot(value),
                  MmdColor.Default));
        }
        break;
        case "emoticon": {
          config.put(loweCasedKey, MmdEmoticon.findForName(
              removePrefixTillDot(value), MmdEmoticon.EMPTY));
        }
        break;
        case "path": {
          config.put(loweCasedKey, splitArrayLine(nextArg.getValue()).stream()
              .map(MmdTopicDynamic::unescapeAsJavaString)
              .toArray(String[]::new));
        }
        break;
        case "anchor":
        case "collapse": {
          config.put(loweCasedKey, Boolean.valueOf(value));
        }
        break;
        case "direction": {
          config.put(loweCasedKey,
              Direction.findForName(removePrefixTillDot(value),
                  Direction.AUTO));
        }
        break;
        default: {
          // do nothing and just ignore
        }
      }
    }

    if (((String) config.get("title")).isEmpty()) {
      config.put("title", others.trim());
    }

    return new MmdTopicDynamic(
        line,
        position,
        (String) config.get("uid"),
        (String) config.get("fileuid"),
        (String[]) config.get("path"),
        (MmdEmoticon) config.get("emoticon"),
        (String) config.get("filelink"),
        (Boolean) config.get("anchor"),
        (String) config.get("jumpto"),
        (String) config.get("note"),
        (String) config.get("uri"),
        (MmdColor) config.get("colortext"),
        (MmdColor) config.get("colorfill"),
        (MmdColor) config.get("colorborder"),
        (Boolean) config.get("collapse"),
        (Direction) config.get("direction"),
        (String) config.get("title")
    );
  }

  private static int safeRead(final Reader reader) {
    try {
      return reader.read();
    } catch (IOException ex) {
      throw new IllegalStateException(ex);
    }
  }

  static List<String> splitArrayLine(final String arrayAsText) {
    if (arrayAsText == null || arrayAsText.isEmpty()) {
      return List.of();
    }

    final List<String> result = new ArrayList<>();

    final StringBuilder buffer = new StringBuilder();
    final Reader reader = new StringReader(arrayAsText);

    int state = 0;

    while (true) {
      final int chr = safeRead(reader);
      if (chr < 0) {
        break;
      }

      switch (state) {
        case 0: { // wait for start array
          if (chr == '{') {
            state = 1;
          }
        }
        break;
        case 1: { // read value
          if (buffer.length() == 0) {
            if (chr == '\"') {
              buffer.append((char) chr);
              state = 2;
            } else if (chr == '}') {
              break;
            } else if (!Character.isWhitespace(chr) && chr != ',') {
              buffer.append((char) chr);
            }
          } else if (Character.isWhitespace(chr) || chr == '}' || chr == ',') {
            result.add(buffer.toString());
            buffer.setLength(0);
            if (chr == '}') {
              break;
            }
          } else {
            buffer.append((char) chr);
          }
        }
        break;
        case 2: { // in string
          if (chr == '\\') {
            buffer.append((char) chr);
            state = 3;
          } else if (chr == '\"') {
            buffer.append((char) chr);
            result.add(buffer.toString());
            buffer.setLength(0);
            state = 1;
          } else {
            buffer.append((char) chr);
          }
        }
        break;
        case 3: { // in special char
          buffer.append((char) chr);
          state = 2;
        }
        break;
        default: {
          throw new IllegalStateException("Unexpected state: " + state);
        }
      }
    }
    if (buffer.length() != 0) {
      result.add(buffer.toString());
    }

    return result;
  }

  static Map.Entry<String, String> nextKeyValuePair(final Reader reader) {
    final StringBuilder buffer = new StringBuilder();
    String name = null;
    String value = null;
    boolean inString = false;
    boolean inSpecChar = false;
    boolean inArray = false;

    while (true) {
      final int chr = safeRead(reader);
      if (chr < 0) {
        break;
      }
      if (name == null) {
        if (chr == ',') {
          // skip
        } else if (chr == '=') {
          name = buffer.toString().trim();
          buffer.setLength(0);
        } else {
          if (buffer.length() != 0 || !Character.isWhitespace(chr)) {
            buffer.append((char) chr);
          }
        }
      } else {
        if (inString) {
          buffer.append((char) chr);
          if (inSpecChar) {
            inSpecChar = false;
          } else if (chr == '\\') {
            inSpecChar = true;
          } else if (chr == '\"') {
            if (inArray) {
              inString = false;
            } else {
              value = buffer.toString();
              buffer.setLength(0);
              break;
            }
          }
        } else if (inArray) {
          if (chr == '}') {
            buffer.append((char) chr);
            value = buffer.toString().trim();
            buffer.setLength(0);
            break;
          } else if (chr == '\"') {
            buffer.append((char) chr);
            inString = true;
          } else {
            buffer.append((char) chr);
          }
        } else if (buffer.length() == 0 && Character.isWhitespace(chr)) {
          // skip
        } else {
          if (chr == '{') {
            buffer.append((char) chr);
            inArray = buffer.length() == 1;
          } else if (chr == ',') {
            value = buffer.toString().trim();
            buffer.setLength(0);
            break;
          } else if (chr == '\"') {
            buffer.append((char) chr);
            inString = true;
          } else {
            buffer.append((char) chr);
          }
        }
      }
    }

    if (buffer.length() > 0) {
      if (name == null) {
        name = buffer.toString().trim();
      } else if (value == null) {
        value = buffer.toString().trim();
      } else {
        throw new IllegalStateException(
            "Unexpected state, buffer has data ['" + buffer +
                "'] but name and value are set: name=" + name +
                ", value=" + value);
      }
    } else if (name == null) {
      return null;
    }

    return Map.entry(name, requireNonNullElse(value, ""));
  }

  public long position() {
    return this.position;
  }

  public long line() {
    return this.line;
  }

  public String uid() {
    return this.uid;
  }

  public String fileUid() {
    return this.fileUid;
  }

  public String[] path() {
    return this.path;
  }

  public MmdEmoticon emoticon() {
    return this.emoticon;
  }

  public String title() {
    return this.title;
  }

  public String fileLink() {
    return this.fileLink;
  }

  public boolean anchor() {
    return this.anchor;
  }

  public String jumpTo() {
    return this.jumpTo;
  }

  public String note() {
    return this.note;
  }

  public String uri() {
    return this.uri;
  }

  public MmdColor colorText() {
    return this.colorText;
  }

  public MmdColor colorFill() {
    return this.colorFill;
  }

  public MmdColor colorBorder() {
    return this.colorBorder;
  }

  public boolean collapse() {
    return this.collapse;
  }

  public Direction direction() {
    return this.direction;
  }


  @Override
  public Class<? extends Annotation> annotationType() {
    return MmdTopic.class;
  }
}
