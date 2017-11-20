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
package com.igormaznitsa.mindmap.swing.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.apache.commons.io.IOUtils;
import com.igormaznitsa.mindmap.swing.panel.utils.ScalableIcon;

public class DefaultImageIconService implements ImageIconService {

  private static final Map<IconID, Icon> MAP = new EnumMap<IconID, Icon>(IconID.class);
  
  static {
    MAP.put(IconID.POPUP_EXPORT, loadIcon("export16.png"));
    MAP.put(IconID.POPUP_IMPORT, loadIcon("import16.png"));
    MAP.put(IconID.POPUP_IMPORT_TXT2MM, loadIcon("import_txt2mm.png"));
    MAP.put(IconID.POPUP_IMPORT_XMIND2MM, loadIcon("xmind16.png"));
    MAP.put(IconID.POPUP_IMPORT_NOVAMIND2MM, loadIcon("novamind16.png"));
    MAP.put(IconID.POPUP_IMPORT_COGGLE2MM, loadIcon("coggle16.png"));
    MAP.put(IconID.POPUP_EXPORT_FREEMIND, loadIcon("mm16.png"));
    MAP.put(IconID.POPUP_EXPORT_SVG, loadIcon("svg16.png"));
    MAP.put(IconID.POPUP_EXPORT_MARKDOWN, loadIcon("md16.png"));
    MAP.put(IconID.POPUP_EXPORT_ASCIIDOC, loadIcon("asciidoc16.png"));
    MAP.put(IconID.POPUP_EXPORT_ORGMODE, loadIcon("orgmode16.png"));
    MAP.put(IconID.POPUP_EXPORT_MINDMUP, loadIcon("mup16.png"));
    MAP.put(IconID.POPUP_EXPORT_PNG, loadIcon("png16.png"));
    MAP.put(IconID.POPUP_EXPORT_TEXT, loadIcon("txt16.png"));
    MAP.put(IconID.ICON_PRINTER, loadIcon("printer16.png"));
    MAP.put(IconID.ICON_EMOTICONS, loadIcon("emoticons16.png"));
    MAP.put(IconID.ICON_IMAGES, loadIcon("pictures16.png"));
    MAP.put(IconID.ICON_PAGE, loadIcon("page16.png"));
    MAP.put(IconID.POPUP_EXTRAS_TEXT, loadIcon("note16.png"));
    MAP.put(IconID.POPUP_EXTRAS_FILE, loadIcon("disk16.png"));
    MAP.put(IconID.POPUP_EXTRAS_JUMP, loadIcon("brick16.png"));
    MAP.put(IconID.POPUP_EXTRAS_URI, loadIcon("url16.png"));
    MAP.put(IconID.POPUP_EDIT_TEXT, loadIcon("text16.png"));
    MAP.put(IconID.POPUP_ADD_CHILD, loadIcon("add16.png"));
    MAP.put(IconID.POPUP_CLONE_TOPIC, loadIcon("draw_clone16.png"));
    MAP.put(IconID.POPUP_REMOVE_TOPIC, loadIcon("delete16.png"));
    MAP.put(IconID.POPUP_ABOUT, loadIcon("info16.png"));
    MAP.put(IconID.POPUP_OPTIONS, loadIcon("settings16.png"));
    MAP.put(IconID.POPUP_SHOWJUMPS, loadIcon("showjumps16.png"));
    MAP.put(IconID.POPUP_UNFOLDALL, loadIcon("toggle_expand16.png"));
    MAP.put(IconID.POPUP_COLLAPSEALL, loadIcon("toggle16.png"));
    MAP.put(IconID.POPUP_CHANGECOLOR, loadIcon("color_swatches16.png"));
  }
  
  public DefaultImageIconService(){
  }
  
  @Nonnull
  private static Icon loadIcon(@Nonnull final String name) {
    final InputStream in = ScalableIcon.class.getClassLoader().getResourceAsStream("com/igormaznitsa/mindmap/swing/panel/icons/" + name); //NOI18N
    try {
      return new ImageIcon(ImageIO.read(in));
    } catch (IOException ex) {
      throw new Error("Can't load icon " + name, ex); //NOI18N
    } finally {
      IOUtils.closeQuietly(in);
    }
  }
  
  
  @Override
  @Nullable
  public Icon getIconForId(@Nonnull final IconID id) {
    return MAP.get(id);
  }
  
}
