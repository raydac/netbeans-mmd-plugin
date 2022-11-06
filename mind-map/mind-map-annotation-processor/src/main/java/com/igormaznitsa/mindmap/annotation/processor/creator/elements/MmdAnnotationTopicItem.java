package com.igormaznitsa.mindmap.annotation.processor.creator.elements;

import com.igormaznitsa.mindmap.annotation.processor.MmdAnnotation;
import com.igormaznitsa.mindmap.model.annotations.MmdFile;
import com.igormaznitsa.mindmap.model.annotations.MmdFiles;
import com.igormaznitsa.mindmap.model.annotations.MmdTopic;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.lang.model.element.Element;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

public class MmdAnnotationTopicItem extends AbstractMmdAnnotationItem {
  public MmdAnnotationTopicItem(final MmdAnnotation annotation) {
    super(annotation);
    if (!(annotation.getAnnotation() instanceof MmdTopic)) {
      throw new IllegalArgumentException("Expected annotation " + MmdTopic.class.getName());
    }
  }

  public String findClassName() {
    return this.annotation.getElement().getSimpleName().toString();
  }

  private static List<Pair<Element, MmdFile>> findFirstElementMarkedMmdFile(final Element element) {
    if (element == null) {
      return List.of();
    }
    final MmdFiles[] fileAnnotations = element.getAnnotationsByType(MmdFiles.class);
    final MmdFile[] fileAnnotation = element.getAnnotationsByType(MmdFile.class);

    if (fileAnnotation.length == 0 && fileAnnotations.length == 0) {
      return findFirstElementMarkedMmdFile(element.getEnclosingElement());
    } else {
      final List<Pair<Element, MmdFile>> result = new ArrayList<>();
      for (final MmdFile f : fileAnnotation) {
        result.add(Pair.of(element, f));
      }
      Arrays.stream(fileAnnotations)
          .flatMap(x -> Arrays.stream(x.value()))
          .forEach(f -> result.add(Pair.of(element, f)));

      return result;
    }
  }

  private static Optional<String> findFileUidAmongParentTopics(final Element element) {
    if (element == null) {
      return Optional.empty();
    }
    final MmdTopic topicAnnotation = element.getAnnotation(MmdTopic.class);
    if (topicAnnotation != null && StringUtils.isNotBlank(topicAnnotation.mmdFileUid())) {
      return Optional.of(topicAnnotation.mmdFileUid());
    } else {
      return findFileUidAmongParentTopics(element.getEnclosingElement());
    }
  }

  public Optional<String> findFileUidAttribute() {
    return findFileUidAmongParentTopics(this.annotation.getElement());
  }

  public List<Pair<Element, MmdFile>> findDirectMmdFileMarks() {
    return findFirstElementMarkedMmdFile(this.annotation.getElement());
  }

  public MmdTopic getTopicAnnotation() {
    return (MmdTopic) this.annotation.getAnnotation();
  }
}
