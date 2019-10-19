package com.igormaznitsa.ideamindmap.lang.tokens;

public class MMHeaderLine extends MMElementType {
  public MMHeaderLine() {
    super("HEADER_LINE");
  }

  @SuppressWarnings( {"HardCodedStringLiteral"})
  public String toString() {
    return "HeaderLine:" + super.toString();
  }

}
