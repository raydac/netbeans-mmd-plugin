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

package com.igormaznitsa.mindmap.ide.commons;

import java.awt.Color;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;

public class SwingUtils {

  public static JPopupMenu addTextActions(final JPopupMenu menu) {
    final Action cut = new DefaultEditorKit.CutAction();
    cut.putValue(Action.NAME, "Cut");
    menu.add(cut);

    final Action copy = new DefaultEditorKit.CopyAction();
    copy.putValue(Action.NAME, "Copy");
    menu.add(copy);

    final Action paste = new DefaultEditorKit.PasteAction();
    paste.putValue(Action.NAME, "Paste");
    menu.add(paste);

    menu.add(new SelectAllTextAction());

    return menu;
  }

  public static class SelectAllTextAction extends TextAction {

    private static final long serialVersionUID = 3800454333693618054L;

    SelectAllTextAction() {
      super("Select All");
    }

    public static int calculateBrightness(final Color color) {
      return (int) Math.sqrt(
          color.getRed() * color.getRed() * .241d
              + color.getGreen() * color.getGreen() * .691d
              + color.getBlue() * color.getBlue() * .068d);
    }


    public static boolean figureOutThatDarkTheme() {
      final Color color = UIManager.getColor("Panel.background"); //NOI18N
      if (color == null) {
        return false;
      } else {
        return calculateBrightness(color) < 150;
      }
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
      final JTextComponent component = getFocusedComponent();
      if (component != null) {
        component.selectAll();
        component.requestFocusInWindow();
      }
    }
  }
}
