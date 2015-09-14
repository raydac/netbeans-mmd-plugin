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

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.commons.lang.StringEscapeUtils;

public abstract class Extra <T> implements Serializable, Constants, Cloneable {
  private static final long serialVersionUID = 2547528075256486018L;

  public enum ExtraType {
    FILE,
    LINK,
    NOTE,
    TOPIC;
    
    public boolean isStringValid(final String str){
      boolean result;
      switch(this){
        case FILE :
        case LINK :{
          try{
            new URI(str);
            result = true;
          }catch(Exception ex){
            result = false;
          }
        }break;
        default: result = true; break;
      }
      return result;
    }
    
    public Extra<?> parseLoaded(final String text) throws URISyntaxException{
      final String preprocessed = StringEscapeUtils.unescapeHtml(text);
      switch(this){
        case FILE : return new ExtraFile(preprocessed);
        case LINK : return new ExtraLink(preprocessed);
        case NOTE : return new ExtraNote(preprocessed);
        case TOPIC : return new ExtraTopic(preprocessed);
        default: throw new Error("Unexpected value ["+this.name()+']'); //NOI18N
      }
    }
  }
  
  public abstract T getValue();
  public abstract ExtraType getType();
  public abstract String getAsString();

  public abstract String provideAsStringForSave();
  
  public final void write(final Writer out) throws IOException {
    out.append("- ").append(getType().name()).append(NEXT_LINE); //NOI18N
    out.append(ModelUtils.makePreBlock(provideAsStringForSave()));
  }
  
}
