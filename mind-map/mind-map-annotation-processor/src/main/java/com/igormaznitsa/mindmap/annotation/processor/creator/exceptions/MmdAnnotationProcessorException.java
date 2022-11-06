package com.igormaznitsa.mindmap.annotation.processor.creator.exceptions;

import com.igormaznitsa.mindmap.annotation.processor.creator.elements.MmdAnnotationTopicItem;
import java.util.Objects;

public class MmdAnnotationProcessorException extends Exception {
  private final MmdAnnotationTopicItem source;

  public MmdAnnotationProcessorException(final MmdAnnotationTopicItem annotation,
                                         final String text) {
    this(annotation, text, null);
  }

  public MmdAnnotationProcessorException(final MmdAnnotationTopicItem source, final String text,
                                         final Throwable cause) {
    super(text, cause);
    this.source = Objects.requireNonNull(source);
  }

  public MmdAnnotationTopicItem getSource() {
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
