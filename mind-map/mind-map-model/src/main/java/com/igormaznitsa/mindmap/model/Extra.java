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

package com.igormaznitsa.mindmap.model;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.text.StringEscapeUtils.unescapeHtml3;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Abstract class describing extra part of a mind maven topic
 *
 * @param <T> type of saved value
 */
public abstract class Extra<T> implements Serializable, Constants, Cloneable {

  private static final long serialVersionUID = 2547528075256486018L;

  /**
   * Get saved value
   *
   * @return saved value must not be null
   */
  public abstract T getValue();

  /**
   * Type of saved value
   *
   * @return type of saved value, must not e null
   */
  public abstract ExtraType getType();

  /**
   * Get value string representation
   *
   * @return string representation of value, must not be null
   */
  public abstract String getAsString();

  /**
   * Check that the extra is allowed for export operations.
   *
   * @return true if allows, false otherwise
   */
  public boolean isExportable() {
    return true;
  }

  /**
   * Called during write operations and allows to add attributes based on value.
   *
   * @param attributesForWrite map to be saved, can be empty but can't be null
   */
  void addAttributesForWrite(final Map<String, String> attributesForWrite) {
  }

  /**
   * Called when it is attached to topic and provides way to set some attributes
   *
   * @param topic new target topic, can't be null
   */
  void attachedToTopic(final Topic topic) {

  }

  /**
   * Called when it is removed from topic and provides way to remove some attributes
   *
   * @param topic topic, can't be null
   */
  void detachedToTopic(final Topic topic) {

  }

  /**
   * Get as string during save mind map file.
   *
   * @return string to be saved into mind map file, must not be null
   */
  public abstract String provideAsStringForSave();

  /**
   * Check content for pattern if allowed.
   *
   * @param baseFolder base folder for mind map file, can be null
   * @param pattern    pattern to be tested, must not be null
   * @return true if extra contains content matches with pattern, false otherwise
   */
  public abstract boolean containsPattern(File baseFolder, Pattern pattern);

  /**
   * Write extra into writer.
   *
   * @param out target writer, must not be null
   * @throws IOException thrown if any error
   */
  public final void write(final Writer out) throws IOException {
    out.append("- ").append(getType().name()).append(NEXT_LINE);
    out.append(ModelUtils.makePreBlock(provideAsStringForSave()));
  }

  @Override
  protected abstract Object clone() throws CloneNotSupportedException;

  /**
   * Type of extra.
   */
  public enum ExtraType {
    /**
     * File extension
     */
    FILE,
    /**
     * Link extension
     */
    LINK,
    /**
     * Text note extension
     */
    NOTE,
    /**
     * Link to another topic extension
     */
    TOPIC,
    /**
     * Unknown
     */
    UNKNOWN;

    /**
     * Used during parse extra text from mind map file.
     *
     * @param str string to be preprocessed, can be null
     * @return preprocessed one or null
     */
    public String preprocessString(final String str) {
      String result = null;
      if (str != null) {
        switch (this) {
          case FILE:
          case LINK: {
            try {
              result = str.trim();
              requireNonNull(URI.create(result));
            } catch (IllegalArgumentException ex) {
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

    /**
     * Parse loaded extra text as an extra object.
     *
     * @param text       loaded text, can't be null
     * @param attributes map of attributes, can't be null
     * @return parsed extra object
     * @throws URISyntaxException if there is malformed uri
     */
    public Extra<?> parseLoaded(final String text, final Map<String, String> attributes)
        throws URISyntaxException {
      final String preprocessed = unescapeHtml3(text);
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
          throw new Error("Unexpected value [" + this.name() + ']');
      }
    }
  }
}
