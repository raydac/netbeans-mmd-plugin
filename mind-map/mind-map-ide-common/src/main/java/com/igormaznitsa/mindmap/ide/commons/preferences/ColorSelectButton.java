/*
 * Copyright (C) 2015-2023 Igor A. Maznitsa
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

package com.igormaznitsa.mindmap.ide.commons.preferences;

import static com.igormaznitsa.mindmap.ide.commons.SwingUtils.SelectAllTextAction.figureOutThatDarkTheme;
import static java.util.Objects.requireNonNull;

import com.igormaznitsa.mindmap.swing.colorpicker.ColorChooser;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactory;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import javax.swing.DefaultButtonModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;

public final class ColorSelectButton {

  public static final Color DIFF_COLORS = new Color(0, true);
  private final List<Color> usedColors = new CopyOnWriteArrayList<>();
  private final JButton delegateButton;
  private Color value = null;
  private boolean lastResultOk;

  private final Function<Color,Color> valueProcessor;

  public ColorSelectButton(final Component dialogComponent,
                           final UIComponentFactory componentFactory,
                           final DialogProvider dialogProvider) {
    this(dialogComponent, componentFactory, dialogProvider, x -> x);
  }

  public ColorSelectButton(final Component dialogComponent,
                           final UIComponentFactory componentFactory,
                           final DialogProvider dialogProvider,
                           final Function<Color,Color> valueProcessor) {
    this(dialogComponent, componentFactory, dialogProvider, valueProcessor, x -> x);
  }

  public ColorSelectButton(final Component dialogComponent,
                           final UIComponentFactory componentFactory,
                           final DialogProvider dialogProvider,
                           final Function<Color,Color> valueProcessor,
                           final Function<JButton, JButton> buttonProcessor) {

    this.valueProcessor = requireNonNull(valueProcessor);

    final JButton button = componentFactory.makeButton();

    button.setHorizontalAlignment(JButton.LEFT);
    button.setVerticalAlignment(JButton.CENTER);

    button.setModel(new DefaultButtonModel() {
      @Override
      protected void fireActionPerformed(ActionEvent e) {
        final ColorChooser colorChooser = new ColorChooser(usedColors, value);

        if (dialogProvider.msgOkCancel(dialogComponent,
            String.format(
                MmcI18n.getInstance().findBundle().getString("ColorSelectButton.dialogTitle"),
                ColorSelectButton.this.delegateButton.getText()),
            colorChooser.getPanel())) {
          final Color selectedColor = colorChooser.getColor();
          if (selectedColor != null) {
            setValue(selectedColor);
            ColorSelectButton.this.lastResultOk = true;
          } else {
            ColorSelectButton.this.lastResultOk = false;
          }
          super.fireActionPerformed(e);
        }
      }
    });

    this.delegateButton = buttonProcessor.apply(button);

    setValue(Color.BLACK);
  }

  public ColorSelectButton setText(final String text) {
    this.delegateButton.setText(text);
    return this;
  }

  public ColorSelectButton setToolTipText(final String text) {
    this.delegateButton.setToolTipText(text);
    return this;
  }

  public JButton asButton(){
    return this.delegateButton;
  }

  private static ImageIcon makeColorIconForColor(final Color color) {
    final Image img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
    final Graphics gfx = img.getGraphics();
    try {
      final boolean darkTheme = figureOutThatDarkTheme();
      if (color == null) {
        gfx.setColor(darkTheme ? Color.darkGray : Color.white);
        gfx.fillRect(0, 0, 16, 16);
        gfx.setColor(darkTheme ? Color.yellow : Color.black);
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

  public void setUsedColors(final List<Color> colors) {
    this.usedColors.clear();
    this.usedColors.addAll(colors);
  }

  public boolean isLastOkPressed() {
    return this.lastResultOk;
  }

  public Color getValue() {
    return this.valueProcessor.apply(this.value);
  }

  public void setValue(final Color color) {
    this.value = color == null ? null : new Color(color.getRGB() & 0xFFFFFF, false);
    this.delegateButton.setIcon(makeColorIconForColor(this.value));
  }
}
