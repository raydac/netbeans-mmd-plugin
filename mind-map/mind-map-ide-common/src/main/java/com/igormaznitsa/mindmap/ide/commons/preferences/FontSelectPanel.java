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

import static com.igormaznitsa.mindmap.ide.commons.preferences.FontSelectPanel.FontStyle.findFor;
import static java.util.Objects.requireNonNull;

import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactory;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.DefaultButtonModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;

public class FontSelectPanel {

  private static final String TEXT_ETALON =
      "Sed ut perspiciatis unde omnis iste natus error. Sit voluptatem accusantium doloremque laudantium. Totam rem aperiam, eaque ipsa quae ab illo.";
  private final JPanel panel;
  private final JTextArea textArea;
  private final JComboBox<String> comboFontFamoly;
  private final JComboBox<FontStyle> comboFontStyle;
  private final JSpinner spinnerFontSize;

  private final JButton buttonSelect;
  private Font value;

  private boolean allowListeners;

  public FontSelectPanel(
      final Supplier<Component> dialogParentSupplier,
      final String description,
      final UIComponentFactory componentFactory,
      final DialogProvider dialogProvider,
      final Font font) {
    final ResourceBundle bundle = MmcI18n.getInstance().findBundle();
    this.buttonSelect = componentFactory.makeButton();
    this.buttonSelect.setHorizontalAlignment(JButton.CENTER);

    this.buttonSelect.setModel(new DefaultButtonModel() {
      @Override
      protected void fireActionPerformed(final ActionEvent e) {
        final Font old = FontSelectPanel.this.value;
        if (!dialogProvider.msgOkCancel(dialogParentSupplier.get(), String.format(
                bundle.getString("panelFontSelector.title"), description),
            FontSelectPanel.this.asPanel())) {
          FontSelectPanel.this.value = old;
        }
        updateFontForParameters();
        buttonSelect.setText(asString(FontSelectPanel.this.value));
        super.fireActionPerformed(e);
      }
    });

    this.panel = componentFactory.makePanel();
    this.panel.setLayout(new BorderLayout());
    this.textArea = componentFactory.makeTextArea();
    this.textArea.setRows(10);

    this.textArea.setText(TEXT_ETALON);
    this.textArea.setLineWrap(true);
    this.textArea.setWrapStyleWord(true);

    this.comboFontFamoly = componentFactory.makeComboBox(String.class);
    this.comboFontFamoly.setModel(new DefaultComboBoxModel<>(getAllFontFamilies().toArray(new String[0])));

    this.comboFontStyle = componentFactory.makeComboBox(FontStyle.class);
    this.comboFontStyle.setModel(new DefaultComboBoxModel<>(FontStyle.values()));

    this.spinnerFontSize = componentFactory.makeSpinner();
    this.spinnerFontSize.setModel(new SpinnerNumberModel(8, 4, 128, 1));

    final JPanel topPanel = componentFactory.makePanel();
    topPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

    JLabel label = componentFactory.makeLabel();
    label.setText(bundle.getString("panelFontSelector.labelName"));
    topPanel.add(label);
    topPanel.add(this.comboFontFamoly);

    label = componentFactory.makeLabel();
    label.setText(bundle.getString("panelFontSelector.labelStyle"));
    topPanel.add(label);
    topPanel.add(this.comboFontStyle);

    label = componentFactory.makeLabel();
    label.setText(bundle.getString("panelFontSelector.labelSize"));
    topPanel.add(label);
    topPanel.add(this.spinnerFontSize);

    final JScrollPane scrollPane = componentFactory.makeScrollPane();
    scrollPane.setViewportView(this.textArea);

    this.panel.add(topPanel, BorderLayout.NORTH);
    this.panel.add(scrollPane, BorderLayout.CENTER);

    this.comboFontFamoly.addActionListener(a -> {
      if (this.allowListeners) {
        this.updateFontForParameters();
      }
    });
    this.comboFontStyle.addActionListener(a -> {
      if (this.allowListeners) {
        this.updateFontForParameters();
      }
    });
    this.spinnerFontSize.addChangeListener(a -> {
      if (this.allowListeners) {
        this.updateFontForParameters();
      }
    });
    this.setValue(font);
  }

  private static List<String> getAllFontFamilies() {
    return Stream.of(GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts())
        .map(Font::getFamily)
        .distinct()
        .sorted()
        .collect(Collectors.toList());
  }

  public static String asString(final Font font) {
    return font.getFamily() + ", " +
        findFor(font.getStyle()).map(FontStyle::toString).orElse("UNKNOWN")
        + ", " + font.getSize();
  }

  private void updateFontForParameters() {
    final String fontFamily = this.comboFontFamoly.getSelectedItem().toString();
    final FontStyle fontStyle = (FontStyle) this.comboFontStyle.getSelectedItem();

    this.value = new Font(fontFamily, fontStyle.getStyle(),
        ((Number) this.spinnerFontSize.getValue()).intValue());

    this.textArea.setFont(this.value);
    this.textArea.revalidate();
    this.textArea.repaint();
  }

  public JButton asButton() {
    return this.buttonSelect;
  }

  public JPanel asPanel() {
    return this.panel;
  }

  public Font getValue() {
    return this.value;
  }

  public void setValue(final Font font) {
    this.allowListeners = false;
    try {
      this.value = requireNonNull(font, "Font must not be null");
      this.comboFontFamoly.setSelectedItem(this.value.getFamily());
      this.comboFontStyle.setSelectedItem(
          FontStyle.findFor(this.value.getStyle()).orElse(FontStyle.PLAIN));
      this.spinnerFontSize.setValue(this.value.getSize());
      this.textArea.setFont(this.value);
      this.buttonSelect.setText(asString(font));
      this.panel.repaint();
    }finally {
      this.allowListeners = true;
    }
  }

  public enum FontStyle {
    PLAIN("Plain", Font.PLAIN),
    BOLD("Bold", Font.BOLD),
    ITALIC("Italic", Font.ITALIC),
    ITALIC_BOLD("Italic+Bold", Font.ITALIC | Font.BOLD);

    private static final FontStyle[] styles = FontStyle.values();

    private final String title;
    private final int style;

    FontStyle(final String title, final int style) {
      this.title = title;
      this.style = style;
    }

    public static Optional<FontStyle> findFor(final int style) {
      FontStyle found = null;
      for (final FontStyle f : styles) {
        if (f.style == style) {
          found = f;
          break;
        }
      }
      return Optional.ofNullable(found);
    }

    public int getStyle() {
      return this.style;
    }

    @Override
    public String toString() {
      return this.title;
    }
  }

}
