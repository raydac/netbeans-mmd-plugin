package com.igormaznitsa.mindmap.annoit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.ExtraFile;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.Topic;
import org.junit.Assert;
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

    final Topic pathTopic = findForPath(map, "root", "path");
    assertEquals(1, pathTopic.getChildren().size());
    assertNotNull(pathTopic.getChildren().get(0).getExtras().get(Extra.ExtraType.FILE));

    final Topic gooseTopic = findForPath(map, "root", "goose");
    assertEquals(5, gooseTopic.getChildren().size());
    assertTrue(gooseTopic.getChildren().stream()
        .allMatch(x -> x.getExtras().get(Extra.ExtraType.FILE) != null));
  }

  @Test
  public void testFileAnchorOnAnnotationInRepeatableValues() throws Exception {
    final MindMap map1 =
        this.loadMindMap("com/igormaznitsa/mindmap/annoit/collectors/FileOne.mmd");
    final MindMap map2 =
        this.loadMindMap("com/igormaznitsa/mindmap/annoit/collectors/FileTwo.mmd");

    final Topic topic1 = findForPath(map1, "Root", "Topic1");
    final Topic topic2 = findForPath(map2, "Root", "Topic2");
    assertLine(15, topic1);
    assertLine(16, topic2);
  }

  @Test
  public void testFileAnchorOnAnnotationBetweenAnotherAnnotations() throws Exception {
    final MindMap map =
        this.loadMindMap("com/igormaznitsa/mindmap/annoit/paths/RootFile.mmd");
    final Topic topic = findForPath(map, "root", "goose", "between");
    assertLine(29, topic);
  }

  @Test
  public void testFileAnchorPointsMmdTopicBeforeClass() throws Exception {
    final MindMap map =
        this.loadMindMap("com/igormaznitsa/mindmap/annoit/paths/RootFile.mmd");
    final Topic topic = findForPath(map, "root", "goose", "multi-root");
    assertLine(10, topic);
  }

  private static void assertLine(final int line, final Topic topic) {
    final ExtraFile extraFile = (ExtraFile) topic.getExtras().get(Extra.ExtraType.FILE);
    Assert.assertEquals(Integer.toString(line), extraFile.getAsURI().getParameters().getProperty("line"));
  }

}
