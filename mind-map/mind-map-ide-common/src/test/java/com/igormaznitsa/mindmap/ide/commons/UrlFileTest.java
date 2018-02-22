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
package com.igormaznitsa.mindmap.ide.commons;

import org.junit.Test;
import static org.junit.Assert.*;

public class UrlFileTest {

  @Test
  public void testParseEmpty() throws Exception {
    assertEquals(0,new UrlFile("").size());
  }

  @Test
  public void testParseOnlySection() throws Exception {
    assertEquals(0,new UrlFile("[InternetShortcut]").size());
  }

  @Test
  public void testParseSectionAndPair() throws Exception {
    assertEquals(1,new UrlFile("[InternetShortcut]\r\nURL=http://www.someaddress.com/").size());
  }

  @Test
  public void testExtractData() throws Exception {
    final UrlFile file = new UrlFile("[InternetShortcut]\r\n"
        + "URL=http://www.someaddress.com\r\n"
        + "WorkingDirectory=C:\\WINDOWS\\\r\n"
        + "ShowCommand=7\r\n"
        + "IconIndex=1\r\n"
        + "\r\n"
        + "IconFile=C:\\WINDOWS\\SYSTEM\\url.dll\r\n"
        + "Modified=20F06BA06D07BD014D\r\n"
        + "HotKey=1601");
    assertEquals(7,file.size());
    
    assertEquals("http://www.someaddress.com",file.getValue("InternetShortcut", "URL"));
    assertEquals("http://www.someaddress.com",file.getURL());
    assertEquals("C:\\WINDOWS\\",file.getValue("InternetShortcut", "WorkingDirectory"));
    assertEquals("1601",file.getValue("InternetShortcut", "HotKey"));
    assertNull(file.getValue("InternetShortcut", "SomeUndefined"));
  }
}
