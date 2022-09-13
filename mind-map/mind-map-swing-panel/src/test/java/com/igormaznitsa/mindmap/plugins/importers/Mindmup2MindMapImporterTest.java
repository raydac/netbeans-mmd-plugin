package com.igormaznitsa.mindmap.plugins.importers;

import static com.igormaznitsa.meta.common.utils.Assertions.assertNotNull;
import static org.junit.Assert.assertEquals;

import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.Topic;
import org.junit.Test;

public class Mindmup2MindMapImporterTest extends AbstractImporterTest {
  private static final Mindmup2MindMapImporter INSTANCE = new Mindmup2MindMapImporter();

  @Test
  public void testParseFile() throws Exception {
    final MindMap map = INSTANCE.doImportFile(findFile("MindMup.mup"));

    final Topic root = assertNotNull(map.getRoot());
    assertEquals("root panel", root.getText());
    assertEquals(5, root.getChildren().size());
  }

}