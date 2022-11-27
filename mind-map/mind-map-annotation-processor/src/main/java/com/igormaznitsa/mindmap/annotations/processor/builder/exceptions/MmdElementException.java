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

import static java.util.Objects.requireNonNull;

import javax.lang.model.element.Element;

public class MmdElementException extends Exception {
  private final Element element;

  public MmdElementException(final String message, final Element source) {
    this(message, source, null);
  }

  public MmdElementException(final String message, final Element source, final Throwable cause) {
    super(message, cause);
    this.element = requireNonNull(source);
  }

  public Element getSource() {
    return this.element;
  }

  @Override
  public String toString() {
    return "MmdElementException{" + "element=" + this.element + '}';
  }
}
