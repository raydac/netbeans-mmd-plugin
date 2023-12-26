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

package com.igormaznitsa.mindmap.annotations;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import java.util.List;

/**
 * Topic direction.
 */
public enum Direction {
  /**
   * Auto
   */
  AUTO,
  /**
   * Left
   */
  LEFT,
  /**
   * Right
   */
  RIGHT;

  private static final List<Direction> LIST_VALUES = stream(Direction.values()).collect(toList());

  /**
   * Get all values as immutable list.
   *
   * @return immutable list of all values.
   * @since 1.6.6
   */
  public static List<Direction> asList() {
    return LIST_VALUES;
  }

  /**
   * Safe case-insensitive emoticon search for name.
   *
   * @param name             emoticon name, can be null
   * @param defaultDirection default direction, can be null
   * @return found direction for name or the default one, the default one can be null
   * @since 1.6.6
   */
  public static Direction findForName(final String name, final Direction defaultDirection) {
    if (name == null) {
      return defaultDirection;
    }
    return LIST_VALUES.stream().filter(x -> x.name().equalsIgnoreCase(name)).findFirst()
        .orElse(defaultDirection);
  }

}
