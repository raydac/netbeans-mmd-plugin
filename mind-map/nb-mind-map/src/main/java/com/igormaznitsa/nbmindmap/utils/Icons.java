/*
 * Copyright (C) 2015-2022 Igor A. Maznitsa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.igormaznitsa.nbmindmap.utils;

import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import org.apache.commons.io.IOUtils;

public enum Icons {
  DOCUMENT("document16.png"), //NOI18N
  EXPANDALL("toggle_expand16.png"), //NOI18N
  COLLAPSEALL("toggle16.png"), //NOI18N
  SOURCE("source16.png"), //NOI18N
  BLUEBALL("blueball16.png"), //NOI18N
  GOLDBALL("goldball16.png"), //NOI18N
  CROSS("cross16.png"), //NOI18N
  BROWSE("world_link16.png"), //NOI18N
  REDO("redo16.png"), //NOI18N
  UNDO("undo16.png"), //NOI18N
  CLEAR_ALL("cross16.png"), //NOI18N
  COPY("page_copy16.png"), //NOI18N
  EXPORT("file_save16.png"), //NOI18N
  IMPORT("disk16.png"), //NOI18N
  PASSWORD_ON("set_password16on.png"), //NOI18N
  PASSWORD_OFF("set_password16.png"), //NOI18N
  PASTE("paste_plain16.png"), //NOI18N
  COINS("coins_in_hand16.png"); //NOI18N

  private final ImageIcon icon;
  
  public ImageIcon getIcon(){
    return this.icon;
  }
  
  private Icons(final String name) {
    final InputStream in = Icons.class.getClassLoader().getResourceAsStream("com/igormaznitsa/nbmindmap/icons/" + name); //NOI18N
    try {
      this.icon = new ImageIcon(ImageIO.read(in));
    }
    catch (IOException ex) {
      throw new Error("Can't load icon " + name, ex); //NOI18N
    }
    finally {
      IOUtils.closeQuietly(in);
    }
  }
}
