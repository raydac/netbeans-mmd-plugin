package com.igormaznitsa.ideamindmap.lang.psi;

import com.igormaznitsa.mindmap.model.ModelUtils;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import org.apache.commons.lang.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;

public class PsiTopic extends ASTWrapperPsiElement implements MMPsiElement {

  private final String unescapedText;
  private final int level;

  public PsiTopic(@NotNull final ASTNode node) {
    super(node);
    final String text = node.getText();
    this.level = ModelUtils.calcCharsOnStart('#', text);
    this.unescapedText = StringEscapeUtils.unescapeHtml(text.substring(level).trim());
  }

  @Override public String getName() {
    return this.unescapedText;
  }

}