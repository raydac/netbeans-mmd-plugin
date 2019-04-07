/*
 * Copyright 2019 Igor Maznitsa.
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
package com.igormaznitsa.mindmap.swing.colorpicker;

import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.mindmap.swing.panel.Texts;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public final class ColorChooser extends JPanel {

  private final ColorPickerPanel colorPicker;
  private final ColorPickerPanel presentedColors;
  private final JLabel sampleDark;
  private final JLabel sampleLight;
  private final boolean selectBackgroundColor;
  
  public ColorChooser(
          @Nullable @MustNotContainNull final List<Color> mapColors, 
          @Nullable final Color selectedColor,
          final boolean selectBackgroundColor
  ) {
    super(new GridBagLayout());

    final String SAMPLE_TEXT = "TEXT";
    
    this.selectBackgroundColor = selectBackgroundColor;
    
    GridBagConstraints data = new GridBagConstraints();
    data.anchor = GridBagConstraints.CENTER;
    data.gridx = 0;
    data.gridy = 0;
    data.fill = GridBagConstraints.BOTH;
    data.insets.set(4, 4, 4, 4);

    this.sampleDark = new JLabel(SAMPLE_TEXT);
    this.sampleDark.setOpaque(true);
    this.sampleDark.setForeground(Color.BLACK);
    this.sampleDark.setHorizontalAlignment(JLabel.CENTER);
    this.sampleDark.setBackground(Color.WHITE);
    
    this.sampleLight = new JLabel(SAMPLE_TEXT);
    this.sampleLight.setOpaque(true);
    this.sampleLight.setForeground(Color.WHITE);
    this.sampleLight.setHorizontalAlignment(SwingConstants.CENTER);
    this.sampleLight.setBackground(Color.BLACK);
    
    this.colorPicker = new ColorPickerPanel(10, 10, 4, 4, null);
    this.presentedColors = new ColorPickerPanel(2, 10, 4, 4, mapColors == null ? Collections.<Color>emptyList() : mapColors);

    this.add(this.colorPicker, data);

    data.gridy = 1;

    final JPanel samplePanel = new JPanel(new GridLayout(2, 1));
    samplePanel.setBorder(BorderFactory.createTitledBorder(Texts.getString("ColorChooser.Text.Example")));
    samplePanel.add(this.sampleDark);
    samplePanel.add(this.sampleLight);

    data.insets.set(4, 32, 4, 32);
    
    this.add(samplePanel, data);

    data.insets.set(4, 4, 4, 4);
    
    data.gridy = 2;
    this.add(this.presentedColors, data);

    this.colorPicker.addColorListener(new ColorPickerPanel.ColorListener() {
      @Override
      public void onColorSelected(@Nonnull final ColorPickerPanel source, @Nonnull final Color color) {
        presentedColors.resetSelected();
        updateSample(color);
      }
    });

    this.presentedColors.addColorListener(new ColorPickerPanel.ColorListener() {
      @Override
      public void onColorSelected(@Nonnull final ColorPickerPanel source, @Nonnull final Color color) {
        colorPicker.resetSelected();
        updateSample(color);
      }
    });

    if (selectedColor != null) {
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          presentedColors.setColor(selectedColor);
          updateSample(selectedColor);
        }
      });
    }

    this.doLayout();
    this.repaint();
  }

  public boolean isSelectBackgroundColor() {
    return this.selectBackgroundColor;
  }
  
  private void updateSample(@Nonnull final Color color) {
    if (this.selectBackgroundColor) {
    this.sampleDark.setBackground(color);
    this.sampleLight.setBackground(color);
    } else {
      this.sampleDark.setForeground(color);
      this.sampleLight.setForeground(color);
    }
  }
  
  @Nullable
  public Color getColor() {
    final Color colorMain = this.colorPicker.getColor();
    final Color colorSecond = this.presentedColors.getColor();
    return colorMain == null ? colorSecond : colorMain;
  }

  public static void main(@Nonnull @MustNotContainNull String... args) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final JPanel panel = new JPanel(new BorderLayout());
        panel.add(new ColorChooser(Arrays.asList(Color.WHITE, Color.BLACK, Color.RED, Color.ORANGE, Color.PINK), Color.ORANGE, false), BorderLayout.CENTER);

        frame.setContentPane(panel);
        frame.pack();
        frame.setVisible(true);
      }
    });
  }

}
