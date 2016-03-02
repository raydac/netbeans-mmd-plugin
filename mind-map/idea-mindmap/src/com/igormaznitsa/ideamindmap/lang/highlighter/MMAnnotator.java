package com.igormaznitsa.ideamindmap.lang.highlighter;

import com.igormaznitsa.ideamindmap.lang.psi.MMPsiElement;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.lang.annotation.Annotation;
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
import org.jetbrains.annotations.NotNull;

public class MMAnnotator implements Annotator {

  public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
    if (!(element instanceof MMPsiElement)) return;
    ASTNode keyNode = ((ASTWrapperPsiElement)element).getNode();
    highlightTokens(keyNode, holder, new MMHighlighter());
  }

  private static void highlightTokens(final ASTNode node, final AnnotationHolder holder, MMHighlighter highlighter) {
    Lexer lexer = highlighter.getHighlightingLexer();
    final String s = node.getText();
    lexer.start(s);

    while (lexer.getTokenType() != null) {
      IElementType elementType = lexer.getTokenType();
      TextAttributesKey[] keys = highlighter.getTokenHighlights(elementType);
      for (TextAttributesKey key : keys) {
        final Pair<String,HighlightSeverity> pair = MMHighlighter.DISPLAY_NAMES.get(key);
        final String displayName = pair.getFirst();
        final HighlightSeverity severity = pair.getSecond();
        if (severity != null) {
          int start = lexer.getTokenStart() + node.getTextRange().getStartOffset();
          int end = lexer.getTokenEnd() + node.getTextRange().getStartOffset();
          TextRange textRange = new TextRange(start, end);
          final Annotation annotation;
          if (severity == HighlightSeverity.WARNING) {
            annotation = holder.createWarningAnnotation(textRange, displayName);
          }
          else if (severity == HighlightSeverity.ERROR) {
            annotation = holder.createErrorAnnotation(textRange, displayName);
          }
          else {
            annotation = holder.createInfoAnnotation(textRange, displayName);
          }
          TextAttributes attributes = EditorColorsManager.getInstance().getGlobalScheme().getAttributes(key);
          annotation.setEnforcedTextAttributes(attributes);
        }
      }
      lexer.advance();
    }
  }
}
