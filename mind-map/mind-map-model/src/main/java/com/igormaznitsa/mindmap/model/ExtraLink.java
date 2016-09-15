/*
 * Copyright 2015 Igor Maznitsa.
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
import java.net.URISyntaxException;

import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ExtraLink extends Extra<MMapURI> implements ExtraLinkable {
  private static final long serialVersionUID = -3343908686571445847L;
  private final MMapURI uri;
  
  public ExtraLink(@Nonnull final MMapURI uri) {
    this.uri = uri;
  }

  public ExtraLink(@Nonnull final String text) throws URISyntaxException {
    this.uri = new MMapURI(text);
  }

  @Override
  public boolean containsPattern(@Nullable final File baseFolder, @Nonnull final Pattern pattern) {
    return pattern.matcher(this.uri.toString()).find();
  }

  @Override
  @Nonnull
  public MMapURI getValue() {
    return this.uri;
  }

  @Override
  @Nonnull
  public ExtraType getType() {
    return ExtraType.LINK;
  }

  @Override
  @Nonnull
  public String getAsString() {
    return this.uri.toString();
  }

  @Override
  @Nonnull
  public String provideAsStringForSave() {
    return this.uri.toString();
  }
  
  @Override
  @Nonnull
  public MMapURI getAsURI() {
    return this.uri;
  }

}
