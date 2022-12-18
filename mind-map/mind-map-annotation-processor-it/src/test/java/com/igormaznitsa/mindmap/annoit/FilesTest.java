package com.igormaznitsa.mindmap.annoit;

import com.igormaznitsa.mindmap.model.MindMap;
import org.junit.Assert;
import org.junit.Test;

public class FilesTest extends AbstractMmdTest {
  @Test
  public void testCreateFilesAndAddressing() throws Exception {
    final MindMap klass1map = loadMindMap("com/igormaznitsa/mindmap/annoit/files/Klass1.mmd");
    final MindMap klass2map = loadMindMap("com/igormaznitsa/mindmap/annoit/files/Klass2.mmd");
    final MindMap klass3map = loadMindMap("com/igormaznitsa/mindmap/annoit/files/Klass3.mmd");
    final MindMap klass3_1map = loadMindMap("com/igormaznitsa/mindmap/annoit/files/Klass3_1.mmd");

    findForTitle(klass2map, "klass4_method");
    findForTitle(klass1map, "klass4_method_class1");
    findForTitle(klass3map, "a");
    findForTitle(klass3_1map, "b");
    Assert.assertEquals(2, findForTitle(klass2map, "Controller Start-Stop").size());
    findForPath(klass2map, "Klass2", "Controller Start-Stop", "start");
    findForPath(klass2map, "Klass2", "Controller Start-Stop", "stop");
  }
}
