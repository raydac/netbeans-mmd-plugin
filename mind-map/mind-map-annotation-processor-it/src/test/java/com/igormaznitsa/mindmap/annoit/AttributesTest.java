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
  public void testOrder() throws Exception {
    final MindMap map = this.findMindMap();

    final Topic topic = findForTitle(map, "method one");
    assertEquals(3, topic.getChildren().size());
    assertEquals("SubMethod1", topic.getChildren().get(0).getText());
    assertEquals("SubMethod2", topic.getChildren().get(1).getText());
    assertEquals("SubMethod3", topic.getChildren().get(2).getText());

    final Topic sum = findForTitle(map, "sum");
    assertEquals(3, sum.getChildren().size());
    assertEquals("c", sum.getChildren().get(0).getText());
    assertEquals("b", sum.getChildren().get(1).getText());
    assertEquals("a", sum.getChildren().get(2).getText());
  }

}
