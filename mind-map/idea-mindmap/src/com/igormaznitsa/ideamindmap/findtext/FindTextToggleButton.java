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

package com.igormaznitsa.ideamindmap.findtext;

import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.components.JBLabel;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.Icon;

public class FindTextToggleButton extends JBLabel {
  private final Icon normalIcon;
  private final Icon selectedIcon;
  private final ActionListener listener;
  private boolean selected;

  public FindTextToggleButton(final Icon icon, final String tooltipText, final ActionListener actionListener) {
    super();
    this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    this.setToolTipText(tooltipText);
    this.selectedIcon = icon;
    this.normalIcon = IconLoader.getTransparentIcon(icon);
    this.setSelected(true);
    this.listener = actionListener;
    this.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

    this.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (!e.isPopupTrigger()) {
          setSelected(!selected);
        }
      }
    });
  }

  public boolean isSelected() {
    return this.selected;
  }

  public void setSelected(final boolean flag) {
    this.selected = flag;
    this.setIcon(this.selected ? this.selectedIcon : normalIcon);
    if (this.listener != null) {
      this.listener.actionPerformed(new ActionEvent(this, 0, ""));
    }
    this.repaint();
  }
}
