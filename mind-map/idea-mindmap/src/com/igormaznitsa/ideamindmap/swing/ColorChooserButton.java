/*
 * Copyright 2015-2018 Igor Maznitsa.
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

package com.igormaznitsa.ideamindmap.swing;

import com.igormaznitsa.ideamindmap.utils.IdeaUtils;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.intellij.util.ui.UIUtil;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.ResourceBundle;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.DefaultButtonModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class ColorChooserButton extends JButton {

  public static final Color DIFF_COLORS = new Color(0, true);
  private static final long serialVersionUID = -354752410805059103L;
  private static final Logger LOGGER = LoggerFactory.getLogger(ColorChooserButton.class);
  private static final ResourceBundle BUNDLE = java.util.ResourceBundle.getBundle("i18n/Bundle");
  private final java.util.List<Color> usedColors = new CopyOnWriteArrayList<>();
  private Color value = null;
  private volatile boolean lastResultOk;

  public ColorChooserButton() {
    this(null);
  }

  public ColorChooserButton(@Nullable final DialogProvider dialogProvider) {
    super();

    this.setHorizontalAlignment(SwingConstants.LEFT);

    this.setModel(new DefaultButtonModel() {
      private static final long serialVersionUID = 3109256773218160485L;

      @Override
      protected void fireActionPerformed(ActionEvent e) {
        final Window ownerWindow = SwingUtilities.getWindowAncestor(ColorChooserButton.this);

        final com.igormaznitsa.mindmap.swing.colorpicker.ColorChooser colorChooser = new com.igormaznitsa.mindmap.swing.colorpicker.ColorChooser(usedColors, value);

        final String title = String.format(BUNDLE.getString("ColorChoosingButton.dialogTitle"), getText());
        final boolean result;

        if (dialogProvider == null) {
          result = JOptionPane.showConfirmDialog(ownerWindow, colorChooser.getPanel(), title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null) == JOptionPane.OK_OPTION;
        } else {
          result = dialogProvider.msgOkCancel(ColorChooserButton.this, title, colorChooser.getPanel());
        }

        if (result) {
          setValue(colorChooser.getColor());
          lastResultOk = true;
        } else {
          lastResultOk = false;
        }

        super.fireActionPerformed(e);
      }

    });

    setValue(Color.BLACK);
  }

  private static ImageIcon makeColorIconForColor(final Color color) {
    final int size = UIUtil.isRetina() ? 8 : 16;
    final int halfSize = size / 2;

    final int offsetX = 8;
    final Image img = UIUtil.createImage(size + offsetX, size, BufferedImage.TYPE_INT_ARGB);
    final Graphics gfx = img.getGraphics();
    try {
      if (color == null) {
        gfx.setColor(IdeaUtils.isDarkTheme() ? Color.darkGray : Color.white);
        gfx.fillRect(offsetX, 0, size, size);
        gfx.setColor(IdeaUtils.isDarkTheme() ? Color.yellow : Color.black);
        gfx.drawRect(offsetX, 0, size - 1, size - 1);
        gfx.drawLine(offsetX, 0, offsetX + size - 1, size - 1);
      } else if (color == DIFF_COLORS) {
        gfx.setColor(Color.red);
        gfx.fillRect(offsetX, 0, halfSize, halfSize);
        gfx.setColor(Color.green);
        gfx.fillRect(offsetX + halfSize, 0, halfSize, halfSize);
        gfx.setColor(Color.yellow);
        gfx.fillRect(offsetX, halfSize, halfSize, halfSize);
        gfx.setColor(Color.blue);
        gfx.fillRect(offsetX + halfSize, halfSize, halfSize, halfSize);
        gfx.setColor(Color.black);
        gfx.drawRect(offsetX, 0, size - 1, size - 1);
      } else {
        gfx.setColor(color);
        gfx.fillRect(offsetX, 0, size, size);
        gfx.setColor(Color.black);
        gfx.drawRect(offsetX, 0, size - 1, size - 1);
      }
    } finally {
      gfx.dispose();
    }
    return new ImageIcon(img);
  }

  public void setUsedColors(@Nonnull final java.util.List<Color> colors) {
    this.usedColors.clear();
    this.usedColors.addAll(colors);
  }

  public boolean isLastOkPressed() {
    return this.lastResultOk;
  }

  public Color getValue() {
    return this.value;
  }

  public void setValue(final Color color) {
    this.value = color;
    this.setIcon(makeColorIconForColor(this.value));
  }
}
