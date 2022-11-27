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

import java.io.File;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Auxiliary topic finder object allows to check topic content for pattern
 */
public interface TopicFinder {
  /**
   * Check that topic contains content matches with pattern.
   *
   * @param topic      topic to be checked
   * @param baseFolder base folder for the project
   * @param pattern    pattern to find
   * @param extraTypes types of content
   * @return true if text found, false otherwise
   */
  boolean doesTopicContentMatches(Topic topic,
                                  File baseFolder,
                                  Pattern pattern,
                                  Set<Extra.ExtraType> extraTypes);
}
