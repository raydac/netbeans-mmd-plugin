package com.igormaznitsa.mindmap.annoit;

import static org.junit.Assert.assertEquals;

import com.igormaznitsa.mindmap.annotations.MmdColor;
import com.igormaznitsa.mindmap.annotations.MmdEmoticon;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.StandardTopicAttributes;
import com.igormaznitsa.mindmap.model.Topic;
import java.io.IOException;
import org.junit.Test;

public class AttributesTest extends AbstractMmdTest implements StandardTopicAttributes {
  private MindMap findMindMap() throws IOException {
    return this.loadMindMap("com/igormaznitsa/mindmap/annoit/attributes/attributes.mmd");
  }

  @Test
  public void testAttributes() throws Exception {
    final MindMap map = this.findMindMap();
    assertTopicColors(map.getRoot(), MmdColor.Orange, MmdColor.Aqua, MmdColor.Red);
    assertTopicEmoticon(map.getRoot(), MmdEmoticon.ACORN);
    assertTopicJumpTo(findForTitle(map, "SubMethod3"), map.getRoot());
    assertTopicJumpTo(findForTitle(map, "method three"), findForTitle(map, "method two"));
    assertTopicJumpTo(findForTitle(map, "SubMethod2"), map.getRoot());
  }

  @Test
  public void testOrderAndPath() throws Exception {
    final MindMap map = this.findMindMap();

    final Topic topic = findForTitle(map, "method one");
    assertEquals(3, topic.getChildren().size());
    assertEquals("SubMethod1", topic.getChildren().get(0).getText());
    assertEquals("SubMethod2", topic.getChildren().get(1).getText());
    assertEquals("SubMethod3", topic.getChildren().get(2).getText());

    final Topic marked = findForTitle(map, "MARKED");
    assertFileLinkLine(58, marked);
    assertEquals(4, marked.getChildren().size());
    assertEquals("c", marked.getChildren().get(0).getText());
    assertEquals("b", marked.getChildren().get(1).getText());
    assertEquals("a", marked.getChildren().get(2).getText());
    assertEquals("sum", marked.getChildren().get(3).getText());

    final Topic topicSum = findForTitle(map, "sum");
    assertFileLinkLine(70, topicSum);

    final Topic pathCommentTopic = findForTitle(map, "pathCommentTopic");
    assertTopicPath(pathCommentTopic, "Root topic", "path1", "path2", "path3",
        "pathCommentTopic");
    assertFileLinkLine(61, pathCommentTopic);
  }

}
