package com.igormaznitsa.ideamindmap.swing;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by igorm on 11/14/15.
 */
public class FontSelector implements ActionListener {
  private JComboBox comboBoxName;
  private JComboBox comboBoxStyle;
  private JComboBox comboBoxSize;
  private JPanel mainPanel;
  private JTextArea textArea;

  public FontSelector(final Font initial) {

    final DefaultComboBoxModel<String> modelName = new DefaultComboBoxModel<>(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
    this.comboBoxName.setModel(modelName);
    this.comboBoxName.setSelectedItem(initial.getFamily());

    final DefaultComboBoxModel<String> modelStyle = new DefaultComboBoxModel<>(new String[] { "Plain", "Bold", "Italic", "Bold+Italic" });
    this.comboBoxStyle.setModel(modelStyle);

    this.textArea.setWrapStyleWord(true);
    this.textArea.setLineWrap(true);
    this.textArea.setText("Sed ut perspiciatis unde omnis iste natus error. Sit voluptatem accusantium doloremque laudantium. Totam rem aperiam, eaque ipsa quae ab illo.");
    this.textArea.setEditable(false);

    selectForStyle(initial.getStyle());

    final List<Integer> sizes = new ArrayList<>();
    for(int i=3;i<72;i++){
      sizes.add(i);
    }
    final DefaultComboBoxModel<Integer> modelSize = new DefaultComboBoxModel<>(sizes.toArray(new Integer[]{sizes.size()}));
    this.comboBoxSize.setModel(modelSize);

    this.comboBoxSize.setSelectedItem(initial.getSize());

    this.textArea.setFont(getValue());

    this.comboBoxName.addActionListener(this);
    this.comboBoxStyle.addActionListener(this);
    this.comboBoxSize.addActionListener(this);

    final Dimension size = new Dimension(550,300);

    this.mainPanel.setMinimumSize(size);
    this.mainPanel.setPreferredSize(size);
  }

  private void selectForStyle(final int style) {
    switch (style) {
    case Font.PLAIN:
      this.comboBoxStyle.setSelectedIndex(0);break;
    case Font.BOLD:
      this.comboBoxStyle.setSelectedIndex(1);break;
    case Font.ITALIC:
      this.comboBoxStyle.setSelectedIndex(2);break;
    default:
      this.comboBoxStyle.setSelectedIndex(3);break;
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
    final int size = (int)this.comboBoxSize.getSelectedItem();
    return new Font(family,style,size);
  }

  public JPanel getPanel() {
    return this.mainPanel;
  }

  @Override public void actionPerformed(ActionEvent e) {
    this.textArea.setFont(getValue());
  }
}
