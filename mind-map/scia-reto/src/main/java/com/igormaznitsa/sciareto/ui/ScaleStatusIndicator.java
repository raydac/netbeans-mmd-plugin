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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.annotation.Nonnull;
import javax.swing.BorderFactory;
import javax.swing.JLabel;

public class ScaleStatusIndicator extends JLabel {

  public interface Scalable {

    float getScale();

    void setScale(float scale);

    void addScaleListener(ActionListener scaleListener);

    void removeScaleListener(ActionListener scaleListener);
  }

  private final Scalable observableObject;

  public ScaleStatusIndicator(@Nonnull final Scalable observableObject) {
    super();
    this.observableObject = Assertions.assertNotNull(observableObject);
    this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    this.setToolTipText("Reset scale");
    this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    this.setBackground(new Color(0xf7ffc8));
    this.setForeground(Color.BLACK);
    this.setOpaque(true);

    this.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(@Nonnull final MouseEvent e) {
        observableObject.setScale(1.0f);
      }
    });

    observableObject.addScaleListener(new ActionListener() {
      @Override
      public void actionPerformed(@Nonnull final ActionEvent e) {
        updateTextForScale();
      }
    });
    updateTextForScale();
  }

  private void updateTextForScale() {
    final float scale = this.observableObject.getScale();
    this.setText(String.format("<html><b>&nbsp;Scale: %d%%&nbsp;</b></html>", Math.round(scale * 100.0f)));

    this.repaint();
  }

}
