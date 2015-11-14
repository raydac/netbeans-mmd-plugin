/*
 * Copyright 2015 Igor Maznitsa.
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
package com.igormaznitsa.ideamindmap.swing;

import java.io.File;
import java.util.ResourceBundle;

import com.igormaznitsa.ideamindmap.utils.AllIcons;
import com.igormaznitsa.ideamindmap.utils.IdeaUtils;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.intellij.openapi.vfs.LocalFileSystem;

public final class FileEditPanel extends javax.swing.JPanel {
  private static final com.intellij.openapi.diagnostic.Logger LOGGER = com.intellij.openapi.diagnostic.Logger.getInstance(FileEditPanel.class);
  private static final ResourceBundle BUNDLE = java.util.ResourceBundle.getBundle("/i18n/Bundle");

  public static final class DataContainer {

    private final String path;
    private final boolean showWithSystemTool;

    public DataContainer(final String path, final boolean showWithSystemTool) {
      this.path = path == null ? "" : path;
      this.showWithSystemTool = showWithSystemTool;
    }

    public String getPath() {
      return this.path;
    }

    public boolean isValid() {
      try {
        return this.path.isEmpty() ? true : new File(this.path).exists();
      }
      catch (Exception ex) {
        return false;
      }
    }

    public boolean isShowWithSystemTool() {
      return this.showWithSystemTool;
    }

    public boolean isEmpty() {
      return this.path.trim().isEmpty();
    }
  }

  private static final long serialVersionUID = -6683682013891751388L;
  private final File projectFolder;
  private final DialogProvider dialogProvider;

  public FileEditPanel(final DialogProvider dialogProvider, final File projectFolder, final DataContainer initialData) {
    this.dialogProvider = dialogProvider;
    initComponents();
    this.projectFolder = projectFolder;
    this.textFieldFilePath.setText(initialData == null ? "" : initialData.getPath());
    this.checkBoxShowFileInSystem.setSelected(initialData == null ? false : initialData.isShowWithSystemTool());
  }

  public DataContainer getData() {
    return new DataContainer(this.textFieldFilePath.getText().trim(), this.checkBoxShowFileInSystem.isSelected());
  }

  @SuppressWarnings("unchecked")
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    labelBrowseCurrentLink = new javax.swing.JLabel();
    textFieldFilePath = new javax.swing.JTextField();
    buttonChooseFile = new javax.swing.JButton();
    buttonReset = new javax.swing.JButton();
    optionPanel = new javax.swing.JPanel();
    checkBoxShowFileInSystem = new javax.swing.JCheckBox();

    setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 1, 10));
    setLayout(new java.awt.GridBagLayout());

    labelBrowseCurrentLink.setIcon(AllIcons.Buttons.FILE_LINK_BIG);
    java.util.ResourceBundle bundle = BUNDLE;
    labelBrowseCurrentLink.setToolTipText(bundle.getString("FileEditPanel.labelBrowseCurrentLink.toolTipText")); // NOI18N
    labelBrowseCurrentLink.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    labelBrowseCurrentLink.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    labelBrowseCurrentLink.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        labelBrowseCurrentLinkMouseClicked(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.ipadx = 10;
    add(labelBrowseCurrentLink, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1000.0;
    add(textFieldFilePath, gridBagConstraints);

    buttonChooseFile.setIcon(AllIcons.Buttons.FILE_MANAGER);
    buttonChooseFile.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        buttonChooseFileActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    add(buttonChooseFile, gridBagConstraints);

    buttonReset.setIcon(AllIcons.Buttons.CROSS);
    buttonReset.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        buttonResetActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    add(buttonReset, gridBagConstraints);

    optionPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

    checkBoxShowFileInSystem.setText(bundle.getString("FileEditPanel.checkBoxShowFileInSystem.text"));
    optionPanel.add(checkBoxShowFileInSystem);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.ipadx = 2;
    add(optionPanel, gridBagConstraints);
  }// </editor-fold>

  private void labelBrowseCurrentLinkMouseClicked(java.awt.event.MouseEvent evt) {
    if (evt.getClickCount() > 1) {
      final File file = new File(this.textFieldFilePath.getText().trim());
      IdeaUtils.openInSystemViewer(this.dialogProvider, LocalFileSystem.getInstance().findFileByIoFile(file));
    }
  }

  private void buttonChooseFileActionPerformed(java.awt.event.ActionEvent evt) {
    final String text = this.textFieldFilePath.getText().trim();
    final File predefinedFile = text.isEmpty() ? this.projectFolder : new File(text);

    final File selected = IdeaUtils.chooseFile(this, false, "Select file", predefinedFile, null);

    if (selected != null) {
      this.textFieldFilePath.setText(selected.getAbsolutePath());
    }
  }

  private void buttonResetActionPerformed(java.awt.event.ActionEvent evt) {
    this.textFieldFilePath.setText("");
  }

  private javax.swing.JButton buttonChooseFile;
  private javax.swing.JButton buttonReset;
  private javax.swing.JCheckBox checkBoxShowFileInSystem;
  private javax.swing.JLabel labelBrowseCurrentLink;
  private javax.swing.JPanel optionPanel;
  private javax.swing.JTextField textFieldFilePath;
}
