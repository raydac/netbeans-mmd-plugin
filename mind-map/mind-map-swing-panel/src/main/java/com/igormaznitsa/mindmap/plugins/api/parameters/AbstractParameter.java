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

public abstract class AbstractParameter<T> implements Comparable<AbstractParameter<T>>{
  private final String id;
  private final String title;
  private final String comment;
  private T value;

  private Importance importance;

  private final int order;

  public Importance getImportance() {
    return this.importance;
  }

  public int getOrder() {
    return this.order;
  }

  public String getTitle() {
    return this.title;
  }

  @Override
  public int compareTo(final AbstractParameter<T> that) {
    int compareResult = Integer.compare(this.order, that.order);
    if (compareResult == 0) {
      compareResult = this.id.compareTo(that.id);
    }
    return compareResult;
  }

  public void setValue(final T value) {
    this.value = value;
  }

  public T getValue() {
    return this.value;
  }

  public AbstractParameter(final String id,
                           final String title,
                           final String comment,
                           final T defaultValue,
                           final int order) {
    this.id = requireNonNull(id);
    this.comment = requireNonNull(comment);
    this.title = requireNonNull(title);
    this.value = defaultValue;
    this.order = order;
    this.importance = Importance.MAIN;
  }

  public AbstractParameter(final String id,
                           final String title,
                           final String comment,
                           final T defaultValue,
                           final int order,
                           final Importance importance) {
    this.id = requireNonNull(id);
    this.comment = requireNonNull(comment);
    this.title = requireNonNull(title);
    this.value = defaultValue;
    this.order = order;
    this.importance = importance;
  }

  public AbstractParameter(final String id,
                           final String title,
                           final String comment,
                           final T defaultValue) {
    this(id, title, comment, defaultValue, 0);
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
        "id='" + this.id + '\'' +
        ", title='" + this.title + '\'' +
        ", value=" + this.value +
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
