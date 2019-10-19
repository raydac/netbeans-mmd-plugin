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
import com.igormaznitsa.mindmap.swing.services.UIComponentFactory;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactoryProvider;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public final class ColorChooser {

  private static final List<Color> PALETTE = makePalette();
  private static final int PALETTE_ROWS = 10;
  private final ColorPickerPanel colorPicker;
  private final ColorPickerPanel presentedColors;
  private final JLabel sampleDarkFill;
  private final JLabel sampleLightFill;
  private final JLabel sampleDarkText;
  private final JLabel sampleLightText;
  private final JPanel panel;

  public ColorChooser(
      @Nullable @MustNotContainNull final List<Color> mapColors,
      @Nullable final Color selectedColor
  ) {
    final UIComponentFactory componentFactory = UIComponentFactoryProvider.findInstance();
    this.panel = componentFactory.makePanel();
    this.panel.setLayout(new GridBagLayout());

    final String SAMPLE_TEXT = "TEXT";

    GridBagConstraints data = new GridBagConstraints();
    data.anchor = GridBagConstraints.CENTER;
    data.gridx = 0;
    data.gridy = 0;
    data.fill = GridBagConstraints.BOTH;
    data.insets.set(4, 4, 4, 4);

    this.sampleDarkFill = componentFactory.makeLabel();
    final Font font = this.sampleDarkFill.getFont().deriveFont(Font.BOLD);
    this.sampleDarkFill.setFont(font);

    this.sampleDarkFill.setText(SAMPLE_TEXT);
    this.sampleDarkFill.setOpaque(true);
    this.sampleDarkFill.setForeground(Color.BLACK);
    this.sampleDarkFill.setHorizontalAlignment(JLabel.CENTER);
    this.sampleDarkFill.setBackground(Color.WHITE);

    this.sampleDarkText = componentFactory.makeLabel();
    this.sampleDarkText.setText(SAMPLE_TEXT);
    this.sampleDarkText.setOpaque(true);
    this.sampleDarkText.setForeground(Color.BLACK);
    this.sampleDarkText.setHorizontalAlignment(JLabel.CENTER);
    this.sampleDarkText.setBackground(Color.WHITE);
    this.sampleDarkText.setFont(font);

    this.sampleLightFill = componentFactory.makeLabel();
    this.sampleLightFill.setText(SAMPLE_TEXT);
    this.sampleLightFill.setOpaque(true);
    this.sampleLightFill.setForeground(Color.WHITE);
    this.sampleLightFill.setHorizontalAlignment(SwingConstants.CENTER);
    this.sampleLightFill.setBackground(Color.BLACK);
    this.sampleLightFill.setFont(font);

    this.sampleLightText = componentFactory.makeLabel();
    this.sampleLightText.setText(SAMPLE_TEXT);
    this.sampleLightText.setOpaque(true);
    this.sampleLightText.setForeground(Color.WHITE);
    this.sampleLightText.setHorizontalAlignment(SwingConstants.CENTER);
    this.sampleLightText.setBackground(Color.BLACK);
    this.sampleLightText.setFont(font);

    this.colorPicker = new ColorPickerPanel(componentFactory.makePanel(), PALETTE_ROWS, 12, 4, 4, PALETTE);
    this.presentedColors = new ColorPickerPanel(componentFactory.makePanel(), 2, 12, 4, 4, mapColors == null ? Collections.<Color>emptyList() : mapColors);

    this.panel.add(this.colorPicker.getPanel(), data);

    data.gridy = 1;

    final JPanel samplePanel = componentFactory.makePanel();

    samplePanel.setLayout(new GridLayout(2, 2));
    samplePanel.setBorder(BorderFactory.createTitledBorder(Texts.getString("ColorChooser.Text.Example")));

    samplePanel.add(this.sampleDarkFill);
    samplePanel.add(this.sampleLightText);
    samplePanel.add(this.sampleDarkText);
    samplePanel.add(this.sampleLightFill);

    data.insets.set(4, 32, 4, 32);

    this.panel.add(samplePanel, data);

    data.insets.set(4, 4, 4, 4);

    data.gridy = 2;
    this.panel.add(this.presentedColors.getPanel(), data);

    this.colorPicker.addColorListener(new ColorPickerPanel.ColorListener() {
      @Override
      public void onColorSelected(@Nonnull final ColorPickerPanel source, @Nonnull final Color color) {
        presentedColors.resetSelected();
        updateSamples(color);
      }
    });

    this.presentedColors.addColorListener(new ColorPickerPanel.ColorListener() {
      @Override
      public void onColorSelected(@Nonnull final ColorPickerPanel source, @Nonnull final Color color) {
        colorPicker.resetSelected();
        updateSamples(color);
      }
    });

    if (selectedColor != null) {
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          presentedColors.setColor(selectedColor);
          updateSamples(selectedColor);
        }
      });
    }

    this.panel.doLayout();
  }

  @Nonnull
  @MustNotContainNull
  private static List<Color> makePalette() {
    final List<Color> result = new ArrayList<>();

    final int STEPS = 12;

    result.add(Color.BLACK);
    result.add(Color.BLUE);
    result.add(Color.RED);
    result.add(Color.MAGENTA);
    result.add(Color.GREEN);
    result.add(Color.CYAN);
    result.add(Color.YELLOW);
    result.add(Color.ORANGE);
    result.add(Color.DARK_GRAY);
    result.add(Color.GRAY);
    result.add(Color.LIGHT_GRAY);
    result.add(Color.WHITE);

    result.addAll(makeSteps(Color.RED, Color.ORANGE, STEPS));
    result.addAll(makeSteps(Color.ORANGE, Color.YELLOW, STEPS));
    result.addAll(makeSteps(Color.YELLOW, Color.GREEN, STEPS));
    result.addAll(makeSteps(Color.GREEN, Color.CYAN, STEPS));
    result.addAll(makeSteps(Color.CYAN, Color.BLUE, STEPS));
    result.addAll(makeSteps(Color.BLUE, Color.MAGENTA, STEPS));
    result.addAll(makeSteps(Color.MAGENTA, Color.PINK, STEPS));
    result.addAll(makeSteps(Color.PINK, Color.BLACK, STEPS));
    result.addAll(makeSteps(Color.BLACK, Color.WHITE, STEPS));

    return Collections.unmodifiableList(result);
  }

  @Nonnull
  @MustNotContainNull
  private static List<Color> makeSteps(@Nonnull final Color start, @Nonnull final Color end, final int steps) {
    float sr = start.getRed();
    float sg = start.getGreen();
    float sb = start.getBlue();

    final int GRADATIONS = steps + 2;

    float dr = (end.getRed() - sr) / GRADATIONS;
    float dg = (end.getGreen() - sg) / GRADATIONS;
    float db = (end.getBlue() - sb) / GRADATIONS;

    final List<Color> result = new ArrayList<>();

    for (int i = 0; i < GRADATIONS; i++) {
      if (i > 0 && i < (GRADATIONS - 1)) {
        result.add(new Color(Math.round(sr), Math.round(sg), Math.round(sb)));
      }
      sr += dr;
      sg += dg;
      sb += db;
    }

    return result;
  }

  @Nonnull
  public JPanel getPanel() {
    return this.panel;
  }

  private void updateSamples(@Nonnull final Color color) {
    this.sampleDarkFill.setBackground(color);
    this.sampleLightFill.setBackground(color);
    this.sampleDarkText.setForeground(color);
    this.sampleLightText.setForeground(color);
  }

  @Nullable
  public Color getColor() {
    final Color colorMain = this.colorPicker.getColor();
    final Color colorSecond = this.presentedColors.getColor();
    return colorMain == null ? colorSecond : colorMain;
  }

//  public static void main(@Nonnull @MustNotContainNull String... args) {
//    SwingUtilities.invokeLater(new Runnable() {
//      @Override
//      public void run() {
//        JFrame frame = new JFrame();
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//
//        final JPanel panel = new JPanel(new BorderLayout());
//        panel.add(new ColorChooser(Arrays.asList(Color.WHITE, Color.BLACK, Color.RED, Color.ORANGE, Color.PINK), Color.ORANGE).getPanel(), BorderLayout.CENTER);
//
//        frame.setContentPane(panel);
//        frame.pack();
//        frame.setVisible(true);
//      }
//    });
//  }
}
