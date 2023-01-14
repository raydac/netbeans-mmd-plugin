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

package com.igormaznitsa.mindmap.model;

import static java.util.Objects.requireNonNull;

/**
 * Auxiliary class collects misc functions.
 */
public final class MiscUtils {

  private MiscUtils() {

  }

  /**
   * Select first non-null value among arguments and returns it
   *
   * @param object     first object
   * @param elseObject second object
   * @param <T>        type of objects
   * @return first non-null argument
   * @throws NullPointerException if there is no non-null argument
   */
  public static <T> T ensureNotNull(final T object, final T elseObject) {
    return object == null ? requireNonNull(elseObject) : object;
  }

  /**
   * Check that there is not null value in array.
   *
   * @param objects array to be checked, must not be null
   * @param <T>     type of elements in array
   * @return the same array, must not be null
   * @throws NullPointerException if there is any null in array
   */
  @SafeVarargs
  public static <T> T[] ensureNoNullElement(final T... objects) {
    for (int i = 0; i < objects.length; i++) {
      if (objects[i] == null) {
        throw new NullPointerException("Unexpected null element at " + i + " index");
      }
    }
    return (T[]) objects;
  }

}
