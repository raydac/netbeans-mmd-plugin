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

import java.io.IOException;
import java.io.Reader;
import javax.swing.text.Segment;
import org.fife.ui.rsyntaxtextarea.AbstractJFlexTokenMaker;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenImpl;

public class KStreamsTopologyTokenMaker extends AbstractJFlexTokenMaker {

  /**
   * This character denotes the end of file
   */
  public static final int YYEOF = -1;
  /**
   * lexical states
   */
  public static final int YYINITIAL = 0;
  /**
   * initial size of the lookahead buffer
   */
  private static final int ZZ_BUFFERSIZE = 16384;
  /**
   * Translates characters to character classes
   */
  private static final String ZZ_CMAP_PACKED =
      "\11\0\1\11\1\4\1\0\1\11\1\0\22\0\1\11\1\0\1\0" +
          "\1\0\1\1\1\0\1\0\1\0\1\13\1\13\2\0\1\13\1\30" +
          "\1\0\1\0\1\2\1\36\1\36\1\2\4\2\2\2\1\23\1\0" +
          "\1\34\1\0\1\33\1\0\1\0\1\3\1\10\1\26\1\35\1\22" +
          "\1\17\1\31\1\1\1\20\1\1\1\27\1\21\1\1\1\7\1\25" +
          "\1\15\1\1\1\24\1\16\1\14\1\6\1\1\1\1\1\1\1\32" +
          "\1\1\1\13\1\5\1\13\1\0\1\1\1\0\1\3\1\10\1\26" +
          "\1\35\1\22\1\17\1\31\1\1\1\20\1\1\1\27\1\21\1\1" +
          "\1\7\1\25\1\15\1\1\1\24\1\16\1\14\1\6\1\1\1\1" +
          "\1\1\1\32\1\1\1\12\1\0\1\12\1\0\uff81\0";

