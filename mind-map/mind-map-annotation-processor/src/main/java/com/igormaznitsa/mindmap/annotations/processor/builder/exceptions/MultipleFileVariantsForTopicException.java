package com.igormaznitsa.mindmap.annotations.processor.builder.exceptions;

import com.igormaznitsa.mindmap.annotations.processor.builder.elements.FileItem;
import com.igormaznitsa.mindmap.annotations.processor.builder.elements.TopicItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MultipleFileVariantsForTopicException extends MmdAnnotationProcessorException {

  private final List<FileItem> variants;

  public MultipleFileVariantsForTopicException(
      final TopicItem source, final List<FileItem> variants) {
    super(source, "Detected multiple target MMD file variants for a topic annotation");
    this.variants = Collections.unmodifiableList(new ArrayList<>(variants));
  }

  public List<FileItem> getVariants() {
    return this.variants;
  }

  @Override
  public String toString() {
    return "MultipleFIleVariantsForTopicException{"
        + "source="
        + this.getSource()
        + "variants="
        + this.getVariants()
        + "message="
        + this.getMessage()
        + '}';
  }
}
