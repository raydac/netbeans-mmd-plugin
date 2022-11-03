package com.igormaznitsa.mindmap.annotation.processor.creator.elements;

import com.igormaznitsa.mindmap.annotation.processor.MmdAnnotation;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.model.annotations.MmdFile;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang3.tuple.Pair;

public class MmdAnnotationFileItem extends AbstractMmdAnnotationItem {
  private final MmdFile mmdFileAnnotation;
  private final String uid;

  private final Map<String, Pair<MmdAnnotation, Topic>> mapTopics = new HashMap<>();

  public MmdAnnotationFileItem(final MmdAnnotation annotation) {
    super(annotation);
    if (!(annotation.getAnnotation() instanceof MmdFile)) {
      throw new IllegalArgumentException("Expected annotation " + MmdFile.class.getName());
    }
    this.mmdFileAnnotation = (MmdFile) annotation.getAnnotation();
    if (this.mmdFileAnnotation.fileUid().trim().isEmpty()) {
      this.uid = UUID.randomUUID().toString();
    } else {
      this.uid = this.mmdFileAnnotation.fileUid().trim();
    }
  }

  public void write(
      final Path forceFolder,
      final boolean allowOverwrite,
      final boolean preferRelativePaths,
      final boolean dryStart
  ) throws IOException {

  }

  private MindMap makeMindMap() throws URISyntaxException {
    final MindMap map = new MindMap(true);
    map.putAttribute("showJumps", "true");

    this.fillAttributesWithoutFileAndTopicLinks(map.getRoot(), this.mmdFileAnnotation.rootTopic());

    return map;
  }

  public MmdFile getFileAnnotation() {
    return this.mmdFileAnnotation;
  }

  public String getUid() {
    return this.uid;
  }
}
