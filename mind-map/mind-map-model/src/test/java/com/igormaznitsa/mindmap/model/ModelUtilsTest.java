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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;
import java.util.Properties;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.junit.Test;

public class ModelUtilsTest {

  private void assertEscapeUnescapePre(final String text) {
    assertEquals(text, StringEscapeUtils.unescapeHtml3(ModelUtils.escapeTextForPreBlock(text)));
  }
  
  @Test
  public void testEscapeTextForPreBlock() {
    assertEscapeUnescapePre("");
    assertEscapeUnescapePre("a");
    assertEscapeUnescapePre("a b");
    assertEscapeUnescapePre("абвг");
    assertEscapeUnescapePre("абвг\niuweyqqw123123");
    assertEscapeUnescapePre("<pre>123</pre>");
    assertEscapeUnescapePre("&#32;");
    assertEscapeUnescapePre("123 456 \r \n \t \b <html>``` some");
  }
  
  @Test
  public void testExtractQueryParameters() throws Exception {
    final Properties properties = ModelUtils.extractQueryPropertiesFromURI(new URI("file://hello?some=test&other=&misc=%26ffsdsd&h=1"));
    assertEquals(4, properties.size());
    assertEquals("test", properties.get("some"));
    assertEquals("", properties.get("other"));
    assertEquals("&ffsdsd", properties.get("misc"));
    assertEquals("1", properties.get("h"));
  }

  @Test
  public void testExtractQueryParameters_Empty() throws Exception {
    final Properties properties = ModelUtils.extractQueryPropertiesFromURI(new URI("file://hello"));
    assertTrue(properties.isEmpty());
  }

  @Test
  public void testMakeQueryStringForURI() {
    final Properties props = new Properties();
    assertEquals("", ModelUtils.makeQueryStringForURI(null));
    assertEquals("", ModelUtils.makeQueryStringForURI(props));
    props.put("test", "hello test");
    assertEquals("test=hello+test", ModelUtils.makeQueryStringForURI(props));
    props.put("&key", "&value some");
    assertEquals("%26key=%26value+some&test=hello+test", ModelUtils.makeQueryStringForURI(props));
  }

  
  @Test
  public void testCalcMaxLengthOfBacktickQuotesSubstr() {
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
    assertEquals("Hello\nWorld", ModelUtils.unescapeMarkdown("Hello<br>World"));
    assertEquals("<>\n", ModelUtils.unescapeMarkdown("\\<\\><br>"));
    assertEquals("\\`*_{}[]()#<>+-.!\n",
        ModelUtils.unescapeMarkdown("\\\\\\`\\*\\_\\{\\}\\[\\]\\(\\)\\#\\<\\>\\+\\-\\.\\!<br/>"));
    assertEquals("Hello `<\nWorld>`", ModelUtils.unescapeMarkdown("Hello \\`<<br/>World\\>\\`"));
    assertEquals("", ModelUtils.unescapeMarkdown(""));
  }

  @Test
  public void testEscapeMarkdownStr() {
    assertEquals("Hello<br/>World", ModelUtils.escapeMarkdown("Hello\nWorld"));
    assertEquals("\\\\\\`\\*\\_\\{\\}\\[\\]\\(\\)\\#\\<\\>\\+\\-\\.\\!<br/>",
        ModelUtils.escapeMarkdown("\\`*_{}[]()#<>+-.!\n"));
    assertEquals("Hello \\`\\<<br/>World\\>\\`", ModelUtils.escapeMarkdown("Hello `<\nWorld>`"));
    assertEquals("", ModelUtils.escapeMarkdown(""));
  }

  @Test
  public void testMakeFileForPath() throws Exception {
    assertNull(ModelUtils.makeFileForPath(null));
    assertNull(ModelUtils.makeFileForPath(""));

    assertEquals(new File((File) null, "/some/who/files/2012-11-02 13.47.10.jpg").getCanonicalPath(), ModelUtils.makeFileForPath("file:///some/who/files/2012-11-02 13.47.10.jpg").getAbsolutePath());
    assertEquals(new File((File) null, "/some/who/files/2012-11-02 13.47.10.jpg").getCanonicalPath(), ModelUtils.makeFileForPath("file:///some/who/files/2012-11-02%2013.47.10.jpg").getAbsolutePath());
    assertEquals(new File((File) null, "/some/who/files/main.c++").getCanonicalPath(), ModelUtils.makeFileForPath("file:///some/who/files/main.c++").getAbsolutePath());

    assertEquals(new File((File) null, "/some/folder/temp/").getCanonicalPath(), ModelUtils.makeFileForPath("file:///some/folder/temp/").getAbsolutePath());

    if (SystemUtils.IS_OS_LINUX) {
      assertEquals(new File((File) null, "/some/folder/temp/ :<>?").getCanonicalPath(), ModelUtils.makeFileForPath("file:///some/folder/temp/ :<>?").getAbsolutePath());
      assertEquals(new File((File) null, "/some/folder&ssd/temp/ :<>?test=jks&lls=1").getCanonicalPath(), ModelUtils.makeFileForPath("file:///some/folder&ssd/temp/ :<>?test=jks&lls=1").getAbsolutePath());
    }
    assertEquals("src/main/java/com/igormaznitsa/nbmindmap/nb/QuickSearchProvider.java".replace('/', File.separatorChar), ModelUtils.makeFileForPath("src/main/java/com/igormaznitsa/nbmindmap/nb/QuickSearchProvider.java").getPath());
  }

  @Test
  public void testToFile() throws Exception{
    if (SystemUtils.IS_OS_WINDOWS)
      assertEquals("P:\\Some text document.txt",new MMapURI("file://P:/Some%20text%20document.txt").asFile(null).getAbsolutePath());
    else
      assertEquals("/Some text document.txt",new MMapURI("file:///Some%20text%20document.txt").asFile(null).getAbsolutePath());
  }
  
}
