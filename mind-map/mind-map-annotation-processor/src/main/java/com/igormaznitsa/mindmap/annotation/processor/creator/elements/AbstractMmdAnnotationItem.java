package com.igormaznitsa.mindmap.annotation.processor.creator.elements;

import static java.util.Objects.requireNonNull;

import com.igormaznitsa.mindmap.annotation.processor.MmdAnnotation;
import java.util.Objects;

public abstract class AbstractMmdAnnotationItem {
  protected final MmdAnnotation annotation;

  public AbstractMmdAnnotationItem(final MmdAnnotation annotation) {
    this.annotation = requireNonNull(annotation);
  }

  public MmdAnnotation getAnnotation() {
    return this.annotation;
  }

  @Override
  public boolean equals(final Object that) {
    if (this == that) {
      return true;
    }
    if (that == null || getClass() != that.getClass()) {
      return false;
    }
    return Objects.equals(annotation, ((AbstractMmdAnnotationItem) that).annotation);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.annotation);
  }
}
