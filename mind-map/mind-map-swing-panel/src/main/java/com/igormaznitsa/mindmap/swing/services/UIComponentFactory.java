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

package com.igormaznitsa.mindmap.swing.services;

import com.igormaznitsa.mindmap.plugins.api.parameters.AbstractParameter;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import java.util.Set;
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

public interface UIComponentFactory {
  JPanel makePanel();

  JPanel makePanelWithOptions(DialogProvider dialogProvider, Set<AbstractParameter<?>> parameters);

  <T> JComboBox<T> makeComboBox(Class<T> type);

  JSpinner makeSpinner();

  JButton makeButton();

  JToggleButton makeToggleButton();

  JRadioButton makeRadioButton();

  JToolBar makeToolBar();

  JScrollPane makeScrollPane();

  JCheckBox makeCheckBox();

  JLabel makeLabel();

  JPopupMenu makePopupMenu();

  JTextArea makeTextArea();

  JPasswordField makePasswordField();

  JTextField makeTextField();

  JEditorPane makeEditorPane();

  JMenuItem makeMenuItem(String text, Icon icon);

  JRadioButtonMenuItem makeRadioButtonMenuItem(String text, Icon icon, boolean selected);

  JCheckBoxMenuItem makeCheckboxMenuItem(String text, Icon icon, boolean selected);

  ButtonGroup makeButtonGroup();

  JSeparator makeMenuSeparator();

  JMenu makeMenu(String text);

  JSlider makeSlider();
}
