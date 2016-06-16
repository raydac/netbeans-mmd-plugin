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
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import org.apache.commons.lang.StringEscapeUtils;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import com.igormaznitsa.sciareto.Context;

public final class TabTitle extends JPanel {
  
  private static final long serialVersionUID = -6534083975320248288L;
  private final JLabel titleLabel;
  private volatile File associatedFile;
  private volatile boolean changed;
  private final Context context;
  private final TabProvider parent;
  
  private static final Icon NIMBUS_CLOSE_ICON = new ImageIcon(UiUtils.loadImage("nimbusCloseFrame.png"));
  
  public TabTitle(@Nonnull final Context context, @Nonnull final TabProvider parent, @Nullable final File associatedFile) {
    super(new GridBagLayout());
    this.parent = parent;
    this.context = context;
    this.associatedFile = associatedFile;
    this.changed = this.associatedFile == null;
    this.setOpaque(false);
    final GridBagConstraints constraints = new GridBagConstraints();
    constraints.fill = GridBagConstraints.BOTH;
    constraints.weightx = 1000.0d;
    this.titleLabel = new JLabel();
    this.add(this.titleLabel, constraints);
    
    final Icon uiCloseIcon = UIManager.getIcon("InternalFrameTitlePane.closeIcon");
    
    final JButton closeButton = new JButton(uiCloseIcon == null ? NIMBUS_CLOSE_ICON : uiCloseIcon);
    closeButton.setToolTipText("Close tab");
    closeButton.setBorder(null);
    closeButton.setContentAreaFilled(false);
    closeButton.setMargin(new Insets(0, 0, 0, 0));
    closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    closeButton.setOpaque(false);
    closeButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(@Nonnull final ActionEvent e) {
        processClose();
      }
    });
    constraints.fill = GridBagConstraints.BOTH;
    constraints.weightx = 0.0d;
    constraints.insets = new Insets(2, 8, 2, 0);
    this.add(closeButton, constraints);
    updateView();
  }

  public TabProvider getProvider(){
    return this.parent;
  }
  
  private void processClose(){
    final boolean close = !this.changed || DialogProviderManager.getInstance().getDialogProvider().msgConfirmOkCancel("Non saved file", "Close unsaved document '"+makeName()+"\'?");
    if (close) {
      this.context.closeTab(this);
    }
  }
  
  @Nullable
  public File getAssociatedFile(){
    return this.associatedFile;
  }
  
  public void setAssociatedFile(@Nullable final File file) {
    this.associatedFile = file;
    updateView();
  }
  
  public void setChanged(final boolean flag){
    this.changed = flag;
    updateView();
  }

  public boolean isChanged() {
    return this.changed;
  }
  
  @Nonnull
  private String makeName(){
    final File file = this.associatedFile;
    return file == null ? "Untitled" : file.getName();
  }
  
  
  private void updateView() {
    Utils.safeSwingCall(new Runnable() {
      @Override
      public void run() {
        titleLabel.setText("<html>" + (changed ? "<b>*<u>" : "") + StringEscapeUtils.escapeHtml(makeName()) + (changed ? "</u></b>" : "") + "</html>");
        revalidate();
      }
    });
  }
  
}