  /**
   * Translates characters to character classes
   */
  private static final char[] ZZ_CMAP = zzUnpackCMap(ZZ_CMAP_PACKED);
  private static final String ZZ_ACTION_PACKED_0 =
      "\1\0\2\1\1\2\1\3\1\4\1\5\6\1\1\0" +
          "\6\1\2\0\1\1\1\0\7\1\1\0\3\1\1\0" +
          "\4\1\1\0\3\1\1\0\1\6\2\1\1\0\2\1" +
          "\1\0\3\1\1\0\3\1\1\0\2\1\1\0\1\7" +
          "\5\0";
  /**
   * Translates DFA states to action switch labels.
   */
  private static final int[] ZZ_ACTION = zzUnpackAction();
  private static final String ZZ_ROWMAP_PACKED_0 =
      "\0\0\0\37\0\76\0\37\0\135\0\37\0\37\0\174" +
          "\0\233\0\272\0\331\0\370\0\u0117\0\u0136\0\u0155\0\u0174" +
          "\0\u0193\0\u01b2\0\u01d1\0\u01f0\0\u020f\0\u022e\0\u024d\0\u026c" +
          "\0\u028b\0\u02aa\0\u02c9\0\u02e8\0\u0307\0\u0326\0\u0345\0\u0364" +
          "\0\u0383\0\u03a2\0\u03c1\0\u03e0\0\u03ff\0\u041e\0\u043d\0\u045c" +
          "\0\u047b\0\u049a\0\u04b9\0\u04d8\0\u04f7\0\37\0\u0516\0\u0535" +
          "\0\u0554\0\u0573\0\u0592\0\u05b1\0\u05d0\0\u05ef\0\u060e\0\u062d" +
          "\0\u064c\0\u066b\0\u068a\0\u06a9\0\u06c8\0\u06e7\0\u0706\0\76" +
          "\0\u0725\0\u0744\0\u0763\0\u0782\0\u07a1";
  /**
   * Translates a state to a row index in the transition table
   */
  private static final int[] ZZ_ROWMAP = zzUnpackRowMap();
  private static final String ZZ_TRANS_PACKED_0 =
      "\1\2\1\3\1\2\1\3\1\4\1\2\3\3\1\5" +
          "\1\6\1\7\1\10\1\11\1\12\4\3\1\2\4\3" +
          "\1\13\2\3\1\2\1\14\1\15\1\2\40\0\3\3" +
          "\1\0\1\16\3\3\3\0\7\3\1\0\4\3\1\0" +
          "\2\3\2\0\2\3\11\0\1\5\26\0\3\3\1\0" +
          "\1\16\3\3\3\0\7\3\1\0\1\3\1\17\2\3" +
          "\1\0\2\3\2\0\2\3\1\0\3\3\1\0\1\16" +
          "\3\3\3\0\7\3\1\0\1\20\3\3\1\0\2\3" +
          "\2\0\2\3\1\0\3\3\1\0\1\16\1\21\2\3" +
          "\3\0\1\22\3\3\1\23\2\3\1\0\1\3\1\24" +
          "\2\3\1\0\2\3\2\0\2\3\30\0\1\25\36\0" +
          "\1\26\7\0\2\3\1\27\1\0\1\16\3\3\3\0" +
          "\7\3\1\0\4\3\1\0\2\3\2\0\2\3\6\0" +
          "\1\30\31\0\3\3\1\0\1\16\3\3\3\0\1\3" +
          "\1\31\5\3\1\0\4\3\1\0\2\3\2\0\2\3" +
          "\1\0\3\3\1\0\1\16\3\3\3\0\7\3\1\0" +
          "\1\3\1\32\2\3\1\0\2\3\2\0\2\3\1\0" +
          "\3\3\1\0\1\16\2\3\1\33\3\0\7\3\1\0" +
          "\4\3\1\0\2\3\2\0\2\3\1\0\3\3\1\0" +
          "\1\16\3\3\3\0\7\3\1\0\1\3\1\34\2\3" +
          "\1\0\2\3\2\0\2\3\1\0\3\3\1\0\1\16" +
          "\1\3\1\35\1\3\3\0\7\3\1\0\4\3\1\0" +
          "\2\3\2\0\2\3\1\0\3\3\1\0\1\16\1\36" +
          "\2\3\3\0\7\3\1\0\4\3\1\0\2\3\2\0" +
          "\2\3\33\0\1\7\33\0\1\7\7\0\3\3\1\0" +
          "\1\16\3\3\3\0\1\37\6\3\1\0\4\3\1\0" +
          "\2\3\2\0\2\3\2\0\2\40\4\0\1\40\6\0" +
          "\1\40\2\0\1\40\3\0\1\40\6\0\2\40\1\0" +
          "\3\3\1\0\1\16\3\3\3\0\4\3\1\41\2\3" +
          "\1\0\1\3\1\42\2\3\1\0\2\3\2\0\2\3" +
          "\1\0\3\3\1\0\1\16\3\3\3\0\7\3\1\0" +
          "\2\3\1\43\1\3\1\0\2\3\2\0\2\3\1\0" +
          "\3\3\1\0\1\16\3\3\3\0\7\3\1\0\4\3" +
          "\1\44\2\3\2\0\2\3\1\0\3\3\1\0\1\16" +
          "\3\3\3\0\7\3\1\0\1\45\3\3\1\0\2\3" +
          "\2\0\2\3\1\0\3\3\1\0\1\16\3\3\3\0" +
          "\7\3\1\0\3\3\1\46\1\0\2\3\2\0\2\3" +
          "\1\0\3\3\1\0\1\16\3\3\3\0\7\3\1\0" +
          "\1\47\3\3\1\0\2\3\2\0\2\3\1\0\2\3" +
          "\1\50\1\0\1\16\3\3\3\0\7\3\1\0\4\3" +
          "\1\0\2\3\2\0\2\3\2\0\2\51\4\0\1\51" +
          "\6\0\1\51\2\0\1\51\3\0\1\51\6\0\2\51" +
          "\1\0\3\3\1\0\1\16\3\3\3\0\7\3\1\0" +
          "\2\3\1\52\1\3\1\0\2\3\2\0\2\3\1\0" +
          "\3\3\1\0\1\16\3\3\3\0\5\3\1\53\1\3" +
          "\1\0\4\3\1\0\2\3\2\0\2\3\1\0\3\3" +
          "\1\0\1\16\3\3\3\0\6\3\1\54\1\0\4\3" +
          "\1\0\2\3\2\0\2\3\14\0\1\55\23\0\3\3" +
          "\1\0\1\16\3\3\3\0\6\3\1\52\1\0\4\3" +
          "\1\0\2\3\2\0\2\3\1\0\3\3\1\0\1\16" +
          "\3\3\3\0\7\3\1\56\4\3\1\0\2\3\2\0" +
          "\2\3\1\0\3\3\1\0\1\16\3\3\3\0\7\3" +
          "\1\0\2\3\1\57\1\3\1\0\2\3\2\0\2\3" +
          "\1\0\3\3\1\0\1\16\3\3\3\0\1\60\6\3" +
          "\1\0\4\3\1\0\2\3\2\0\2\3\2\0\2\61" +
          "\4\0\1\61\6\0\1\61\2\0\1\61\3\0\1\61" +
          "\6\0\2\61\1\0\3\3\1\0\1\16\3\3\3\0" +
          "\2\3\1\46\4\3\1\56\4\3\1\0\2\3\2\0" +
          "\2\3\1\0\3\3\1\0\1\16\3\3\3\0\7\3" +
          "\1\0\1\3\1\62\2\3\1\0\2\3\2\0\2\3" +
          "\1\0\3\3\1\0\1\16\3\3\3\0\2\3\1\63" +
          "\4\3\1\0\4\3\1\0\2\3\2\0\2\3\25\0" +
          "\1\64\12\0\3\3\1\0\1\16\3\3\3\0\6\3" +
          "\1\46\1\0\4\3\1\0\2\3\2\0\2\3\1\0" +
          "\3\3\1\0\1\16\3\3\3\0\7\3\1\0\4\3" +
          "\1\0\1\3\1\65\2\0\2\3\2\0\2\3\4\0" +
          "\1\3\6\0\1\3\2\0\1\3\3\0\1\3\6\0" +
          "\2\3\1\0\3\3\1\0\1\16\3\3\3\0\7\3" +
          "\1\0\4\3\1\0\1\66\1\3\2\0\2\3\1\0" +
          "\3\3\1\0\1\16\3\3\3\0\2\3\1\67\4\3" +
          "\1\0\4\3\1\0\2\3\2\0\2\3\15\0\1\70" +
          "\22\0\3\3\1\0\1\16\3\3\3\0\1\3\1\71" +
          "\5\3\1\0\4\3\1\0\2\3\2\0\2\3\1\0" +
          "\3\3\1\0\1\16\3\3\3\0\4\3\1\72\2\3" +
          "\1\0\4\3\1\0\1\3\1\46\2\0\2\3\1\0" +
          "\3\3\1\0\1\16\3\3\3\0\7\3\1\0\1\3" +
          "\1\73\2\3\1\0\2\3\2\0\2\3\25\0\1\74" +
          "\12\0\3\3\1\0\1\16\3\3\3\0\6\3\1\75" +
          "\1\0\4\3\1\0\2\3\2\0\2\3\1\0\3\3" +
          "\1\0\1\16\3\3\3\0\6\3\1\76\1\0\4\3" +
          "\1\0\2\3\2\0\2\3\1\0\3\3\1\0\1\16" +
          "\3\3\3\0\7\3\1\0\1\46\3\3\1\0\2\3" +
          "\2\0\2\3\21\0\1\77\16\0\3\3\1\0\1\16" +
          "\3\3\3\0\7\3\1\0\4\3\1\0\2\3\2\0" +
          "\1\3\1\100\1\0\3\3\1\0\1\16\3\3\3\0" +
          "\2\3\1\46\4\3\1\0\4\3\1\0\2\3\2\0" +
          "\2\3\25\0\1\101\42\0\1\102\25\0\1\103\11\0" +
          "\1\104\26\0\1\105\37\0\1\56\31\0\1\104\20\0";
  /**
   * The transition table of the DFA
   */
  private static final int[] ZZ_TRANS = zzUnpackTrans();
  /* error codes */
  private static final int ZZ_UNKNOWN_ERROR = 0;
  private static final int ZZ_NO_MATCH = 1;
  private static final int ZZ_PUSHBACK_2BIG = 2;
  /* error messages for the codes above */
  private static final String ZZ_ERROR_MSG[] = {
      "Unkown internal scanner error",
      "Error: could not match input",
      "Error: pushback value was too large"
  };
  private static final String ZZ_ATTRIBUTE_PACKED_0 =
      "\1\0\1\11\1\1\1\11\1\1\2\11\6\1\1\0" +
          "\6\1\2\0\1\1\1\0\7\1\1\0\3\1\1\0" +
          "\4\1\1\0\3\1\1\0\1\11\2\1\1\0\2\1" +
          "\1\0\3\1\1\0\3\1\1\0\2\1\1\0\1\1" +
          "\5\0";
  /**
   * ZZ_ATTRIBUTE[aState] contains the attributes of state <code>aState</code>
   */
  private static final int[] ZZ_ATTRIBUTE = zzUnpackAttribute();
  /**
   * the input device
   */
  private java.io.Reader zzReader;
  /**
   * the current state of the DFA
   */
  private int zzState;
  /**
   * the current lexical state
   */
  private int zzLexicalState = YYINITIAL;
  /**
   * this buffer contains the current text to be matched and is
   * the source of the yytext() string
   */
  private char zzBuffer[];
  /**
   * the textposition at the last accepting state
   */
  private int zzMarkedPos;
  /**
   * the textposition at the last state to be included in yytext
   */
  private int zzPushbackPos;
  /**
   * the current text position in the buffer
   */
  private int zzCurrentPos;
  /**
   * startRead marks the beginning of the yytext() string in the buffer
   */
  private int zzStartRead;
  /**
   * endRead marks the last character in the buffer, that has been read
   * from input
   */
  private int zzEndRead;
  /**
   * number of newlines encountered up to the start of the matched text
   */
  private int yyline;
  /**
   * the number of characters up to the start of the matched text
   */
  private int yychar;
  /**
   * the number of characters from the last newline up to the start of the
   * matched text
   */
  private int yycolumn;
  /**
   * zzAtBOL == true <=> the scanner is currently at the beginning of a line
   */
  private boolean zzAtBOL = true;
  /**
   * zzAtEOF == true <=> the scanner is at the EOF
   */
  private boolean zzAtEOF;

