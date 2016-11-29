package com.igormaznitsa.ideamindmap.lang.tokens;

public class MMCodeSnippetStart extends MMElementType {
  public MMCodeSnippetStart() {
    super("CODESNIPPET_START");
  }

  @SuppressWarnings({"HardCodedStringLiteral"})
  public String toString() {
    return "CodeSnippetStart:" + super.toString();
  }

}
