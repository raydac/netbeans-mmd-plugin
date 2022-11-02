package com.igormaznitsa.mindmap.annotation.processor;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.util.Objects;
import javax.lang.model.element.Element;

/**
 * Immutable data object contains found MMD annotation, its path and line number.
 */
public class MmdAnnotation {
  /**
   * Found annotation.
   */
  private final Annotation annotation;
  /**
   * Path to source class file.
   */
  private final Path path;
  /**
   * Line of the annotation in the source class file.
   */
  private final long line;

  /**
   * Element providing the annotation.
   */
  private final Element element;

  /**
   * Constructor.
   *
   * @param element    element providing annotation, must not be null
   * @param annotation found annotation, must not be null
   * @param path       source class file path, must not be null
   * @param line       line number of the annotation in the source class file
   * @throws NullPointerException     thrown if any argument is null
   * @throws IllegalArgumentException thrown if line is zero or negative one
   */
  public MmdAnnotation(
      final Element element,
      final Annotation annotation,
      final Path path,
      final long line
  ) {
    this.element = requireNonNull(element);
    this.annotation = requireNonNull(annotation);
    this.path = requireNonNull(path);
    if (line < 1) {
      throw new IllegalArgumentException("Unexpected line number: " + line);
    }
    this.line = line;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.annotation, this.path, this.line);
  }

  @Override
  public boolean equals(final Object that) {
    if (that == null) {
      return false;
    }
    if (that == this) {
      return true;
    }
    if (that instanceof MmdAnnotation) {
      final MmdAnnotation thatInstance = (MmdAnnotation) that;
      return this.annotation.equals(thatInstance.annotation)
          && this.line == thatInstance.line
          && this.path.equals(thatInstance.path);
    }
    return false;
  }

  /**
   * Get annotation.
   *
   * @return the annotation, must not be null
   */
  public Annotation getAnnotation() {
    return this.annotation;
  }

  /**
   * Get source class file path.
   *
   * @return the source class file path, must not be null
   */
  public Path getPath() {
    return this.path;
  }

  /**
   * Get line number of the annotation in source class file.
   *
   * @return line number, positive one
   */
  public long getLine() {
    return this.line;
  }

  /**
   * Get the base annotated element.
   *
   * @return the ennotated element, must not be null
   */
  public Element getElement() {
    return this.element;
  }
}
