package com.igormaznitsa.ideamindmap.lang.psi;

import com.igormaznitsa.mindmap.model.Extra;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

public class PsiExtraText extends AbstractExtraData {

  public PsiExtraText(@NotNull final ASTNode node) {
    super(node);
  }

  @Override public Extra.ExtraType getExtraType() {
    return Extra.ExtraType.NOTE;
  }
}