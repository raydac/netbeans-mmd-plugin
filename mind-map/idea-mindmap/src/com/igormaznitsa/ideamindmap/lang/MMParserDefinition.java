package com.igormaznitsa.ideamindmap.lang;

import com.igormaznitsa.ideamindmap.lang.psi.PsiAttributes;
import com.igormaznitsa.ideamindmap.lang.psi.PsiCodeSnippetBody;
import com.igormaznitsa.ideamindmap.lang.psi.PsiCodeSnippetEnd;
import com.igormaznitsa.ideamindmap.lang.psi.PsiCodeSnippetStart;
import com.igormaznitsa.ideamindmap.lang.psi.PsiExtraBlock;
import com.igormaznitsa.ideamindmap.lang.psi.PsiExtraFile;
import com.igormaznitsa.ideamindmap.lang.psi.PsiExtraJump;
import com.igormaznitsa.ideamindmap.lang.psi.PsiExtraText;
import com.igormaznitsa.ideamindmap.lang.psi.PsiExtraType;
import com.igormaznitsa.ideamindmap.lang.psi.PsiExtraURI;
import com.igormaznitsa.ideamindmap.lang.psi.PsiHeadDelimiter;
import com.igormaznitsa.ideamindmap.lang.psi.PsiHeadLine;
import com.igormaznitsa.ideamindmap.lang.psi.PsiTopic;
import com.igormaznitsa.ideamindmap.lang.psi.PsiTopicLevel;
import com.igormaznitsa.ideamindmap.lang.psi.PsiTopicTitle;
import com.igormaznitsa.ideamindmap.lang.psi.PsiUnknown;
import com.igormaznitsa.ideamindmap.lang.tokens.MMElementType;
import com.igormaznitsa.ideamindmap.lang.tokens.MMTokens;
import com.igormaznitsa.meta.common.utils.Assertions;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import javax.annotation.Nonnull;

public class MMParserDefinition implements ParserDefinition {
  public static final IFileElementType FILE = new IFileElementType(MMLanguage.INSTANCE);
  private static final TokenSet WHITESPACES = TokenSet.create(MMTokens.WHITE_SPACE);
  private static final TokenSet STRING_LITERALS = TokenSet.create(MMTokens.TOPIC_TITLE, MMTokens.EXTRA_BODY, MMTokens.CODE_SNIPPET_BODY);

  @Nonnull
  @Override
  public Lexer createLexer(final Project project) {
    return new MMLexer();
  }

  @Override
  public PsiParser createParser(final Project project) {
    return new MMPsiParser();
  }

  @Override
  public IFileElementType getFileNodeType() {
    return FILE;
  }

  @Nonnull
  @Override
  public TokenSet getWhitespaceTokens() {
    return WHITESPACES;
  }

  @Nonnull
  @Override
  public TokenSet getCommentTokens() {
    return TokenSet.EMPTY;
  }

  @Nonnull
  @Override
  public TokenSet getStringLiteralElements() {
    return STRING_LITERALS;
  }

  @Nonnull
  @Override
  public PsiElement createElement(final ASTNode node) {
    final IElementType type = node.getElementType();
    if (type instanceof MMElementType) {

      if (type == MMTokens.HEADER_LINE) {
        return new PsiHeadLine(node);
      }

      if (type == MMTokens.HEADER_DELIMITER) {
        return new PsiHeadDelimiter(node);
      }

      if (type == MMTokens.ATTRIBUTES) {
        return new PsiAttributes(node);
      }

      if (type == MMTokens.TOPIC) {
        return new PsiTopic(node);
      }

      if (type == MMTokens.TOPIC_LEVEL) {
        return new PsiTopicLevel(node);
      }

      if (type == MMTokens.TOPIC_TITLE) {
        return new PsiTopicTitle(node);
      }

      if (type == MMTokens.EXTRA_TYPE) {
        return new PsiExtraType(node);
      }

      if (type == MMTokens.EXTRA_DATA) {
        return new PsiExtraBlock(node);
      }

      if (type == MMTokens.CODE_SNIPPET_START) {
        return new PsiCodeSnippetStart(node);
      }

      if (type == MMTokens.CODE_SNIPPET_BODY) {
        return new PsiCodeSnippetBody(node);
      }

      if (type == MMTokens.CODE_SNIPPET_END) {
        return new PsiCodeSnippetEnd(node);
      }

      if (type == MMTokens.EXTRA_BODY) {
        final PsiExtraBlock parent = (PsiExtraBlock) node.getTreeParent().getPsi();
        switch (parent.getType()) {
          case NOTE:
            return new PsiExtraText(node);
          case FILE:
            return new PsiExtraFile(node);
          case LINK:
            return new PsiExtraURI(node);
          case TOPIC:
            return new PsiExtraJump(node);
          default:
            throw Assertions.fail("Unexpected extra type " + parent.getType());
        }
      }
    }
    return new PsiUnknown(node);
  }

  @Override
  public PsiFile createFile(final FileViewProvider fileViewProvider) {
    return new MMDFile(fileViewProvider);
  }

  @Override
  public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode astNode, ASTNode astNode2) {
    return SpaceRequirements.MAY;
  }
}
