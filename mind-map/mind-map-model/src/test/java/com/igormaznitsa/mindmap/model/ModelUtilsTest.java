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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class ModelUtilsTest {
  @Test
  public void testCalcMaxLengthOfBacktickQuotesSubstr() throws Exception {
    assertEquals(0, ModelUtils.calcMaxLengthOfBacktickQuotesSubstr("akldjf lsdkjf"));
    assertEquals(0, ModelUtils.calcMaxLengthOfBacktickQuotesSubstr(null));
    assertEquals(1, ModelUtils.calcMaxLengthOfBacktickQuotesSubstr("akl`djf lsdk`jf"));
    assertEquals(1, ModelUtils.calcMaxLengthOfBacktickQuotesSubstr("akl`djf lsdk`\n`jf"));
    assertEquals(2, ModelUtils.calcMaxLengthOfBacktickQuotesSubstr("akl`djf lsdk``jf"));
    assertEquals(3, ModelUtils.calcMaxLengthOfBacktickQuotesSubstr("akl`djf lsdk``jf```"));
    assertEquals(3, ModelUtils.calcMaxLengthOfBacktickQuotesSubstr("```"));
  }

  @Test
  public void testUnescapeMarkdownStr() {
    assertEquals("Hello\nWorld", ModelUtils.unescapeMarkdownStr("Hello<br>World"));
    assertEquals("<>\n", ModelUtils.unescapeMarkdownStr("\\<\\><br>"));
    assertEquals("\\`*_{}[]()#<>+-.!\n", ModelUtils.unescapeMarkdownStr("\\\\\\`\\*\\_\\{\\}\\[\\]\\(\\)\\#\\<\\>\\+\\-\\.\\!<br/>"));
    assertEquals("Hello `<\nWorld>`", ModelUtils.unescapeMarkdownStr("Hello \\`<<br/>World\\>\\`"));
    assertEquals("", ModelUtils.unescapeMarkdownStr(""));
  }

  @Test
  public void testEscapeMarkdownStr() {
    assertEquals("Hello<br/>World", ModelUtils.escapeMarkdownStr("Hello\nWorld"));
    assertEquals("\\\\\\`\\*\\_\\{\\}\\[\\]\\(\\)\\#\\<\\>\\+\\-\\.\\!<br/>", ModelUtils.escapeMarkdownStr("\\`*_{}[]()#<>+-.!\n"));
    assertEquals("Hello \\`\\<<br/>World\\>\\`", ModelUtils.escapeMarkdownStr("Hello `<\nWorld>`"));
    assertEquals("", ModelUtils.escapeMarkdownStr(""));
  }

  
}
