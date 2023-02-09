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

package com.igormaznitsa.mindmap.swing.panel.ui;

import static java.awt.Color.LIGHT_GRAY;
import static javax.swing.BorderFactory.createCompoundBorder;
import static javax.swing.BorderFactory.createEmptyBorder;
import static javax.swing.BorderFactory.createLineBorder;

import com.igormaznitsa.mindmap.swing.i18n.MmdI18n;
import com.igormaznitsa.mindmap.swing.panel.utils.Focuser;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactory;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactoryProvider;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public final class PasswordPanel extends JPanel {

  private static final int COLUMNS = 24;
  private final JTextField textFieldHint;
  private final JLabel textLabelHint;
  private final JPasswordField textFieldPassword;
  private final Focuser focuser;
  
  public PasswordPanel() {
    this("", "", true);
  }

  public PasswordPanel(final String password, final String passwordHint,
                       final boolean hintEditable) {
    super(new GridBagLayout());
    this.setBorder(createCompoundBorder(
        createLineBorder(LIGHT_GRAY),
        createEmptyBorder(4, 4, 4, 4)));

    final UIComponentFactory componentFactory = UIComponentFactoryProvider.findInstance();

    if (hintEditable) {
      this.textFieldHint = componentFactory.makeTextField();
      this.textFieldHint.setText(passwordHint);
      this.textFieldHint.setColumns(COLUMNS);
      this.textLabelHint = null;
    } else {
      this.textFieldHint = null;
      this.textLabelHint = componentFactory.makeLabel();
      this.textLabelHint.setText(passwordHint);
    }
    this.textFieldPassword = componentFactory.makePasswordField();
    this.textFieldPassword.setColumns(COLUMNS);
    this.textFieldPassword.setText(password);
    this.textFieldPassword.setEchoChar('*');

    final GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.EAST;
    gbc.insets = new Insets(2, 2, 2, 2);
    gbc.fill = GridBagConstraints.HORIZONTAL;

    JLabel label = componentFactory.makeLabel();
    label.setHorizontalAlignment(SwingConstants.RIGHT);
    label.setText(MmdI18n.getInstance().findBundle().getString("PasswordPanel.labelPassword.text"));
    label.setToolTipText(MmdI18n.getInstance().findBundle().getString("PasswordPanel.labelPassword.tooltip"));
    this.add(label, gbc);

    gbc.gridx = 1;
    gbc.anchor = GridBagConstraints.WEST;
    this.add(this.textFieldPassword, gbc);

    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.anchor = GridBagConstraints.EAST;

    label = componentFactory.makeLabel();
    label.setHorizontalAlignment(SwingConstants.RIGHT);
    label.setText(MmdI18n.getInstance().findBundle().getString("PasswordPanel.labelHint.text"));
    label.setToolTipText(MmdI18n.getInstance().findBundle().getString("PasswordPanel.labelHint.tooltip"));
    this.add(label, gbc);

    gbc.gridx = 1;
    gbc.anchor = GridBagConstraints.WEST;
    this.add(this.textFieldHint == null ? this.textLabelHint : this.textFieldHint, gbc);

    gbc.gridx = 1;
    gbc.gridy = 2;
    gbc.anchor = GridBagConstraints.EAST;
    gbc.fill = GridBagConstraints.NONE;
    final JCheckBox showPasswordCheckbox = componentFactory.makeCheckBox();
    showPasswordCheckbox.setText(MmdI18n.getInstance().findBundle().getString("PasswordPanel.checkboxShowPassword.text"));
    showPasswordCheckbox
        .setToolTipText(MmdI18n.getInstance().findBundle().getString("PasswordPanel.checkboxShowPassword.tooltip"));
    this.add(showPasswordCheckbox, gbc);

    showPasswordCheckbox.addActionListener(e -> {
      if (showPasswordCheckbox.isSelected()) {
        textFieldPassword.setEchoChar((char) 0);
      } else {
        textFieldPassword.setEchoChar('*');
      }
    });

    this.focuser = new Focuser(this.textFieldPassword);
  }

  public char[] getPassword() {
    return this.textFieldPassword.getPassword();
  }

  public String getHint() {
    return this.textFieldHint == null ? this.textLabelHint.getText() : this.textFieldHint.getText();
  }
}
