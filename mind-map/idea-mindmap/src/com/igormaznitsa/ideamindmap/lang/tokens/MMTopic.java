package com.igormaznitsa.ideamindmap.lang.tokens;

public class MMTopic extends MMElementType {
  public MMTopic() {
    super("TOPIC");
  }

  @SuppressWarnings({"HardCodedStringLiteral"})
  public String toString() {
    return "Topic:" + super.toString();
  }

}
