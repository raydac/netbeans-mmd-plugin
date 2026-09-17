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

package com.igormaznitsa.ideamindmap.lang.highlighter;

import com.igormaznitsa.ideamindmap.lang.psi.MMPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import javax.annotation.Nonnull;

public class MMAnnotator implements Annotator {

  private static void highlightTokens(final ASTNode node, final AnnotationHolder holder, final MMHighlighter highlighter) {
    Lexer lexer = highlighter.getHighlightingLexer();
    final String s = node.getText();
    lexer.start(s);

    while (lexer.getTokenType() != null) {
      IElementType elementType = lexer.getTokenType();
      TextAttributesKey[] keys = highlighter.getTokenHighlights(elementType);
      for (TextAttributesKey key : keys) {
        final Pair<String, HighlightSeverity> pair = MMHighlighter.DISPLAY_NAMES.get(key);
        final String displayName = pair.getFirst();
        final HighlightSeverity severity = pair.getSecond();
        if (severity != null) {
          final int start = lexer.getTokenStart() + node.getTextRange().getStartOffset();
          final int end = lexer.getTokenEnd() + node.getTextRange().getStartOffset();
          final TextAttributes attributes = EditorColorsManager.getInstance().getGlobalScheme().getAttributes(key);
          holder.newAnnotation(severity, displayName)
                  .range(new TextRange(start, end))
                  .enforcedTextAttributes(attributes)
                  .create();
        }
      }
      lexer.advance();
    }
  }

  @Override
  public void annotate(@Nonnull final PsiElement element, @Nonnull final AnnotationHolder holder) {
    if (!(element instanceof MMPsiElement)) {
      return;
    }
    final ASTNode keyNode = element.getNode();
    highlightTokens(keyNode, holder, new MMHighlighter());
  }
}
