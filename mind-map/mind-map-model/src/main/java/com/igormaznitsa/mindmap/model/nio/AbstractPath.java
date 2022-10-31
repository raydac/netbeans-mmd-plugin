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
 * Abstract path object to be used for mind map purposes.
 */
public abstract class AbstractPath implements Path {

  /**
   * Constructor.
   */
  protected AbstractPath() {
  }

  /**
   * File based constructor
   *
   * @param file used to create path
   */
  public AbstractPath(final File file) {
  }

  /**
   * Constructor to create path based on list of path items
   *
   * @param first first path item, must not be null
   * @param items rest of path items
   */
  public AbstractPath(final String first,
                      final String... items) {
  }

}
