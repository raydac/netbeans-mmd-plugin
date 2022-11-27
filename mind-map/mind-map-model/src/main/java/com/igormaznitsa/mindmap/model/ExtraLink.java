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

import java.io.File;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

/**
 * Extra item describing some link to URI. IN opposite to ExtraFile it doesn't provide auxiliary info and extension processing.
 */
public class ExtraLink extends Extra<MMapURI> implements ExtraLinkable {
  private static final long serialVersionUID = -3343908686571445847L;
  private final MMapURI uri;

  private ExtraLink(final ExtraLink extraLink) {
    super();
    this.uri = extraLink.uri;
  }

  /**
   * Constructor.
   *
   * @param uri uri to be used as value for the link object, must not be null
   */
  public ExtraLink(final MMapURI uri) {
    this.uri = requireNonNull(uri);
  }

  /**
   * Constructor.
   *
   * @param uri text of URI link, must not be null
   * @throws URISyntaxException thrown if malformed URI
   */
  public ExtraLink(final String uri) throws URISyntaxException {
    super();
    this.uri = new MMapURI(requireNonNull(uri));
  }

  @Override
  protected Object clone() throws CloneNotSupportedException {
    return new ExtraLink(this);
  }

  @Override
  public int hashCode() {
    return this.uri.hashCode();
  }

  @Override
  public boolean equals(final Object that) {
    if (that == null) {
      return false;
    }
    if (this == that) {
      return true;
    }

    if (that instanceof ExtraLink) {
      return this.uri.equals(((ExtraLink) that).uri);
    } else {
      return false;
    }
  }

  @Override
  public boolean containsPattern(final File baseFolder, final Pattern pattern) {
    return pattern.matcher(this.uri.toString()).find();
  }

  @Override
  public MMapURI getValue() {
    return this.uri;
  }

  @Override
  public ExtraType getType() {
    return ExtraType.LINK;
  }

  @Override
  public String getAsString() {
    return this.uri.toString();
  }

  @Override
  public String provideAsStringForSave() {
    return this.uri.asString(false, true);
  }

  @Override
  public MMapURI getAsURI() {
    return this.uri;
  }

}
