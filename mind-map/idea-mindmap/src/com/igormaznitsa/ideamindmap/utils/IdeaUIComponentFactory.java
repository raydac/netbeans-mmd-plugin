package com.igormaznitsa.ideamindmap.utils;

import com.igormaznitsa.mindmap.swing.services.UIComponentFactory;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.JBCheckboxMenuItem;
import com.intellij.openapi.ui.JBMenuItem;
import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JToolBar;

public class IdeaUIComponentFactory implements UIComponentFactory{
  @Override public JPanel makePanel() {
    return new JBPanel();
  }

  @Override public JComboBox makeComboBox() {
    return new ComboBox();
  }

  @Override public JButton makeButton() {
    return new JButton();
  }

  @Override public JToolBar makeToolBar() {
    return new JToolBar();
  }

  @Override public JScrollPane makeScrollPane() {
    return new JBScrollPane();
  }

  @Override public JCheckBox makeCheckBox() {
    return new JBCheckBox();
  }

  @Override public JLabel makeLabel() {
    return new JBLabel();
  }

  @Override public JPopupMenu makePopupMenu() {
    return new JBPopupMenu();
  }

  @Override public JTextArea makeTextArea() {
    return new JTextArea();
  }

  @Override public JEditorPane makeEditorPane() {
    return new JEditorPane();
  }

  @Override public JMenuItem makeMenuItem(final String s, final Icon icon) {
    return new JBMenuItem(s, icon);
  }

  @Override public JCheckBoxMenuItem makeCheckboxMenuItem(final String s, final Icon icon, final boolean b) {
    return new JBCheckboxMenuItem(s, icon, b);
  }

  @Override public JSeparator makeMenuSeparator() {
    return new JSeparator();
  }

  @Override public JMenu makeMenu(final String s) {
    return new JMenu(s);
  }

  @Override public JSlider makeSlider() {
    return new JSlider();
  }
}
