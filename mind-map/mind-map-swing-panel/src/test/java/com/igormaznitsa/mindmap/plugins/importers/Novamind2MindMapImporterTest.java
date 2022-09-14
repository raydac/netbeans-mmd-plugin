package com.igormaznitsa.mindmap.plugins.importers;

import static com.igormaznitsa.meta.common.utils.Assertions.assertNotNull;
import static org.junit.Assert.assertEquals;

import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.Topic;
import org.junit.Test;

public class Novamind2MindMapImporterTest extends AbstractImporterTest {
  private static final Novamind2MindMapImporter INSTANCE = new Novamind2MindMapImporter();

  @Test
  public void testParseFile() throws Exception {
    final MindMap map = INSTANCE.doImportFile(findFile("Novamind.nm5"));

    final Topic root = assertNotNull(map.getRoot());
    assertEquals("Your Flight Plan", root.getText());
    assertEquals(13, root.getChildren().size());
  }

}