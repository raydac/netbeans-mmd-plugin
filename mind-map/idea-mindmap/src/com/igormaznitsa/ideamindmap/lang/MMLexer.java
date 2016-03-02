package com.igormaznitsa.ideamindmap.lang;

import com.igormaznitsa.ideamindmap.lang.tokens.MMTokens;
import com.igormaznitsa.meta.common.utils.Assertions;
import com.intellij.lexer.Lexer;
import com.intellij.lexer.LexerPosition;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MMLexer extends Lexer {

  private final com.igormaznitsa.mindmap.model.parser.MindMapLexer delegate = new com.igormaznitsa.mindmap.model.parser.MindMapLexer();

  private static final class Position implements LexerPosition {

    private com.igormaznitsa.mindmap.model.parser.MindMapLexer.LexerPosition pos;

    public void load(@NotNull final com.igormaznitsa.mindmap.model.parser.MindMapLexer.LexerPosition position) {
      if (this.pos == null) {
        this.pos = position.makeCopy();
      }
      else {
        this.pos.set(position);
      }
    }

    public void save(@NotNull final com.igormaznitsa.mindmap.model.parser.MindMapLexer.LexerPosition position) {
      position.set(this.pos);
    }

    @Override public int getOffset() {
      return this.pos.getOffset();
    }

    @Override public int getState() {
      return this.pos.getState().ordinal();
    }
  }

  private final Position pos = new Position();

  @Override public void start(@NotNull final CharSequence buffer, final int startOffset, final int endOffset, final int initialState) {
    this.delegate.start(buffer, startOffset, endOffset, com.igormaznitsa.mindmap.model.parser.MindMapLexer.TokenType.values()[initialState]);
    this.delegate.advance();
  }

  @Override public int getState() {
    return this.delegate.getCurrentPosition().getState().ordinal();
  }

  @Nullable @Override public IElementType getTokenType() {
    final com.igormaznitsa.mindmap.model.parser.MindMapLexer.TokenType type = this.delegate.getTokenType();

    IElementType result = null;

    if (type != null) {
      switch (type) {
      case HEAD_LINE:
        result = MMTokens.HEADER_LINE;
        break;
      case HEAD_DELIMITER:
        result = MMTokens.HEADER_DELIMITER;
        break;
      case ATTRIBUTE:
        result = MMTokens.ATTRIBUTES;
        break;
      case WHITESPACE:
        result = MMTokens.WHITE_SPACE;
        break;
      case EXTRA_TYPE:
        result = MMTokens.EXTRA_TYPE;
      break;
      case EXTRA_TEXT:
        result = MMTokens.EXTRA_BODY;
      break;
      case TOPIC:
        result = MMTokens.TOPIC;
      break;
      case UNKNOWN_LINE:
        result = MMTokens.UNKNOWN;
        break;
      default:
        throw Assertions.fail("Unsupported token detected [" + type + ']');
      }
    }
    return result;
  }

  @Override public int getTokenStart() {
    return this.delegate.getTokenStartOffset();
  }

  @Override public int getTokenEnd() {
    return this.delegate.getTokenEndOffset();
  }

  @Override public void advance() {
    this.delegate.advance();
  }

  @NotNull @Override public LexerPosition getCurrentPosition() {
    this.pos.load(this.delegate.getCurrentPosition());
    return this.pos;
  }

  @Override public void restore(@NotNull LexerPosition position) {
    this.pos.save(this.delegate.getCurrentPosition());
  }

  @NotNull @Override public CharSequence getBufferSequence() {
    return this.delegate.getBufferSequence();
  }

  @Override public int getBufferEnd() {
    return this.delegate.getBufferEnd();
  }
}
