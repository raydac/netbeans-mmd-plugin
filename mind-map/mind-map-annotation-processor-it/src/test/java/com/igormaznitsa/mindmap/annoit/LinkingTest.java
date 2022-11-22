package com.igormaznitsa.mindmap.annoit;

import com.igormaznitsa.mindmap.model.MindMap;
import org.junit.Test;

public class LinkingTest extends AbstractMmdTest {
  @Test
  public void testInterfaces() throws Exception {
    final MindMap map =
        loadMindMap("com/igormaznitsa/mindmap/annoit/linking/interfaces/RootInterface.mmd");
    assertTopicPath(findForTitle(map, "method3_1"), "RootInterface", "Interface1", "Interface2",
        "Interface3", "method3_1");
  }
}