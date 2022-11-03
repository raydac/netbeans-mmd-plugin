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

package com.igormaznitsa.mindmap.model;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Extra topic item contains text, the text can be encrypted.
 */
public class ExtraNote extends Extra<String> {
  public static final String ATTR_ENCRYPTED = "extras.note.encrypted";
  public static final String ATTR_PASSWORD_HINT = "extras.note.encrypted.hint";
  private static final long serialVersionUID = 8612886872756838147L;
  private final String text;
  private final boolean encrypted;
  private final String hint;

  private ExtraNote(final ExtraNote extraNote) {
    super();
    this.text = extraNote.text;
    this.encrypted = extraNote.encrypted;
    this.hint = extraNote.hint;
  }


  /**
   * Create note for non-encrypted text
   *
   * @param text text, must not be null
   */
  public ExtraNote(final String text) {
    this(text, false, null);
  }

  /**
   * Constructor.
   *
   * @param text      text of note, must not be null
   * @param encrypted flag shows that text is encrypted if true
   * @param hint      non-encrypted text hint for encrypted text, can be null if missing
   */
  public ExtraNote(final String text,
                   final boolean encrypted,
                   final String hint) {
    super();
    this.text = requireNonNull(text);
    this.encrypted = encrypted;
    this.hint = hint;
  }

  @Override
  protected Object clone() throws CloneNotSupportedException {
    return new ExtraNote(this);
  }

  @Override
  public boolean isExportable() {
    return !this.encrypted;
  }

  /**
   * Flag shows that text is encrypted
   *
   * @return true if text is encrypted, false otherwise
   */
  public boolean isEncrypted() {
    return this.encrypted;
  }

  /**
   * Get hint for encrypted text
   *
   * @return hint as on-encrypted string, can be null if hint missing
   */
  public String getHint() {
    return this.hint;
  }

  @Override
  void attachedToTopic(final Topic topic) {
    if (this.encrypted) {
      topic.putAttribute(ATTR_ENCRYPTED, "true");
      topic.putAttribute(ATTR_PASSWORD_HINT, this.hint);
    } else {
      topic.putAttribute(ATTR_ENCRYPTED, null);
      topic.putAttribute(ATTR_PASSWORD_HINT, null);
    }
  }

  @Override
  void detachedToTopic(final Topic topic) {
    topic.putAttribute(ATTR_ENCRYPTED, null);
    topic.putAttribute(ATTR_PASSWORD_HINT, null);
  }

  @Override
  void addAttributesForWrite(final Map<String, String> attributesForWrite) {
    if (this.encrypted) {
      attributesForWrite.put(ATTR_ENCRYPTED, "true");
    }
    if (this.hint != null) {
      attributesForWrite.put(ATTR_PASSWORD_HINT, this.hint);
    }
  }

  @Override
  public boolean containsPattern(final File baseFolder, final Pattern pattern) {
    return !this.encrypted && pattern.matcher(this.text).find();
  }

  @Override
  public boolean equals(final Object that) {
    if (that == null) {
      return false;
    }
    if (this == that) {
      return true;
    }
    if (that instanceof ExtraNote) {
      final ExtraNote thatNote = (ExtraNote) that;
      return (Objects.equals(this.hint, thatNote.hint))
          && this.encrypted == thatNote.encrypted
          && this.text.equals(((ExtraNote) that).text);
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return this.text.hashCode();
  }

  @Override
  public String getValue() {
    return this.text;
  }

  @Override
  public ExtraType getType() {
    return ExtraType.NOTE;
  }

  @Override
  public String getAsString() {
    return this.text;
  }

  @Override
  public String provideAsStringForSave() {
    return this.text;
  }

}
