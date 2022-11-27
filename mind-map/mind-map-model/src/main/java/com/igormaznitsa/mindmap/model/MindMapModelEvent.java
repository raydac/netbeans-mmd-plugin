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
 * Object contains mind map model event.
 */
public class MindMapModelEvent {
  private static final Topic[] EMPTY = new Topic[0];
  private final MindMap source;
  private final Topic[] path;

  /**
   * Constructor.
   *
   * @param source source mind map must not be null
   * @param path   path to changed topic, can be null
   */
  public MindMapModelEvent(
      final MindMap source,
      final Topic[] path
  ) {
    this.source = requireNonNull(source);
    this.path = path == null ? EMPTY : path.clone();
  }

  /**
   * Source mind map
   *
   * @return source mind map, must not be null
   */
  public MindMap getSource() {
    return this.source;
  }

  /**
   * Path to changed topic.
   *
   * @return array of topics as path to the changed one, must not be null
   */
  public Topic[] getPath() {
    return this.path;
  }
}
