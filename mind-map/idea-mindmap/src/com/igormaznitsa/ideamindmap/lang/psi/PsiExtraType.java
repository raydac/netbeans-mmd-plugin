package com.igormaznitsa.ideamindmap.lang.psi;

import com.igormaznitsa.mindmap.model.Extra;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PsiExtraType extends ASTWrapperPsiElement implements PsiNamedElement, MMPsiElement {

  private final Extra.ExtraType type;

  public PsiExtraType(@Nonnull final ASTNode node) {
    super(node);
    final String text = node.getText();
    Extra.ExtraType result = null;
    if (text.length() > 1) {
      try {
        result = Extra.ExtraType.valueOf(text.substring(1).trim());
      }catch (IllegalArgumentException ex){
        result = null;
      }
    }else{
      result = null;
    }
    this.type = result;
  }

  @Nullable
  public Extra.ExtraType getType(){
    return type;
  }

  @Override public PsiElement setName(@NonNls @Nonnull String name) throws IncorrectOperationException {
    throw new IncorrectOperationException("Can't rename");
  }
}