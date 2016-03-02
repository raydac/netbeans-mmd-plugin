package com.igormaznitsa.ideamindmap.lang.psi;

import com.igormaznitsa.mindmap.model.Extra;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

public class PsiExtraURI extends AbstractExtraData {

  public PsiExtraURI(@NotNull final ASTNode node) {
    super(node);
  }

  @Override public Extra.ExtraType getExtraType() {
    return Extra.ExtraType.LINK;
  }
}