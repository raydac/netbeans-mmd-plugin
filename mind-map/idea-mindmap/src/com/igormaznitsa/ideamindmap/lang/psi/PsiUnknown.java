package com.igormaznitsa.ideamindmap.lang.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

public class PsiUnknown extends ASTWrapperPsiElement implements MMPsiElement {

  public PsiUnknown(@NotNull final ASTNode node) {
    super(node);
  }
}