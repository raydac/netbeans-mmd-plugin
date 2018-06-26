/*
 * Copyright (C) 2018 Igor Maznitsa.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

package com.igormaznitsa.ideamindmap.findtext;

import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.components.JBLabel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class FindTextToggleButton extends JBLabel {
  private boolean selected;
  private final Icon normalIcon;
  private final Icon selectedIcon;
  private final ActionListener listener;

  public FindTextToggleButton(final Icon icon, final String tooltipText, final ActionListener actionListener) {
    super();
    this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    this.setToolTipText(tooltipText);
    this.selectedIcon = icon;
    this.normalIcon = IconLoader.getDisabledIcon(icon);
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
