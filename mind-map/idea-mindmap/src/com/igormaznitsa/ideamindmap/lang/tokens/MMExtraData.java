package com.igormaznitsa.ideamindmap.lang.tokens;

public class MMExtraData extends MMElementType {
  public MMExtraData() {
    super("EXTRA_DATA");
  }

  @SuppressWarnings( {"HardCodedStringLiteral"})
  public String toString() {
    return "AbstractExtraData:" + super.toString();
  }

}
