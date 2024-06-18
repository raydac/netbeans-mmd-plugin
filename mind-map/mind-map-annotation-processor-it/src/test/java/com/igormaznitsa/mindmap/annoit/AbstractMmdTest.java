package com.igormaznitsa.mindmap.annoit;


import static com.igormaznitsa.mindmap.model.StandardTopicAttributes.MMD_TOPIC_ATTRIBUTE_COLOR_BORDER;
import static com.igormaznitsa.mindmap.model.StandardTopicAttributes.MMD_TOPIC_ATTRIBUTE_COLOR_FILL;
import static com.igormaznitsa.mindmap.model.StandardTopicAttributes.MMD_TOPIC_ATTRIBUTE_COLOR_TEXT;
import static com.igormaznitsa.mindmap.model.StandardTopicAttributes.MMD_TOPIC_ATTRIBUTE_EMOTICON;
import static com.igormaznitsa.mindmap.model.StandardTopicAttributes.MMD_TOPIC_ATTRIBUTE_LINK_UID;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.igormaznitsa.mindmap.annotations.MmdColor;
import com.igormaznitsa.mindmap.annotations.MmdEmoticon;
import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.ExtraFile;
import com.igormaznitsa.mindmap.model.ExtraTopic;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.Topic;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import org.apache.commons.io.FilenameUtils;
import org.junit.Assert;

public abstract class AbstractMmdTest {
  protected static void assertTopicJumpTo(final Topic source, final Topic target) {
    final ExtraTopic extraLinkTo = (ExtraTopic) source.getExtras().get(Extra.ExtraType.TOPIC);
    assertNotNull("Can't find any link in source", extraLinkTo);
    final String targetTopicLinkUid = target.getAttribute(MMD_TOPIC_ATTRIBUTE_LINK_UID);
    assertNotNull("Can't find any link uid in target", targetTopicLinkUid);
    assertEquals(extraLinkTo.getValue(), targetTopicLinkUid);
  }

  protected static void assertTopicEmoticon(final Topic topic, final MmdEmoticon emoticon) {
    assertEquals(topic.getAttribute(MMD_TOPIC_ATTRIBUTE_EMOTICON), emoticon.getId());
  }

  protected static Topic findForPath(final MindMap mindMap, final String... path) {
    assertNotEquals("Empty path", 0, path.length);
    Topic topic = null;
    for (final String s : path) {
      if (topic == null) {
        topic = mindMap.getRoot();
      } else {
        topic = topic.getChildren().stream().filter(x -> s.equals(x.getText())).findFirst()
            .orElse(null);
      }
      assertNotNull("Can't find for path: " + Arrays.toString(path), topic);
    }
    return requireNonNull(topic);
  }

  protected static void assertTopicPath(final Topic topic, final String... path) {
    final String[] detectedPath = Arrays.stream(topic.getPath()).map(Topic::getText).toArray(
        String[]::new);
    assertArrayEquals(path, detectedPath);
  }

  protected static void assertTopicColors(
      final Topic topic,
      final MmdColor colorText,
      final MmdColor colorFill,
      final MmdColor colorBorder) {

    if (colorText != MmdColor.Default) {
      assertEquals(colorText.getHtmlColor(), topic.getAttribute(MMD_TOPIC_ATTRIBUTE_COLOR_TEXT));
    }

    if (colorFill != MmdColor.Default) {
      assertEquals(colorFill.getHtmlColor(), topic.getAttribute(MMD_TOPIC_ATTRIBUTE_COLOR_FILL));
    }

    if (colorBorder != MmdColor.Default) {
      assertEquals(colorBorder.getHtmlColor(),
          topic.getAttribute(MMD_TOPIC_ATTRIBUTE_COLOR_BORDER));
    }
  }

  protected Path getBaseDir() {
    final String baseDir = System.getProperty("mmd.basedir", null);
    assertNotNull("Can't find MMD base folder", baseDir);
    return new File(baseDir).toPath();
  }

  protected Path getSrcDir() {
    final String srcDir = System.getProperty("mmd.src.dir", null);
    assertNotNull("Can't find MMD source folder", srcDir);
    return new File(srcDir).toPath();
  }

  protected static Topic findForTitle(final MindMap mindMap, final String title) {
    for (final Topic t : mindMap) {
      if (t.getText().equals(title)) {
        return t;
      }
    }
    fail("Can't find any topic for title: " + title);
    throw new Error("Must not be called");
  }

  protected Path findPath(final String path) {
    return new File(FilenameUtils.normalizeNoEndSeparator(path)).toPath();
  }

  protected MindMap loadMindMap(final String path) throws IOException {
    final Path asPath = findPath(path);
    assertFalse("path must be relative one", asPath.isAbsolute());
    final Path mindMapFile = this.getSrcDir().resolve(asPath);
    assertTrue("Can't find mind map file: " + mindMapFile, Files.isRegularFile(mindMapFile));
    return new MindMap(new StringReader(readFileToString(mindMapFile.toFile(), UTF_8)));
  }

  protected static void assertFileLinkLine(final int expectedLine, final Topic topic) {
    final ExtraFile extraFile = (ExtraFile) topic.getExtras().get(Extra.ExtraType.FILE);
    Assert.assertEquals(Integer.toString(expectedLine),
        extraFile.getAsURI().getParameters().getProperty("line"));
  }

}
