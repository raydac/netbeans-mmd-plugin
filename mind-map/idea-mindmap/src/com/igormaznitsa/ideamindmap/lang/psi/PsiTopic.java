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

import com.igormaznitsa.mindmap.model.ModelUtils;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import javax.annotation.Nonnull;
import org.apache.commons.text.StringEscapeUtils;

public class PsiTopic extends ASTWrapperPsiElement implements MMPsiElement {

  private final String unescapedText;
  private final int level;

  public PsiTopic(@Nonnull final ASTNode node) {
    super(node);
    final String text = node.getText();
    this.level = ModelUtils.countPrefixChars('#', text);
    this.unescapedText = StringEscapeUtils.unescapeHtml3(text.substring(level).trim());
  }

  @Override
  public String getName() {
    return this.unescapedText;
  }

}