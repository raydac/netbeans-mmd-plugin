/*
 * Copyright 2016 Igor Maznitsa.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.igormaznitsa.mindmap.model.parser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.igormaznitsa.meta.annotation.ReturnsOriginal;
import com.igormaznitsa.meta.common.utils.Assertions;

/**
 * Allows to extract lexeme from mind map file.
 */
public final class MindMapLexer {
  /**
   * Type of allowed lexeme.
   */
  public enum TokenType {
    HEAD_LINE,
    HEAD_DELIMITER,
    ATTRIBUTE,
    TOPIC,
    EXTRA_TYPE,
    EXTRA_TEXT,
    WHITESPACE,
    UNKNOWN_LINE
  }

  /**
   * Class contains information about current lexer state.
   */
  public static final class LexerPosition {

    private int offset;
    private TokenType state = TokenType.HEAD_LINE;
    private boolean tokenCompleted;

    private LexerPosition(@Nonnull final LexerPosition pos) {
      this.offset = pos.offset;
      this.state = pos.state;
      this.tokenCompleted = pos.tokenCompleted;
    }

    private LexerPosition(final int offset, @Nonnull final TokenType state) {
      this.tokenCompleted = true;
      this.offset = offset;
      this.state = Assertions.assertNotNull(state);
    }

    public int getOffset() {
      return this.offset;
    }

    public boolean isTokenCompleted() {
      return this.tokenCompleted;
    }

    @Nonnull
    public TokenType getState() {
      return this.state;
    }

    public void set(@Nullable final LexerPosition position) {
      if (position != null && this != position) {
        this.offset = position.offset;
        this.state = position.state;
        this.tokenCompleted = position.tokenCompleted;
      }
    }

    public LexerPosition makeCopy() {
      return new LexerPosition(this);
    }
  }

  private CharSequence buffer = "";
  private int endOffset;
  private int tokenStart;
  private int tokenEnd;
  private TokenType tokenType = TokenType.UNKNOWN_LINE;
  private final LexerPosition position = new LexerPosition(0, TokenType.UNKNOWN_LINE);

  public int getTokenStartOffset() {
    return this.tokenStart;
  }

  public int getTokenEndOffset() {
    return this.tokenEnd;
  }

  public void start(@Nonnull final CharSequence buffer, final int startOffset, final int endOffset, @Nonnull final MindMapLexer.TokenType initialState) {
    this.buffer = buffer;
    this.tokenType = initialState;
    this.position.offset = startOffset;
    this.position.tokenCompleted = true;
    this.position.state = this.tokenType;
    this.endOffset = endOffset;
  }

  public void setBufferEndOffset(final int value) {
    this.endOffset = value;
  }

  @Nonnull
  public CharSequence getTokenSequence() {
    return getBufferSequence().subSequence(this.tokenStart, this.tokenEnd);
  }

  @Nonnull
  public String getTokenText() {
    return getTokenSequence().toString();
  }

  @Nullable
  public TokenType getTokenType() {
    return this.tokenStart == this.tokenEnd ? null : this.tokenType;
  }

  @Nonnull
  public TokenPosition makeTokenPosition() {
    return new TokenPosition(this.tokenStart, this.tokenEnd);
  }

