package com.igormaznitsa.mindmap.plugins.api.parameters;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

public abstract class AbstractParameter<T> {
  private final String id;
  private final String title;
  private final String comment;
  private T value;

  public String getTitle() {
    return this.title;
  }

  public void setValue(final T value) {
    this.value = requireNonNull(value);
  }

  public T getValue() {
    return this.value;
  }

  public AbstractParameter(final String id, final String title, final String comment,
                           final T defaultValue) {
    this.id = requireNonNull(id);
    this.comment = requireNonNull(comment);
    this.title = requireNonNull(title);
    this.value = requireNonNull(defaultValue);
  }

  public String getComment() {
    return this.comment;
  }

  public String asString() {
    return this.value.toString();
  }

  public abstract void fromString(final String value);

  public String getId() {
    return this.id;
  }

  @Override
  public int hashCode() {
    return this.id.hashCode();
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + '{' +
        "id='" + id + '\'' +
        ", title='" + title + '\'' +
        ", value=" + value +
        '}';
  }

  @Override
  public boolean equals(final Object that) {
    if (this == that) {
      return true;
    }
    if (that == null || getClass() != that.getClass()) {
      return false;
    }
    final AbstractParameter<?> thatParameter = (AbstractParameter<?>) that;
    return Objects.equals(id, thatParameter.id);
  }
}
