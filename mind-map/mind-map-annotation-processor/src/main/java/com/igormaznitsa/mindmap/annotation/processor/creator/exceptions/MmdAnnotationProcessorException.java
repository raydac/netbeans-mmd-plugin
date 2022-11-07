package com.igormaznitsa.mindmap.annotation.processor.creator.exceptions;

import com.igormaznitsa.mindmap.annotation.processor.creator.elements.AbstractMmdAnnotationItem;
import java.util.Objects;

public class MmdAnnotationProcessorException extends Exception {
  private final AbstractMmdAnnotationItem source;

  public MmdAnnotationProcessorException(final AbstractMmdAnnotationItem annotation,
                                         final String text) {
    this(annotation, text, null);
  }

  public MmdAnnotationProcessorException(final AbstractMmdAnnotationItem source, final String text,
                                         final Throwable cause) {
    super(text, cause);
    this.source = Objects.requireNonNull(source);
  }

  public AbstractMmdAnnotationItem getSource() {
    return this.source;
  }

  @Override
  public String toString() {
    return "MmdAnnotationProcessorException{" +
        "source=" + this.source + ','
        + "message=" + this.getMessage()
        + '}';
  }
}
