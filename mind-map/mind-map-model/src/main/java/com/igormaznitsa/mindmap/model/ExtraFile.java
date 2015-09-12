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

import java.net.URISyntaxException;

public class ExtraFile extends Extra<MMapURI> implements ExtraLinkable{
  private static final long serialVersionUID = -478916403235887225L;

  private final MMapURI fileUri;

  private volatile String cachedString;

  private final boolean mmdFileFlag;
  
  public ExtraFile(final MMapURI file){
    this.fileUri = file;
    this.mmdFileFlag = this.fileUri.getExtension().equalsIgnoreCase("mmd");
  }

  public ExtraFile(final String text) throws URISyntaxException {
    this(new MMapURI(text));
  }

  public boolean isMMDFile(){
    return this.mmdFileFlag;
  }
  
  @Override
  public MMapURI getValue() {
    return fileUri;
  }

  @Override
  public ExtraType getType() {
    return ExtraType.FILE;
  }

  @Override
  public String getAsString() {
    if (this.cachedString == null){
      this.cachedString = this.fileUri.asFile(null).getPath();
    }
    return this.cachedString;
  }

  @Override
  public String provideAsStringForSave() {
    return this.fileUri.toString();
  }

  @Override
  public MMapURI getAsURI() {
    return this.fileUri;
  }

}
