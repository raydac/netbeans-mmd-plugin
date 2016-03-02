package com.igormaznitsa.ideamindmap.lang.psi;

import com.igormaznitsa.mindmap.model.Extra;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import org.apache.commons.lang.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractExtraData extends ASTWrapperPsiElement implements MMPsiElement {

  private final String processedText;

  public AbstractExtraData(@NotNull final ASTNode node) {
    super(node);
    final String text = node.getText();
    final String groupPre = getExtraType().preprocessString(text.substring(5, text.length() - 6));
    this.processedText = StringEscapeUtils.unescapeHtml(groupPre);
  }

  public abstract Extra.ExtraType getExtraType();

  @Override public String getName() {
    return this.processedText;
  }

}
