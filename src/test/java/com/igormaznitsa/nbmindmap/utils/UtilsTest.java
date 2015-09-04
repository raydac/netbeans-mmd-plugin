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
package com.igormaznitsa.nbmindmap.utils;

import org.junit.Test;
import static org.junit.Assert.*;

public class UtilsTest {

  @Test
  public void testMakeFileForPath() throws Exception {
    assertNull(Utils.makeFileForPath(null));
    assertNull(Utils.makeFileForPath(""));
    assertEquals("/some/who/files/2012-11-02 13.47.10.jpg", Utils.makeFileForPath("file:///some/who/files/2012-11-02 13.47.10.jpg").getAbsolutePath());
    assertEquals("/some/who/files/2012-11-02 13.47.10.jpg", Utils.makeFileForPath("file:///some/who/files/2012-11-02%2013.47.10.jpg").getAbsolutePath());
    assertEquals("/some/who/files/main.c++", Utils.makeFileForPath("file:///some/who/files/main.c++").getAbsolutePath());
    assertEquals("/some/folder/temp/<: ,,,, ... main.c++ >", Utils.makeFileForPath("file:///some/folder/temp/<: ,,,, ... main.c++ >").getAbsolutePath());
    assertEquals("/some/folder/temp/ :<>?", Utils.makeFileForPath("file:///some/folder/temp/ :<>?").getAbsolutePath());
  }

  @Test
  public void testCalcMaxLengthOfBacktickQuotesSubstr() throws Exception {
    assertEquals(0, Utils.calcMaxLengthOfBacktickQuotesSubstr("akldjf lsdkjf"));
    assertEquals(0, Utils.calcMaxLengthOfBacktickQuotesSubstr(null));
    assertEquals(1, Utils.calcMaxLengthOfBacktickQuotesSubstr("akl`djf lsdk`jf"));
    assertEquals(1, Utils.calcMaxLengthOfBacktickQuotesSubstr("akl`djf lsdk`\n`jf"));
    assertEquals(2, Utils.calcMaxLengthOfBacktickQuotesSubstr("akl`djf lsdk``jf"));
    assertEquals(3, Utils.calcMaxLengthOfBacktickQuotesSubstr("akl`djf lsdk``jf```"));
    assertEquals(3, Utils.calcMaxLengthOfBacktickQuotesSubstr("```"));
  }

  @Test
  public void testUnescapeMarkdownStr() {
    assertEquals("Hello\nWorld", Utils.unescapeMarkdownStr("Hello<br>World"));
    assertEquals("<>\n", Utils.unescapeMarkdownStr("\\<\\><br>"));
    assertEquals("\\`*_{}[]()#<>+-.!\n", Utils.unescapeMarkdownStr("\\\\\\`\\*\\_\\{\\}\\[\\]\\(\\)\\#\\<\\>\\+\\-\\.\\!<br/>"));
    assertEquals("Hello `<\nWorld>`", Utils.unescapeMarkdownStr("Hello \\`<<br/>World\\>\\`"));
    assertEquals("", Utils.unescapeMarkdownStr(""));
  }

  @Test
  public void testEscapeMarkdownStr() {
    assertEquals("Hello<br/>World", Utils.escapeMarkdownStr("Hello\nWorld"));
    assertEquals("\\\\\\`\\*\\_\\{\\}\\[\\]\\(\\)\\#\\<\\>\\+\\-\\.\\!<br/>", Utils.escapeMarkdownStr("\\`*_{}[]()#<>+-.!\n"));
    assertEquals("Hello \\`\\<<br/>World\\>\\`", Utils.escapeMarkdownStr("Hello `<\nWorld>`"));
    assertEquals("", Utils.escapeMarkdownStr(""));
  }

}
