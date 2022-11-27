/*
 * Copyright (C) 2015-2022 Igor A. Maznitsa
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

package com.igormaznitsa.ideamindmap.utils;

import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.mindmap.plugins.api.parameters.AbstractParameter;
import com.igormaznitsa.mindmap.swing.services.DefaultParametersPanelFactory;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactory;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.JBCheckboxMenuItem;
import com.intellij.openapi.ui.JBMenuItem;
import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.JBPasswordField;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.ButtonGroup;
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
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

public class IdeaUIComponentFactory implements UIComponentFactory {

  @Override
  @Nonnull
  public JPanel makePanel() {
    return new JBPanel();
  }

  @Nonnull
  @Override
  public JPanel makePanelWithOptions(@Nonnull Set<AbstractParameter<?>> parameters) {
    return DefaultParametersPanelFactory.getInstance().make(parameters);
  }

  @Nonnull
  @Override
  public JToggleButton makeToggleButton() {
    return new JToggleButton();
  }

  @Nonnull
  @Override
  public JRadioButton makeRadioButton() {
    return new JBRadioButton();
  }

  @Override
  @Nonnull
  public JComboBox makeComboBox() {
    return new ComboBox();
  }

  @Override
  @Nonnull
  public JButton makeButton() {
    return new JButton();
  }

  @Override
  @Nonnull
  public JToolBar makeToolBar() {
    return new JToolBar();
  }

  @Override
  @Nonnull
  public JScrollPane makeScrollPane() {
    return new JBScrollPane();
  }

  @Override
  @Nonnull
  public JCheckBox makeCheckBox() {
    return new JBCheckBox();
  }

  @Override
  @Nonnull
  public JLabel makeLabel() {
    return new JBLabel();
  }

  @Override
  @Nonnull
  public JPopupMenu makePopupMenu() {
    return new JBPopupMenu();
  }

  @Override
  @Nonnull
  public JTextArea makeTextArea() {
    return new JTextArea();
  }

  @Override
  @Nonnull
  public JSpinner makeSpinner() {
    return new JSpinner();
  }

  @Override
  @Nonnull
  public JEditorPane makeEditorPane() {
    return new JEditorPane();
  }

  @Override
  @Nonnull
  public JMenuItem makeMenuItem(@Nonnull final String s, final Icon icon) {
    return new JBMenuItem(s, icon);
  }

  @Override
  @Nonnull
  public JCheckBoxMenuItem makeCheckboxMenuItem(@Nonnull final String s, final Icon icon, final boolean b) {
    return new JBCheckboxMenuItem(s, icon, b);
  }

  @Nonnull
  @Override
  public JRadioButtonMenuItem makeRadioButtonMenuItem(@Nonnull String s, @Nullable Icon icon, boolean b) {
    return new JRadioButtonMenuItem(s, icon, b);
  }

  @Nonnull
  @Override
  public ButtonGroup makeButtonGroup() {
    return new ButtonGroup();
  }

  @Override
  @Nonnull
  public JSeparator makeMenuSeparator() {
    return new JSeparator();
  }

  @Override
  @Nonnull
  public JMenu makeMenu(@Nonnull final String s) {
    return new JMenu(s);
  }

  @Override
  @Nonnull
  public JSlider makeSlider() {
    return new JSlider();
  }

  @Nonnull
  @Override
  public JTextField makeTextField() {
    return new JBTextField();
  }

  @Nonnull
  @Override
  public JPasswordField makePasswordField() {
    return new JBPasswordField();
  }

}
