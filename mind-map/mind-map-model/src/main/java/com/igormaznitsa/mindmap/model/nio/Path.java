/*
 * Copyright 2015-2018 Igor Maznitsa.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.igormaznitsa.mindmap.model.nio;

import java.io.File;

/**
 * Interface of Path to be used in main map project to isolate implementation and provide way to replace file processing if needed.
 */
public interface Path extends Iterable<Path> {

  /**
   * Check that the path is started with path provided as argument.
   *
   * @param basePath path to be checked, must not be null
   * @return true if this path starts with the given path; otherwise false
   */
  boolean startsWith(Path basePath);

  /**
   * Check that path is absolute one
   *
   * @return true if path is absolute one, false otherwise
   */
  boolean isAbsolute();

  /**
   * Constructs a relative path between this path and a given path.
   *
   * @param filePath path to search relative one, must not be null
   * @return the resulting relative path, or an empty path if both paths are equal
   * @throws IllegalArgumentException if other is not a Path that can be relativized against this path
   */
  Path relativize(Path filePath);

  /**
   * Returns the root component of this path as a Path object, or null if this path does not have a root component.
   *
   * @return a path representing the root component of this path, or null
   */
  Path getRoot();

  /**
   * Returns a File object representing this path. Where this Path is associated with the default provider, then this method is equivalent to returning a File object constructed with the String representation of this path.
   * If this path was created by invoking the File toPath method then there is no guarantee that the File object returned by this method is equal to the original File.
   *
   * @return a File object representing this path
   * @throws UnsupportedOperationException if this Path is not associated with the default provider
   */
  File toFile();
}
