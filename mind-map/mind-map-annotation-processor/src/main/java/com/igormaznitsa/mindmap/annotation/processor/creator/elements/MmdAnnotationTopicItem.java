package com.igormaznitsa.mindmap.annotation.processor.creator.elements;

import com.igormaznitsa.mindmap.annotation.processor.MmdAnnotation;
import com.igormaznitsa.mindmap.model.annotations.MmdTopic;

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

  public MmdTopic getTopicAnnotation() {
    return (MmdTopic) this.annotation.getAnnotation();
  }
}
