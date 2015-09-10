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
package com.igormaznitsa.mindmap.swing.panel.utils;

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
    assertEquals("src/main/java/com/igormaznitsa/nbmindmap/nb/QuickSearchProvider.java", Utils.makeFileForPath("src/main/java/com/igormaznitsa/nbmindmap/nb/QuickSearchProvider.java").getPath());
  }
}
