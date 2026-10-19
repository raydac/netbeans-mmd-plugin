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

/**
 * Set of conditional operators allowed over versions.
 *
 * @since 1.0.0
 */
public enum Condition {
  /**
   * Unknown.
   *
   * @since 1.0.0
   */
  UNKNOWN(""),
  /**
   * Equals.
   *
   * @since 1.0.0
   */
  EQU("="),
  /**
   * Not equals.
   *
   * @since 1.0.0
   */
  NOT_EQU("!="),
  /**
   * Less.
   *
   * @since 1.0.0
   */
  LESS("<"),
  /**
   * Great.
   *
   * @since 1.0.0
   */
  GREAT(">"),
  /**
   * Less or equals.
   *
   * @since 1.0.0
   */
  LESS_OR_EQU("<="),
  /**
   * Great or equals.
   *
   * @since 1.0.0
   */
  GREAT_OR_EQU(">=");

  private final String str;

  Condition(final String str) {
    this.str = str;
  }

  /**
   * Find conditional operator for its string representation.
   *
   * @param text text to be decoded, it can be null.
   * @return detected operator or Condition#EQU if it was not recognized.
   * @since 1.0.0
   */
  public static Condition decode(final String text) {
    if (text != null && text.isEmpty()) {
      return EQU;
    }
    for (final Condition op : Condition.values()) {
      if (op.str.equals(text)) {
        return op;
      }
    }
    return UNKNOWN;
  }

  @Override
  public String toString() {
    return this.str;
  }

}
