/*
 * Copyright (C) 2015-2022 Igor A. Maznitsa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.igormaznitsa.ideamindmap.lang.psi;

import com.igormaznitsa.mindmap.model.Extra;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.util.IncorrectOperationException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.jetbrains.annotations.NonNls;

public class PsiExtraType extends ASTWrapperPsiElement implements PsiNamedElement, MMPsiElement {

  private final Extra.ExtraType type;

  public PsiExtraType(@Nonnull final ASTNode node) {
    super(node);
    final String text = node.getText();
    Extra.ExtraType result;
    if (text.length() > 1) {
      try {
        result = Extra.ExtraType.valueOf(text.substring(1).trim());
      } catch (IllegalArgumentException ex) {
        result = null;
      }
    } else {
      result = null;
    }
    this.type = result;
  }

  @Nullable
  public Extra.ExtraType getType() {
    return type;
  }

  @Override
  public PsiElement setName(@NonNls @Nonnull String name) throws IncorrectOperationException {
    throw new IncorrectOperationException("Can't rename");
  }
}