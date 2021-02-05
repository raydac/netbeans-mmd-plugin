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

package com.igormaznitsa.ideamindmap.swing;

import com.igormaznitsa.ideamindmap.utils.AllIcons;
import com.igormaznitsa.ideamindmap.utils.IdeaUtils;
import com.igormaznitsa.mindmap.ide.commons.FilePathWithLine;
import com.igormaznitsa.mindmap.ide.commons.SwingUtils;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.panel.HasPreferredFocusComponent;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactory;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactoryProvider;
import com.intellij.openapi.vfs.LocalFileSystem;
import java.io.File;
import java.util.ResourceBundle;
import javax.swing.Box;
import javax.swing.JComponent;

public final class FileEditPanel extends javax.swing.JPanel implements HasPreferredFocusComponent {
  private static final Logger LOGGER = LoggerFactory.getLogger(FileEditPanel.class);
  private static final ResourceBundle BUNDLE = java.util.ResourceBundle.getBundle("i18n/Bundle");

  private static final UIComponentFactory UI_COMPO_FACTORY = UIComponentFactoryProvider.findInstance();
  private static final long serialVersionUID = -6683682013891751388L;
  private final File projectFolder;
  private final DialogProvider dialogProvider;
  private javax.swing.JButton buttonChooseFile;
  private javax.swing.JButton buttonReset;
  private javax.swing.JCheckBox checkBoxShowFileInSystem;
  private javax.swing.JLabel labelBrowseCurrentLink;
  private javax.swing.JPanel optionPanel;
  private javax.swing.JTextField textFieldFilePath;

  public FileEditPanel(final DialogProvider dialogProvider, final File projectFolder, final DataContainer initialData) {
    this.dialogProvider = dialogProvider;
    initComponents();
    this.projectFolder = projectFolder;
    this.textFieldFilePath.setText(initialData == null ? "" : initialData.getPathWithLine().toString());
    this.checkBoxShowFileInSystem.setSelected(initialData != null && initialData.isShowWithSystemTool());
  }

  public DataContainer getData() {
    return new DataContainer(this.textFieldFilePath.getText().trim(), this.checkBoxShowFileInSystem.isSelected());
  }

  @Override
  public JComponent getComponentPreferredForFocus() {
    return this.textFieldFilePath;
  }

  @SuppressWarnings("unchecked")
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    labelBrowseCurrentLink = UI_COMPO_FACTORY.makeLabel();
    textFieldFilePath = UI_COMPO_FACTORY.makeTextField();
    textFieldFilePath.setComponentPopupMenu(SwingUtils.addTextActions(UI_COMPO_FACTORY.makePopupMenu()));
    buttonChooseFile = UI_COMPO_FACTORY.makeButton();
    buttonReset = UI_COMPO_FACTORY.makeButton();
    optionPanel = UI_COMPO_FACTORY.makePanel();
    checkBoxShowFileInSystem = UI_COMPO_FACTORY.makeCheckBox();

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
    optionPanel.add(Box.createHorizontalStrut(96));
    optionPanel.add(checkBoxShowFileInSystem);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.ipadx = 2;
    add(optionPanel, gridBagConstraints);
  }

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

  public static final class DataContainer {

    private final FilePathWithLine pathWithLine;
    private final boolean showWithSystemTool;

    public DataContainer(final String path, final boolean showWithSystemTool) {
      this.pathWithLine = new FilePathWithLine(path);
      this.showWithSystemTool = showWithSystemTool;
    }

    public FilePathWithLine getPathWithLine() {
      return this.pathWithLine;
    }

    public boolean isValid() {
      try {
        return this.pathWithLine.isEmptyOrOnlySpaces() || new File(this.pathWithLine.getPath()).exists();
      } catch (Exception ex) {
        return false;
      }
    }

    public boolean isShowWithSystemTool() {
      return this.showWithSystemTool;
    }
  }
}
