package com.igormaznitsa.sciareto.ui.editors.mmeditors.dialogs;

import static java.awt.Color.LIGHT_GRAY;
import static javax.swing.BorderFactory.createCompoundBorder;
import static javax.swing.BorderFactory.createEmptyBorder;
import static javax.swing.BorderFactory.createLineBorder;


import com.igormaznitsa.mindmap.swing.services.UIComponentFactory;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactoryProvider;
import com.igormaznitsa.sciareto.ui.Focuser;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.annotation.Nonnull;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public final class PasswordPanel extends JPanel {

  private static final int COLUMNS = 16;
  private final JTextField textFieldHint;
  private final JLabel textLabelHint;
  private final JPasswordField textFieldPassword;
  private final Focuser focuser;

  public PasswordPanel() {
    this("", "", true);
  }

  public PasswordPanel(@Nonnull final String password, @Nonnull final String passwordHint,
                       final boolean hintEditable) {
    super(new GridBagLayout());
    this.setBorder(createCompoundBorder(
        createLineBorder(LIGHT_GRAY),
        createEmptyBorder(4, 4, 4, 4)));

    final UIComponentFactory uifactory = UIComponentFactoryProvider.findInstance();

    if (hintEditable) {
      this.textFieldHint = uifactory.makeTextField();
      this.textFieldHint.setText(passwordHint);
      this.textFieldHint.setColumns(COLUMNS);
      this.textLabelHint = null;
    } else {
      this.textFieldHint = null;
      this.textLabelHint = uifactory.makeLabel();
      this.textLabelHint.setText(passwordHint);
    }
    this.textFieldPassword = uifactory.makePasswordField();
    this.textFieldPassword.setColumns(COLUMNS);
    this.textFieldPassword.setText(password);
    this.textFieldPassword.setEchoChar('*');

    final GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.EAST;
    gbc.insets = new Insets(2, 2, 2, 2);

    JLabel label = uifactory.makeLabel();
    label.setText("Password:");
    this.add(label, gbc);

    gbc.gridx = 1;
    gbc.anchor = GridBagConstraints.WEST;
    this.add(this.textFieldPassword, gbc);

    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.anchor = GridBagConstraints.EAST;

    label = uifactory.makeLabel();
    label.setText("Hint:");
    this.add(label, gbc);

    gbc.gridx = 1;
    gbc.anchor = GridBagConstraints.WEST;

    this.add(this.textFieldHint == null ? this.textLabelHint : this.textFieldHint, gbc);

    gbc.gridx = 1;
    gbc.gridy = 2;
    gbc.anchor = GridBagConstraints.EAST;
    final JCheckBox showPasswordCheckbox = uifactory.makeCheckBox();
    showPasswordCheckbox.setText("Show password");
    this.add(showPasswordCheckbox, gbc);

    showPasswordCheckbox.addActionListener(e -> {
      if (showPasswordCheckbox.isSelected()) {
        this.textFieldPassword.setEchoChar((char) 0);
      } else {
        this.textFieldPassword.setEchoChar('*');
      }
    });

    this.focuser = new Focuser(this.textFieldPassword);
  }

  @Nonnull
  public char[] getPassword() {
    return this.textFieldPassword.getPassword();
  }

  @Nonnull
  public String getHint() {
    return this.textFieldHint == null ? this.textLabelHint.getText() : this.textFieldHint.getText();
  }
}
