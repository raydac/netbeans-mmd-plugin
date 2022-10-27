package com.igormaznitsa.mindmap.model;

import static java.util.Objects.requireNonNull;

public final class MiscUtils {

  private MiscUtils() {

  }

  public static <T> T ensureNotNull(final T object, final T elseObject) {
    return object == null ? requireNonNull(elseObject) : object;
  }

  public static <T> T[] ensureDoesntHaveNull(final T... objects) {
    for (int i = 0; i < objects.length; i++) {
      if (objects[i] == null) {
        throw new NullPointerException("Unexpected null element at " + i + " index");
      }
    }
    return objects;
  }

}
