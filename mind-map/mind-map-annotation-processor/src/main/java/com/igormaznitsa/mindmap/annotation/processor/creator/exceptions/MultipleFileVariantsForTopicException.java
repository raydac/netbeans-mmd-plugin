package com.igormaznitsa.mindmap.annotation.processor.creator.exceptions;

import com.igormaznitsa.mindmap.annotation.processor.creator.elements.MmdAnnotationFileItem;
import com.igormaznitsa.mindmap.annotation.processor.creator.elements.MmdAnnotationTopicItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MultipleFileVariantsForTopicException extends MmdAnnotationProcessorException {

  private final List<MmdAnnotationFileItem> variants;

  public MultipleFileVariantsForTopicException(final MmdAnnotationTopicItem source,
                                               final List<MmdAnnotationFileItem> variants) {
    super(source, "Detected multiple target MMD file variants for a topic annotation");
    this.variants = Collections.unmodifiableList(new ArrayList<>(variants));
  }

  public List<MmdAnnotationFileItem> getVariants() {
    return this.variants;
  }

  @Override
  public String toString() {
    return "MultipleFIleVariantsForTopicException{"
        + "source=" + this.getSource()
        + "variants=" + this.getVariants()
        + "message=" + this.getMessage()
        + '}';
  }
}
