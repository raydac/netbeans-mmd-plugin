package com.igormaznitsa.mindmap.annoit;

import static org.junit.Assert.assertThrows;

import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.Topic;
import org.junit.Test;

public class MarkedLocalVariablesTest extends AbstractMmdTest {
  @Test
  public void testCreateLinksForAnnotatedLocalVariables() throws Exception {
    final MindMap map = this.loadRootMmd();

    final Topic topicEee = findForPath(map, "root", "LOCAL_VARIABLES", "EEE");
    final Topic topicHello = findForPath(map, "root", "LOCAL_VARIABLES", "hello");
    final Topic topicSystem = findForPath(map, "root", "LOCAL_VARIABLES", "system");
    final Topic topicSomeText1 = findForPath(map, "root", "LOCAL_VARIABLES", "some text1");
    final Topic topicSomeText2 = findForPath(map, "root", "LOCAL_VARIABLES", "some text2");
    final Topic topicVisible = findForPath(map, "root", "LOCAL_VARIABLES", "visible");

    assertThrows(AssertionError.class, () -> findForPath(map, "root", "LOCAL_VARIABLES", "someB"));

    assertFileLinkLine(51, topicEee);
    assertFileLinkLine(15, topicHello);
    assertFileLinkLine(44, topicSystem);
    assertFileLinkLine(29, topicSomeText1);
    assertFileLinkLine(32, topicSomeText2);
    assertFileLinkLine(39, topicVisible);
  }
}
