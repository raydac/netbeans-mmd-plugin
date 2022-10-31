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
 * Service to create path objects.
 */
public interface PathService {
  /**
   * Find or create path for provided file
   *
   * @param file file to be used for target path, must not be null
   * @return found created path, must not be null
   * @throws java.nio.file.InvalidPathException if a Path object cannot be constructed from the abstract path
   */
  Path getForFile(File file);


  /**
   * Converts a path string, or a sequence of strings that when joined form a path string, to a Path.
   * Params:
   *
   * @param first the path string or initial part of the path string, must not be null
   * @param items additional strings to be joined to form the path string
   * @return the resulting Path, must not be null
   * @throws java.nio.file.InvalidPathException if the path string cannot be converted to a Path
   */
  Path getForPathItems(String first, String... items);
}
