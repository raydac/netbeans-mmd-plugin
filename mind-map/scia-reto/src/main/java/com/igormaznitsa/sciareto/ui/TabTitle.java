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
package com.igormaznitsa.sciareto.ui;

import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.plaf.metal.MetalIconFactory;
import org.apache.commons.lang.StringEscapeUtils;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;

public final class TabTitle extends JPanel {
  
  private static final long serialVersionUID = -6534083975320248288L;
  private final JLabel titleLabel;
  private volatile File associatedFile;
  private volatile boolean changed;
  
  public TabTitle(@Nullable final File associatedFile) {
    super(new GridBagLayout());
    this.associatedFile = associatedFile;
    this.changed = this.associatedFile == null;
    this.setOpaque(false);
    final GridBagConstraints constraints = new GridBagConstraints();
    constraints.fill = GridBagConstraints.BOTH;
    constraints.weightx = 1000.0d;
    this.titleLabel = new JLabel();
    this.add(this.titleLabel, constraints);
    final JButton closeButton = new JButton(MetalIconFactory.getInternalFrameCloseIcon(16));
    closeButton.setToolTipText("Close tab");
    closeButton.setBorder(null);
    closeButton.setContentAreaFilled(false);
    closeButton.setMargin(new Insets(0, 0, 0, 0));
    closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    closeButton.setOpaque(false);
    closeButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(@Nonnull final ActionEvent e) {
      }
    });
    constraints.fill = GridBagConstraints.BOTH;
    constraints.weightx = 0.0d;
    constraints.insets = new Insets(2, 8, 2, 0);
    this.add(closeButton, constraints);
  }

  @Nullable
  public File getAssociatedFile(){
    return this.associatedFile;
  }
  
  public void setAssociatedFile(@Nullable final File file) {
    this.associatedFile = file;
    updateTitle();
  }
  
  public void setChanged(final boolean flag){
    this.changed = flag;
    updateTitle();
  }
  
  private void updateTitle() {
    final File file = this.associatedFile;
    Utils.safeSwingCall(new Runnable() {
      @Override
      public void run() {
        titleLabel.setText("<html>" + (changed ? "<b>*<u>" : "") + StringEscapeUtils.escapeHtml(associatedFile == null ? "Untitled" : associatedFile.getName()) + (changed ? "</u></b>" : "") + "</html>");
        revalidate();
      }
    });
  }
  
}
