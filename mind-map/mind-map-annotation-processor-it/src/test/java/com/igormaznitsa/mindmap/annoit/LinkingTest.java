package com.igormaznitsa.mindmap.annoit;

import static org.junit.Assert.assertEquals;

import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.Topic;
import org.junit.Test;

public class LinkingTest extends AbstractMmdTest {
  @Test
  public void testInterfaces() throws Exception {
    final MindMap map =
        loadMindMap("com/igormaznitsa/mindmap/annoit/linking/interfaces/RootInterface.mmd");
    assertTopicPath(findForTitle(map, "method3_1"), "RootInterface", "Interface1", "Interface2",
        "Interface3", "method3_1");
    assertTopicJumpTo(findForPath(map, "RootInterface", "Interface1", "method2"),
        findForPath(map, "RootInterface"));
    assertTopicJumpTo(findForPath(map, "RootInterface"),
        findForPath(map, "RootInterface", "Interface1", "Interface2", "Interface3", "method3_1"));
    assertTopicJumpTo(findForPath(map, "RootInterface", "Interface1", "method1"),
        findForPath(map, "RootInterface", "Interface1", "Interface2", "Klazz1", "method2"));
  }

  @Test
  public void testMultipleTopicsForElement() throws Exception {
    final MindMap map =
        loadMindMap("com/igormaznitsa/mindmap/annoit/linking/interfaces/RootInterface.mmd");

    final Topic multi1 =
        findForPath(map, "RootInterface", "Interface1", "Interface2", "Klazz1", "multi1");
    final Topic multi2 =
        findForPath(map, "RootInterface", "Interface1", "Interface2", "Klazz1", "multi2");
    final Topic multi3 =
        findForPath(map, "RootInterface", "Interface1", "Interface2", "Klazz1", "multi3");

    assertEquals(multi1.getExtras().get(Extra.ExtraType.FILE),
        multi2.getExtras().get(Extra.ExtraType.FILE));
    assertEquals(multi2.getExtras().get(Extra.ExtraType.FILE),
        multi3.getExtras().get(Extra.ExtraType.FILE));
  }
}
