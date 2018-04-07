/* 
 * Copyright (C) 2018 Igor Maznitsa.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package com.igormaznitsa.sciareto.ui.editors;

import static com.igormaznitsa.meta.common.utils.Assertions.fail;
import javax.annotation.Nonnull;
import javax.swing.text.Segment;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMaker;
import org.fife.ui.rsyntaxtextarea.RSyntaxUtilities;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenMap;

public class PlantUmlTokenMaker extends AbstractTokenMaker {

  @Override
  @Nonnull
  public TokenMap getWordsToHighlight() {
    final TokenMap tokenMap = new TokenMap();

    tokenMap.put("@startuml", Token.RESERVED_WORD);
    tokenMap.put("@enduml", Token.RESERVED_WORD);
    tokenMap.put("header", Token.RESERVED_WORD);
    tokenMap.put("endheader", Token.RESERVED_WORD);
    tokenMap.put("scale", Token.RESERVED_WORD);
    tokenMap.put("skinparam", Token.RESERVED_WORD);
    tokenMap.put("title", Token.RESERVED_WORD);
    tokenMap.put("caption", Token.RESERVED_WORD);
    tokenMap.put("usecase", Token.RESERVED_WORD);
    tokenMap.put("as", Token.RESERVED_WORD);
    tokenMap.put("actor", Token.RESERVED_WORD);
    tokenMap.put("boundary", Token.RESERVED_WORD);
    tokenMap.put("control", Token.RESERVED_WORD);
    tokenMap.put("entity", Token.RESERVED_WORD);
    tokenMap.put("database", Token.RESERVED_WORD);
    tokenMap.put("collections", Token.RESERVED_WORD);
    tokenMap.put("participant", Token.RESERVED_WORD);
    tokenMap.put("order", Token.RESERVED_WORD);
    tokenMap.put("autonumber", Token.RESERVED_WORD);
    tokenMap.put("resume", Token.RESERVED_WORD);
    tokenMap.put("newpage", Token.RESERVED_WORD);
    tokenMap.put("alt", Token.RESERVED_WORD);
    tokenMap.put("if", Token.RESERVED_WORD);
    tokenMap.put("then", Token.RESERVED_WORD);
    tokenMap.put("endif", Token.RESERVED_WORD);
    tokenMap.put("elseif", Token.RESERVED_WORD);
    tokenMap.put("repeat", Token.RESERVED_WORD);
    tokenMap.put("while", Token.RESERVED_WORD);
    tokenMap.put("endwhile", Token.RESERVED_WORD);
    tokenMap.put("detach", Token.RESERVED_WORD);
    tokenMap.put("else", Token.RESERVED_WORD);
    tokenMap.put("opt", Token.RESERVED_WORD);
    tokenMap.put("loop", Token.RESERVED_WORD);
    tokenMap.put("par", Token.RESERVED_WORD);
    tokenMap.put("break", Token.RESERVED_WORD);
    tokenMap.put("critical", Token.RESERVED_WORD);
    tokenMap.put("group", Token.RESERVED_WORD);
    tokenMap.put("note", Token.RESERVED_WORD);
    tokenMap.put("end", Token.RESERVED_WORD);
    tokenMap.put("over", Token.RESERVED_WORD);
    tokenMap.put("right", Token.RESERVED_WORD);
    tokenMap.put("left", Token.RESERVED_WORD);
    tokenMap.put("of", Token.RESERVED_WORD);
    tokenMap.put("rnote", Token.RESERVED_WORD);
    tokenMap.put("hnote", Token.RESERVED_WORD);
    tokenMap.put("ref", Token.RESERVED_WORD);
    tokenMap.put("create", Token.RESERVED_WORD);
    tokenMap.put("box", Token.RESERVED_WORD);
    tokenMap.put("hide", Token.RESERVED_WORD);
    tokenMap.put("footbox", Token.RESERVED_WORD);
    tokenMap.put("skinparam", Token.RESERVED_WORD);
    tokenMap.put("sequence", Token.RESERVED_WORD);
    tokenMap.put("activate", Token.RESERVED_WORD);
    tokenMap.put("deactivate", Token.RESERVED_WORD);
    tokenMap.put("start", Token.RESERVED_WORD);
    tokenMap.put("stop", Token.RESERVED_WORD);
    tokenMap.put("file", Token.RESERVED_WORD);
    tokenMap.put("folder", Token.RESERVED_WORD);
    tokenMap.put("frame", Token.RESERVED_WORD);
    tokenMap.put("interface", Token.RESERVED_WORD);
    tokenMap.put("class", Token.RESERVED_WORD);
    tokenMap.put("namespace", Token.RESERVED_WORD);
    tokenMap.put("page", Token.RESERVED_WORD);
    tokenMap.put("node", Token.RESERVED_WORD);
    tokenMap.put("package", Token.RESERVED_WORD);
    tokenMap.put("queue", Token.RESERVED_WORD);
    tokenMap.put("stack", Token.RESERVED_WORD);
    tokenMap.put("rectangle", Token.RESERVED_WORD);
    tokenMap.put("storage", Token.RESERVED_WORD);
    tokenMap.put("card", Token.RESERVED_WORD);
    tokenMap.put("cloud", Token.RESERVED_WORD);
    tokenMap.put("component", Token.RESERVED_WORD);
    tokenMap.put("agent", Token.RESERVED_WORD);
    tokenMap.put("artifact", Token.RESERVED_WORD);

    return tokenMap;
  }

  @Override
  @Nonnull
  public Token getTokenList(@Nonnull final Segment text, final int startTokenType, final int startOffset) {
    resetTokenList();

    final char[] array = text.array;
    final int offset = text.offset;
    final int count = text.count;
    final int end = offset + count;

    int newStartOffset = startOffset - offset;

    int currentTokenStart = offset;
    int currentTokenType = startTokenType;

    for (int i = offset; i < end; i++) {
      char c = array[i];

      switch (currentTokenType) {
        case Token.NULL: {
          currentTokenStart = i;
          switch (c) {
            case '"': {
              currentTokenType = Token.LITERAL_STRING_DOUBLE_QUOTE;
            }
            break;
            case '#': {
              currentTokenType = Token.LITERAL_NUMBER_HEXADECIMAL;
            }
            break;
            case '\'': {
              currentTokenType = Token.COMMENT_EOL;
            }
            break;
            default: {
              if (RSyntaxUtilities.isWhitespace(c)) {
                currentTokenType = Token.WHITESPACE;
              } else if (RSyntaxUtilities.isDigit(c)) {
                currentTokenType = Token.LITERAL_NUMBER_DECIMAL_INT;
                break;
              } else if (c == ':') {
                currentTokenType = Token.COMMENT_DOCUMENTATION;
                break;
              } else {
                currentTokenType = Token.IDENTIFIER;
              }
            }
            break;
          }
        }
        break;

        case Token.WHITESPACE: {
          switch (c) {
            case '"': {
              addToken(text, currentTokenStart, i - 1, Token.WHITESPACE, newStartOffset + currentTokenStart);
              currentTokenStart = i;
              currentTokenType = Token.LITERAL_STRING_DOUBLE_QUOTE;
            }
            break;
            case '\'': {
              addToken(text, currentTokenStart, i - 1, Token.WHITESPACE, newStartOffset + currentTokenStart);
              currentTokenStart = i;
              currentTokenType = Token.COMMENT_EOL;
            }
            break;
            default: {
              if (!RSyntaxUtilities.isWhitespace(c)) {
                addToken(text, currentTokenStart, i - 1, Token.WHITESPACE, newStartOffset + currentTokenStart);
                currentTokenStart = i;
                if (RSyntaxUtilities.isDigit(c)) {
                  currentTokenType = Token.LITERAL_NUMBER_DECIMAL_INT;
                  break;
                } else if (c == ':') {
                  currentTokenType = Token.COMMENT_DOCUMENTATION;
                  break;
                } else {
                  currentTokenType = Token.IDENTIFIER;
                }
              }
            }
            break;
          }
        }
        break;
        case Token.COMMENT_DOCUMENTATION: {
          switch (c) {
            case '"': {
              addToken(text, currentTokenStart, i - 1, Token.COMMENT_DOCUMENTATION, newStartOffset + currentTokenStart);
              currentTokenStart = i;
              currentTokenType = Token.LITERAL_STRING_DOUBLE_QUOTE;
            }
            break;
            case ';': {
              addToken(text, currentTokenStart, i - 1, Token.COMMENT_DOCUMENTATION, newStartOffset + currentTokenStart);
              currentTokenStart = i;
              currentTokenType = Token.NULL;
            }
            break;
            default: {
              if (RSyntaxUtilities.isWhitespace(c)) {
                // do nothing because can contain
              }
            }
            break;
          }
        }
        break;

        case Token.LITERAL_NUMBER_HEXADECIMAL: {
          switch (c) {
            case '"': {
              addToken(text, currentTokenStart, i - 1, Token.LITERAL_NUMBER_HEXADECIMAL, newStartOffset + currentTokenStart);
              currentTokenStart = i;
              currentTokenType = Token.LITERAL_STRING_DOUBLE_QUOTE;
            }
            break;
            case ':': {
              addToken(text, currentTokenStart, i - 1, Token.LITERAL_NUMBER_HEXADECIMAL, newStartOffset + currentTokenStart);
              currentTokenStart = i;
              currentTokenType = Token.COMMENT_DOCUMENTATION;
            }
            break;
            case '\'': {
              addToken(text, currentTokenStart, i - 1, Token.LITERAL_NUMBER_HEXADECIMAL, newStartOffset + currentTokenStart);
              currentTokenStart = i;
              currentTokenType = Token.COMMENT_EOL;
            }
            break;

            default: {
              if (RSyntaxUtilities.isWhitespace(c)) {
                addToken(text, currentTokenStart, i - 1, Token.LITERAL_NUMBER_HEXADECIMAL, newStartOffset + currentTokenStart);
                currentTokenStart = i;
                currentTokenType = Token.WHITESPACE;
              } else if (RSyntaxUtilities.isDigit(c)) {
                // Still a literal number.
              } else {
                // Otherwise, remember this was a number and start over.
                addToken(text, currentTokenStart, i - 1, Token.LITERAL_NUMBER_HEXADECIMAL, newStartOffset + currentTokenStart);
                i--;
                currentTokenType = Token.NULL;
              }
            }
            break;
          }
        }
        break;

        case Token.LITERAL_NUMBER_DECIMAL_INT: {
          switch (c) {
            case '"': {
              addToken(text, currentTokenStart, i - 1, Token.LITERAL_NUMBER_DECIMAL_INT, newStartOffset + currentTokenStart);
              currentTokenStart = i;
              currentTokenType = Token.LITERAL_STRING_DOUBLE_QUOTE;
            }
            break;
            case ':': {
              addToken(text, currentTokenStart, i - 1, Token.LITERAL_NUMBER_DECIMAL_INT, newStartOffset + currentTokenStart);
              currentTokenStart = i;
              currentTokenType = Token.COMMENT_DOCUMENTATION;
            }
            break;
            case '\'': {
              addToken(text, currentTokenStart, i - 1, Token.LITERAL_NUMBER_DECIMAL_INT, newStartOffset + currentTokenStart);
              currentTokenStart = i;
              currentTokenType = Token.COMMENT_EOL;
            }
            break;

            default: {
              if (RSyntaxUtilities.isWhitespace(c)) {
                addToken(text, currentTokenStart, i - 1, Token.LITERAL_NUMBER_DECIMAL_INT, newStartOffset + currentTokenStart);
                currentTokenStart = i;
                currentTokenType = Token.WHITESPACE;
              } else if (RSyntaxUtilities.isDigit(c)) {
                // Still a literal number.
              } else {
                // Otherwise, remember this was a number and start over.
                addToken(text, currentTokenStart, i - 1, Token.LITERAL_NUMBER_DECIMAL_INT, newStartOffset + currentTokenStart);
                i--;
                currentTokenType = Token.NULL;
              }
            }
            break;
          }
        }
        break;
        case Token.IDENTIFIER:
        case Token.RESERVED_WORD: {
          if (RSyntaxUtilities.isWhitespace(c)) {
            final int value = wordsToHighlight.get(text, currentTokenStart, i - 1);
            if (value < 0) {
              addToken(text, currentTokenStart, i - 1, Token.IDENTIFIER, newStartOffset + currentTokenStart);
            } else {
              addToken(text, currentTokenStart, i - 1, value, newStartOffset + currentTokenStart);
            }
            currentTokenStart = i;
            currentTokenType = Token.WHITESPACE;
          }
        }
        break;
        case Token.COMMENT_EOL: {
          if (c == '\n') {
            i = end - 1;
            addToken(text, currentTokenStart, i, currentTokenType, newStartOffset + currentTokenStart);
            // We need to set token type to null so at the bottom we don't add one more token.
            currentTokenType = Token.NULL;
          }
        }
        break;

        case Token.LITERAL_STRING_DOUBLE_QUOTE: {
          if (c == '"') {
            addToken(text, currentTokenStart, i, Token.LITERAL_STRING_DOUBLE_QUOTE, newStartOffset + currentTokenStart);
            currentTokenType = Token.NULL;
          }
        }
        break;
        default: {
          throw fail("Should never hapen, state : " + currentTokenType);
        }
      }
    }

    switch (currentTokenType) {
      // Remember what token type to begin the next line with.
      case Token.LITERAL_STRING_DOUBLE_QUOTE: {
        addToken(text, currentTokenStart, end - 1, currentTokenType, newStartOffset + currentTokenStart);
      }
      break;
      case Token.NULL: {
        addNullToken();
      }
      break;
      case Token.RESERVED_WORD:
      case Token.IDENTIFIER: {
        final int value = wordsToHighlight.get(text, currentTokenStart, end - 1);
        if (value < 0) {
          addToken(text, currentTokenStart, end - 1, Token.IDENTIFIER, newStartOffset + currentTokenStart);
        } else {
          addToken(text, currentTokenStart, end - 1, value, newStartOffset + currentTokenStart);
        }
        addNullToken();
      }
      break;
      // All other token types don't continue to the next line...
      default: {
        addToken(text, currentTokenStart, end - 1, currentTokenType, newStartOffset + currentTokenStart);
        addNullToken();
      }
      break;
    }
    return this.firstToken;
  }

}
