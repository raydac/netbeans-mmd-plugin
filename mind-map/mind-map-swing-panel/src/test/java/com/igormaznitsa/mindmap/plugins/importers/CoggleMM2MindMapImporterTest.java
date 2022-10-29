package com.igormaznitsa.mindmap.plugins.importers;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.Topic;
import org.junit.Test;

public class CoggleMM2MindMapImporterTest extends AbstractImporterTest {
  private static final CoggleMM2MindMapImporter INSTANCE = new CoggleMM2MindMapImporter();

  @Test
  public void testParseFile() throws Exception {
    final MindMap map = INSTANCE.doImportFile(findFile("Coggle.mm"));

    final Topic root = map.getRoot();
    assertNotNull(root);
    assertEquals("GILGAMESH  ", root.getText());
    assertEquals(6, root.getChildren().size());
  }

}