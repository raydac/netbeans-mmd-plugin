package com.igormaznitsa.ideamindmap.lang;

import com.intellij.lang.Language;

public class MindMapLanguage extends Language {

  public static final MindMapLanguage INSTANCE = new MindMapLanguage();

  public MindMapLanguage() {
    super("NBMindMap", "text/x-nbmmd+plain");
  }

}
