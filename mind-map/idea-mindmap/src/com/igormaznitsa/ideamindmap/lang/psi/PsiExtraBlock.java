package com.igormaznitsa.ideamindmap.lang.psi;

import com.igormaznitsa.ideamindmap.lang.tokens.MMTokens;
import com.igormaznitsa.mindmap.model.Extra;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PsiExtraBlock extends ASTWrapperPsiElement implements MMPsiElement {
  public PsiExtraBlock(@Nonnull final ASTNode node) {
    super(node);
  }

  @Nullable
  public Extra.ExtraType getType() {
    final PsiExtraType type = this.findChildByType(MMTokens.EXTRA_TYPE);
    if (type == null) {
      return null;
    }
    return type.getType();
  }
}