package com.igormaznitsa.ideamindmap.lang.tokens;

public class MMHeaderDelimiter extends MMElementType {
  public MMHeaderDelimiter() {
    super("HEADER_DELIMITER");
  }

  @SuppressWarnings( {"HardCodedStringLiteral"})
  public String toString() {
    return "HeaderDelimiter:" + super.toString();
  }

}
