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

package com.igormaznitsa.mindmap.swing.services;

import com.igormaznitsa.mindmap.plugins.api.HasOptions;
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

public interface UIComponentFactory {
  @Nonnull
  JPanel makePanel();

  @Nonnull
  JPanel makePanelWithOptions(@Nonnull HasOptions optionsProcessor);

  @Nonnull
  JComboBox makeComboBox();

  @Nonnull
  JSpinner makeSpinner();

  @Nonnull
  JButton makeButton();

  @Nonnull
  JToggleButton makeToggleButton();

  @Nonnull
  JRadioButton makeRadioButton();

  @Nonnull
  JToolBar makeToolBar();

  @Nonnull
  JScrollPane makeScrollPane();

  @Nonnull
  JCheckBox makeCheckBox();

  @Nonnull
  JLabel makeLabel();

  @Nonnull
  JPopupMenu makePopupMenu();

  @Nonnull
  JTextArea makeTextArea();

  @Nonnull
  JPasswordField makePasswordField();

  @Nonnull
  JTextField makeTextField();

  @Nonnull
  JEditorPane makeEditorPane();

  @Nonnull
  JMenuItem makeMenuItem(@Nonnull String text, @Nullable Icon icon);

  @Nonnull
  JRadioButtonMenuItem makeRadioButtonMenuItem(@Nonnull String text, @Nullable Icon icon, boolean selected);

  @Nonnull
  JCheckBoxMenuItem makeCheckboxMenuItem(@Nonnull String text, @Nullable Icon icon, boolean selected);

  @Nonnull
  ButtonGroup makeButtonGroup();

  @Nonnull
  JSeparator makeMenuSeparator();

  @Nonnull
  JMenu makeMenu(@Nonnull String text);

  @Nonnull
  JSlider makeSlider();
}
