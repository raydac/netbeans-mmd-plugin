package com.igormaznitsa.mindmap.annotation.processor;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FoundMmdAnnotation {
  private final Annotation annotation;
  private final File file;
  private final long line;

  public FoundMmdAnnotation(
      @Nonnull final Annotation annotation,
      @Nonnull final File file,
      final long line
  ) {
    this.annotation = annotation;
    this.file = file;
    this.line = line;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.annotation, this.file, this.line);
  }

  @Override
  public boolean equals(@Nullable final Object that) {
    if (that == null) {
      return false;
    }
    if (that == this) {
      return true;
    }
    if (that instanceof FoundMmdAnnotation) {
      final FoundMmdAnnotation thatInstance = (FoundMmdAnnotation) that;
      return this.annotation.equals(thatInstance.annotation)
          && this.file.equals(thatInstance.file)
          && this.line == thatInstance.line;
    }
    return false;
  }

  @Nonnull
  public Annotation getAnnotation() {
    return this.annotation;
  }

  @Nonnull
  public File getFile() {
    return this.file;
  }

  public long getLine() {
    return this.line;
  }
}
