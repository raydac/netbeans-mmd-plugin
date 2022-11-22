package com.igormaznitsa.ideamindmap.lang.psi;

import com.igormaznitsa.mindmap.model.ModelUtils;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import javax.annotation.Nonnull;
import org.apache.commons.text.StringEscapeUtils;

public class PsiTopic extends ASTWrapperPsiElement implements MMPsiElement {

  private final String unescapedText;
  private final int level;

  public PsiTopic(@Nonnull final ASTNode node) {
    super(node);
    final String text = node.getText();
    this.level = ModelUtils.countPrefixChars('#', text);
    this.unescapedText = StringEscapeUtils.unescapeHtml3(text.substring(level).trim());
  }

  @Override
  public String getName() {
    return this.unescapedText;
  }

}