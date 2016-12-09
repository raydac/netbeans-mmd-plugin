package com.igormaznitsa.ideamindmap.lang.tokens;

public class MMTopicTitle extends MMElementType {
  public MMTopicTitle() {
    super("TOPIC_TITLE");
  }

  @SuppressWarnings({"HardCodedStringLiteral"})
  public String toString() {
    return "Topic.Title:" + super.toString();
  }

}
