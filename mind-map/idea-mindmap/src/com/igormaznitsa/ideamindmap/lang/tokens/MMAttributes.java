package com.igormaznitsa.ideamindmap.lang.tokens;

public class MMAttributes extends MMElementType {
  public MMAttributes() {
    super("ATTRIBUTES");
  }

  @SuppressWarnings( {"HardCodedStringLiteral"})
  public String toString() {
    return "Attributes:" + super.toString();
  }

}
