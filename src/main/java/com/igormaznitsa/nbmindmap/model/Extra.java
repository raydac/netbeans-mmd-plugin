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
import java.io.Serializable;
import java.io.Writer;
import java.net.URISyntaxException;

public abstract class Extra <T> implements Serializable, Constants {
  private static final long serialVersionUID = 2547528075256486018L;

  public enum ExtraType {
    FILE,
    LINK,
    NOTE,
    SRC_POSITION;
    
    public Extra<?> make(final String text) throws URISyntaxException{
      switch(this){
        case FILE : return new ExtraFile(text);
        case LINK : return new ExtraLink(text);
        case NOTE : return new ExtraNote(text);
        case SRC_POSITION : return new ExtraSource(text);
        default: throw new Error("Unexpected value ["+this.name()+']');
      }
    }
  }
  
  public abstract T getValue();
  public abstract ExtraType getType();
  public abstract void writeContent(Writer out) throws IOException;

  public final void write(final Writer out) throws IOException {
    out.append("- ").append(getType().name()).append(NEXT_LINE);
    writeContent(out);
  }
  
  protected static String makeCodeBlock(final String text) throws IOException {
    final int maxQuotes = Utils.calcMaxLengthOfBacktickQuotesSubstr(text)+1;
    final StringBuilder result = new StringBuilder(text.length()+16);
    Utils.writeChar(result, '`', maxQuotes);
    result.append(text);
    Utils.writeChar(result, '`', maxQuotes);
    return result.toString();
  }
}
