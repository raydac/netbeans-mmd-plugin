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

import static com.intellij.openapi.util.IconLoader.getIcon;

import javax.swing.Icon;

public class AllIcons {
  public static class Logo {
    public static final Icon MINDMAP = getIcon("/icons/logo/logo.png", AllIcons.class);
  }

  public static class FindText {
    public static final Icon CASE = getIcon("/icons/find/case16.png", AllIcons.class);
    public static final Icon FILE = getIcon("/icons/find/disk16.png", AllIcons.class);
    public static final Icon NOTE = getIcon("/icons/find/note16.png", AllIcons.class);
    public static final Icon TEXT = getIcon("/icons/find/text16.png", AllIcons.class);
    public static final Icon URL = getIcon("/icons/find/url16.png", AllIcons.class);
    public static final Icon NEXT = getIcon("/icons/find/resultset_next.png", AllIcons.class);
    public static final Icon PREV = getIcon("/icons/find/resultset_previous.png", AllIcons.class);
    public static final Icon CLOSE = getIcon("/icons/find/nimbusCloseFrame.png", AllIcons.class);
  }

  public static class ImportExport {
    public static final Icon TXT2MM = getIcon("/icons/txt2mm.png", AllIcons.class);
    public static final Icon XMIND2MM = getIcon("/icons/xmind2mm.png", AllIcons.class);
    public static final Icon ORGMODE = getIcon("/icons/orgmode.png", AllIcons.class);
    public static final Icon COGGLE = getIcon("/icons/coggle.png", AllIcons.class);
    public static final Icon NOVAMIND = getIcon("/icons/novamind.png", AllIcons.class);
  }

  public static class File {
    public static final Icon MINDMAP = getIcon("/icons/mmdfile.png", AllIcons.class);
    public static final Icon FOLDER = getIcon("/icons/folder.png", AllIcons.class);
  }

  public static class Tree {
    public static final Icon DOCUMENT = getIcon("/icons/document.png", AllIcons.class);
    public static final Icon BLUEBALL = getIcon("/icons/blueball.png", AllIcons.class);
    public static final Icon GOLDBALL = getIcon("/icons/goldball.png", AllIcons.class);
  }

  public static class Buttons {
    public static final Icon CROSS = getIcon("/icons/cross.png", AllIcons.class);
    public static final Icon CANCEL = getIcon("/icons/cancel.png", AllIcons.class);
    public static final Icon TICK = getIcon("/icons/tick.png", AllIcons.class);
    public static final Icon QUESTION = getIcon("/icons/question.png", AllIcons.class);
    public static final Icon EXPANDALL = getIcon("/icons/toggle_expand.png", AllIcons.class);
    public static final Icon COLLAPSEALL = getIcon("/icons/toggle.png", AllIcons.class);
    public static final Icon SELECT = getIcon("/icons/select.png", AllIcons.class);
    public static final Icon FILE_LINK_BIG = getIcon("/icons/file_link.png", AllIcons.class);
    public static final Icon URL_LINK_BIG = getIcon("/icons/url_link.png", AllIcons.class);
    public static final Icon FILE_MANAGER = getIcon("/icons/file_manager.png", AllIcons.class);
    public static final Icon COINS = getIcon("/icons/coins_in_hand.png", AllIcons.class);
    public static final Icon IMPORT = getIcon("/icons/disk.png", AllIcons.class);
    public static final Icon EXPORT = getIcon("/icons/file_save.png", AllIcons.class);
    public static final Icon COPY = getIcon("/icons/page_copy.png", AllIcons.class);
    public static final Icon PASTE = getIcon("/icons/paste_plain.png", AllIcons.class);
    public static final Icon CLEARALL = getIcon("/icons/cross.png", AllIcons.class);
    public static final Icon PROTECT_OFF = getIcon("/icons/set_password.png", AllIcons.class);
    public static final Icon PROTECT_ON = getIcon("/icons/set_passwordon.png", AllIcons.class);
    public static final Icon BROWSE = getIcon("/icons/world_link.png", AllIcons.class);
    public static final Icon REDO = getIcon("/icons/redo.png", AllIcons.class);
    public static final Icon UNDO = getIcon("/icons/undo.png", AllIcons.class);
  }

  public static class PopUp {

    public static final Icon EDITTEXT = getIcon("/icons/text.png", AllIcons.class);
    public static final Icon ADD = getIcon("/icons/add.png", AllIcons.class);
    public static final Icon DELETE = getIcon("/icons/delete.png", AllIcons.class);
    public static final Icon CLONE = getIcon("/icons/draw_clone.png", AllIcons.class);
    public static final Icon NOTE = getIcon("/icons/note.png", AllIcons.class);
    public static final Icon URL = getIcon("/icons/url.png", AllIcons.class);
    public static final Icon TOPIC = getIcon("/icons/brick.png", AllIcons.class);
    public static final Icon FILE = getIcon("/icons/disk.png", AllIcons.class);
    public static final Icon EXPANDALL = getIcon("/icons/toggle_expand.png", AllIcons.class);
    public static final Icon COLLAPSEALL = getIcon("/icons/toggle.png", AllIcons.class);
    public static final Icon SHOWJUMPS = getIcon("/icons/showjumps.png", AllIcons.class);
    public static final Icon OPTIONS = getIcon("/icons/settings.png", AllIcons.class);
    public static final Icon INFO = getIcon("/icons/info.png", AllIcons.class);
    public static final Icon COLORS = getIcon("/icons/color_swatches.png", AllIcons.class);
    public static final Icon EXPORT = getIcon("/icons/document_export.png", AllIcons.class);
    public static final Icon IMPORT = getIcon("/icons/document_import.png", AllIcons.class);
    public static final Icon PAGE = getIcon("/icons/page.png", AllIcons.class);
    public static final Icon PRINTER = getIcon("/icons/printer.png", AllIcons.class);
    public static final Icon EMOTICONS = getIcon("/icons/emoticons.png", AllIcons.class);
    public static final Icon IMAGES = getIcon("/icons/pictures.png", AllIcons.class);
    public static final Icon TEXT_ALIGN = getIcon("/icons/text_align.png", AllIcons.class);
    public static final Icon TEXT_ALIGN_LEFT = getIcon("/icons/text_align_left.png", AllIcons.class);
    public static final Icon TEXT_ALIGN_CENTER = getIcon("/icons/text_align_center.png", AllIcons.class);
    public static final Icon TEXT_ALIGN_RIGHT = getIcon("/icons/text_align_right.png", AllIcons.class);
  }

}
