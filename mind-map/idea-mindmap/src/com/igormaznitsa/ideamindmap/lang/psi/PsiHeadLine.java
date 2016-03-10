package com.igormaznitsa.ideamindmap.lang.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;

import javax.annotation.Nonnull;

public class PsiHeadLine extends ASTWrapperPsiElement implements MMPsiElement  {

  public PsiHeadLine(@Nonnull final ASTNode node) {
    super(node);
  }
}