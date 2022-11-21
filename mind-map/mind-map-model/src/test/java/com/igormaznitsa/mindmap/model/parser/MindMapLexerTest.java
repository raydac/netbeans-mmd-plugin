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

package com.igormaznitsa.mindmap.model.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.igormaznitsa.mindmap.model.parser.MindMapLexer.TokenType;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class MindMapLexerTest {

  private static void assertLexer(final MindMapLexer lexer,
                                  final MindMapLexer.TokenType expectedType,
                                  final String expectedText, final int expectedTokenStart,
                                  final int expectedTokenEnd) {
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
  public void testExtraLink() {
    final MindMapLexer lexer = new MindMapLexer();
    final String testString = "- LINK\n<pre>http://www.google.com</pre>\n";
    lexer.start(testString, 0, testString.length(), MindMapLexer.TokenType.WHITESPACE);
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.EXTRA_TYPE, "- LINK\n", 0, 7);
    lexer.advance();

    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.EXTRA_TEXT, "<pre>http://www.google.com</pre>", 7,
        39);
    lexer.advance();

    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.WHITESPACE, "\n", 39, 40);
    lexer.advance();

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
    assertLexer(lexer, MindMapLexer.TokenType.TOPIC_LEVEL, "# ", 32, 34);
    lexer.advance();

    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.TOPIC_TITLE, "Topic\n", 34, 40);
    lexer.advance();

    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertNull(lexer.getTokenType());
  }

  @Test
  public void testTopicWithAttributeAndText() {
    final MindMapLexer lexer = new MindMapLexer();
    final String testString =
        "Header\n--\n# Hello\n> attrs='some'\n- LINK  \n<pre>Hurraa <ugu>\nrabotaet</pre>\nhmm";
    lexer.start(testString, 0, testString.length(), MindMapLexer.TokenType.HEAD_LINE);
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.HEAD_LINE, "Header\n", 0, 7);
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.HEAD_DELIMITER, "--\n", 7, 10);

    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.TOPIC_LEVEL, "# ", 10, 12);

    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.TOPIC_TITLE, "Hello\n", 12, 18);

    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.ATTRIBUTE, "> attrs='some'\n", 18, 33);
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.EXTRA_TYPE, "- LINK  \n", 33, 42);
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.EXTRA_TEXT, "<pre>Hurraa <ugu>\nrabotaet</pre>", 42,
        74);
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
    assertLexer(lexer, MindMapLexer.TokenType.ATTRIBUTE, "> hello='world'", 10,
        testString.length());
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertNull(lexer.getTokenType());
  }

  @Test
  public void testTwoPhaseReading_Topic() {
    final MindMapLexer lexer = new MindMapLexer();
    final String testString = "Header\n--\n# Topic name";
    lexer.start(testString, 0, 14, MindMapLexer.TokenType.HEAD_LINE);
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.HEAD_LINE, "Header\n", 0, 7);
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.HEAD_DELIMITER, "--\n", 7, 10);

    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.TOPIC_LEVEL, "# ", 10, 12);

    lexer.advance();
    assertFalse(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.TOPIC_TITLE, "To", 12, 14);

    lexer.setBufferEndOffset(testString.length());
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.TOPIC_TITLE, "Topic name", 12, 22);
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
    assertLexer(lexer, MindMapLexer.TokenType.TOPIC_LEVEL, "# ", 10, 12);

    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.TOPIC_TITLE, "\\#Topic name", 12, 24);
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
    assertLexer(lexer, MindMapLexer.TokenType.EXTRA_TEXT, "<pre>Hello world</pre>", 10,
        testString.length());
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
    assertLexer(lexer, MindMapLexer.TokenType.UNKNOWN_LINE, "<pra>Hello world</pre>", 10,
        testString.length());
    lexer.advance();
    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertNull(lexer.getTokenType());
  }

  @Test
  public void testTwoPhaseReading_CodeSnippetInTheEnd_NoNextLine() {
    final MindMapLexer lexer = new MindMapLexer();
    final String testString =
        "Header\n--\n```Java\nSystem.out.println(\"Hello world\");\nSystem.exit(0);\n```";

    lexer.start(testString, 0, testString.length(), MindMapLexer.TokenType.HEAD_LINE);
    lexer.advance();

    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.HEAD_LINE, "Header\n", 0, 7);
    lexer.advance();

    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.HEAD_DELIMITER, "--\n", 7, 10);
    lexer.advance();

    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.CODE_SNIPPET_START, "```Java\n", 10, 18);
    lexer.advance();

    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.CODE_SNIPPET_BODY,
        "System.out.println(\"Hello world\");\nSystem.exit(0);\n", 18, 69);
    lexer.advance();

    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.CODE_SNIPPET_END, "```", 69, 72);
    lexer.advance();
  }

  @Test
  public void testTwoPhaseReading_CodeSnippetInTheEnd_NextLine() {
    final MindMapLexer lexer = new MindMapLexer();
    final String testString =
        "Header\n--\n```Java\nSystem.out.println(\"Hello world\");\nSystem.exit(0);\n```\n";

    lexer.start(testString, 0, testString.length(), MindMapLexer.TokenType.HEAD_LINE);
    lexer.advance();

    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.HEAD_LINE, "Header\n", 0, 7);
    lexer.advance();

    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.HEAD_DELIMITER, "--\n", 7, 10);
    lexer.advance();

    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.CODE_SNIPPET_START, "```Java\n", 10, 18);
    lexer.advance();

    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.CODE_SNIPPET_BODY,
        "System.out.println(\"Hello world\");\nSystem.exit(0);\n", 18, 69);
    lexer.advance();

    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.CODE_SNIPPET_END, "```\n", 69, 73);
    lexer.advance();
  }

  @Test
  public void testTwoPhaseReading_CodeSnippet_NotClosed() {
    final MindMapLexer lexer = new MindMapLexer();
    final String testString = "Header\n--\n```Java\nSystem.exit(0);\n";

    lexer.start(testString, 0, testString.length(), MindMapLexer.TokenType.HEAD_LINE);
    lexer.advance();

    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.HEAD_LINE, "Header\n", 0, 7);
    lexer.advance();

    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.HEAD_DELIMITER, "--\n", 7, 10);
    lexer.advance();

    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.CODE_SNIPPET_START, "```Java\n", 10, 18);
    lexer.advance();

    assertFalse(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.CODE_SNIPPET_BODY, "System.exit(0);\n", 18, 34);
    lexer.advance();

    assertFalse(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.CODE_SNIPPET_BODY, "System.exit(0);\n", 18, 34);
    lexer.advance();

    assertFalse(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.CODE_SNIPPET_BODY, "System.exit(0);\n", 18, 34);
    lexer.advance();
  }

  @Test
  public void testTwoPhaseReading_CodeSnippetInTheEnd_TopicAfterCodeSnipet() {
    final MindMapLexer lexer = new MindMapLexer();
    final String testString =
        "Header\n--\n```Java\nSystem.out.println(\"Hello world\");\nSystem.exit(0);\n```\n# Topic\n```Basic\n``` `\n```\n```Empty\n```";

    lexer.start(testString, 0, testString.length(), MindMapLexer.TokenType.HEAD_LINE);
    lexer.advance();

    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.HEAD_LINE, "Header\n", 0, 7);
    lexer.advance();

    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.HEAD_DELIMITER, "--\n", 7, 10);
    lexer.advance();

    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.CODE_SNIPPET_START, "```Java\n", 10, 18);
    lexer.advance();

    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.CODE_SNIPPET_BODY,
        "System.out.println(\"Hello world\");\nSystem.exit(0);\n", 18, 69);
    lexer.advance();

    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.CODE_SNIPPET_END, "```\n", 69, 73);
    lexer.advance();

    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.TOPIC_LEVEL, "# ", 73, 75);
    lexer.advance();

    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.TOPIC_TITLE, "Topic\n", 75, 81);
    lexer.advance();

    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.CODE_SNIPPET_START, "```Basic\n", 81, 90);
    lexer.advance();

    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.CODE_SNIPPET_BODY, "``` `\n", 90, 96);
    lexer.advance();

    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.CODE_SNIPPET_END, "```\n", 96, 100);
    lexer.advance();

    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.CODE_SNIPPET_START, "```Empty\n", 100, 109);
    lexer.advance();

    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.CODE_SNIPPET_END, "```", 109, 112);
    lexer.advance();

  }

  @Test
  public void testEmptyTitleWithoutAnyFollowingEmptyLine() throws Exception {
    final MindMapLexer lexer = new MindMapLexer();
    final String text = "MMM\n---\n\n#\n> fillColor=`#FF00FF`";

    lexer.start(text, 0, text.length(), TokenType.HEAD_LINE);
    lexer.advance();

    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.HEAD_LINE, "MMM\n", 0, 4);
    lexer.advance();

    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, MindMapLexer.TokenType.HEAD_DELIMITER, "---\n", 4, 8);
    lexer.advance();

    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, TokenType.WHITESPACE, "\n", 8, 9);
    lexer.advance();

    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, TokenType.TOPIC_LEVEL, "#", 9, 10);
    lexer.advance();

    assertTrue(lexer.getCurrentPosition().isTokenCompleted());
    assertLexer(lexer, TokenType.TOPIC_TITLE, "\n", 10, 11);
    lexer.advance();

  }

  @Test
  public void testTextSplittingForLostOrDuplicatedTokens() throws Exception {
    final String etalon =
        IOUtils.toString(MindMapLexerTest.class.getResourceAsStream("cancer_risk.mmd"),
            StandardCharsets.UTF_8);

    final StringBuilder accum1 = new StringBuilder();
    final StringBuilder accum2 = new StringBuilder();
    final StringBuilder accum3 = new StringBuilder();

    final MindMapLexer lexer = new MindMapLexer();
    lexer.start(etalon, 0, etalon.length(), MindMapLexer.TokenType.HEAD_LINE);
    lexer.advance();

    int prevEnd = 0;

    while (true) {
      final TokenType type = lexer.getTokenType();
      if (type == null) {
        break;
      }

      assertEquals(prevEnd, lexer.getTokenStartOffset());
      prevEnd = lexer.getTokenEndOffset();
//      assertNotEquals("Unknown line : "+lexer.getTokenText(),TokenType.UNKNOWN_LINE, type);

      accum1.append(etalon, lexer.getTokenStartOffset(), lexer.getTokenEndOffset());
      accum2.append(lexer.getTokenText());
      accum3.append(lexer.getTokenSequence());
      lexer.advance();
    }

    assertEquals(etalon, accum1.toString());
    assertEquals(etalon, accum2.toString());
    assertEquals(etalon, accum3.toString());
  }

}
