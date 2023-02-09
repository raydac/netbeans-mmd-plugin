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

package com.igormaznitsa.ideamindmap.utils;

import static com.intellij.openapi.util.IconLoader.findIcon;

import com.intellij.openapi.util.IconLoader;
import javax.swing.Icon;

public class AllIcons {
  public static class Logo {
    public static final Icon MINDMAP = findIcon("/icons/logo/logo.png");
  }

  public static class FindText {
    public static final Icon CASE = findIcon("/icons/find/case16.png");
    public static final Icon FILE = findIcon("/icons/find/disk16.png");
    public static final Icon NOTE = findIcon("/icons/find/note16.png");
    public static final Icon TEXT = findIcon("/icons/find/text16.png");
    public static final Icon URL = findIcon("/icons/find/url16.png");
    public static final Icon NEXT = findIcon("/icons/find/resultset_next.png");
    public static final Icon PREV = findIcon("/icons/find/resultset_previous.png");
    public static final Icon CLOSE = findIcon("/icons/find/nimbusCloseFrame.png");
  }

  public static class ImportExport {
    public static final Icon TXT2MM = findIcon("/icons/txt2mm.png");
    public static final Icon XMIND2MM = findIcon("/icons/xmind2mm.png");
    public static final Icon ORGMODE = findIcon("/icons/orgmode.png");
    public static final Icon COGGLE = findIcon("/icons/coggle.png");
    public static final Icon NOVAMIND = findIcon("/icons/novamind.png");
  }

  public static class File {
    public static final Icon MINDMAP = findIcon("/icons/mmdfile.png");
    public static final Icon FOLDER = findIcon("/icons/folder.png");
  }

  public static class Tree {
    public static final Icon DOCUMENT = findIcon("/icons/document.png");
    public static final Icon BLUEBALL = findIcon("/icons/blueball.png");
    public static final Icon GOLDBALL = findIcon("/icons/goldball.png");
  }

  public static class Buttons {
    public static final Icon CROSS = findIcon("/icons/cross.png");
    public static final Icon CANCEL = findIcon("/icons/cancel.png");
    public static final Icon TICK = findIcon("/icons/tick.png");
    public static final Icon QUESTION = findIcon("/icons/question.png");
    public static final Icon EXPANDALL = findIcon("/icons/toggle_expand.png");
    public static final Icon COLLAPSEALL = findIcon("/icons/toggle.png");
    public static final Icon SELECT = findIcon("/icons/select.png");
    public static final Icon FILE_LINK_BIG = findIcon("/icons/file_link.png");
    public static final Icon URL_LINK_BIG = findIcon("/icons/url_link.png");
    public static final Icon FILE_MANAGER = findIcon("/icons/file_manager.png");
    public static final Icon COINS = findIcon("/icons/coins_in_hand.png");
    public static final Icon IMPORT = findIcon("/icons/disk.png");
    public static final Icon EXPORT = findIcon("/icons/file_save.png");
    public static final Icon COPY = findIcon("/icons/page_copy.png");
    public static final Icon PASTE = findIcon("/icons/paste_plain.png");
    public static final Icon CLEARALL = findIcon("/icons/cross.png");
    public static final Icon PROTECT_OFF = findIcon("/icons/set_password.png");
    public static final Icon PROTECT_ON = findIcon("/icons/set_passwordon.png");
    public static final Icon BROWSE = findIcon("/icons/world_link.png");
    public static final Icon REDO = findIcon("/icons/redo.png");
    public static final Icon UNDO = findIcon("/icons/undo.png");
  }

  public static class PopUp {

    public static final Icon EDITTEXT = findIcon("/icons/text.png");
    public static final Icon ADD = findIcon("/icons/add.png");
    public static final Icon DELETE = findIcon("/icons/delete.png");
    public static final Icon CLONE = findIcon("/icons/draw_clone.png");
    public static final Icon NOTE = findIcon("/icons/note.png");
    public static final Icon URL = findIcon("/icons/url.png");
    public static final Icon TOPIC = findIcon("/icons/brick.png");
    public static final Icon FILE = findIcon("/icons/disk.png");
    public static final Icon EXPANDALL = findIcon("/icons/toggle_expand.png");
    public static final Icon COLLAPSEALL = findIcon("/icons/toggle.png");
    public static final Icon SHOWJUMPS = findIcon("/icons/showjumps.png");
    public static final Icon OPTIONS = findIcon("/icons/settings.png");
    public static final Icon INFO = findIcon("/icons/info.png");
    public static final Icon COLORS = findIcon("/icons/color_swatches.png");
    public static final Icon EXPORT = findIcon("/icons/document_export.png");
    public static final Icon IMPORT = findIcon("/icons/document_import.png");
    public static final Icon PAGE = findIcon("/icons/page.png");
    public static final Icon PRINTER = findIcon("/icons/printer.png");
    public static final Icon EMOTICONS = findIcon("/icons/emoticons.png");
    public static final Icon IMAGES = findIcon("/icons/pictures.png");
    public static final Icon TEXT_ALIGN = findIcon("/icons/text_align.png");
    public static final Icon TEXT_ALIGN_LEFT = findIcon("/icons/text_align_left.png");
    public static final Icon TEXT_ALIGN_CENTER = findIcon("/icons/text_align_center.png");
    public static final Icon TEXT_ALIGN_RIGHT = findIcon("/icons/text_align_right.png");
  }

}