  /**
   * Constructor.  This must be here because JFlex does not generate a
   * no-parameter constructor.
   */
  public KStreamsTopologyTokenMaker() {
  }

  /**
   * Creates a new scanner
   * There is also a java.io.InputStream version of this constructor.
   *
   * @param in the java.io.Reader to read input from.
   */
  public KStreamsTopologyTokenMaker(java.io.Reader in) {
    this.zzReader = in;
  }

  /**
   * Creates a new scanner.
   * There is also java.io.Reader version of this constructor.
   *
   * @param in the java.io.Inputstream to read input from.
   */
  public KStreamsTopologyTokenMaker(java.io.InputStream in) {
    this(new java.io.InputStreamReader(in));
  }

  private static int[] zzUnpackAction() {
    int[] result = new int[69];
    int offset = 0;
    offset = zzUnpackAction(ZZ_ACTION_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAction(String packed, int offset, int[] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do {
        result[j++] = value;
      } while (--count > 0);
    }
    return j;
  }

  private static int[] zzUnpackRowMap() {
    int[] result = new int[69];
    int offset = 0;
    offset = zzUnpackRowMap(ZZ_ROWMAP_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackRowMap(String packed, int offset, int[] result) {
    int i = 0;  /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int high = packed.charAt(i++) << 16;
      result[j++] = high | packed.charAt(i++);
    }
    return j;
  }

  private static int[] zzUnpackTrans() {
    int[] result = new int[1984];
    int offset = 0;
    offset = zzUnpackTrans(ZZ_TRANS_PACKED_0, offset, result);
    return result;
  }

  /* user code: */

  private static int zzUnpackTrans(String packed, int offset, int[] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      value--;
      do {
        result[j++] = value;
      } while (--count > 0);
    }
    return j;
  }

  private static int[] zzUnpackAttribute() {
    int[] result = new int[69];
    int offset = 0;
    offset = zzUnpackAttribute(ZZ_ATTRIBUTE_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAttribute(String packed, int offset, int[] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do {
        result[j++] = value;
      } while (--count > 0);
    }
    return j;
  }

  /**
   * Unpacks the compressed character translation table.
   *
   * @param packed the packed character translation table
   * @return the unpacked character translation table
   */
  private static char[] zzUnpackCMap(String packed) {
    char[] map = new char[0x10000];
    int i = 0;  /* index in packed string  */
    int j = 0;  /* index in unpacked array */
    while (i < 196) {
      int count = packed.charAt(i++);
      char value = packed.charAt(i++);
      do {
        map[j++] = value;
      } while (--count > 0);
    }
    return map;
  }

  /**
   * Adds the token specified to the current linked list of tokens.
   *
   * @param tokenType The token's type.
   * @see #addToken(int, int, int)
   */
  private void addHyperlinkToken(int start, int end, int tokenType) {
    int so = start + offsetShift;
    addToken(zzBuffer, start, end, tokenType, so, true);
  }

  /**
   * Adds the token specified to the current linked list of tokens.
   *
   * @param tokenType The token's type.
   */
  private void addToken(int tokenType) {
    addToken(zzStartRead, zzMarkedPos - 1, tokenType);
  }

  /**
   * Adds the token specified to the current linked list of tokens.
   *
   * @param tokenType The token's type.
   * @see #addHyperlinkToken(int, int, int)
   */
  private void addToken(int start, int end, int tokenType) {
    int so = start + offsetShift;
    addToken(zzBuffer, start, end, tokenType, so, false);
  }

  /**
   * Adds the token specified to the current linked list of tokens.
   *
   * @param array       The character array.
   * @param start       The starting offset in the array.
   * @param end         The ending offset in the array.
   * @param tokenType   The token's type.
   * @param startOffset The offset in the document at which this token
   *                    occurs.
   * @param hyperlink   Whether this token is a hyperlink.
   */
  public void addToken(char[] array, int start, int end, int tokenType,
                       int startOffset, boolean hyperlink) {
    super.addToken(array, start, end, tokenType, startOffset, hyperlink);
    zzStartRead = zzMarkedPos;
  }

  /**
   * {@inheritDoc}
   */
  public String[] getLineCommentStartAndEnd(int languageIndex) {
    return null;
  }

  /**
   * Returns the first token in the linked list of tokens generated
   * from <code>text</code>.  This method must be implemented by
   * subclasses so they can correctly implement syntax highlighting.
   *
   * @param text             The text from which to get tokens.
   * @param initialTokenType The token type we should start with.
   * @param startOffset      The offset into the document at which
   *                         <code>text</code> starts.
   * @return The first <code>Token</code> in a linked list representing
   * the syntax highlighted text.
   */
  public Token getTokenList(Segment text, int initialTokenType, int startOffset) {

    resetTokenList();
    this.offsetShift = -text.offset + startOffset;

    // Start off in the proper state.
    int state = Token.NULL;
    switch (initialTokenType) {
      /* No multi-line comments */
      /* No documentation comments */
      default:
        state = Token.NULL;
    }

    s = text;
    try {
      yyreset(zzReader);
      yybegin(state);
      return yylex();
    } catch (IOException ioe) {
      ioe.printStackTrace();
      return new TokenImpl();
    }

  }

  /**
   * Refills the input buffer.
   *
   * @return <code>true</code> if EOF was reached, otherwise
   * <code>false</code>.
   */
  private boolean zzRefill() {
    return zzCurrentPos >= s.offset + s.count;
  }

  /**
   * Resets the scanner to read from a new input stream.
   * Does not close the old reader.
   * <p>
   * All internal variables are reset, the old input stream
   * <b>cannot</b> be reused (internal buffer is discarded and lost).
   * Lexical state is set to <tt>YY_INITIAL</tt>.
   *
   * @param reader the new input stream
   */
  public final void yyreset(Reader reader) {
    // 's' has been updated.
    zzBuffer = s.array;
    /*
     * We replaced the line below with the two below it because zzRefill
     * no longer "refills" the buffer (since the way we do it, it's always
     * "full" the first time through, since it points to the segment's
     * array).  So, we assign zzEndRead here.
     */
    //zzStartRead = zzEndRead = s.offset;
    zzStartRead = s.offset;
    zzEndRead = zzStartRead + s.count - 1;
    zzCurrentPos = zzMarkedPos = zzPushbackPos = s.offset;
    zzLexicalState = YYINITIAL;
    zzReader = reader;
    zzAtBOL = true;
    zzAtEOF = false;
  }

  /**
   * Closes the input stream.
   */
  public final void yyclose() throws java.io.IOException {
    zzAtEOF = true;            /* indicate end of file */
    zzEndRead = zzStartRead;  /* invalidate buffer    */

    if (zzReader != null) {
      zzReader.close();
    }
  }


  /**
   * Enters a new lexical state
   *
   * @param newState the new lexical state
   */
  public final void yybegin(int newState) {
    zzLexicalState = newState;
  }


  /**
   * Returns the text matched by the current regular expression.
   */
  public final String yytext() {
    return new String(zzBuffer, zzStartRead, zzMarkedPos - zzStartRead);
  }


  /**
   * Returns the character at position <tt>pos</tt> from the
   * matched text.
   * <p>
   * It is equivalent to yytext().charAt(pos), but faster
   *
   * @param pos the position of the character to fetch.
   *            A value from 0 to yylength()-1.
   * @return the character at position pos
   */
  public final char yycharat(int pos) {
    return zzBuffer[zzStartRead + pos];
  }


  /**
   * Returns the length of the matched text region.
   */
  public final int yylength() {
    return zzMarkedPos - zzStartRead;
  }


  /**
   * Reports an error that occured while scanning.
   * <p>
   * In a wellformed scanner (no or only correct usage of
   * yypushback(int) and a match-all fallback rule) this method
   * will only be called with things that "Can't Possibly Happen".
   * If this method is called, something is seriously wrong
   * (e.g. a JFlex bug producing a faulty scanner etc.).
   * <p>
   * Usual syntax/scanner level error handling should be done
   * in error fallback rules.
   *
   * @param errorCode the code of the errormessage to display
   */
  private void zzScanError(int errorCode) {
    String message;
    try {
      message = ZZ_ERROR_MSG[errorCode];
    } catch (ArrayIndexOutOfBoundsException e) {
      message = ZZ_ERROR_MSG[ZZ_UNKNOWN_ERROR];
    }

    throw new Error(message);
  }


  /**
   * Pushes the specified amount of characters back into the input stream.
   * <p>
   * They will be read again by then next call of the scanning method
   *
   * @param number the number of characters to be read again.
   *               This number must not be greater than yylength()!
   */
  public void yypushback(int number) {
    if (number > yylength()) {
      zzScanError(ZZ_PUSHBACK_2BIG);
    }

    zzMarkedPos -= number;
  }


  /**
   * Resumes scanning until the next regular expression is matched,
   * the end of input is encountered or an I/O-Error occurs.
   *
   * @return the next token
   * @throws java.io.IOException if any I/O-Error occurs
   */
  public org.fife.ui.rsyntaxtextarea.Token yylex() throws java.io.IOException {
    int zzInput;
    int zzAction;

    // cached fields:
    int zzCurrentPosL;
    int zzMarkedPosL;
    int zzEndReadL = zzEndRead;
    char[] zzBufferL = zzBuffer;
    char[] zzCMapL = ZZ_CMAP;

    int[] zzTransL = ZZ_TRANS;
    int[] zzRowMapL = ZZ_ROWMAP;
    int[] zzAttrL = ZZ_ATTRIBUTE;

    while (true) {
      zzMarkedPosL = zzMarkedPos;

      zzAction = -1;

      zzCurrentPosL = zzCurrentPos = zzStartRead = zzMarkedPosL;

      zzState = zzLexicalState;


      zzForAction:
      {
        while (true) {

          if (zzCurrentPosL < zzEndReadL) {
            zzInput = zzBufferL[zzCurrentPosL++];
          } else if (zzAtEOF) {
            zzInput = YYEOF;
            break zzForAction;
          } else {
            // store back cached positions
            zzCurrentPos = zzCurrentPosL;
            zzMarkedPos = zzMarkedPosL;
            boolean eof = zzRefill();
            // get translated positions and possibly new buffer
            zzCurrentPosL = zzCurrentPos;
            zzMarkedPosL = zzMarkedPos;
            zzBufferL = zzBuffer;
            zzEndReadL = zzEndRead;
            if (eof) {
              zzInput = YYEOF;
              break zzForAction;
            } else {
              zzInput = zzBufferL[zzCurrentPosL++];
            }
          }
          int zzNext = zzTransL[zzRowMapL[zzState] + zzCMapL[zzInput]];
          if (zzNext == -1) {
            break zzForAction;
          }
          zzState = zzNext;

          int zzAttributes = zzAttrL[zzState];
          if ((zzAttributes & 1) == 1) {
            zzAction = zzState;
            zzMarkedPosL = zzCurrentPosL;
            if ((zzAttributes & 8) == 8) {
              break zzForAction;
            }
          }

        }
      }

      // store back cached position
      zzMarkedPos = zzMarkedPosL;

      switch (zzAction < 0 ? zzAction : ZZ_ACTION[zzAction]) {
        case 1: {
          addToken(Token.IDENTIFIER);
        }
        case 8:
          break;
        case 4: {
          addToken(Token.SEPARATOR);
        }
        case 9:
          break;
        case 2: {
          addNullToken();
          return firstToken;
        }
        case 10:
          break;
        case 3: {
          addToken(Token.WHITESPACE);
        }
        case 11:
          break;
        case 5: {
          addToken(Token.RESERVED_WORD_2);
        }
        case 12:
          break;
        case 6: {
          addToken(Token.RESERVED_WORD);
        }
        case 13:
          break;
        case 7: {
          addToken(Token.DATA_TYPE);
        }
        case 14:
          break;
        default:
          if (zzInput == YYEOF && zzStartRead == zzCurrentPos) {
            zzAtEOF = true;
            switch (zzLexicalState) {
              case YYINITIAL: {
                addNullToken();
                return firstToken;
              }
              case 70:
                break;
              default:
                return null;
            }
          } else {
            zzScanError(ZZ_NO_MATCH);
          }
      }
    }
  }


}
