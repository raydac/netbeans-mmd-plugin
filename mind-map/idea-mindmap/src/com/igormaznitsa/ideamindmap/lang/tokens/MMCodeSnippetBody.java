package com.igormaznitsa.ideamindmap.lang.tokens;

public class MMCodeSnippetBody extends MMElementType {
  public MMCodeSnippetBody() {
    super("CODESNIPPET_BODY");
  }

  @SuppressWarnings( {"HardCodedStringLiteral"})
  public String toString() {
    return "CodeSnippetBody:" + super.toString();
  }

}
