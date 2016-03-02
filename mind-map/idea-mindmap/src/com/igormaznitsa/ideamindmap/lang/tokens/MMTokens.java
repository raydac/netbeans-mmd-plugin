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
  IElementType EXTRA_DATA = new MMExtraData();
  IElementType EXTRA_BODY = new MMExtraBody();
  IElementType EXTRA_TYPE = new MMExtraType();
  IFileElementType FILE = new IFileElementType(MMLanguage.INSTANCE);

  TokenSet COMMENTS = TokenSet.create(HEADER_LINE,UNKNOWN);
  TokenSet IDENTIFIERS = TokenSet.create(TOPIC,ATTRIBUTES,HEADER_DELIMITER,EXTRA_TYPE,EXTRA_DATA,EXTRA_BODY);
  TokenSet LITERALS = TokenSet.create(FILE);

}
