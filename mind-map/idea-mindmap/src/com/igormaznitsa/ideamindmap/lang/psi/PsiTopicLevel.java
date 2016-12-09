package com.igormaznitsa.ideamindmap.lang.psi;

import com.igormaznitsa.mindmap.model.ModelUtils;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;

import javax.annotation.Nonnull;

public class PsiTopicLevel extends ASTWrapperPsiElement implements MMPsiElement {

  private final int level;

  public PsiTopicLevel(@Nonnull final ASTNode node) {
    super(node);
    final String text = node.getText();
    this.level = ModelUtils.calcCharsOnStart('#', text);
  }

  public int getLevel(){
    return this.level;
  }

}