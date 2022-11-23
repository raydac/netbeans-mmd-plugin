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

package com.igormaznitsa.sciareto.ui;

import com.igormaznitsa.meta.common.utils.Assertions;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.annotation.Nonnull;
import javax.swing.BorderFactory;
import javax.swing.JLabel;

public class ScaleStatusIndicator extends JLabel {

  private final Scalable observableObject;

  private final java.util.ResourceBundle bundle =
      java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18n/Bundle"); // NOI18N

  private final String textTemplate;
     
  
  public ScaleStatusIndicator(@Nonnull final Scalable observableObject, final boolean darkScheme) {
    super();
    this.observableObject = Assertions.assertNotNull(observableObject);
    this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    this.setToolTipText(bundle.getString("scaleIndicator.tooltip"));
    this.textTemplate = bundle.getString("scaleIndicator.text");
    this.setBackground(darkScheme ? Color.DARK_GRAY.brighter() : new Color(0xf7ffc8));
    this.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
    this.setForeground(darkScheme ? Color.YELLOW.darker() : Color.BLACK);
    this.setOpaque(false);

    this.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(@Nonnull final MouseEvent e) {
        observableObject.doZoomReset();;
      }
    });

    observableObject.addScaleListener(e -> updateTextForScale());
    updateTextForScale();
  }

  public void doZoomIn() {
     this.observableObject.doZoomIn();
  }

  public void doZoomOut() {
    this.observableObject.doZoomOut();
  }

  public void doZoomReset() {
    this.observableObject.doZoomReset();
  }

  @Override
  public void paintComponent(@Nonnull final Graphics g) {
    final Dimension size = this.getSize();
    g.setColor(this.getBackground());
    size.width--;
    size.height--;
    final int radius = size.height / 2;
    g.fillRoundRect(0, 0, size.width, size.height, radius, radius);
    g.setColor(this.getBackground().darker().darker());
    g.drawRoundRect(0, 0, size.width, size.height, radius, radius);
    super.paintComponent(g);
  }

  private void updateTextForScale() {
    String scalePercent = Long.toString(Math.round(this.observableObject.getScale() * 100.0f));
    if (scalePercent.length() < 3 && this.textTemplate.startsWith("<html>")) {
      scalePercent = "&nbsp;&nbsp;" + scalePercent;
    }
    this.setText(
        String.format(this.textTemplate, scalePercent));

    this.repaint();
  }

  public interface Scalable {

    float getScale();

    void doZoomIn();

    void doZoomOut();

    void doZoomReset();

    void setScale(float scale);

    void addScaleListener(ActionListener scaleListener);

    void removeScaleListener(ActionListener scaleListener);
  }

}
