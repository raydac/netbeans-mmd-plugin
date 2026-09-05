/*
 * Copyright (C) 2015-2022 Igor A. Maznitsa
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package com.igormaznitsa.sciareto.ui.editors;

import static com.formdev.flatlaf.FlatClientProperties.SCROLL_PANE_SMOOTH_SCROLLING;
import static com.igormaznitsa.sciareto.ui.UiUtils.hideContainerBorder;

import javax.annotation.Nullable;
import javax.swing.JComponent;
import javax.swing.JScrollPane;

public class EditorScrollPanel extends JScrollPane {

  public EditorScrollPanel() {
    this(null);
  }

  public EditorScrollPanel(@Nullable final JComponent view) {
    super(view);
    this.setAutoscrolls(true);
    this.putClientProperty(SCROLL_PANE_SMOOTH_SCROLLING, true);
    hideContainerBorder(this);
  }
}
