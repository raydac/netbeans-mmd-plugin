/* 
 * Copyright (C) 2018 Igor Maznitsa.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package com.igormaznitsa.sciareto.notifications;

import com.igormaznitsa.sciareto.SciaRetoStarter;
import com.igormaznitsa.sciareto.ui.UiUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.atomic.AtomicInteger;

final class MessagePanel extends JPanel implements ActionListener {

  private static final long serialVersionUID = 4382438833881000822L;

  private static final Icon NIMBUS_CLOSE_ICON = new ImageIcon(UiUtils.loadIcon("nimbusCloseFrame.png")); //NOI18N

  private static final AtomicInteger ACTIVE_MESSAGES = new AtomicInteger();
  
  MessagePanel(@Nullable final Image icon, @Nullable final String title, @Nonnull final Color background, @Nonnull final JComponent component) {
    super(new GridBagLayout());
    this.setBackground(background);
    this.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK), BorderFactory.createEmptyBorder(8, 8, 8, 8)));

    final GridBagConstraints constraints = new GridBagConstraints();

    final JLabel labelTitle = new JLabel(title == null ? "" : title, icon == null ? null : new ImageIcon(icon),SwingConstants.CENTER); //NOI18N
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
        SciaRetoStarter.getApplicationFrame().getGlassPane().setVisible(false);
      }
      parent.revalidate();
    }
  }
}
