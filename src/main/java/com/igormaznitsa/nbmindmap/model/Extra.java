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

import com.igormaznitsa.nbmindmap.utils.Utils;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.net.URISyntaxException;
import org.apache.commons.lang.StringEscapeUtils;

public abstract class Extra <T> implements Serializable, Constants, Cloneable {
  private static final long serialVersionUID = 2547528075256486018L;

  public enum ExtraType {
    FILE,
    LINK,
    NOTE,
    TOPIC,
    LINE;
    
    public Extra<?> parseLoaded(final String text) throws URISyntaxException{
      final String preprocessed = StringEscapeUtils.unescapeHtml(text);
      switch(this){
        case FILE : return new ExtraFile(preprocessed);
        case LINK : return new ExtraLink(preprocessed);
        case NOTE : return new ExtraNote(preprocessed);
        case TOPIC : return new ExtraTopic(preprocessed);
        case LINE : return new ExtraLine(preprocessed);
        default: throw new Error("Unexpected value ["+this.name()+']'); //NOI18N
      }
    }
  }
  
  public abstract T getValue();
  public abstract ExtraType getType();
  public abstract String getAsString();

  public final void write(final Writer out) throws IOException {
    out.append("- ").append(getType().name()).append(NEXT_LINE); //NOI18N
    out.append(Utils.makePreBlock(getAsString()));
  }
  
}
