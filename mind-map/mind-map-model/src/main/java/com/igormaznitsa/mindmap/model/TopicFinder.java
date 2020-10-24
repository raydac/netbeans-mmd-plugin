package com.igormaznitsa.mindmap.model;

import java.io.File;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface TopicFinder {
  /**
   * Check that topic contains content matches with pattern.
   *
   * @param topic      topic to be checked
   * @param baseFolder base folder for the project
   * @param pattern    pattern to find
   * @param extraTypes types of content
   * @return true if text found, false otherwise
   * @since 1.4.10
   */
  boolean doesTopicContentMatches(@Nonnull Topic topic, @Nullable File baseFolder,
                                  @Nonnull Pattern pattern,
                                  @Nullable Set<Extra.ExtraType> extraTypes);
}
