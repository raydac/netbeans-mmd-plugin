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

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import javax.annotation.Nonnull;
import org.apache.commons.text.StringEscapeUtils;

public class PsiTopicTitle extends ASTWrapperPsiElement implements MMPsiElement {

  private final String unescapedText;

  public PsiTopicTitle(@Nonnull final ASTNode node) {
    super(node);
    this.unescapedText = StringEscapeUtils.unescapeHtml3(node.getText());
  }

  @Override
  @Nonnull
  public String getName() {
    return this.unescapedText;
  }

}