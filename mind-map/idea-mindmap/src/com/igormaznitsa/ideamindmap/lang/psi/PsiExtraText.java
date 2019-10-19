package com.igormaznitsa.ideamindmap.lang.psi;

import com.igormaznitsa.mindmap.model.Extra;
import com.intellij.lang.ASTNode;
import javax.annotation.Nonnull;

public class PsiExtraText extends AbstractExtraData {

  public PsiExtraText(@Nonnull final ASTNode node) {
    super(node);
  }

  @Override
  public Extra.ExtraType getExtraType() {
    return Extra.ExtraType.NOTE;
  }
}