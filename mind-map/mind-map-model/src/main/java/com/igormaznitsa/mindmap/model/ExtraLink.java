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

import java.net.URI;
import java.net.URISyntaxException;

public class ExtraLink extends Extra<URI> implements ExtraLinkable {
  private static final long serialVersionUID = -3343908686571445847L;
  private final URI uri;
  
  public ExtraLink(final URI uri){
    this.uri = uri;
  }

  public ExtraLink(final String text) throws URISyntaxException {
    this.uri = new URI(text);
  }
  
  @Override
  public URI getValue() {
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
  public URI getAsURI() {
    return this.uri;
  }

}
