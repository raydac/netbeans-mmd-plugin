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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.swing.text.Segment;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMaker;
import org.fife.ui.rsyntaxtextarea.RSyntaxUtilities;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenMap;

public class PlantUmlTokenMaker extends AbstractTokenMaker {

  private static final List<String> RESERVED_WORDS = new ArrayList<>();

  static {
    RESERVED_WORDS.add("@startsalt");
    RESERVED_WORDS.add("@endsalt");
    RESERVED_WORDS.add("@startgantt");
    RESERVED_WORDS.add("@endgantt");
    RESERVED_WORDS.add("@startlatex");
    RESERVED_WORDS.add("@endlatex");
    RESERVED_WORDS.add("@startmath");
    RESERVED_WORDS.add("@endmath");
    RESERVED_WORDS.add("@startdot");
    RESERVED_WORDS.add("@enddot");
    RESERVED_WORDS.add("@startuml");
    RESERVED_WORDS.add("@enduml");
    RESERVED_WORDS.add("@startmindmap");
    RESERVED_WORDS.add("@endmindmap");
    RESERVED_WORDS.add("@startwbs");
    RESERVED_WORDS.add("@endwbs");
    RESERVED_WORDS.add("header");
    RESERVED_WORDS.add("endheader");
    RESERVED_WORDS.add("legend");
    RESERVED_WORDS.add("endlegend");
    RESERVED_WORDS.add("scale");
    RESERVED_WORDS.add("skinparam");
    RESERVED_WORDS.add("title");
    RESERVED_WORDS.add("usecase");
    RESERVED_WORDS.add("boundary");
    RESERVED_WORDS.add("caption");
    RESERVED_WORDS.add("control");
    RESERVED_WORDS.add("collections");
    RESERVED_WORDS.add("entity");
    RESERVED_WORDS.add("database");
    RESERVED_WORDS.add("detach");
    RESERVED_WORDS.add("participant");
    RESERVED_WORDS.add("order");
    RESERVED_WORDS.add("as");
    RESERVED_WORDS.add("actor");
    RESERVED_WORDS.add("autonumber");
    RESERVED_WORDS.add("alt");
    RESERVED_WORDS.add("resume");
    RESERVED_WORDS.add("newpage");
    RESERVED_WORDS.add("is");
    RESERVED_WORDS.add("if");
    RESERVED_WORDS.add("then");
    RESERVED_WORDS.add("endif");
    RESERVED_WORDS.add("elseif");
    RESERVED_WORDS.add("repeat");
    RESERVED_WORDS.add("while");
    RESERVED_WORDS.add("endwhile");
    RESERVED_WORDS.add("else");
    RESERVED_WORDS.add("opt");
    RESERVED_WORDS.add("loop");
    RESERVED_WORDS.add("par");
    RESERVED_WORDS.add("break");
    RESERVED_WORDS.add("critical");
    RESERVED_WORDS.add("group");
    RESERVED_WORDS.add("note");
    RESERVED_WORDS.add("end");
    RESERVED_WORDS.add("over");
    RESERVED_WORDS.add("top");
    RESERVED_WORDS.add("bottom");
    RESERVED_WORDS.add("right");
    RESERVED_WORDS.add("left");
    RESERVED_WORDS.add("of");
    RESERVED_WORDS.add("rnote");
    RESERVED_WORDS.add("hnote");
    RESERVED_WORDS.add("ref");
    RESERVED_WORDS.add("create");
    RESERVED_WORDS.add("box");
    RESERVED_WORDS.add("hide");
    RESERVED_WORDS.add("footbox");
    RESERVED_WORDS.add("skinparam");
    RESERVED_WORDS.add("sequence");
    RESERVED_WORDS.add("activate");
    RESERVED_WORDS.add("deactivate");
    RESERVED_WORDS.add("start");
    RESERVED_WORDS.add("state");
    RESERVED_WORDS.add("stop");
    RESERVED_WORDS.add("file");
    RESERVED_WORDS.add("folder");
    RESERVED_WORDS.add("frame");
    RESERVED_WORDS.add("fork");
    RESERVED_WORDS.add("interface");
    RESERVED_WORDS.add("class");
    RESERVED_WORDS.add("namespace");
    RESERVED_WORDS.add("page");
    RESERVED_WORDS.add("node");
    RESERVED_WORDS.add("package");
    RESERVED_WORDS.add("queue");
    RESERVED_WORDS.add("stack");
    RESERVED_WORDS.add("rectangle");
    RESERVED_WORDS.add("storage");
    RESERVED_WORDS.add("card");
    RESERVED_WORDS.add("cloud");
    RESERVED_WORDS.add("component");
    RESERVED_WORDS.add("agent");
    RESERVED_WORDS.add("artifact");

    Collections.sort(RESERVED_WORDS);
  }

  @Override
  @Nonnull
  public TokenMap getWordsToHighlight() {
    final TokenMap tokenMap = new TokenMap();

    RESERVED_WORDS.forEach(str -> tokenMap.put(str, Token.RESERVED_WORD));
    return tokenMap;
  }

  @Nonnull
  public CompletionProvider makeCompletionProvider() {
    final DefaultCompletionProvider result = new DefaultCompletionProvider();
    RESERVED_WORDS.stream()
            .map(str -> str.startsWith("@") ? str.substring(1) : str)
            .forEach(str -> result.addCompletion(new BasicCompletion(result, str)));

    result.setAutoActivationRules(true, "@");

    return result;
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
            case '/': {
              currentTokenType = Token.COMMENT_KEYWORD;
            }
            break;
            default: {
              if (RSyntaxUtilities.isWhitespace(c)) {
                currentTokenType = Token.WHITESPACE;
              } else if (RSyntaxUtilities.isDigit(c)) {
                currentTokenType = Token.LITERAL_NUMBER_DECIMAL_INT;
                break;
              } else {
                currentTokenType = Token.IDENTIFIER;
              }
            }
            break;
          }
        }
        break;
        case Token.COMMENT_MULTILINE: {
          if (c == '/' && i > offset && array[i - 1] == '\'') {
            addToken(text, currentTokenStart, i, Token.COMMENT_MULTILINE, newStartOffset + currentTokenStart);
            currentTokenType = Token.NULL;
          }
        }
        break;
        case Token.COMMENT_KEYWORD: {
          switch (c) {
            case '\'': {
              currentTokenType = Token.COMMENT_MULTILINE;
            }
            break;
            default: {
              currentTokenType = Token.IDENTIFIER;
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
                } else {
                  currentTokenType = Token.IDENTIFIER;
                }
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
          if (RSyntaxUtilities.isWhitespace(c) || !RSyntaxUtilities.isLetterOrDigit(c)) {
            final int value = wordsToHighlight.get(text, currentTokenStart, i - 1);
            if (value < 0) {
              addToken(text, currentTokenStart, i - 1, Token.IDENTIFIER, newStartOffset + currentTokenStart);
            } else {
              addToken(text, currentTokenStart, i - 1, value, newStartOffset + currentTokenStart);
            }
            currentTokenStart = i;

            currentTokenType = RSyntaxUtilities.isWhitespace(c) ? Token.WHITESPACE : Token.IDENTIFIER;
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
      case Token.COMMENT_MULTILINE:
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

  private static boolean isAllowedCharReservedWord(final char c) {
    return RSyntaxUtilities.isLetterOrDigit(c) || c == '<' || c == '>' || c == '/';
  }

}
