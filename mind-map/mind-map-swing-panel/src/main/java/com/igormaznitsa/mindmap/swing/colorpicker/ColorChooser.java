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

import com.igormaznitsa.mindmap.swing.i18n.MmdI18n;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactory;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactoryProvider;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
  private final Icon COLOR_WHEEL_ICON = new ImageIcon(ColorChooser.class.getResource("/com/igormaznitsa/mindmap/swing/panel/icons/color_wheel32.png"));
  private Color tunedColor;

  public ColorChooser(
      final List<Color> mapColors,
      final Color selectedColor
  ) {
    final UIComponentFactory componentFactory = UIComponentFactoryProvider.findInstance();
    this.panel = componentFactory.makePanel();
    this.panel.setLayout(new GridBagLayout());

    final String SAMPLE_TEXT = "    TEXT    ";

    GridBagConstraints data = new GridBagConstraints();
    data.anchor = GridBagConstraints.CENTER;
    data.gridx = 0;
    data.gridy = 0;
    data.fill = GridBagConstraints.BOTH;
    data.insets.set(4, 4, 4, 4);

    this.sampleDarkFill = componentFactory.makeLabel();
    final Font font = this.sampleDarkFill.getFont().deriveFont(Font.BOLD, this.sampleDarkFill.getFont().getSize() * 2);
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

    this.colorPicker =
        new ColorPickerPanel(componentFactory.makePanel(), PALETTE_ROWS, 12, 4, 4, PALETTE);
    this.presentedColors = new ColorPickerPanel(componentFactory.makePanel(), 2, 12, 4, 4,
        mapColors == null ? Collections.emptyList() : mapColors);

    this.panel.add(this.colorPicker.getPanel(), data);

    data.gridy = 1;

    final JPanel samplePanel = componentFactory.makePanel();

    samplePanel.setLayout(new GridLayout(2, 2));
    samplePanel.setBorder(BorderFactory.createTitledBorder(MmdI18n.getInstance().findBundle().getString("ColorChooser.Text.Example")));

    samplePanel.add(this.sampleDarkFill);
    samplePanel.add(this.sampleLightText);
    samplePanel.add(this.sampleDarkText);
    samplePanel.add(this.sampleLightFill);

    final JPanel sampleAndTunePanel = new JPanel(new GridBagLayout());
    final GridBagConstraints sampleAndTunePanelData = new GridBagConstraints();

    sampleAndTunePanelData.gridx = 0;
    sampleAndTunePanelData.gridy = 0;
    sampleAndTunePanelData.weightx = 1000;
    sampleAndTunePanelData.fill = GridBagConstraints.HORIZONTAL;
    sampleAndTunePanel.add(samplePanel, sampleAndTunePanelData);

    sampleAndTunePanelData.weightx = 1;
    sampleAndTunePanelData.anchor = GridBagConstraints.CENTER;
    sampleAndTunePanelData.insets = new Insets(0, 2, 0, 2);
    sampleAndTunePanelData.fill = GridBagConstraints.BOTH;
    sampleAndTunePanelData.gridx = 1;

    final JButton buttonTuneColor = new JButton(COLOR_WHEEL_ICON);
    buttonTuneColor.setFocusable(false);
    buttonTuneColor.setBorderPainted(false);
    buttonTuneColor.setContentAreaFilled(false);
    buttonTuneColor.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    buttonTuneColor.setToolTipText(MmdI18n.getInstance().findBundle().getString("ColorChooser.ButtonColorWheel.Tooltip"));
    buttonTuneColor.addActionListener(event -> {
      Color choosedColor;
      try {
        choosedColor =
            (Color) JColorChooser.class.getMethod("showDialog", Component.class, String.class,
                    Color.class, boolean.class)
                .invoke(null, panel, MmdI18n.getInstance().findBundle().getString("ColorChooser.ChooseColorDialogTitle"),
                    sampleDarkFill.getBackground(), false);
      } catch (Exception ex) {
        try {
          choosedColor =
              (Color) JColorChooser.class.getMethod("showDialog", Component.class, String.class,
                      Color.class)
                  .invoke(null, panel, MmdI18n.getInstance().findBundle().getString("ColorChooser.ChooseColorDialogTitle"),
                      sampleDarkFill.getBackground());
        } catch (Exception exx) {
          choosedColor = null;
          JOptionPane.showMessageDialog(panel, exx.getMessage(), "Internal error",
              JOptionPane.ERROR_MESSAGE);
        }
      }
      if (choosedColor != null) {
        colorPicker.resetSelected();
        presentedColors.setColor(null);
        final Color colorWithoutAlpha = new Color(choosedColor.getRGB());
        tunedColor = colorWithoutAlpha;
        updateSamples(colorWithoutAlpha);
        panel.repaint();
      }
    });

    sampleAndTunePanel.add(buttonTuneColor, sampleAndTunePanelData);

    data.insets.set(4, 32, 4, 32);
    this.panel.add(sampleAndTunePanel, data);

    data.insets.set(4, 4, 4, 4);

    data.gridy = 2;
    this.panel.add(this.presentedColors.getPanel(), data);

    this.colorPicker.addColorListener((source, color) -> {
      presentedColors.resetSelected();
      updateSamples(color);
    });

    this.presentedColors.addColorListener((source, color) -> {
      colorPicker.resetSelected();
      updateSamples(color);
    });

    if (selectedColor != null) {
      SwingUtilities.invokeLater(() -> {
        colorPicker.resetSelected();
        tunedColor = null;
        presentedColors.setColor(selectedColor);
        updateSamples(selectedColor);
      });
    }

    this.panel.doLayout();
  }

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

  private static List<Color> makeSteps(final Color start, final Color end, final int steps) {
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

  public JPanel getPanel() {
    return this.panel;
  }

  private void updateSamples(final Color color) {
    this.sampleDarkFill.setBackground(color);
    this.sampleLightFill.setBackground(color);
    this.sampleDarkText.setForeground(color);
    this.sampleLightText.setForeground(color);
  }

  public Color getColor() {
    final Color colorMain = this.colorPicker.getColor();
    final Color colorSecond = this.presentedColors.getColor();
    final Color tunedColorByWheel = this.tunedColor;
    if (colorMain != null) {
      return colorMain;
    }
    if (colorSecond != null) {
      return colorSecond;
    }
    return tunedColorByWheel;
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
