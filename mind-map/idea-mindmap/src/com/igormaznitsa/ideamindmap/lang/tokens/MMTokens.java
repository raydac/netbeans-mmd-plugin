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

package com.igormaznitsa.ideamindmap.lang.tokens;

import com.igormaznitsa.ideamindmap.lang.MMLanguage;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;

public interface MMTokens {
  IElementType WHITE_SPACE = TokenType.WHITE_SPACE;
  IElementType UNKNOWN = TokenType.DUMMY_HOLDER;

  IElementType HEADER_LINE = new MMHeaderLine();
  IElementType HEADER_DELIMITER = new MMHeaderDelimiter();
  IElementType ATTRIBUTES = new MMAttributes();
  IElementType TOPIC = new MMTopic();
  IElementType TOPIC_LEVEL = new MMTopicLevel();
  IElementType TOPIC_TITLE = new MMTopicTitle();
  IElementType EXTRA_DATA = new MMExtraData();
  IElementType EXTRA_BODY = new MMExtraBody();
  IElementType EXTRA_TYPE = new MMExtraType();
  IElementType CODE_SNIPPET_START = new MMCodeSnippetStart();
  IElementType CODE_SNIPPET_BODY = new MMCodeSnippetBody();
  IElementType CODE_SNIPPET_END = new MMCodeSnippetEnd();
  IFileElementType FILE = new IFileElementType(MMLanguage.INSTANCE);

  TokenSet COMMENTS = TokenSet.create(HEADER_LINE, UNKNOWN);
  TokenSet IDENTIFIERS = TokenSet.create(TOPIC, ATTRIBUTES, HEADER_DELIMITER, EXTRA_TYPE, EXTRA_DATA, EXTRA_BODY, CODE_SNIPPET_START, CODE_SNIPPET_BODY, CODE_SNIPPET_END);
  TokenSet LITERALS = TokenSet.create(FILE);

}
