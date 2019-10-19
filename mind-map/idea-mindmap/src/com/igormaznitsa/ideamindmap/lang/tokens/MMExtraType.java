package com.igormaznitsa.ideamindmap.lang.tokens;

public class MMExtraType extends MMElementType {
  public MMExtraType() {
    super("EXTRA_TYPE");
  }

  @SuppressWarnings( {"HardCodedStringLiteral"})
  public String toString() {
    return "ExtraType:" + super.toString();
  }

}
