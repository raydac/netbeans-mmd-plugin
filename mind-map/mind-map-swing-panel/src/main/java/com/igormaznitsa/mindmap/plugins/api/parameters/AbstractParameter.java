/*
 * Copyright (C) 2015-2022 Igor A. Maznitsa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
