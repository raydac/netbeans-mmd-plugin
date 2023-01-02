package com.igormaznitsa.mindmap.annoit;

import com.igormaznitsa.mindmap.model.MindMap;
import org.junit.Test;

public class CollectorsTest extends AbstractMmdTest {
  @Test
  public void testCollectors() throws Exception {
    final MindMap map1 =
        this.loadMindMap("com/igormaznitsa/mindmap/annoit/collectors/FileOne.mmd");
    final MindMap map2 =
        this.loadMindMap("com/igormaznitsa/mindmap/annoit/collectors/FileTwo.mmd");
    findForPath(map1, "Root", "Topic1");
    findForPath(map2, "Root", "Topic2");
  }

}
