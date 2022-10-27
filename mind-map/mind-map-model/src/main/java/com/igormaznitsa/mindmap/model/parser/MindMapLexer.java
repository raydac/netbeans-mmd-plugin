/*
 * Copyright 2015-2018 Igor Maznitsa.
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

import static java.util.Objects.requireNonNull;

/**
 * Allows to extract lexeme from mind map file.
 */
public final class MindMapLexer {

  private final LexerPosition position = new LexerPosition(0, TokenType.UNKNOWN_LINE);
  private CharSequence buffer = "";
  private int endOffset;
  private int tokenStart;
  private int tokenEnd;
  private TokenType tokenType = TokenType.UNKNOWN_LINE;

  public int getTokenStartOffset() {
    return this.tokenStart;
  }

  public int getTokenEndOffset() {
    return this.tokenEnd;
  }

  public void start(final CharSequence buffer, final int startOffset, final int endOffset,
                    final MindMapLexer.TokenType initialState) {
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

  public CharSequence getTokenSequence() {
    return getBufferSequence().subSequence(this.tokenStart, this.tokenEnd);
  }

  public String getTokenText() {
    return getTokenSequence().toString();
  }

  public TokenType getTokenType() {
    return this.tokenStart == this.tokenEnd ? null : this.tokenType;
  }

  public TokenPosition makeTokenPosition() {
    return new TokenPosition(this.tokenStart, this.tokenEnd);
  }

  public void resetTokenTypeToNull() {
    this.tokenType = null;
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
        case CODE_SNIPPET_END:
        case WHITESPACE: {
          skipAllWhitespaceAndSpecial();
          if (this.position.offset > this.tokenStart || isBufferEnd()) {
            tokenHasBeenCompleted = true;
            inAction = false;
          } else {
            final char chr = readChar();
            switch (chr) {
              case '#': {
                this.position.state = TokenType.TOPIC_LEVEL;
              }
              break;
              case '-':
              case '>': {
                if (isBufferEnd()) {
                  this.position.state = chr == '>' ? TokenType.ATTRIBUTE : TokenType.EXTRA_TYPE;
                  tokenHasBeenCompleted = false;
                  inAction = false;
                } else {
                  this.position.state =
                      readChar() == ' ' ? chr == '>' ? TokenType.ATTRIBUTE : TokenType.EXTRA_TYPE :
                          TokenType.UNKNOWN_LINE;
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
          } else if (readChar() == '>' && getTokenLength() > 5) {
            if (prevTextInBufferIs("</pre>")) {
              tokenHasBeenCompleted = true;
              inAction = false;
            }
          }
        }
        break;
        case CODE_SNIPPET_START: {
          tokenHasBeenCompleted = toStartPositionOfCodeSnippetEnd();
          if (isEmptyToken()) {
            this.position.state = TokenType.CODE_SNIPPET_END;
          } else {
            this.position.state = TokenType.CODE_SNIPPET_BODY;
            inAction = false;
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
        case TOPIC_LEVEL: {
          if (!isBufferEnd()) {
            final char ch = readChar();
            if (ch == '#') {
              continue;
            } else if (!Character.isWhitespace(ch)) {
              back();
            }
            tokenHasBeenCompleted = true;
            inAction = false;
          }
        }
        break;
        case TOPIC_TITLE:
        case UNKNOWN_LINE: {
          tokenHasBeenCompleted = skipToNextLine();
          inAction = false;
        }
        break;
        default:
          throw new IllegalStateException("Detected unexpected lexer state " + this.position.state);
      }
    }

    this.position.tokenCompleted = tokenHasBeenCompleted;
    this.tokenType = this.position.state;
    this.tokenEnd = this.position.offset;

    if (tokenHasBeenCompleted) {
      switch (this.tokenType) {
        case HEAD_LINE: {
          if (hasTextAt("> ", this.tokenStart)) {
            this.tokenType = TokenType.ATTRIBUTE;
          }
        }
        break;
        case TOPIC_LEVEL: {
          this.position.state = TokenType.TOPIC_TITLE;
        }
        break;
        case UNKNOWN_LINE: {
          if (tokenStartsWith("```")) {
            this.tokenType =
                isAllLineFromChars('`') ? TokenType.CODE_SNIPPET_END : TokenType.CODE_SNIPPET_START;
            this.position.state = this.tokenType;
          }
        }
        break;
        default: {
          this.position.state = TokenType.WHITESPACE;
        }
        break;
      }
    }
  }

  private int getTokenLength() {
    return this.position.offset - this.tokenStart;
  }

  private boolean isLineStart() {
    boolean result;
    final int startPos = this.position.offset - 1;
    if (startPos < 0) {
      result = true;
    } else {
      result = this.buffer.charAt(startPos) == '\n';
    }
    return result;
  }

  private boolean isEmptyToken() {
    return this.position.offset == this.tokenStart;
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

  private boolean tokenStartsWith(final String text) {
    boolean result = true;
    int index = 0;

    if (this.position.offset - this.tokenStart >= text.length()) {
      final int end = this.tokenStart + text.length();
      for (int i = this.tokenStart; i < end; i++) {
        if (text.charAt(index++) != this.buffer.charAt(i)) {
          result = false;
          break;
        }
      }
    } else {
      result = false;
    }
    return result;
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
      } else {
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

  private boolean toStartPositionOfCodeSnippetEnd() {
    boolean found = false;

    boolean lineStart = isLineStart();
    int lineStartPosition = lineStart ? this.position.offset : -1;
    int startingBacktickCounter = 0;

    int detectedSpaces = 0;

    while (!found && !isBufferEnd()) {
      final char ch = readChar();

      switch (ch) {
        case '`': {
          if (detectedSpaces == 0 && (startingBacktickCounter > 0 || lineStart)) {
            startingBacktickCounter++;
          } else {
            startingBacktickCounter = 0;
          }
          lineStart = false;
        }
        break;
        case '\n': {
          if (startingBacktickCounter == 3) {
            found = true;
          } else {
            lineStartPosition = this.position.offset;
            startingBacktickCounter = 0;
          }
          lineStart = true;
          detectedSpaces = 0;
        }
        break;
        default: {
          if (Character.isWhitespace(ch)) {
            detectedSpaces++;
          } else if (!Character.isISOControl(ch) && !Character.isWhitespace(ch)) {
            startingBacktickCounter = 0;
          }
          lineStart = false;
        }
        break;
      }
    }

    if (found || startingBacktickCounter == 3) {
      found = true;
      this.position.offset = lineStartPosition;
    }

    return found;
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

  public LexerPosition getCurrentPosition() {
    return this.position;
  }

  public void restore(final LexerPosition position) {
    if (position != this.position) {
      this.position.set(position);
    }
  }

  public CharSequence getBufferSequence() {
    return this.buffer;
  }

  public int getBufferEnd() {
    return this.endOffset;
  }

  /**
   * Type of allowed lexeme.
   */
  public enum TokenType {
    HEAD_LINE,
    HEAD_DELIMITER,
    ATTRIBUTE,
    TOPIC_LEVEL,
    TOPIC_TITLE,
    CODE_SNIPPET_START,
    CODE_SNIPPET_BODY,
    CODE_SNIPPET_END,
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
    private TokenType state;
    private boolean tokenCompleted;

    private LexerPosition(final LexerPosition pos) {
      this.offset = pos.offset;
      this.state = pos.state;
      this.tokenCompleted = pos.tokenCompleted;
    }

    private LexerPosition(final int offset, final TokenType state) {
      this.tokenCompleted = true;
      this.offset = offset;
      this.state = requireNonNull(state);
    }

    public int getOffset() {
      return this.offset;
    }

    public boolean isTokenCompleted() {
      return this.tokenCompleted;
    }

    public TokenType getState() {
      return this.state;
    }

    public void set(final LexerPosition position) {
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
}
