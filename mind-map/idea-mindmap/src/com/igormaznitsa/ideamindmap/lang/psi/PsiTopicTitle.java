package com.igormaznitsa.ideamindmap.lang.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import javax.annotation.Nonnull;
import org.apache.commons.text.StringEscapeUtils;

public class PsiTopicTitle extends ASTWrapperPsiElement implements MMPsiElement {

  private final String unescapedText;

  public PsiTopicTitle(@Nonnull final ASTNode node) {
    super(node);
    this.unescapedText = StringEscapeUtils.unescapeHtml3(node.getText());
  }

  @Override
  @Nonnull
  public String getName() {
    return this.unescapedText;
  }

}