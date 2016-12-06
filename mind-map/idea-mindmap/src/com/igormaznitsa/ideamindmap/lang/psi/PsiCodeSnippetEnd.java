package com.igormaznitsa.ideamindmap.lang.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;

import javax.annotation.Nonnull;

public class PsiCodeSnippetEnd extends ASTWrapperPsiElement implements MMPsiElement  {

  public PsiCodeSnippetEnd(@Nonnull final ASTNode node) {
    super(node);
  }
}