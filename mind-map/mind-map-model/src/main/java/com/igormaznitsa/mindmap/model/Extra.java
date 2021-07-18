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

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class Extra<T> implements Serializable, Constants, Cloneable {

  private static final long serialVersionUID = 2547528075256486018L;

  @Nonnull
  public abstract T getValue();

  @Nonnull
  public abstract ExtraType getType();

  @Nonnull
  public abstract String getAsString();

  public boolean isExportable() {
    return true;
  }

  void addAttributesForWrite(@Nonnull final Map<String, String> attributesForWrite) {
  }

  void attachedToTopic(@Nonnull final Topic topic) {

  }

  void detachedToTopic(@Nonnull final Topic topic) {

  }

  @Nonnull
  public abstract String provideAsStringForSave();

  public abstract boolean containsPattern(@Nullable File baseFolder, @Nonnull Pattern pattern);

  public final void write(@Nonnull final Writer out) throws IOException {
    out.append("- ").append(getType().name()).append(NEXT_LINE); //NOI18N
    out.append(ModelUtils.makePreBlock(provideAsStringForSave()));
  }

  public enum ExtraType {

    FILE,
    LINK,
    NOTE,
    TOPIC,
    UNKNOWN;

    @Nullable
    public String preprocessString(@Nullable final String str) {
      String result = null;
      if (str != null) {
        switch (this) {
          case FILE:
          case LINK: {
            try {
              result = str.trim();
              URI.create(result);
            } catch (Exception ex) {
              result = null;
            }
          }
          break;
          case TOPIC: {
            result = str.trim();
          }
          break;
          default:
            result = str;
            break;
        }
      }
      return result;
    }

    @Nonnull
    public Extra<?> parseLoaded(@Nonnull final String text, @Nonnull Map<String, String> attributes)
        throws URISyntaxException {
      final String preprocessed = StringEscapeUtils.unescapeHtml3(text);
      switch (this) {
        case FILE:
          return new ExtraFile(preprocessed);
        case LINK:
          return new ExtraLink(preprocessed);
        case NOTE: {
          final boolean encrypted = Boolean.parseBoolean(attributes.get(ExtraNote.ATTR_ENCRYPTED));
          final String passwordTip = attributes.get(ExtraNote.ATTR_PASSWORD_HINT);
          return new ExtraNote(preprocessed, encrypted, passwordTip);
        }
        case TOPIC:
          return new ExtraTopic(preprocessed);
        default:
          throw new Error("Unexpected value [" + this.name() + ']'); //NOI18N
      }
    }
  }

}
