package com.igormaznitsa.mindmap.annotations.processor.builder.exceptions;

import com.igormaznitsa.mindmap.annotations.processor.builder.elements.AbstractItem;
import java.util.Objects;

public class MmdAnnotationProcessorException extends Exception {

  private final AbstractItem source;

  public MmdAnnotationProcessorException(
      final AbstractItem annotation, final String text) {
    this(annotation, text, null);
  }

  public MmdAnnotationProcessorException(
      final AbstractItem source, final String text, final Throwable cause) {
    super(text, cause);
    this.source = Objects.requireNonNull(source);
  }

  public AbstractItem getSource() {
    return this.source;
  }

  @Override
  public String toString() {
    return "MmdAnnotationProcessorException{"
        + "source="
        + this.source
        + ','
        + "message="
        + this.getMessage()
        + '}';
  }
}
