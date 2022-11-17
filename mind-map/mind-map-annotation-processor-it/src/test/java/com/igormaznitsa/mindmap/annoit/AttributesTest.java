package com.igormaznitsa.mindmap.annoit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import com.igormaznitsa.mindmap.annotations.MmdColor;
import com.igormaznitsa.mindmap.annotations.MmdEmoticon;
import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.ExtraTopic;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.StandardTopicAttributes;
import com.igormaznitsa.mindmap.model.Topic;
import org.junit.Test;

public class AttributesTest extends AbstractMmdTest implements StandardTopicAttributes {
  @Test
  public void testAttributes() throws Exception {
    final MindMap map =
        this.loadMindMap("com/igormaznitsa/mindmap/annoit/attributes/attributes.mmd");
    assertTopicColors(map.getRoot(), MmdColor.ORANGE, MmdColor.AQUA, MmdColor.RED);
    assertTopicEmoticon(map.getRoot(), MmdEmoticon.ACORN);
    assertTopicJumpTo(findForTitle(map, "SubMethod3"), map.getRoot());
    assertTopicJumpTo(findForTitle(map, "method three"), findForTitle(map, "method two"));
    assertTopicJumpTo(findForTitle(map, "SubMethod2"), map.getRoot());
  }

  private Topic findForTitle(final MindMap mindMap, final String title) {
    for (final Topic t : mindMap) {
      if (t.getText().equals(title)) {
        return t;
      }
    }
    fail("Can't find any topic for title: " + title);
    throw new Error("Must not be called");
  }

  private void assertTopicJumpTo(final Topic source, final Topic target) {
    final ExtraTopic extraLinkTo = (ExtraTopic) source.getExtras().get(Extra.ExtraType.TOPIC);
    assertNotNull("Can't find any link in source", extraLinkTo);
    final String targetTopicLinkUid = target.getAttribute(MMD_TOPIC_ATTRIBUTE_LINK_UID);
    assertNotNull("Can't find any link uid in target", targetTopicLinkUid);
    assertEquals(extraLinkTo.getValue(), targetTopicLinkUid);
  }

  private void assertTopicEmoticon(final Topic topic, final MmdEmoticon emoticon) {
    assertEquals(topic.getAttribute(MMD_TOPIC_ATTRIBUTE_EMOTICON), emoticon.getId());
  }

  private void assertTopicColors(
      final Topic topic,
      final MmdColor colorText,
      final MmdColor colorFill,
      final MmdColor colorBorder) {

    if (colorText != MmdColor.DEFAULT) {
      assertEquals(colorText.getHtmlColor(), topic.getAttribute(MMD_TOPIC_ATTRIBUTE_COLOR_TEXT));
    }

    if (colorFill != MmdColor.DEFAULT) {
      assertEquals(colorFill.getHtmlColor(), topic.getAttribute(MMD_TOPIC_ATTRIBUTE_COLOR_FILL));
    }

    if (colorBorder != MmdColor.DEFAULT) {
      assertEquals(colorBorder.getHtmlColor(),
          topic.getAttribute(MMD_TOPIC_ATTRIBUTE_COLOR_BORDER));
    }
  }
}
