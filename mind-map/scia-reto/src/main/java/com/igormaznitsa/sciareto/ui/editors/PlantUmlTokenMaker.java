/*
 * Copyright (C) 2015-2022 Igor A. Maznitsa
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package com.igormaznitsa.sciareto.ui.editors;

import static com.igormaznitsa.meta.common.utils.Assertions.fail;
import static java.util.Objects.requireNonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

  private static final List<String> RESERVED_WORDS;

  private static final String PLANTUML_KEWORD_RESOURCE = "/puml/pumlkeywords.txt";

  static {
    final List<String> loaded = new ArrayList<>();
    try (final BufferedReader reader = new BufferedReader(new InputStreamReader(
        requireNonNull(PlantUmlTokenMaker.class.getResourceAsStream(PLANTUML_KEWORD_RESOURCE),
            "Can't find PlantUML keywords list for resource: " + PLANTUML_KEWORD_RESOURCE)))) {
      String line;
      while ((line = reader.readLine()) != null) {
        final String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith(";")) {
          continue;
        }
        loaded.add(trimmed);
      }
    } catch (IOException ex) {
      throw new Error("Can't read PlantUML keyword list from " + PLANTUML_KEWORD_RESOURCE, ex);
    }
    Collections.sort(loaded);
    RESERVED_WORDS = Collections.unmodifiableList(loaded);
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

    result.setAutoActivationRules(false, "@");

    return result;
  }

  @Override
  @Nonnull
  public Token getTokenList(@Nonnull final Segment text, final int startTokenType,
                            final int startOffset) {
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
            addToken(text, currentTokenStart, i, Token.COMMENT_MULTILINE,
                newStartOffset + currentTokenStart);
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
              addToken(text, currentTokenStart, i - 1, Token.WHITESPACE,
                  newStartOffset + currentTokenStart);
              currentTokenStart = i;
              currentTokenType = Token.LITERAL_STRING_DOUBLE_QUOTE;
            }
            break;
            case '\'': {
              addToken(text, currentTokenStart, i - 1, Token.WHITESPACE,
                  newStartOffset + currentTokenStart);
              currentTokenStart = i;
              currentTokenType = Token.COMMENT_EOL;
            }
            break;
            default: {
              if (!RSyntaxUtilities.isWhitespace(c)) {
                addToken(text, currentTokenStart, i - 1, Token.WHITESPACE,
                    newStartOffset + currentTokenStart);
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
              addToken(text, currentTokenStart, i - 1, Token.LITERAL_NUMBER_HEXADECIMAL,
                  newStartOffset + currentTokenStart);
              currentTokenStart = i;
              currentTokenType = Token.LITERAL_STRING_DOUBLE_QUOTE;
            }
            break;
            case '\'': {
              addToken(text, currentTokenStart, i - 1, Token.LITERAL_NUMBER_HEXADECIMAL,
                  newStartOffset + currentTokenStart);
              currentTokenStart = i;
              currentTokenType = Token.COMMENT_EOL;
            }
            break;

            default: {
              if (RSyntaxUtilities.isWhitespace(c)) {
                addToken(text, currentTokenStart, i - 1, Token.LITERAL_NUMBER_HEXADECIMAL,
                    newStartOffset + currentTokenStart);
                currentTokenStart = i;
                currentTokenType = Token.WHITESPACE;
              } else if (RSyntaxUtilities.isDigit(c)) {
                // Still a literal number.
              } else {
                // Otherwise, remember this was a number and start over.
                addToken(text, currentTokenStart, i - 1, Token.LITERAL_NUMBER_HEXADECIMAL,
                    newStartOffset + currentTokenStart);
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
              addToken(text, currentTokenStart, i - 1, Token.LITERAL_NUMBER_DECIMAL_INT,
                  newStartOffset + currentTokenStart);
              currentTokenStart = i;
              currentTokenType = Token.LITERAL_STRING_DOUBLE_QUOTE;
            }
            break;
            case '\'': {
              addToken(text, currentTokenStart, i - 1, Token.LITERAL_NUMBER_DECIMAL_INT,
                  newStartOffset + currentTokenStart);
              currentTokenStart = i;
              currentTokenType = Token.COMMENT_EOL;
            }
            break;

            default: {
              if (RSyntaxUtilities.isWhitespace(c)) {
                addToken(text, currentTokenStart, i - 1, Token.LITERAL_NUMBER_DECIMAL_INT,
                    newStartOffset + currentTokenStart);
                currentTokenStart = i;
                currentTokenType = Token.WHITESPACE;
              } else if (RSyntaxUtilities.isDigit(c)) {
                // Still a literal number.
              } else {
                // Otherwise, remember this was a number and start over.
                addToken(text, currentTokenStart, i - 1, Token.LITERAL_NUMBER_DECIMAL_INT,
                    newStartOffset + currentTokenStart);
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
              addToken(text, currentTokenStart, i - 1, Token.IDENTIFIER,
                  newStartOffset + currentTokenStart);
            } else {
              addToken(text, currentTokenStart, i - 1, value, newStartOffset + currentTokenStart);
            }
            currentTokenStart = i;

            currentTokenType =
                RSyntaxUtilities.isWhitespace(c) ? Token.WHITESPACE : Token.IDENTIFIER;
          }
        }
        break;
        case Token.COMMENT_EOL: {
          if (c == '\n') {
            i = end - 1;
            addToken(text, currentTokenStart, i, currentTokenType,
                newStartOffset + currentTokenStart);
            // We need to set token type to null so at the bottom we don't add one more token.
            currentTokenType = Token.NULL;
          }
        }
        break;

        case Token.LITERAL_STRING_DOUBLE_QUOTE: {
          if (c == '"') {
            addToken(text, currentTokenStart, i, Token.LITERAL_STRING_DOUBLE_QUOTE,
                newStartOffset + currentTokenStart);
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
        addToken(text, currentTokenStart, end - 1, currentTokenType,
            newStartOffset + currentTokenStart);
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
          addToken(text, currentTokenStart, end - 1, Token.IDENTIFIER,
              newStartOffset + currentTokenStart);
        } else {
          addToken(text, currentTokenStart, end - 1, value, newStartOffset + currentTokenStart);
        }
        addNullToken();
      }
      break;
      // All other token types don't continue to the next line...
      default: {
        addToken(text, currentTokenStart, end - 1, currentTokenType,
            newStartOffset + currentTokenStart);
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
