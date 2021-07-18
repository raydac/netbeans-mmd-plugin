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

import java.io.File;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ExtraNote extends Extra<String> {
  public static final String ATTR_ENCRYPTED = "extras.note.encrypted";
  public static final String ATTR_PASSWORD_HINT = "extras.note.encrypted.hint";
  private static final long serialVersionUID = 8612886872756838147L;
  private final String text;
  private final boolean encrypted;
  private final String hint;

  public ExtraNote(@Nonnull final String text) {
    this.text = text;
    this.encrypted = false;
    this.hint = null;
  }

  public ExtraNote(@Nonnull final String text,
                   final boolean encrypted,
                   @Nullable final String hint) {
    this.text = text;
    this.encrypted = encrypted;
    this.hint = hint;
  }

  @Override
  public boolean isExportable() {
    return !this.encrypted;
  }

  public boolean isEncrypted() {
    return this.encrypted;
  }

  @Nullable
  public String getHint() {
    return this.hint;
  }

  @Override
  void attachedToTopic(@Nonnull final Topic topic) {
    if (this.encrypted) {
      topic.setAttribute(ATTR_ENCRYPTED, "true");
      topic.setAttribute(ATTR_PASSWORD_HINT, this.hint);
    } else {
      topic.setAttribute(ATTR_ENCRYPTED, null);
      topic.setAttribute(ATTR_PASSWORD_HINT, null);
    }
  }

  @Override
  void detachedToTopic(@Nonnull final Topic topic) {
    topic.setAttribute(ATTR_ENCRYPTED, null);
    topic.setAttribute(ATTR_PASSWORD_HINT, null);
  }

  @Override
  void addAttributesForWrite(@Nonnull final Map<String, String> attributesForWrite) {
    if (this.encrypted) {
      attributesForWrite.put(ATTR_ENCRYPTED, "true");
    }
    if (this.hint != null) {
      attributesForWrite.put(ATTR_PASSWORD_HINT, this.hint);
    }
  }

  @Override
  public boolean containsPattern(@Nullable final File baseFolder, @Nonnull final Pattern pattern) {
    return !this.encrypted && pattern.matcher(this.text).find();
  }

  @Override
  public boolean equals(@Nullable final Object that) {
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
  @Nonnull
  public String getValue() {
    return this.text;
  }

  @Override
  @Nonnull
  public ExtraType getType() {
    return ExtraType.NOTE;
  }

  @Override
  @Nonnull
  public String getAsString() {
    return this.text;
  }

  @Override
  @Nonnull
  public String provideAsStringForSave() {
    return this.text;
  }

}
