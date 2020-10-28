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
package com.igormaznitsa.mindmap.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


import java.io.File;
import java.util.regex.Pattern;
import org.junit.Test;

public class ExtraNoteTest {

  @Test
  public void testEqualsNonEncrypted() throws Exception {
    assertTrue(new ExtraNote("aaa").equals(new ExtraNote("aaa")));
    assertFalse(new ExtraNote("aaa").equals(new ExtraNote("Aaa")));
    assertFalse(new ExtraNote("aaa").equals(new ExtraNote("aaaa")));
    assertFalse(new ExtraNote("aaa").equals(new ExtraNote("")));
  }

  @Test
  public void testEqualsEncrypted() throws Exception {
    assertTrue(new ExtraNote("aaa", true, null).equals(new ExtraNote("aaa", true, null)));
    assertFalse(new ExtraNote("aaa", true, null).equals(new ExtraNote("aaa", false, null)));
    assertFalse(new ExtraNote("aaa", true, "tip").equals(new ExtraNote("aaa", true, null)));
    assertFalse(new ExtraNote("aaa", true, "tip").equals(new ExtraNote("aaa", true, "top")));
    assertFalse(new ExtraNote("aaa", true, "tip").equals(new ExtraNote("aaa", false, "tip")));

    assertFalse(new ExtraNote("aaa", true, "tip").equals(new ExtraNote("Aaa", true, "tip")));
    assertFalse(new ExtraNote("aaa").equals(new ExtraNote("aaaa", true, "tip")));
    assertFalse(new ExtraNote("aaa").equals(new ExtraNote("", true, "tip")));
  }

  @Test
  public void testContainsPattern() throws Exception {
    final ExtraNote note = new ExtraNote(
        "domr dsf sdf sdf \n sdf http://www.1cpp.ru/forum/YaBB.pl?num=1341507344 fsdf sdfd \n");
    assertTrue(note.containsPattern(null,
        Pattern.compile(Pattern.quote("http://www.1cpp.ru/forum/YaBB.pl?num=1341507344"))));
    assertTrue(note.containsPattern(new File(System.getProperty("user.home")),
        Pattern.compile(Pattern.quote("http://www.1cpp.ru/forum/YaBB.pl?num=1341507344"))));
    assertTrue(note.containsPattern(null, Pattern
        .compile(Pattern.quote("http://www.1cpp.ru/forum/YaBB.pl?num=1341507344"),
            Pattern.CASE_INSENSITIVE)));
    assertTrue(note.containsPattern(new File(System.getProperty("user.home")), Pattern
        .compile(Pattern.quote("http://www.1cpp.ru/forum/YaBB.pl?num=1341507344"),
            Pattern.CASE_INSENSITIVE)));
    assertTrue(note.containsPattern(null, Pattern.compile(Pattern.quote("num=1341507344"))));
    assertTrue(note.containsPattern(new File(System.getProperty("user.home")),
        Pattern.compile(Pattern.quote("num=1341507344"))));

    assertFalse(note.containsPattern(null, Pattern.compile(Pattern.quote("yab3"), Pattern.CASE_INSENSITIVE)));
  }
  
}
