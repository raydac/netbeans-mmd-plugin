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
