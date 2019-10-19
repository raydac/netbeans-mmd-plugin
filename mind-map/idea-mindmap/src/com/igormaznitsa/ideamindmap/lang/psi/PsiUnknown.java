package com.igormaznitsa.ideamindmap.lang.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import javax.annotation.Nonnull;

public class PsiUnknown extends ASTWrapperPsiElement implements MMPsiElement {

  public PsiUnknown(@Nonnull final ASTNode node) {
    super(node);
  }
}