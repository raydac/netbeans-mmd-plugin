package com.igormaznitsa.ideamindmap.lang;

import com.igormaznitsa.ideamindmap.lang.tokens.MMTokens;
import com.igormaznitsa.meta.common.utils.Assertions;
import com.igormaznitsa.mindmap.model.ModelUtils;
import com.intellij.lang.ASTNode;
import com.intellij.lang.LightPsiParser;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.tree.IElementType;
import javax.annotation.Nonnull;

public class MMPsiParser implements PsiParser, LightPsiParser {
  @Nonnull
  @Override
  public ASTNode parse(@Nonnull final IElementType root, @Nonnull final PsiBuilder builder) {
    parseLight(root, builder);
    final ASTNode result = builder.getTreeBuilt();
    return result;
  }

  @Override
  public void parseLight(final IElementType root, final PsiBuilder builder) {
    builder.setDebugMode(ApplicationManager.getApplication().isUnitTestMode());
    final PsiBuilder.Marker marker = builder.mark();

    parseHeader(builder);
    parseTopics(builder);

    marker.done(root);
  }

  private void parseHeader(@Nonnull final PsiBuilder builder) {
    boolean doLoop = true;
    while (doLoop && !builder.eof()) {
      final PsiBuilder.Marker marker = builder.mark();

      if (builder.getTokenType() == null) {
        marker.drop();
      } else {
        final IElementType token = builder.getTokenType();

        if (token == MMTokens.HEADER_DELIMITER) {
          marker.done(token);
          doLoop = false;
        } else if (token == MMTokens.HEADER_LINE
            || token == MMTokens.UNKNOWN
            || token == MMTokens.WHITE_SPACE
            || token == MMTokens.ATTRIBUTES) {
          marker.done(token);
        } else {
          throw Assertions.fail("Unexpected header token : " + token);
        }
      }
      builder.advanceLexer();
    }
  }

  private void parseTopics(@Nonnull final PsiBuilder builder) {
    while (!builder.eof()) {
      final PsiBuilder.Marker marker = builder.mark();
      final IElementType token = builder.getTokenType();

      if (token == null) {
        marker.drop();
      } else {
        if (token == MMTokens.TOPIC_LEVEL) {
          final PsiBuilder.Marker levelMarker = builder.mark();
          levelMarker.done(token);
          final int topicLevel = ModelUtils.calcCharsOnStart('#', builder.getTokenText());
          if (topicLevel != 1) {
            marker.done(MMTokens.UNKNOWN);
          } else {
            builder.advanceLexer();
            recursiveParseTopic(builder, topicLevel);
            marker.done(MMTokens.TOPIC);
          }
        } else {
          marker.done(MMTokens.UNKNOWN);
        }
      }
      builder.advanceLexer();
    }
  }

  private int recursiveParseTopic(@Nonnull final PsiBuilder builder, final int level) {

    while (!builder.eof()) {
      final PsiBuilder.Marker marker = builder.mark();

      final IElementType token = builder.getTokenType();

      if (token == null) {
        marker.drop();
      } else {
        if (token == MMTokens.TOPIC_LEVEL) {
          final PsiBuilder.Marker levelMarker = builder.mark();
          levelMarker.done(token);

          final int theTopicLevel = ModelUtils.calcCharsOnStart('#', builder.getTokenText());
          if (theTopicLevel <= 1) {
            marker.done(MMTokens.UNKNOWN);
          } else {
            if (theTopicLevel <= level) {
              marker.rollbackTo();
              return theTopicLevel;
            } else {
              builder.advanceLexer();
              final int parsedTopicLevel = recursiveParseTopic(builder, theTopicLevel);
              marker.done(MMTokens.TOPIC);
              if (parsedTopicLevel < theTopicLevel) {
                return parsedTopicLevel;
              }
              if (parsedTopicLevel == theTopicLevel) {
                continue;
              }
            }
          }
        } else if (token == MMTokens.TOPIC_TITLE || token == MMTokens.CODE_SNIPPET_BODY || token == MMTokens.CODE_SNIPPET_END || token == MMTokens.CODE_SNIPPET_START || token == MMTokens.ATTRIBUTES) {
          marker.done(token);
        } else if (token == MMTokens.EXTRA_TYPE) {
          try {
            if (parseExtraBlock(builder)) {
              continue;
            }
          } finally {
            marker.done(MMTokens.EXTRA_DATA);
          }
        } else if (token == MMTokens.WHITE_SPACE) {
          marker.done(token);
        } else {
          marker.done(MMTokens.UNKNOWN);
        }
      }
      builder.advanceLexer();
    }


    return level;
  }

  private boolean parseExtraBlock(@Nonnull final PsiBuilder builder) {
    // read type
    final PsiBuilder.Marker type = builder.mark();
    if (builder.getTokenType() != MMTokens.EXTRA_TYPE) {
      throw Assertions.fail("Unexpected token " + builder.getTokenType());
    }
    builder.advanceLexer();
    type.done(MMTokens.EXTRA_TYPE);

    boolean dataFound = false;

    // read body
    while (!builder.eof()) {
      final PsiBuilder.Marker marker = builder.mark();
      if (builder.eof() || builder.getTokenType() == null) {
        marker.drop();
        break;
      } else {
        final IElementType token = builder.getTokenType();
        if (token == MMTokens.TOPIC_LEVEL || token == MMTokens.EXTRA_TYPE) {
          marker.rollbackTo();
          return true;
        } else if (token == MMTokens.EXTRA_BODY || token == MMTokens.WHITE_SPACE) {
          if (dataFound && token == MMTokens.EXTRA_BODY) {
            builder.advanceLexer();
            marker.done(MMTokens.UNKNOWN);
            break;
          } else {
            builder.advanceLexer();
            marker.done(token);
            dataFound = dataFound || token == MMTokens.EXTRA_BODY;
          }
        } else {
          marker.done(MMTokens.UNKNOWN);
          break;
        }
      }
    }
    return false;
  }

}
