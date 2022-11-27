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

package com.igormaznitsa.ideamindmap.lang;

import com.igormaznitsa.ideamindmap.lang.tokens.MMTokens;
import com.igormaznitsa.meta.common.utils.Assertions;
import com.intellij.lexer.Lexer;
import com.intellij.lexer.LexerPosition;
import com.intellij.psi.tree.IElementType;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MMLexer extends Lexer {

  private final com.igormaznitsa.mindmap.model.parser.MindMapLexer delegate = new com.igormaznitsa.mindmap.model.parser.MindMapLexer();
  private final Position pos = new Position();

  @Override
  public void start(@Nonnull final CharSequence buffer, final int startOffset, final int endOffset, final int initialState) {
    this.delegate.start(buffer, startOffset, endOffset, com.igormaznitsa.mindmap.model.parser.MindMapLexer.TokenType.values()[initialState]);
    this.delegate.advance();
  }

  @Override
  public int getState() {
    return this.delegate.getCurrentPosition().getState().ordinal();
  }

  @Nullable
  @Override
  public IElementType getTokenType() {
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
        case CODE_SNIPPET_START:
          result = MMTokens.CODE_SNIPPET_START;
          break;
        case CODE_SNIPPET_BODY:
          result = MMTokens.CODE_SNIPPET_BODY;
          break;
        case CODE_SNIPPET_END:
          result = MMTokens.CODE_SNIPPET_END;
          break;
        case TOPIC_TITLE:
          result = MMTokens.TOPIC_TITLE;
          break;
        case TOPIC_LEVEL:
          result = MMTokens.TOPIC_LEVEL;
          break;
        case UNKNOWN_LINE: {
          result = MMTokens.UNKNOWN;
        }
        break;
        default:
          throw Assertions.fail("Unsupported token detected [" + type + ']');
      }
    }
    return result;
  }

  @Override
  public int getTokenStart() {
    return this.delegate.getTokenStartOffset();
  }

  @Override
  public int getTokenEnd() {
    return this.delegate.getTokenEndOffset();
  }

  @Override
  public void advance() {
    final int position = this.delegate.getCurrentPosition().getOffset();
    this.delegate.advance();

    // processing situation with wrong document contains non-completed token
    if (position == this.delegate.getCurrentPosition().getOffset()) {
      this.delegate.resetTokenTypeToNull();
    }
  }

  @Nonnull
  @Override
  public LexerPosition getCurrentPosition() {
    this.pos.load(this.delegate.getCurrentPosition());
    return this.pos;
  }

  @Override
  public void restore(@Nonnull LexerPosition position) {
    this.pos.save(this.delegate.getCurrentPosition());
  }

  @Nonnull
  @Override
  public CharSequence getBufferSequence() {
    return this.delegate.getBufferSequence();
  }

  @Override
  public int getBufferEnd() {
    return this.delegate.getBufferEnd();
  }

  private static final class Position implements LexerPosition {

    private com.igormaznitsa.mindmap.model.parser.MindMapLexer.LexerPosition pos;

    public void load(@Nonnull final com.igormaznitsa.mindmap.model.parser.MindMapLexer.LexerPosition position) {
      if (this.pos == null) {
        this.pos = position.makeCopy();
      } else {
        this.pos.set(position);
      }
    }

    public void save(@Nonnull final com.igormaznitsa.mindmap.model.parser.MindMapLexer.LexerPosition position) {
      position.set(this.pos);
    }

    @Override
    public int getOffset() {
      return this.pos.getOffset();
    }

    @Override
    public int getState() {
      return this.pos.getState().ordinal();
    }
  }
}
