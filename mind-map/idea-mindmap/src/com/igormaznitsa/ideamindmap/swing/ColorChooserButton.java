/*
 * Copyright 2015 Igor Maznitsa.
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
import com.intellij.ui.ColorChooser;
import com.intellij.util.ui.UIUtil;

import javax.annotation.Nullable;
import javax.swing.DefaultButtonModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.util.ResourceBundle;

public class ColorChooserButton extends JButton {

  private static final long serialVersionUID = -354752410805059103L;

  private static final Logger LOGGER = LoggerFactory.getLogger(ColorChooserButton.class);
  private Color value = null;
  private volatile boolean lastResultOk;
  public static final Color DIFF_COLORS = new Color(0, true);
  private static final ResourceBundle BUNDLE = java.util.ResourceBundle.getBundle("/i18n/Bundle");

  public ColorChooserButton() {
    this(null);
  }

  public ColorChooserButton(@Nullable final DialogProvider dialogProvider) {
    super();

    final ColorChooserButton theInstance = this;

    this.setModel(new DefaultButtonModel() {
      private static final long serialVersionUID = 3109256773218160485L;

      @Override
      protected void fireActionPerformed(ActionEvent e) {
        final PropertyEditor editor = PropertyEditorManager.findEditor(Color.class);
        if (editor == null) {
          LOGGER.error("Can't find registered color editor");
          if (dialogProvider != null)
            dialogProvider.msgError("Can't find color editor! unexpected state! Contact developer!");
          return;
        }

        editor.setValue(value);

        final Color selectedColor = ColorChooser.chooseColor(theInstance, String.format(BUNDLE.getString("ColorChoosingButton.dialogTitle"), getText()), getValue());
        if (selectedColor != null) {
          setValue(selectedColor);
          lastResultOk = true;
        }
        else {
          lastResultOk = false;
        }

        super.fireActionPerformed(e);
      }

    });

    setValue(Color.BLACK);
  }

  public boolean isLastOkPressed() {
    return this.lastResultOk;
  }

  private static ImageIcon makeColorIconForColor(final Color color) {
    final int size = UIUtil.isRetina() ? 8 : 16;
    final int halfSize = size / 2;

    final Image img = UIUtil.createImage(size, size, BufferedImage.TYPE_INT_RGB);
    final Graphics gfx = img.getGraphics();
    try {
      if (color == null) {
        gfx.setColor(IdeaUtils.isDarkTheme() ? Color.darkGray : Color.white);
        gfx.fillRect(0, 0, size, size);
        gfx.setColor(IdeaUtils.isDarkTheme() ? Color.yellow : Color.black);
        gfx.drawRect(0, 0, size - 1, size - 1);
        gfx.drawLine(0, 0, size - 1, size - 1);
      }
      else if (color == DIFF_COLORS) {
        gfx.setColor(Color.red);
        gfx.fillRect(0, 0, halfSize, halfSize);
        gfx.setColor(Color.green);
        gfx.fillRect(halfSize, 0, halfSize, halfSize);
        gfx.setColor(Color.blue);
        gfx.fillRect(0, halfSize, halfSize, halfSize);
        gfx.setColor(Color.yellow);
        gfx.fillRect(halfSize, halfSize, halfSize, halfSize);
      }
      else {
        gfx.setColor(color);
        gfx.fillRect(0, 0, size, size);
        gfx.setColor(Color.black);
        gfx.drawRect(0, 0, size - 1, size - 1);
      }
    }
    finally {
      gfx.dispose();
    }
    return new ImageIcon(img);
  }

  public void setValue(final Color color) {
    this.value = color;
    this.setIcon(makeColorIconForColor(this.value));
  }

  public Color getValue() {
    return this.value;
  }
}
