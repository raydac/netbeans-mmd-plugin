package com.igormaznitsa.ideamindmap.lang.psi;

import com.igormaznitsa.mindmap.model.Extra;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import javax.annotation.Nonnull;
import org.apache.commons.text.StringEscapeUtils;

public abstract class AbstractExtraData extends ASTWrapperPsiElement implements MMPsiElement {

  private final String processedText;

  public AbstractExtraData(@Nonnull final ASTNode node) {
    super(node);
    final String text = node.getText();
    final String groupPre = getExtraType().preprocessString(text.substring(5, text.length() - 6));
    this.processedText = StringEscapeUtils.unescapeHtml3(groupPre);
  }

  public abstract Extra.ExtraType getExtraType();

  @Override
  @Nonnull
  public String getName() {
    return this.processedText;
  }

}
