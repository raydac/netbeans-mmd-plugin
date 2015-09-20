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

import java.io.File;
import org.junit.Test;
import static org.junit.Assert.*;

public class ExtraFileTest {
  @Test
  public void testHasParent() throws Exception {
    assertFalse(new ExtraFile("file:///item1/item2/item2.txt").hasParent(null, new MMapURI("file:///item1/item2/item2.txt")));
    assertTrue(new ExtraFile("file:///item1/item2/item2.txt").hasParent(null, new MMapURI("file:///item1/item2")));
    assertTrue(new ExtraFile("file:///item1/item2/item2.txt").hasParent(null, new MMapURI("file:///item1/")));
    assertTrue(new ExtraFile("file:///item1/item2/item2.txt").hasParent(null, new MMapURI("file:///")));

    assertFalse(new ExtraFile("file:///item1/item2/item2.txt").hasParent(null, new MMapURI("file:///item3/item2/item2.txt")));
    assertFalse(new ExtraFile("file:///item1/item2/item2.txt").hasParent(null, new MMapURI("file:///item1/item2/item2.tx")));
    assertFalse(new ExtraFile("file:///item1/item2/item2.txt").hasParent(null, new MMapURI("file:///item3/item2")));
    assertFalse(new ExtraFile("file:///item1/item2/item2.txt").hasParent(null, new MMapURI("file:///item3")));
    assertFalse(new ExtraFile("file:///item1/item2/item2.txt").hasParent(null, new MMapURI("file:///item")));
    assertFalse(new ExtraFile("file:///item1/item2.txt").hasParent(null, new MMapURI("file:///item1/item2/")));

    final File base = new File("/some/base");

    assertFalse(new ExtraFile(MMapURI.makeFromFilePath(base, "chunga/changa.txt", null)).hasParent(base, new MMapURI(base, new File("chunga/changa.txt"), null)));
    assertFalse(new ExtraFile(MMapURI.makeFromFilePath(base, "chunga/changa.txt", null)).hasParent(base, new MMapURI(base, new File("chunga/changa.tx"), null)));
    assertFalse(new ExtraFile(MMapURI.makeFromFilePath(base, "chunga/changa.tx", null)).hasParent(base, new MMapURI(base, new File("chunga/changa.txt"), null)));
    assertTrue(new ExtraFile(MMapURI.makeFromFilePath(base, "chunga/changa.tx", null)).hasParent(base, new MMapURI(base, new File("chunga"), null)));

    assertTrue(new ExtraFile(MMapURI.makeFromFilePath(base, "chunga/changa.txt", null)).hasParent(base, new MMapURI("file:///some/base/chunga")));
  }
  
  @Test
  public void testIsSame() throws Exception {
    assertTrue(new ExtraFile("file:///item1/item2/item2.txt").isSame(null, new MMapURI("file:///item1/item2/item2.txt")));
    assertFalse(new ExtraFile("file:///item1/item2/item2.txt").isSame(null, new MMapURI("file:///item1/item2")));
    assertFalse(new ExtraFile("file:///item1/item2/item2.txt").isSame(null, new MMapURI("file:///item1/")));
    assertFalse(new ExtraFile("file:///item1/item2/item2.txt").isSame(null, new MMapURI("file:///")));

    assertFalse(new ExtraFile("file:///item1/item2/item2.txt").isSame(null, new MMapURI("file:///item3/item2/item2.txt")));
    assertFalse(new ExtraFile("file:///item1/item2/item2.txt").isSame(null, new MMapURI("file:///item1/item2/item2.tx")));
    assertFalse(new ExtraFile("file:///item1/item2/item2.txt").isSame(null, new MMapURI("file:///item3/item2")));
    assertFalse(new ExtraFile("file:///item1/item2/item2.txt").isSame(null, new MMapURI("file:///item3")));
    assertFalse(new ExtraFile("file:///item1/item2/item2.txt").isSame(null, new MMapURI("file:///item")));
    assertFalse(new ExtraFile("file:///item1/item2.txt").isSame(null, new MMapURI("file:///item1/item2/")));

    final File base = new File("/some/base");

    assertTrue(new ExtraFile(MMapURI.makeFromFilePath(base, "chunga/changa.txt", null)).isSame(base, new MMapURI(base, new File("chunga/changa.txt"), null)));
    assertFalse(new ExtraFile(MMapURI.makeFromFilePath(base, "chunga/changa.txt", null)).isSame(base, new MMapURI(base, new File("chunga/changa.tx"), null)));
    assertFalse(new ExtraFile(MMapURI.makeFromFilePath(base, "chunga/changa.tx", null)).isSame(base, new MMapURI(base, new File("chunga/changa.txt"), null)));
    assertFalse(new ExtraFile(MMapURI.makeFromFilePath(base, "chunga/changa.tx", null)).isSame(base, new MMapURI(base, new File("chunga"), null)));

    assertTrue(new ExtraFile(MMapURI.makeFromFilePath(base, "chunga/changa.txt", null)).isSame(base, new MMapURI("file:///some/base/chunga/changa.txt")));
  }
  
  @Test
  public void testIsSameOrHasParent() throws Exception {
    assertTrue(new ExtraFile("file:///item1/item2/item2.txt").isSameOrHasParent(null, new MMapURI("file:///item1/item2/item2.txt")));
    assertTrue(new ExtraFile("file:///item1/item2/item2.txt").isSameOrHasParent(null, new MMapURI("file:///item1/item2")));
    assertTrue(new ExtraFile("file:///item1/item2/item2.txt").isSameOrHasParent(null, new MMapURI("file:///item1/")));
    assertTrue(new ExtraFile("file:///item1/item2/item2.txt").isSameOrHasParent(null, new MMapURI("file:///")));
    
    assertFalse(new ExtraFile("file:///item1/item2/item2.txt").isSameOrHasParent(null, new MMapURI("file:///item3/item2/item2.txt")));
    assertFalse(new ExtraFile("file:///item1/item2/item2.txt").isSameOrHasParent(null, new MMapURI("file:///item1/item2/item2.tx")));
    assertFalse(new ExtraFile("file:///item1/item2/item2.txt").isSameOrHasParent(null, new MMapURI("file:///item3/item2")));
    assertFalse(new ExtraFile("file:///item1/item2/item2.txt").isSameOrHasParent(null, new MMapURI("file:///item3")));
    assertFalse(new ExtraFile("file:///item1/item2/item2.txt").isSameOrHasParent(null, new MMapURI("file:///item")));
    assertFalse(new ExtraFile("file:///item1/item2.txt").isSameOrHasParent(null, new MMapURI("file:///item1/item2/")));
 
    final File base = new File("/some/base");
    
    assertTrue(new ExtraFile(MMapURI.makeFromFilePath(base, "chunga/changa.txt", null)).isSameOrHasParent(base, new MMapURI(base,new File("chunga/changa.txt"),null)));
    assertFalse(new ExtraFile(MMapURI.makeFromFilePath(base, "chunga/changa.txt", null)).isSameOrHasParent(base, new MMapURI(base,new File("chunga/changa.tx"),null)));
    assertFalse(new ExtraFile(MMapURI.makeFromFilePath(base, "chunga/changa.tx", null)).isSameOrHasParent(base, new MMapURI(base,new File("chunga/changa.txt"),null)));
    
    assertTrue(new ExtraFile(MMapURI.makeFromFilePath(base, "chunga/changa.txt", null)).isSameOrHasParent(base, new MMapURI("file:///some/base/chunga/changa.txt")));
  }
}
