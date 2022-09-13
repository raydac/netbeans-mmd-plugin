package com.igormaznitsa.mindmap.plugins.importers;

import com.igormaznitsa.meta.common.utils.Assertions;
import com.igormaznitsa.mindmap.model.MindMap;
import java.io.File;
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
    Assertions.assertEquals("Avoid \nFocus-Stealing \nTraps", parsed.getRoot().getText());
    Assertions.assertEquals(6, parsed.getRoot().getChildren().size());
  }

  @Test
  public void testXMind2020() throws Exception {
    final MindMap parsed = INSTANCE.parseZipFile(findZip("xmind2020.xmind"));
    Assertions.assertEquals("Central Topic", parsed.getRoot().getText());
    Assertions.assertEquals(4, parsed.getRoot().getChildren().size());
  }

}