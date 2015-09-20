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
package com.igormaznitsa.nbmindmap.nb.refactoring.elements;

import org.junit.Test;
import static org.junit.Assert.*;

public class RenameFileActionPluginTest {
  
  @Test
  public void testSomeMethod() {
    assertEquals("/hello",RenameFileActionPlugin.replaceNameInPath(0, "/", "hello"));
    assertEquals("/hello/world",RenameFileActionPlugin.replaceNameInPath(1, "/gala/world", "hello"));
    assertEquals("smart/est/hello/world",RenameFileActionPlugin.replaceNameInPath(2, "smart/homo/hello/world", "est"));
    assertEquals("c:\\smartest\\est\\hello\\world.txt",RenameFileActionPlugin.replaceNameInPath(2, "c:\\smartest\\lskjdflkdsjfwioeqwoieqwlkdlkd\\hello\\world.txt", "est"));
    assertEquals("zomby",RenameFileActionPlugin.replaceNameInPath(2, "zomby", "est"));
    assertEquals("",RenameFileActionPlugin.replaceNameInPath(0, "", "est"));
    
    assertEquals("/hello/world/test.txt",RenameFileActionPlugin.replaceNameInPath(1, "/hello/some/test.txt", "world"));
    assertEquals("/aha/world/test.txt",RenameFileActionPlugin.replaceNameInPath(1, "/hello/some/test.txt", "aha/world"));
    assertEquals("/hello/aha/world1/test.txt",RenameFileActionPlugin.replaceNameInPath(1, "/hello/aha/world/test.txt", "aha/world1"));
  }
  
}
