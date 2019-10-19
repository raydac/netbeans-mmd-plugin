package com.igormaznitsa.ideamindmap.lang.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import javax.annotation.Nonnull;

public class PsiCodeSnippetStart extends ASTWrapperPsiElement implements MMPsiElement {

  public PsiCodeSnippetStart(@Nonnull final ASTNode node) {
    super(node);
  }
}