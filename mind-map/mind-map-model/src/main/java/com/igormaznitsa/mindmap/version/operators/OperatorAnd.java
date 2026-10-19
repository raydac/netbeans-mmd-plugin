/*
 * Copyright (C) 2015-2026 Igor A. Maznitsa
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

package com.igormaznitsa.mindmap.version.operators;

import com.igormaznitsa.mindmap.version.Version;

/**
 * Implementation of AND operator.
 *
 * @since 1.0.0
 */
public final class OperatorAnd implements Operator {

  private static final long serialVersionUID = -1503624332275479528L;

  private final Operator a;
  private final Operator b;

  /**
   * Constructor.
   *
   * @param a the left part, must not be null
   * @param b the right part, must not be null
   * @since 1.0.0
   */
  public OperatorAnd(final Operator a, final Operator b) {
    this.a = a;
    this.b = b;
  }

  @Override
  public boolean isValid(final Version version) {
    return version != null && this.a.isValid(version) && this.b.isValid(version);
  }

  @Override
  public String toString() {
    return a.toString() + ',' + b.toString();
  }
}
