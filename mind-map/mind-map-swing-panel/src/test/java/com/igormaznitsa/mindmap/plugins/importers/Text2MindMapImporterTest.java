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

package com.igormaznitsa.mindmap.plugins.importers;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.Topic;
import java.io.File;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class Text2MindMapImporterTest {
  private static final Text2MindMapImporter INSTANCE = new Text2MindMapImporter();

  @Test
  public void testDoImport_Empty() throws Exception {
    final MindMap result = INSTANCE.makeFromLines(asList("          "));
    assertNull(result.getRoot());
  }

  @Test
  public void testDoImport_OnlyRoot() throws Exception {
    final MindMap result = INSTANCE.makeFromLines(asList("\tSolar system   "));
    assertEquals("Solar system", result.getRoot().getText());
  }

  @Test
  public void testDoImport_Multilevel() throws Exception {
    final MindMap result = INSTANCE.makeFromLines(asList("Solar system", "\tMercury", "\tVenus", "\tEarth", "\t\tMoon", "\tMars", "\t\tFobos", "\t\tDemos", "Jupiter"));
    assertEquals("Solar system", result.getRoot().getText());
    assertEquals(5, result.getRoot().getChildren().size());
    final Topic mars = result.getRoot().getChildren().get(3);
    final Topic jupiter = result.getRoot().getChildren().get(4);
    assertEquals("Mars", mars.getText());
    assertEquals(2, mars.getChildren().size());
    assertEquals("Jupiter", jupiter.getText());
  }


  @Test
  public void testDoImport_Multilevel2() throws Exception {
    final MindMap result = INSTANCE.makeFromLines(asList("solar system", "\tjupiter", "\tmars", " \t\tfobos", "\t\tdeimos", "\tpluto", "\tsaturn"));
    assertEquals("solar system", result.getRoot().getText());
    assertEquals(4, result.getRoot().getChildren().size());
    final Topic root = result.getRoot();
    assertEquals("jupiter", root.getChildren().get(0).getText());
    assertEquals("mars", root.getChildren().get(1).getText());
    assertEquals("pluto", root.getChildren().get(2).getText());
    assertEquals("saturn", root.getChildren().get(3).getText());
    final Topic mars = result.getRoot().getChildren().get(1);
    assertEquals(2, mars.getChildren().size());
    assertEquals("fobos", mars.getChildren().get(0).getText());
    assertEquals("deimos", mars.getChildren().get(1).getText());
  }

  @Test
  public void testImportFromFile() throws Exception {
    final File file = new File(Text2MindMapImporter.class.getResource("tabbedtext.txt").getFile());
    assertTrue(file.isFile());
    final List<String> lines = FileUtils.readLines(file, "UTF-8");
    final MindMap result = INSTANCE.makeFromLines(lines);
    assertEquals(5, result.getRoot().getChildren().size());
  }

}
