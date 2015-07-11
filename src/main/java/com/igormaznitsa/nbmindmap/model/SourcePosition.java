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
package com.igormaznitsa.nbmindmap.model;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SourcePosition implements Serializable {
  private static final long serialVersionUID = -2188884872060263941L;
  
  private final String sourceName;
  private final int line;
  private final int pos;
  
  private static final Pattern pattern = Pattern.compile("^([^\\[]*)\\[(\\-?\\d+)\\:(\\-?\\d+)\\]$");
  
  public SourcePosition(final String sourceName, final int line, final int pos){
    this.sourceName = sourceName == null ? "" : sourceName;
    this.line = line;
    this.pos = pos;
  }
  
  public SourcePosition(final String packedSourceRecord){
    final Matcher matcher = pattern.matcher(packedSourceRecord);
    if (matcher.find()){
      this.sourceName = matcher.group(1);
      this.line = Integer.parseInt(matcher.group(2));
      this.pos = Integer.parseInt(matcher.group(3));
    }else{
      throw new IllegalArgumentException("Wrong format of source position ["+packedSourceRecord+']');
    }
  }
  
  public String getSourceName(){
    return this.sourceName;
  }
  
  public int getLine(){
    return this.line;
  }

  public int getPosition(){
    return this.pos;
  }

  @Override
  public String toString(){
    return this.sourceName+'['+this.line+':'+this.pos+']';
  }
}
