/*
 * Copyright 2016 Igor Maznitsa.
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

public class DefaultSwingUIComponentService implements UIComponentFactory {

  @Override
  public JPanel makePanel () {
    return new JPanel();
  }

  @Override
  public JComboBox makeComboBox () {
    return new JComboBox();
  }

  @Override
  public JButton makeButton () {
    return new JButton();
  }

  @Override
  public JToolBar makeToolBar () {
    return new JToolBar();
  }

  @Override
  public JScrollPane makeScrollPane () {
    return new JScrollPane();
  }

  @Override
  public JCheckBox makeCheckBox () {
    return new JCheckBox();
  }

  @Override
  public JLabel makeLabel () {
    return new JLabel();
  }

  @Override
  public JPopupMenu makePopupMenu () {
    return new JPopupMenu();
  }

  @Override
  public JTextArea makeTextArea () {
    return new JTextArea();
  }

  @Override
  public JMenuItem makeMenuItem (final String text, final Icon icon) {
    return new JMenuItem(text, icon);
  }

  @Override
  public JCheckBoxMenuItem makeCheckboxMenuItem (final String text, final Icon icon, final boolean selected) {
    return new JCheckBoxMenuItem(text, icon, selected);
  }
  
  @Override
  public JSeparator makeMenuSeparator () {
    return new JSeparator();
  }

  @Override
  public JMenu makeMenu (final String text) {
    return new JMenu(text);
  }

  @Override
  public JEditorPane makeEditorPane () {
    return new JEditorPane();
  }

  @Override
  public JSlider makeSlider () {
    return new JSlider();
  }
}
