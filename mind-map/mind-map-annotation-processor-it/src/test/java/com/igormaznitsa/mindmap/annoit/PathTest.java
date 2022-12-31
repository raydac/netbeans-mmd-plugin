package com.igormaznitsa.mindmap.annoit;

import com.igormaznitsa.mindmap.model.MindMap;
import org.junit.Test;

public class PathTest extends AbstractMmdTest {
  @Test
  public void testAttributes() throws Exception {
    final MindMap map =
        this.loadMindMap("com/igormaznitsa/mindmap/annoit/paths/RootFile.mmd");
    findForPath(map, "root", "method1");
    findForPath(map, "root", "a", "b", "c", "method2");
    findForPath(map, "root", "a", "b", "c", "method2");
    findForPath(map, "root", "a", "b", "c", "z", "class1Method1");
    findForPath(map, "root", "a", "b", "c", "d", "xxx");
    findForPath(map, "root", "a", "b", "c", "d", "xxx", "Class2Method");
    findForPath(map, "root", "a", "b", "c", "d", "class1Method3");
    findForPath(map, "root", "a", "class1Method2");
    findForPath(map, "root", "classABC1");
    findForPath(map, "root", "classABC2");
    findForPath(map, "root", "Class3Method");
  }

}
