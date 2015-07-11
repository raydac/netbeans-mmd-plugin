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

import java.io.IOException;
import java.io.Writer;

public class ExtraSource extends Extra<SourcePosition> {
  private static final long serialVersionUID = -8556885025460722094L;

  private final SourcePosition position;
  
  public ExtraSource(final SourcePosition pos){
    this.position = pos;
  }
  
  public ExtraSource(final String text){
    this.position = new SourcePosition(text);
  }
  
  @Override
  public SourcePosition getValue() {
    return this.position;
  }

  @Override
  public ExtraType getType() {
    return ExtraType.SRC_POSITION;
  }

  @Override
  public void writeContent(final Writer out) throws IOException {
    out.write(makeCodeBlock(this.position.toString()));
  }
}
