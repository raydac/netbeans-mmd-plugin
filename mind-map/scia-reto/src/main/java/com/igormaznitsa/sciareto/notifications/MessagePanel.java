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
package com.igormaznitsa.sciareto.notifications;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import com.igormaznitsa.sciareto.Main;
import com.igormaznitsa.sciareto.ui.UiUtils;

final class MessagePanel extends JPanel implements ActionListener {

  private static final long serialVersionUID = 4382438833881000822L;

  private static final Icon NIMBUS_CLOSE_ICON = new ImageIcon(UiUtils.loadImage("nimbusCloseFrame.png"));

  private static final AtomicInteger ACTIVE_MESSAGES = new AtomicInteger();
  
  MessagePanel(@Nullable final Image icon, @Nullable final String title, @Nonnull final Color background, @Nonnull final JComponent component) {
    super(new GridBagLayout());
    this.setBackground(background);
    this.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK), BorderFactory.createEmptyBorder(8, 8, 8, 8)));

    final GridBagConstraints constraints = new GridBagConstraints();

    final JLabel labelTitle = new JLabel(title == null ? "" : title, icon == null ? null : new ImageIcon(icon),SwingConstants.CENTER);
    labelTitle.setForeground(Color.black);
    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.gridwidth = 1;
    constraints.gridheight = 1;
    constraints.weightx = 1000;
    constraints.fill = GridBagConstraints.BOTH;
    constraints.anchor = GridBagConstraints.NORTHWEST;
    
    labelTitle.setFont(labelTitle.getFont().deriveFont(Font.BOLD));
    
    this.add(labelTitle, constraints);

    constraints.gridx = 1;
    constraints.gridy = 0;

    final JButton closeButton = new JButton(NIMBUS_CLOSE_ICON);
    closeButton.addActionListener(this);
    closeButton.setBorder(null);
    closeButton.setContentAreaFilled(false);
    closeButton.setMargin(new Insets(0, 0, 0, 0));
    closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    closeButton.setOpaque(false);

    constraints.weightx = 1;
    
    this.add(closeButton, constraints);

    constraints.gridx = 0;
    constraints.gridy = 1;

    constraints.gridwidth = 2;
    constraints.weightx = 1;

    final JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
    separator.setForeground(Color.DARK_GRAY);
    this.add(separator, constraints);
    
    constraints.gridx = 0;
    constraints.gridy = 2;
    constraints.weightx = 1000;
    constraints.weighty = 1000;

    this.add(component, constraints);

    this.setAlignmentY(Component.RIGHT_ALIGNMENT);
    
    doLayout();
  
    ACTIVE_MESSAGES.incrementAndGet();
  }

  @Override
  @Nonnull
  public Dimension getMaximumSize() {
    return new Dimension(super.getPreferredSize().width, Integer.MAX_VALUE);
  }

  @Override
  public void actionPerformed(@Nonnull final ActionEvent e) {
    final Container parent = this.getParent();
    if (parent != null) {
      parent.remove(this);
      if (ACTIVE_MESSAGES.decrementAndGet()<=0){
        Main.getApplicationFrame().getGlassPane().setVisible(false);
      }
      parent.revalidate();
    }
  }
}
