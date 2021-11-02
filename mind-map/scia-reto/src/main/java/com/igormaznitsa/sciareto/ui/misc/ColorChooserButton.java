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
package com.igormaznitsa.sciareto.ui.misc;

import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.swing.colorpicker.ColorChooser;
import com.igormaznitsa.sciareto.SciaRetoStarter;
import com.igormaznitsa.sciareto.ui.DialogProviderManager;
import com.igormaznitsa.sciareto.ui.UiUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class ColorChooserButton extends JButton {

  private static final long serialVersionUID = -354752410805059103L;

  private static final Logger LOGGER = LoggerFactory.getLogger(ColorChooserButton.class);

  private Color value = null;

  private volatile boolean lastResultOk;

  public static final Color DIFF_COLORS = new Color(0, true);

  private final List<Color> usedColors = new CopyOnWriteArrayList<>();
  
  public ColorChooserButton() {
    super();

    final ColorChooserButton theButtonInstance = this;

    this.setModel(new DefaultButtonModel() {
      private static final long serialVersionUID = 3109256773218160485L;

      @Override
      protected void fireActionPerformed(ActionEvent e) {
        final Window ownerWindow = SwingUtilities.getWindowAncestor(theButtonInstance);

        final ColorChooser colorChooser = new ColorChooser(usedColors, value);

        if (DialogProviderManager.getInstance().getDialogProvider()
                .msgOkCancel(ownerWindow == null ? SciaRetoStarter.getApplicationFrame() : ownerWindow,
                        String.format(UiUtils.BUNDLE.getString("ColorChoosingButton.dialogTitle"),
                                getText()),
                        colorChooser.getPanel())) {
          final Color selectedColor = colorChooser.getColor();
          if (selectedColor != null) {
            setValue(selectedColor);
            lastResultOk = true;
          } else {
            lastResultOk = false;
          }

          super.fireActionPerformed(e);
        }
      }
    });

    setValue(Color.BLACK);
  }

  public void setUsedColors(@Nonnull @MustNotContainNull final List<Color> colors) {
    this.usedColors.clear();
    this.usedColors.addAll(colors);
  }
  
  public boolean isLastOkPressed() {
    return this.lastResultOk;
  }

  @Nonnull
  private static ImageIcon makeColorIconForColor(@Nullable final Color color) {
    final Image img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
    final Graphics gfx = img.getGraphics();
    try {
      if (color == null) {
        gfx.setColor(UiUtils.DARK_THEME ? Color.darkGray : Color.white);
        gfx.fillRect(0, 0, 16, 16);
        gfx.setColor(UiUtils.DARK_THEME ? Color.yellow : Color.black);
        gfx.drawRect(0, 0, 15, 15);
        gfx.drawLine(0, 0, 15, 15);
      } else if (color == DIFF_COLORS) {
        gfx.setColor(Color.red);
        gfx.fillRect(0, 0, 8, 8);
        gfx.setColor(Color.green);
        gfx.fillRect(8, 0, 8, 8);
        gfx.setColor(Color.blue);
        gfx.fillRect(0, 8, 8, 8);
        gfx.setColor(Color.yellow);
        gfx.fillRect(8, 8, 8, 8);
      } else {
        gfx.setColor(color);
        gfx.fillRect(0, 0, 16, 16);
        gfx.setColor(Color.black);
        gfx.drawRect(0, 0, 15, 15);
      }
    } finally {
      gfx.dispose();
    }
    return new ImageIcon(img);
  }

  public void setValue(@Nullable final Color color) {
    this.value = color;
    this.setIcon(makeColorIconForColor(this.value));
  }

  @Nullable
  public Color getValue() {
    return this.value;
  }
}