  public void advance() {
    boolean tokenHasBeenCompleted = this.position.isTokenCompleted();
    this.tokenStart = tokenHasBeenCompleted ? this.position.offset : this.tokenStart;
    boolean inAction = true;

    while (inAction && !isBufferEnd()) {
      switch (this.position.state) {
        case HEAD_LINE: {
          tokenHasBeenCompleted = skipToNextLine();
          if (tokenHasBeenCompleted && isAllLineFromChars('-')) {
            this.position.state = TokenType.HEAD_DELIMITER;
          }
          inAction = false;
        }
        break;
        case HEAD_DELIMITER: {
          this.position.state = TokenType.WHITESPACE;
        }
        break;
        case WHITESPACE: {
          skipAllWhitespaceAndSpecial();
          if (this.position.offset > this.tokenStart || isBufferEnd()) {
            tokenHasBeenCompleted = true;
            inAction = false;
          }
          else {
            final char chr = readChar();
            switch (chr) {
              case '#': {
                this.position.state = TokenType.TOPIC;
              }
              break;
              case '-':
              case '>': {
                if (isBufferEnd()) {
                  this.position.state = chr == '>' ? TokenType.ATTRIBUTE : TokenType.EXTRA_TYPE;
                  tokenHasBeenCompleted = false;
                  inAction = false;
                }
                else {
                  this.position.state = readChar() == ' ' ? chr == '>' ? TokenType.ATTRIBUTE : TokenType.EXTRA_TYPE : TokenType.UNKNOWN_LINE;
                }
              }
              break;
              case '<': {
                tokenHasBeenCompleted = false;
                this.position.state = TokenType.EXTRA_TEXT;
              }
              break;
              default: {
                this.position.state = TokenType.UNKNOWN_LINE;
              }
              break;
            }
          }
        }
        break;
        case EXTRA_TEXT: {
          if (getTokenLength() <= 5 && !isTokenMayStartWith("<pre>")) {
            this.position.state = TokenType.UNKNOWN_LINE;
          }
          else if (readChar() == '>' && getTokenLength() > 5) {
            if (prevTextInBufferIs("</pre>")) {
              tokenHasBeenCompleted = true;
              inAction = false;
            }
          }
        }
        break;
        case ATTRIBUTE:
        case EXTRA_TYPE: {
          if (!isBufferEnd()) {
            if (getTokenLength() == 1) {
              if (readChar() != ' ') {
                this.position.state = TokenType.UNKNOWN_LINE;
                continue;
              }
            }
            tokenHasBeenCompleted = skipToNextLine();
            inAction = false;
          }
        }
        break;
        case TOPIC:
        case UNKNOWN_LINE: {
          tokenHasBeenCompleted = skipToNextLine();
          inAction = false;
        }
        break;
        default:
          throw Assertions.fail("Detected unexpected lexer state " + this.position.state);
      }
    }

    this.position.tokenCompleted = tokenHasBeenCompleted;
    this.tokenType = this.position.getState();
    this.tokenEnd = this.position.getOffset();
    this.tokenType = this.position.state;
    if (tokenHasBeenCompleted) {
      if (this.tokenType == TokenType.HEAD_LINE) {
        if (hasTextAt("> ", this.tokenStart)) {
          this.tokenType = TokenType.ATTRIBUTE;
        }
      }
      else {
        this.position.state = TokenType.WHITESPACE;
      }
    }
  }

  private int getTokenLength() {
    return this.position.offset - this.tokenStart;
  }

  private boolean prevTextInBufferIs(final String text) {
    final int len = text.length();
    int startPos = this.position.offset - len;
    if (startPos < 0) {
      return false;
    }
    for (int i = 0; i < len; i++) {
      if (this.buffer.charAt(startPos++) != text.charAt(i)) {
        return false;
      }
    }
    return true;
  }

  private boolean hasTextAt(final String text, int position) {
    boolean result = false;
    if (position >= 0 && position + text.length() <= this.buffer.length()) {
      boolean ok = true;
      for (int i = 0; i < text.length(); i++) {
        if (text.charAt(i) != this.buffer.charAt(position++)) {
          ok = false;
          break;
        }
      }
      result = ok;
    }
    return result;
  }

  private boolean isBufferEnd() {
    return this.position.offset >= this.endOffset;
  }

  private boolean isTokenMayStartWith(final String text) {
    boolean result = true;
    int index = 0;
    for (int i = this.tokenStart; i <= this.position.offset && index < text.length(); i++) {
      if (text.charAt(index++) != this.buffer.charAt(i)) {
        result = false;
        break;
      }
    }
    return result;
  }

  private boolean isAllLineFromChars(final char c) {
    boolean detected = false;
    final int prelimit = this.position.offset - 1;

    for (int i = this.tokenStart; i < this.position.offset; i++) {
      final char chr = this.buffer.charAt(i);
      if ((chr == '\r') || (chr == '\n' && i == prelimit)) {
        continue;
      }
      if (chr != c) {
        return false;
      }
      else {
        detected = true;
      }
    }
    return detected;
  }

  private void skipAllWhitespaceAndSpecial() {
    while (!isBufferEnd()) {
      final char chr = readChar();
      if (!(Character.isWhitespace(chr) || Character.isISOControl(chr))) {
        back();
        break;
      }
    }
  }

  private boolean skipToNextLine() {
    boolean result = false;
    while (!isBufferEnd()) {
      if (readChar() == '\n') {
        result = true;
        break;
      }
    }
    return this.buffer.length() == this.position.offset || result;
  }

  private char readChar() {
    return this.buffer.charAt(this.position.offset++);
  }

  private void back() {
    if (this.position.offset > 0) {
      this.position.offset--;
    }
  }

  @Nonnull
  @ReturnsOriginal
  public LexerPosition getCurrentPosition() {
    return this.position;
  }

  public void restore(@Nonnull LexerPosition position) {
    if (position != this.position) {
      this.position.set(position);
    }
  }

  @Nonnull
  public CharSequence getBufferSequence() {
    return this.buffer;
  }

  public int getBufferEnd() {
    return this.endOffset;
  }
}
