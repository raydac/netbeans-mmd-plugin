/*
 * Copyright 2016 Igor Maznitsa.
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
package com.igormaznitsa.mindmap.plugins.external;

import javax.annotation.Nonnull;

public enum Attribute {
  API("api"), 
  TITLE("title"), 
  AUTHOR("author"), 
  COMPATIBLE("compatible"), 
  VERSION("version"), 
  REFERENCE("reference"), 
  PLUGINS("plugins");
  
  private final String attrname;

  private Attribute(@Nonnull final String attrname) {
    this.attrname = attrname;
  }

  @Nonnull
  public String getAttrName() {
    return this.attrname;
  }

}
