package com.igormaznitsa.mindmap.plugins.importers;

import static java.util.Objects.requireNonNull;
import static org.junit.Assert.assertEquals;

import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.Topic;
import java.io.File;
import java.io.FileInputStream;
import org.junit.Test;

public class Freemind2MindMapImporterTest {
  private static final Freemind2MindMapImporter INSTANCE = new Freemind2MindMapImporter();

  private static File findFile(final String resource) throws Exception {
    return new File(
        requireNonNull(Freemind2MindMapImporterTest.class.getResource(resource)).toURI());
  }

  @Test
  public void testFile1() throws Exception {
    final File file = findFile("freemind1.mm");
    try (final FileInputStream in = new FileInputStream(file)) {
      final MindMap mindMap = INSTANCE.extractTopics(file, in);
      final Topic root = mindMap.getRoot();
      assertEquals("Root", root.getText());
      assertEquals(3, root.getChildren().size());

      final Topic topic1 = root.getChildren().get(0);
      final Topic topic2 = root.getChildren().get(1);
      final Topic topic3 = root.getChildren().get(2);

      assertEquals("Node1", topic1.getText());
      assertEquals(1, topic1.getChildren().size());

      final Topic topic1_1 = topic1.getChildren().get(0);
      assertEquals("Node1.1", topic1_1.getText());
      assertEquals(0, topic1_1.getChildren().size());

      assertEquals("Node2", topic2.getText());
      assertEquals(1, topic2.getChildren().size());

      final Topic topic2_1 = topic2.getChildren().get(0);
      assertEquals("Node2.2", topic2_1.getText());
      assertEquals(0, topic2_1.getChildren().size());
      assertEquals("Hello world", topic2_1.getExtras().get(Extra.ExtraType.NOTE).getValue());

      assertEquals("Node3", topic3.getText());
      assertEquals(1, topic3.getChildren().size());

      final Topic topic3_1 = topic3.getChildren().get(0);
      assertEquals("", topic3_1.getText());
      assertEquals(0, topic3_1.getChildren().size());
      assertEquals(1, topic3_1.getAttributes().size());
    }
  }

  @Test
  public void testFile2() throws Exception {
    final File file = findFile("freemind2.mm");
    try (final FileInputStream in = new FileInputStream(file)) {
      final MindMap mindMap = INSTANCE.extractTopics(file, in);
      final Topic root = mindMap.getRoot();
      assertEquals("FreeMind\n- free mind mapping software -", root.getText());
      assertEquals(44, root.getChildren().size());
    }
  }


}