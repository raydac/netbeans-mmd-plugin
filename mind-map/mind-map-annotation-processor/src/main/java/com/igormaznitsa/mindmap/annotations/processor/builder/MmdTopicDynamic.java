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
import java.util.Arrays;
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
    CONFIG_DEFAULT = Map.ofEntries(Map.entry("uid", ""), Map.entry("fileuid", ""),
        Map.entry("path", EMPTY_STRINGS), Map.entry("emoticon", MmdEmoticon.EMPTY),
        Map.entry("filelink", ""), Map.entry("anchor", true), Map.entry("jumpto", ""),
        Map.entry("note", ""), Map.entry("uri", ""), Map.entry("colortext", MmdColor.Default),
        Map.entry("colorfill", MmdColor.Default), Map.entry("colorborder", MmdColor.Default),
        Map.entry("collapse", false), Map.entry("substitute", false),
        Map.entry("direction", Direction.AUTO),
        Map.entry("order", -1),
        Map.entry("title", ""));
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
  public final boolean substitute;
  public final Direction direction;
  public final String title;
  private final long position;
  private final long line;
  private final int order;

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
      final boolean substitute,
      final Direction direction,
      final String title,
      final int order) {
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
    this.order = order;
    this.substitute = substitute;
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
        false,
        Direction.AUTO,
        requireNonNullElse(title, ""),
        -1
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
      final String lowerCasedKey = nextArg.getKey().toLowerCase(Locale.ENGLISH);

      switch (lowerCasedKey) {
        case "uid":
        case "fileuid":
        case "filelink":
        case "jumpto":
        case "note":
        case "uri":
        case "title": {
          config.put(lowerCasedKey, value);
        }
        break;
        case "colortext":
        case "colorborder":
        case "colorfill": {
          config.put(lowerCasedKey,
              MmdColor.findForName(removePrefixTillDot(value),
                  MmdColor.Default));
        }
        break;
        case "emoticon": {
          config.put(lowerCasedKey, MmdEmoticon.findForName(
              removePrefixTillDot(value), MmdEmoticon.EMPTY));
        }
        break;
        case "path": {
          config.put(lowerCasedKey, splitArrayLine(nextArg.getValue()).stream()
              .map(MmdTopicDynamic::unescapeAsJavaString)
              .toArray(String[]::new));
        }
        break;
        case "substitute":
        case "anchor":
        case "collapse": {
          config.put(lowerCasedKey, Boolean.valueOf(value));
        }
        break;
        case "order": {
          config.put(lowerCasedKey, Integer.valueOf(value.trim()));
        }
        break;
        case "direction": {
          config.put(lowerCasedKey,
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
        (Boolean) config.get("substitute"),
        (Direction) config.get("direction"),
        (String) config.get("title"),
        (Integer) config.getOrDefault("order", -1)
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

  @Override
  public String uid() {
    return this.uid;
  }

  @Override
  public String fileUid() {
    return this.fileUid;
  }

  @Override
  public String[] path() {
    return this.path;
  }

  @Override
  public MmdEmoticon emoticon() {
    return this.emoticon;
  }

  @Override
  public String title() {
    return this.title;
  }

  @Override
  public String fileLink() {
    return this.fileLink;
  }

  @Override
  public boolean anchor() {
    return this.anchor;
  }

  @Override
  public String jumpTo() {
    return this.jumpTo;
  }

  @Override
  public String note() {
    return this.note;
  }

  @Override
  public String uri() {
    return this.uri;
  }

  @Override
  public MmdColor colorText() {
    return this.colorText;
  }

  @Override
  public MmdColor colorFill() {
    return this.colorFill;
  }

  @Override
  public MmdColor colorBorder() {
    return this.colorBorder;
  }

  @Override
  public boolean collapse() {
    return this.collapse;
  }

  @Override
  public Direction direction() {
    return this.direction;
  }

  @Override
  public int order() {
    return this.order;
  }

  @Override
  public boolean substitute() {
    return this.substitute;
  }

  @Override
  public Class<? extends Annotation> annotationType() {
    return MmdTopic.class;
  }

  @Override
  public String toString() {
    return "MmdTopicDynamic{" +
        "uid='" + uid + '\'' +
        ", fileUid='" + fileUid + '\'' +
        ", path=" + Arrays.toString(path) +
        ", emoticon=" + emoticon +
        ", fileLink='" + fileLink + '\'' +
        ", anchor=" + anchor +
        ", jumpTo='" + jumpTo + '\'' +
        ", note='" + note + '\'' +
        ", uri='" + uri + '\'' +
        ", colorText=" + colorText +
        ", colorFill=" + colorFill +
        ", colorBorder=" + colorBorder +
        ", collapse=" + collapse +
        ", direction=" + direction +
        ", title='" + title + '\'' +
        ", position=" + position +
        ", line=" + line +
        ", order=" + order +
        '}';
  }
}
