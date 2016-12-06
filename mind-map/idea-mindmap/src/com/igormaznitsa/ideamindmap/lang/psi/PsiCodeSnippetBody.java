package com.igormaznitsa.ideamindmap.lang.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;

import javax.annotation.Nonnull;

public class PsiCodeSnippetBody extends ASTWrapperPsiElement implements MMPsiElement  {

  private final String text;

  public PsiCodeSnippetBody(@Nonnull final ASTNode node) {
    super(node);
    text = node.getText();
  }
}