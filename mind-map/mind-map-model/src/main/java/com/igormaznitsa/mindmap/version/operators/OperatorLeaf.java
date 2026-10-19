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
 * It is a pseudo-operator, it does nothing and represents a leaf in the operator tree.
 *
 * @since 1.0.0
 */
public final class OperatorLeaf implements Operator {

  private static final long serialVersionUID = -4529836368426048811L;

  private final Condition op;
  private final Version base;

  /**
   * Constructor.
   *
   * @param op   condition, must not be null
   * @param base the base version value to be used with the condition, must not be null
   * @since 1.0.0
   */
  public OperatorLeaf(final Condition op, final Version base) {
    this.op = op;
    this.base = base;
  }

  @Override
  public boolean isValid(final Version version) {
    if (version != null) {
      final int result = this.base.compareTo(version);
      switch (this.op) {
        case UNKNOWN: {
          throw new IllegalArgumentException("Detected illegal condition");
        }
        case EQU:
          return result == 0;
        case NOT_EQU:
          return result != 0;
        case LESS:
          return result > 0;
        case GREAT:
          return result < 0;
        case LESS_OR_EQU:
          return result >= 0;
        case GREAT_OR_EQU:
          return result <= 0;
        default:
          throw new Error("Detected unexpected operation : " + this.op);
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return this.op.toString() + this.base.toString();
  }
}
