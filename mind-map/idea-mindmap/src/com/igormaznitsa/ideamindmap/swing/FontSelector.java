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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class FontSelector implements ActionListener {
  private JComboBox comboBoxName;
  private JComboBox comboBoxStyle;
  private JComboBox comboBoxSize;
  private JPanel mainPanel;
  private JTextArea textArea;

  @SuppressWarnings("unchecked")
  public FontSelector(final Font initial) {

    final DefaultComboBoxModel<String> modelName = new DefaultComboBoxModel<String>(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
    this.comboBoxName.setModel(modelName);
    this.comboBoxName.setSelectedItem(initial.getFamily());

    final DefaultComboBoxModel<String> modelStyle = new DefaultComboBoxModel<String>(new String[] {"Plain", "Bold", "Italic", "Bold+Italic"});
    this.comboBoxStyle.setModel(modelStyle);

    this.textArea.setWrapStyleWord(true);
    this.textArea.setLineWrap(true);
    this.textArea.setText("Sed ut perspiciatis unde omnis iste natus error. Sit voluptatem accusantium doloremque laudantium. Totam rem aperiam, eaque ipsa quae ab illo.");
    this.textArea.setEditable(false);

    selectForStyle(initial.getStyle());

    final List<Integer> sizes = new ArrayList<Integer>();
    for (int i = 3; i < 72; i++) {
      sizes.add(i);
    }
    final DefaultComboBoxModel<Integer> modelSize = new DefaultComboBoxModel<Integer>(sizes.toArray(new Integer[] {sizes.size()}));
    this.comboBoxSize.setModel(modelSize);

    this.comboBoxSize.setSelectedItem(initial.getSize());

    this.textArea.setFont(getValue());

    this.comboBoxName.addActionListener(this);
    this.comboBoxStyle.addActionListener(this);
    this.comboBoxSize.addActionListener(this);

    final Dimension size = new Dimension(550, 300);

    this.mainPanel.setMinimumSize(size);
    this.mainPanel.setPreferredSize(size);
  }

  private void selectForStyle(final int style) {
    switch (style) {
      case Font.PLAIN:
        this.comboBoxStyle.setSelectedIndex(0);
        break;
      case Font.BOLD:
        this.comboBoxStyle.setSelectedIndex(1);
        break;
      case Font.ITALIC:
        this.comboBoxStyle.setSelectedIndex(2);
        break;
      default:
        this.comboBoxStyle.setSelectedIndex(3);
        break;
    }
  }

  private int getFontStyle() {
    switch (this.comboBoxStyle.getSelectedIndex()) {
      case 0:
        return Font.PLAIN;
      case 1:
        return Font.BOLD;
      case 2:
        return Font.ITALIC;
      default:
        return Font.BOLD | Font.ITALIC;
    }
  }

  public Font getValue() {
    final String family = (String) this.comboBoxName.getSelectedItem();
    final int style = getFontStyle();
    final int size = (Integer) this.comboBoxSize.getSelectedItem();
    return new Font(family, style, size);
  }

  public JPanel getPanel() {
    return this.mainPanel;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    this.textArea.setFont(getValue());
  }
}
