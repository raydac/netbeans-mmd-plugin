package com.igormaznitsa.mindmap.annoit;

import static org.junit.Assert.assertThrows;

import com.igormaznitsa.mindmap.model.MindMap;
import org.junit.Test;

public class MarkedLocalVariablesTest extends AbstractMmdTest {
  @Test
  public void testCreateLinksForAnnotatedLocalVariables() throws Exception {
    final MindMap map =
        this.loadMindMap("com/igormaznitsa/mindmap/annoit/paths/RootFile.mmd");

    findForPath(map, "root", "LOCAL_VARIABLES", "EEE");
    findForPath(map, "root", "LOCAL_VARIABLES", "hello");
    findForPath(map, "root", "LOCAL_VARIABLES", "system");

    assertThrows(AssertionError.class, () -> findForPath(map, "root", "LOCAL_VARIABLES", "someB"));
  }
}
