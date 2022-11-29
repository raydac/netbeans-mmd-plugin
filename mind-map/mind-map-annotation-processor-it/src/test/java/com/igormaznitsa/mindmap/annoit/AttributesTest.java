package com.igormaznitsa.mindmap.annoit;

import com.igormaznitsa.mindmap.annotations.MmdColor;
import com.igormaznitsa.mindmap.annotations.MmdEmoticon;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.StandardTopicAttributes;
import org.junit.Test;

public class AttributesTest extends AbstractMmdTest implements StandardTopicAttributes {
  @Test
  public void testAttributes() throws Exception {
    final MindMap map =
        this.loadMindMap("com/igormaznitsa/mindmap/annoit/attributes/attributes.mmd");
    assertTopicColors(map.getRoot(), MmdColor.Orange, MmdColor.Aqua, MmdColor.Red);
    assertTopicEmoticon(map.getRoot(), MmdEmoticon.ACORN);
    assertTopicJumpTo(findForTitle(map, "SubMethod3"), map.getRoot());
    assertTopicJumpTo(findForTitle(map, "method three"), findForTitle(map, "method two"));
    assertTopicJumpTo(findForTitle(map, "SubMethod2"), map.getRoot());
  }

}
