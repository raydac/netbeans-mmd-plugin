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
    return objects;
  }

}
