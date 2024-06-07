package com.igormaznitsa.mindmap.annoit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.igormaznitsa.mindmap.annotations.MmdTopic;
import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.ExtraFile;
import com.igormaznitsa.mindmap.model.MMapURI;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.Topic;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import org.junit.Assert;
import org.junit.Ignore;
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
    assertEquals(4, gooseTopic.getChildren().size());
    assertTrue(gooseTopic.getChildren().stream()
        .allMatch(x -> x.getExtras().get(Extra.ExtraType.FILE) != null));
  }

  @Test
  @Ignore("because not fixed yet")
  public void testFileAnchorPointsMmdTopic() throws Exception {
    final File baseDir = this.getBaseDir().toFile().getParentFile();
    final MindMap map =
        this.loadMindMap("com/igormaznitsa/mindmap/annoit/paths/RootFile.mmd");
    map.stream()
        .filter(t -> t.getExtras().containsKey(Extra.ExtraType.FILE))
        .forEach(t -> {
          ExtraFile extraFile = (ExtraFile)t.getExtras().get(Extra.ExtraType.FILE);
          final MMapURI uri = extraFile.getAsURI();
          final int lineNumber = Integer.parseInt(uri.getParameters().getProperty("line"));
          final List<String> lines;
          try {
            lines = Files.readAllLines(uri.asFile(baseDir).toPath());
          } catch (Exception ex) {
            throw new IllegalStateException("Can't read " + uri, ex);
          }
          final String srcLine = lines.get(lineNumber - 1);
          Assert.assertTrue("uri: " + uri +" line: " + srcLine,srcLine.contains("@"+ MmdTopic.class.getSimpleName()));
        });
  }

}
