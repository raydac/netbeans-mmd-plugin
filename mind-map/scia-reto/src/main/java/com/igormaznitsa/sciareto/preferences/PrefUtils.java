/*
 * Copyright 2015-2018 Igor Maznitsa.
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
package com.igormaznitsa.sciareto.preferences;

import java.awt.Font;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class PrefUtils {
  private PrefUtils(){
    
  }
  
  @Nonnull
  public static String font2str(@Nonnull final Font font){
    final StringBuilder buffer = new StringBuilder();
    buffer.append(font.getFontName()).append('|').append(font.getStyle()).append('|').append(font.getSize());
    return buffer.toString();
  }

  @Nullable
  public static Font str2font(@Nullable final String text, @Nullable final Font defaultFont){
    if (text == null) return defaultFont;
    final String [] fields = text.split("\\|"); //NOI18N
    if (fields.length!=3) return defaultFont;
    try{
      return new Font(fields[0], Integer.parseInt(fields[1]),Integer.parseInt(fields[2]));
    }catch(NumberFormatException ex){
      return defaultFont;
    }
  }
}
