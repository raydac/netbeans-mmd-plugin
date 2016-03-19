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
package com.igormaznitsa.mindmap.model.parser;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class MindMapLexerTest {

  private static void assertLexer(final MindMapLexer lexer, final MindMapLexer.TokenType expectedType, final String expectedText, final int expectedTokenStart, final int expectedTokenEnd) {
    assertEquals(expectedType, lexer.getTokenType());
    assertEquals(expectedText, lexer.getTokenText());

    final TokenPosition pos = lexer.makeTokenPosition();

    assertEquals(expectedTokenStart, pos.getStartOffset());
    assertEquals(expectedTokenEnd, pos.getEndOffset());
  }

  @Test
  public void testEmpty() {
    final MindMapLexer lexer = new MindMapLexer();
    lexer.start("", 0, 0, MindMapLexer.TokenType.HEAD_LINE);
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertNull(lexer.getTokenType());
  }

  @Test
  public void testOneHeadLine() {
    final MindMapLexer lexer = new MindMapLexer();
    final String testString = "  hello world";
    lexer.start(testString, 0, testString.length(), MindMapLexer.TokenType.HEAD_LINE);
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.HEAD_LINE, testString, 0, testString.length());
  }

  @Test
  public void testMultilineHeader() {
    final MindMapLexer lexer = new MindMapLexer();
    final String testString = " First line\nSecond line \n--\n";
    lexer.start(testString, 0, testString.length(), MindMapLexer.TokenType.HEAD_LINE);
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.HEAD_LINE, " First line\n", 0, 12);
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.HEAD_LINE, "Second line \n", 12, 25);
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.HEAD_DELIMITER, "--\n", 25, 28);
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertNull(lexer.getTokenType());
  }

  @Test
  public void testMultilineHeaderWithPseudoDelimiter() {
    final MindMapLexer lexer = new MindMapLexer();
    final String testString = " First line\nSecond line \n-- \n--";
    lexer.start(testString, 0, testString.length(), MindMapLexer.TokenType.HEAD_LINE);
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.HEAD_LINE, " First line\n", 0, 12);
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.HEAD_LINE, "Second line \n", 12, 25);
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.HEAD_LINE, "-- \n", 25, 29);
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.HEAD_DELIMITER, "--", 29, 31);
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertNull(lexer.getTokenType());
  }

  @Test
  public void testHeaderWithAttributes() {
    final MindMapLexer lexer = new MindMapLexer();
    final String testString = "First line\n> attr='hello'\n--\n";
    lexer.start(testString, 0, testString.length(), MindMapLexer.TokenType.HEAD_LINE);
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.HEAD_LINE, "First line\n", 0, 11);
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.ATTRIBUTE, "> attr='hello'\n", 11, 26);
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.HEAD_DELIMITER, "--\n", 26, 29);
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertNull(lexer.getTokenType());
  }

  @Test
  public void testHeaderWithAttributesAndTopic() {
    final MindMapLexer lexer = new MindMapLexer();
    final String testString = "First line\n> attr='hello'\n--\n   # Topic\n";
    lexer.start(testString, 0, testString.length(), MindMapLexer.TokenType.HEAD_LINE);
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.HEAD_LINE, "First line\n", 0, 11);
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.ATTRIBUTE, "> attr='hello'\n", 11, 26);
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.HEAD_DELIMITER, "--\n", 26, 29);
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.WHITESPACE, "   ", 29, 32);
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.TOPIC, "# Topic\n", 32, 40);
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertNull(lexer.getTokenType());
  }

  @Test
  public void testTopicWithAttributeAndText() {
    final MindMapLexer lexer = new MindMapLexer();
    final String testString = "Header\n--\n# Hello\n> attrs='some'\n- LINK  \n<pre>Hurraa <ugu>\nrabotaet</pre>\nhmm";
    lexer.start(testString, 0, testString.length(), MindMapLexer.TokenType.HEAD_LINE);
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.HEAD_LINE, "Header\n", 0, 7);
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.HEAD_DELIMITER, "--\n", 7, 10);
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.TOPIC, "# Hello\n", 10, 18);
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.ATTRIBUTE, "> attrs='some'\n", 18, 33);
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.EXTRA_TYPE, "- LINK  \n", 33, 42);
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.EXTRA_TEXT, "<pre>Hurraa <ugu>\nrabotaet</pre>", 42, 74);
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.WHITESPACE, "\n", 74, 75);
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.UNKNOWN_LINE, "hmm", 75, 78);
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertNull(lexer.getTokenType());
  }

  @Test
  public void testTwoPhaseReading_HeaderDelimiter() {
    final MindMapLexer lexer = new MindMapLexer();
    final String testString = "Header\n---------------";
    lexer.start(testString, 0, 10, MindMapLexer.TokenType.HEAD_LINE);
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.HEAD_LINE, "Header\n", 0, 7);
    lexer.advance();
    assertFalse(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.HEAD_LINE, "---", 7, 10);
    lexer.setBufferEndOffset(testString.length());
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.HEAD_DELIMITER, "---------------", 7, 22);
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertNull(lexer.getTokenType());
  }

  @Test
  public void testTwoPhaseReading_HeadLineAttribute() {
    final MindMapLexer lexer = new MindMapLexer();
    final String testString = "Header\n> hello='world'";
    lexer.start(testString, 0, 10, MindMapLexer.TokenType.HEAD_LINE);
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.HEAD_LINE, "Header\n", 0, 7);
    lexer.advance();
    assertFalse(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.HEAD_LINE, "> h", 7, 10);
    lexer.setBufferEndOffset(testString.length());
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.ATTRIBUTE, "> hello='world'", 7, 22);
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertNull(lexer.getTokenType());
  }

  @Test
  public void testTwoPhaseReading_Attribute() {
    final MindMapLexer lexer = new MindMapLexer();
    final String testString = "Header\n--\n> hello='world'";
    lexer.start(testString, 0, 11, MindMapLexer.TokenType.HEAD_LINE);
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.HEAD_LINE, "Header\n", 0, 7);
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.HEAD_DELIMITER, "--\n", 7, 10);
    lexer.advance();
    assertFalse(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.ATTRIBUTE, ">", 10, 11);
    lexer.setBufferEndOffset(testString.length());
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.ATTRIBUTE, "> hello='world'", 10, testString.length());
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertNull(lexer.getTokenType());
  }

  @Test
  public void testTwoPhaseReading_Topic() {
    final MindMapLexer lexer = new MindMapLexer();
    final String testString = "Header\n--\n#Topic name";
    lexer.start(testString, 0, 14, MindMapLexer.TokenType.HEAD_LINE);
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.HEAD_LINE, "Header\n", 0, 7);
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.HEAD_DELIMITER, "--\n", 7, 10);
    lexer.advance();
    assertFalse(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.TOPIC, "#Top", 10, 14);
    lexer.setBufferEndOffset(testString.length());
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.TOPIC, "#Topic name", 10, 21);
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertNull(lexer.getTokenType());
  }

  @Test
  public void testParseTopic_StartsWithHash() {
    final MindMapLexer lexer = new MindMapLexer();
    final String testString = "Header\n--\n# \\#Topic name";
    lexer.start(testString, 0, testString.length(), MindMapLexer.TokenType.HEAD_LINE);
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.HEAD_LINE, "Header\n", 0, 7);
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.HEAD_DELIMITER, "--\n", 7, 10);
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.TOPIC, "# \\#Topic name", 10, 24);    
  }
  
  @Test
  public void testTwoPhaseReading_ExtraText() {
    final MindMapLexer lexer = new MindMapLexer();
    final String testString = "Header\n--\n<pre>Hello world</pre>";
    lexer.start(testString, 0, 14, MindMapLexer.TokenType.HEAD_LINE);
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.HEAD_LINE, "Header\n", 0, 7);
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.HEAD_DELIMITER, "--\n", 7, 10);
    lexer.advance();
    assertFalse(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.EXTRA_TEXT, "<pre", 10, 14);
    lexer.setBufferEndOffset(testString.length());
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.EXTRA_TEXT, "<pre>Hello world</pre>", 10, testString.length());
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertNull(lexer.getTokenType());
  }

  @Test
  public void testTwoPhaseReading_WrongTagExtraText() {
    final MindMapLexer lexer = new MindMapLexer();
    final String testString = "Header\n--\n<pra>Hello world</pre>";
    lexer.start(testString, 0, 13, MindMapLexer.TokenType.HEAD_LINE);
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.HEAD_LINE, "Header\n", 0, 7);
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.HEAD_DELIMITER, "--\n", 7, 10);
    lexer.advance();
    assertFalse(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.EXTRA_TEXT, "<pr", 10, 13);
    lexer.setBufferEndOffset(testString.length());
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.UNKNOWN_LINE, "<pra>Hello world</pre>", 10, testString.length());
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertNull(lexer.getTokenType());
  }

}
