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
