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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.igormaznitsa.mindmap.annotations.Direction;
import com.igormaznitsa.mindmap.annotations.MmdColor;
import com.igormaznitsa.mindmap.annotations.MmdEmoticon;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class MmdTopicDynamicTest {

  private static void assertArray(final String arrayText, final String... expected) {
    final List<String> parsed = MmdTopicDynamic.splitArrayLine(arrayText);
    assertEquals(expected.length, parsed.size());
    for (int i = 0; i < expected.length; i++) {
      assertEquals("i=" + i, expected[i], parsed.get(i));
    }
  }

  private static void assertNextPair(final Reader reader, final String key, final String value) {
    final Map.Entry<String, String> result = MmdTopicDynamic.nextKeyValuePair(reader);
    assertNotNull(result);
    assertEquals(key, result.getKey());
    assertEquals(value, result.getValue());
  }

  private static void assertEnd(final Reader reader) {
    assertNull(MmdTopicDynamic.nextKeyValuePair(reader));
  }

  @Test
  public void testSimple() {
    final Reader reader = new StringReader("a = b, c = \"abc\\\"e\\\"");
    assertNextPair(reader, "a", "b");
    assertNextPair(reader, "c", "\"abc\\\"e\\\"");
    assertEnd(reader);
  }

  @Test
  public void testWithArray() {
    final Reader reader =
        new StringReader("a = b, array = {\"hello\",\"world\"}, Some = 1123, bool = False");
    assertNextPair(reader, "a", "b");
    assertNextPair(reader, "array", "{\"hello\",\"world\"}");
    assertNextPair(reader, "Some", "1123");
    assertNextPair(reader, "bool", "False");
    assertEnd(reader);
  }

  @Test
  public void testEmpty() {
    final Reader reader = new StringReader(" ");
    assertEnd(reader);
  }

  @Test
  public void testOnlyKey() {
    final Reader reader = new StringReader("huzza");
    assertNextPair(reader, "huzza", "");
    assertEnd(reader);
  }

  @Test
  public void testOnlyValue() {
    final Reader reader = new StringReader("=huzza");
    assertNextPair(reader, "", "huzza");
    assertEnd(reader);
  }

  @Test
  public void testArrayCharInKeyAndInValue() {
    final Reader reader = new StringReader("hu{zza=abc, huzza=a}bc");
    assertNextPair(reader, "hu{zza", "abc");
    assertNextPair(reader, "huzza", "a}bc");
    assertEnd(reader);
  }

  @Test
  public void testSplitArrayLine() {
    assertArray("");
    assertArray("abc");
    assertArray("{abc}", "abc");
    assertArray("{abc,cde,\"hello\\world\\\\\"}", "abc", "cde", "\"hello\\world\\\\\"");
    assertArray("{abc,cde", "abc", "cde");
    assertArray("{abc,cde, \"  ddd", "abc", "cde", "\"  ddd");
  }


  @Test
  public void testMakeWithOnlyTitle() {
    final MmdTopicDynamic topicDynamic = MmdTopicDynamic.of(111, 222, "Hello some \"title\"");
    assertEquals(111L, topicDynamic.line());
    assertEquals(222L, topicDynamic.position());
    assertEquals(MmdTopicDynamic.CONFIG_DEFAULT.get("uid"), topicDynamic.uid());
    assertEquals(MmdTopicDynamic.CONFIG_DEFAULT.get("uri"), topicDynamic.uri());
    assertEquals(MmdTopicDynamic.CONFIG_DEFAULT.get("fileuid"), topicDynamic.fileUid());
    assertEquals(MmdTopicDynamic.CONFIG_DEFAULT.get("anchor"), topicDynamic.anchor());
    assertEquals(MmdTopicDynamic.CONFIG_DEFAULT.get("collapse"), topicDynamic.collapse());
    assertEquals(MmdTopicDynamic.CONFIG_DEFAULT.get("colorborder"), topicDynamic.colorBorder());
    assertEquals(MmdTopicDynamic.CONFIG_DEFAULT.get("colortext"), topicDynamic.colorText());
    assertEquals(MmdTopicDynamic.CONFIG_DEFAULT.get("colorfill"), topicDynamic.colorFill());
    assertEquals(MmdTopicDynamic.CONFIG_DEFAULT.get("emoticon"), topicDynamic.emoticon());
    assertEquals(MmdTopicDynamic.CONFIG_DEFAULT.get("direction"), topicDynamic.direction());
    assertEquals(MmdTopicDynamic.CONFIG_DEFAULT.get("filelink"), topicDynamic.fileLink());
    assertEquals(MmdTopicDynamic.CONFIG_DEFAULT.get("path"), topicDynamic.path());
    assertEquals(MmdTopicDynamic.CONFIG_DEFAULT.get("jumpto"), topicDynamic.jumpTo());
    assertEquals(MmdTopicDynamic.CONFIG_DEFAULT.get("note"), topicDynamic.note());
    assertEquals("Hello some \"title\"", topicDynamic.title());
  }

  @Test
  public void testMakeAllParameters() {
    final MmdTopicDynamic topicDynamic = MmdTopicDynamic.of(111, 222,
        "title = \"It's some title\", uid = \"UID666\", fileUid = \"FILE777\", path = {\"alpha\",\"beta\",\"gamma\"}, emoticon = MmdEmoticon.ABACUS, fileLink = \"fileLink000\", anchor = false, jumpTo = \"AAAA\", uri = \"http://google.com\", note = \"It's some note\\nnext line\", direction = Direction.LEFT, collapse = true, colorFill = MmdColor.Azure, colorBorder = MmdColor.Bisque, colorText = MmdColor.BlueViolet",
        "empty");
    assertEquals(111L, topicDynamic.line());
    assertEquals(222L, topicDynamic.position());
    assertEquals("UID666", topicDynamic.uid());
    assertEquals("http://google.com", topicDynamic.uri());
    assertEquals("FILE777", topicDynamic.fileUid());
    assertEquals(false, topicDynamic.anchor());
    assertEquals(true, topicDynamic.collapse());
    assertEquals(MmdColor.Bisque, topicDynamic.colorBorder());
    assertEquals(MmdColor.BlueViolet, topicDynamic.colorText());
    assertEquals(MmdColor.Azure, topicDynamic.colorFill());
    assertEquals(MmdEmoticon.ABACUS, topicDynamic.emoticon());
    assertEquals(Direction.LEFT, topicDynamic.direction());
    assertEquals("fileLink000", topicDynamic.fileLink());
    assertArrayEquals(new String[] {"alpha", "beta", "gamma"}, topicDynamic.path());
    assertEquals("AAAA", topicDynamic.jumpTo());
    assertEquals("It's some note\nnext line", topicDynamic.note());
    assertEquals("It's some title", topicDynamic.title());
  }
}