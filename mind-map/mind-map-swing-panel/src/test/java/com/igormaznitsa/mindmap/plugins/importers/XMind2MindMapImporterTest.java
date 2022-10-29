package com.igormaznitsa.mindmap.plugins.importers;

import static org.junit.Assert.assertEquals;

import com.igormaznitsa.mindmap.model.MindMap;
import java.util.zip.ZipFile;
import org.junit.Test;

public class XMind2MindMapImporterTest extends AbstractImporterTest {

  private static final XMind2MindMapImporter INSTANCE = new XMind2MindMapImporter();

  private ZipFile findZip(final String resource) throws Exception {
    return new ZipFile(findFile(resource));
  }

  @Test
  public void testXMindOld() throws Exception {
    final MindMap parsed = INSTANCE.parseZipFile(findZip("xmindOld.xmind"));
    assertEquals("Avoid \nFocus-Stealing \nTraps", parsed.getRoot().getText());
    assertEquals(6, parsed.getRoot().getChildren().size());
  }

  @Test
  public void testXMind2020() throws Exception {
    final MindMap parsed = INSTANCE.parseZipFile(findZip("xmind2020.xmind"));
    assertEquals("Central Topic", parsed.getRoot().getText());
    assertEquals(4, parsed.getRoot().getChildren().size());
  }

}