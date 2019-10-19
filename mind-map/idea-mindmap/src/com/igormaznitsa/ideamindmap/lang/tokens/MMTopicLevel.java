package com.igormaznitsa.ideamindmap.lang.tokens;

public class MMTopicLevel extends MMElementType {
  public MMTopicLevel() {
    super("TOPIC_LEVEL");
  }

  @SuppressWarnings( {"HardCodedStringLiteral"})
  public String toString() {
    return "Topic.Level:" + super.toString();
  }

}
