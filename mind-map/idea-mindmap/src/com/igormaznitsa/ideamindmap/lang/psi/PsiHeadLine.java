package com.igormaznitsa.ideamindmap.lang.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

public class PsiHeadLine extends ASTWrapperPsiElement implements MMPsiElement  {

  public PsiHeadLine(@NotNull final ASTNode node) {
    super(node);
  }
}