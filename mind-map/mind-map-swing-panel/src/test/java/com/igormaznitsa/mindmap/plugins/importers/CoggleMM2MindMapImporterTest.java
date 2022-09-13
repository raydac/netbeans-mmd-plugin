package com.igormaznitsa.mindmap.plugins.importers;


import static com.igormaznitsa.meta.common.utils.Assertions.assertNotNull;
import static org.junit.Assert.assertEquals;

import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.Topic;
import org.junit.Test;

public class CoggleMM2MindMapImporterTest extends AbstractImporterTest {
  private static final CoggleMM2MindMapImporter INSTANCE = new CoggleMM2MindMapImporter();

  @Test
  public void testParseFile() throws Exception {
    final MindMap map = INSTANCE.doImportFileAsMap(findFile("Coggle.mm"));

    final Topic root = assertNotNull(map.getRoot());
    assertEquals("GILGAMESH  ", root.getText());
    assertEquals(6, root.getChildren().size());
  }

}