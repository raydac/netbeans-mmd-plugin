package com.igormaznitsa.mindmap.annoit;

import static org.junit.Assert.assertThrows;

import com.igormaznitsa.mindmap.model.MindMap;
import org.junit.Test;

public class MarkedLocalVariablesTest extends AbstractMmdTest {
  @Test
  public void testCreateLinksForAnnotatedLocalVariables() throws Exception {
    final MindMap map = this.loadRootMmd();

    findForPath(map, "root", "LOCAL_VARIABLES", "EEE");
    findForPath(map, "root", "LOCAL_VARIABLES", "hello");
    findForPath(map, "root", "LOCAL_VARIABLES", "system");
    findForPath(map, "root", "LOCAL_VARIABLES", "some text1");
    findForPath(map, "root", "LOCAL_VARIABLES", "some text2");
    findForPath(map, "root", "LOCAL_VARIABLES", "visible");

    assertThrows(AssertionError.class, () -> findForPath(map, "root", "LOCAL_VARIABLES", "someB"));
  }
}
