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

  TokenSet COMMENTS = TokenSet.create(HEADER_LINE,UNKNOWN);
  TokenSet IDENTIFIERS = TokenSet.create(TOPIC,ATTRIBUTES,HEADER_DELIMITER,EXTRA_TYPE,EXTRA_DATA,EXTRA_BODY,CODE_SNIPPET_START,CODE_SNIPPET_BODY,CODE_SNIPPET_END);
  TokenSet LITERALS = TokenSet.create(FILE);

}
