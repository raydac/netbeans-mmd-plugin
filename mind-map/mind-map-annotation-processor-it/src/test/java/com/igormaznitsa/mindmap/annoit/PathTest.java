package com.igormaznitsa.mindmap.annoit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.ExtraFile;
import com.igormaznitsa.mindmap.model.ExtraLink;
import com.igormaznitsa.mindmap.model.ExtraNote;
import com.igormaznitsa.mindmap.model.ExtraTopic;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.Topic;
import org.junit.Test;

public class PathTest extends AbstractMmdTest {
  @Test
  public void testAttributes() throws Exception {
    final MindMap map = this.loadRootMmd();
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
    assertFileLinkLine(15, topic1);
    assertFileLinkLine(16, topic2);
  }

  @Test
  public void testFileAnchorOnAnnotationBetweenAnotherAnnotations() throws Exception {
    final MindMap map = this.loadRootMmd();
    final Topic topic = findForPath(map, "root", "goose", "between");
    assertFileLinkLine(29, topic);
  }

  @Test
  public void testFileAnchorPointsMmdTopicBeforeClass() throws Exception {
    final MindMap map = this.loadRootMmd();
    final Topic topic = findForPath(map, "root", "goose", "multi-root");
    assertFileLinkLine(10, topic);
  }

  @Test
  public void testSubstitution() throws Exception {
    final MindMap map = this.loadRootMmd();

    final Topic topic1 = findForPath(map, "root", "substitution", System.getProperty("os.name"));
    final Topic topic2 = findForPath(map, "root", "substitution", System.getProperty("os.name"),
        System.getProperty("java.vendor"));

    assertNoExtensions(topic1);
    final ExtraFile extraFile = (ExtraFile) topic2.getExtras().get(Extra.ExtraType.FILE);
    final ExtraTopic extraTopic = (ExtraTopic) topic2.getExtras().get(Extra.ExtraType.TOPIC);
    final ExtraNote extraNote = (ExtraNote) topic2.getExtras().get(Extra.ExtraType.NOTE);
    final ExtraLink extraLink = (ExtraLink) topic2.getExtras().get(Extra.ExtraType.LINK);

    assertNotNull(extraNote);
    assertNotNull(extraFile);
    assertNotNull(extraTopic);
    assertNotNull(extraLink);

    assertEquals("${root}", map.getRoot().getText());
    assertEquals(System.getProperty("os.name") + "-111", extraTopic.getAsString());
    assertFalse(extraLink.getAsString().contains("${"));
    assertEquals(System.getProperty("user.home") + "/test.txt", extraFile.getAsString());
    assertEquals(System.getProperty("java.vendor"), topic2.getText());
    assertEquals(System.getProperty("user.name"), extraNote.getAsString());
    assertEquals("666-" + System.getProperty("os.name"),
        topic2.getAttribute(ExtraTopic.TOPIC_UID_ATTR));
  }

}
