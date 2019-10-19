package com.igormaznitsa.ideamindmap.lang.tokens;

public class MMCodeSnippetEnd extends MMElementType {
  public MMCodeSnippetEnd() {
    super("CODESNIPPET_END");
  }

  @SuppressWarnings( {"HardCodedStringLiteral"})
  public String toString() {
    return "CodeSnippetEnd:" + super.toString();
  }

}
