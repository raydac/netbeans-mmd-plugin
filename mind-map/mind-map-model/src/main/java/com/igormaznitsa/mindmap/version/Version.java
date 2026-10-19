/*
 * Copyright (C) 2015-2026 Igor A. Maznitsa
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

package com.igormaznitsa.mindmap.version;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * It represents a version and allows getting access to its information parts. The Representer can be created from text representation of a version or through providing version fields directly.
 * Format of version is : {prefix-}?{ddd.ddd.ddd...ddd}?{-postfix}?
 *
 * @since 1.0.0
 */
public final class Version implements Comparable<Version>, Serializable {

  private static final Pattern EXTRACTOR = Pattern.compile("^([^\\d.]+)-|\\.?([\\d]+)|-?(.*)$");
  private static final long serialVersionUID = -4409642391893263592L;

  private final long[] numericParts;
  private final String prefix;
  private final String postfix;
  private final int hash;

  /**
   * Create instance from a string.
   *
   * @param string text representation of version
   * @since 1.0.0
   */
  public Version(final String string) {
    if (string == null) {
      this.prefix = "";
      this.postfix = "";
      this.numericParts = new long[0];
    } else {
      final List<Long> detectedDigits = new ArrayList<>();
      final Matcher matcher = EXTRACTOR.matcher(string.trim());
      String tail = "";
      String start = "";
      while (matcher.find()) {
        final String grpStart = matcher.group(1);
        final String grpNum = matcher.group(2);
        final String grpTail = matcher.group(3);
        if (grpStart != null) {
          start = grpStart;
        } else if (grpNum != null) {
          detectedDigits.add(Long.parseLong(grpNum));
        } else if (grpTail != null) {
          tail = grpTail;
          break;
        }
      }
      this.numericParts = new long[detectedDigits.size()];
      for (int i = 0; i < detectedDigits.size(); i++) {
        this.numericParts[i] = detectedDigits.get(i);
      }
      this.postfix = tail;
      this.prefix = start;
    }

    this.hash = this.toString().hashCode();
  }

  /**
   * Create version based only on numeric parts. Both Prefix and postfix are empty.
   *
   * @param parts numeric parts of the version, it can be null
   * @since 1.0.0
   */
  public Version(final long... parts) {
    this(null, parts, null);
  }

  /**
   * Create version from provided components.
   *
   * @param prefix       the prefix, it can be null
   * @param numericParts the numeric parts, it can be null
   * @param postfix      the postfix, it can be null
   * @since 1.0.0
   */
  public Version(final String prefix, final long[] numericParts, final String postfix) {
    this.prefix = prefix == null ? "" : prefix.trim();
    this.postfix = postfix == null ? "" : postfix.trim();

    this.numericParts = new long[numericParts == null ? 0 : numericParts.length];
    if (numericParts != null) {
      for (int i = 0; i < numericParts.length; i++) {
        this.numericParts[i] = Math.abs(numericParts[i]);
      }
    }

    this.hash = this.toString().hashCode();
  }

  /**
   * Create copy of version with changed prefix.
   *
   * @param prefix the new prefix, it can be null
   * @return copy of the version with replaced prefix
   * @since 1.0.0
   */
  public Version changePrefix(final String prefix) {
    return new Version(prefix, this.numericParts, this.postfix);
  }

  /**
   * Create copy of version with changed postfix.
   *
   * @param postfix the new postfix, it can be null
   * @return copy of the version with replaced postfix
   * @since 1.0.0
   */
  public Version changePostfix(final String postfix) {
    return new Version(this.prefix, this.numericParts, postfix);
  }

  /**
   * Create copy of version with changed numeric parts.
   *
   * @param numericParts the new numeric parts, it can be null
   * @return copy of the version with replaced numeric parts
   * @since 1.0.0
   */
  public Version changeNumeric(final long... numericParts) {
    return new Version(this.prefix, numericParts, this.postfix);
  }

  /**
   * Get numeric part of version at position.
   *
   * @param position the position of needed numeric part.
   * @return the numeric part, if it is not presented then 0 will be returned
   * @since 1.0.0
   */
  public long getNumericPartAtPosition(final int position) {
    return position < 0 || position >= this.numericParts.length ? 0L : this.numericParts[position];
  }

  /**
   * Get the prefix part.
   *
   * @return the prefix, it is not null
   * @since 1.0.0
   */
  public String getPrefix() {
    return this.prefix;
  }

  /**
   * Get the postfix part.
   *
   * @return the postfix part, it is not null
   * @since 1.0.0
   */
  public String getPostfix() {
    return this.postfix;
  }

  /**
   * Get the major (first) numeric element of version.
   *
   * @return the first element of version
   * @since 1.0.0
   */
  public long getMajor() {
    return this.getNumericPartAtPosition(0);
  }

  /**
   * Get the minor (second) numeric element of version.
   *
   * @return the second element of version.
   * @since 1.0.0
   */
  public long getMinor() {
    return this.getNumericPartAtPosition(1);
  }

  /**
   * Get the micro (third) numeric element of version.
   *
   * @return the third element of version.
   * @since 1.0.0
   */
  public long getMicro() {
    return this.getNumericPartAtPosition(2);
  }

  @Override
  public int hashCode() {
    return this.hash;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }
    boolean result = false;
    if (obj instanceof Version) {
      final Version that = (Version) obj;
      result =
          this.prefix.equals(that.prefix) && Arrays.equals(this.numericParts, that.numericParts) &&
              this.postfix.equals(that.postfix);
    }
    return result;
  }

  @Override
  public String toString() {
    final StringBuilder result = new StringBuilder();
    if (!this.prefix.isEmpty()) {
      result.append(this.prefix);
    }

    if (this.numericParts.length > 0) {
      if (result.length() > 0) {
        result.append('-');
      }
      boolean nofirst = false;
      for (final long i : this.numericParts) {
        if (nofirst) {
          result.append('.');
        }
        result.append(i);
        nofirst = true;
      }
    }
    if (!this.postfix.isEmpty()) {
      if (result.length() > 0 && result.charAt(result.length() - 1) != '-') {
        result.append('-');
      }
      result.append(this.postfix);
    }
    return result.toString();
  }

  @Override
  public int compareTo(final Version version) {
    final long[] thatNumbers = version.numericParts;

    final int comparePrefix = this.prefix.compareTo(version.prefix);
    if (comparePrefix != 0) {
      return comparePrefix;
    }

    final int maxnum = Math.max(this.numericParts.length, thatNumbers.length);

    for (int i = 0; i < maxnum; i++) {
      final long x = i < this.numericParts.length ? this.numericParts[i] : 0L;
      final long y = i < thatNumbers.length ? thatNumbers[i] : 0L;
      final int result = Long.compare(x, y);
      if (result != 0) {
        return result;
      }
    }

    return this.postfix.compareTo(version.postfix);
  }

  /**
   * Check is there any numeric part in the version.
   *
   * @return true if there is numeric part, false otherwise.
   * @since 1.0.0
   */
  public boolean isNumericPartPresented() {
    return this.numericParts.length > 0;
  }
}
