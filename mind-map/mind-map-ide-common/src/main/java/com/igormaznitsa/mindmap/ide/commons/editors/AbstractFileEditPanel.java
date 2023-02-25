package com.igormaznitsa.mindmap.ide.commons.editors;

import com.igormaznitsa.mindmap.ide.commons.FilePathWithLine;
import com.igormaznitsa.mindmap.ide.commons.SwingUtils;
import com.igormaznitsa.mindmap.ide.commons.preferences.MmcI18n;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.panel.HasPreferredFocusComponent;
import com.igormaznitsa.mindmap.swing.panel.utils.Focuser;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactory;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactoryProvider;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ResourceBundle;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

public abstract class AbstractFileEditPanel implements HasPreferredFocusComponent {

  protected final File projectFolder;
  protected final UIComponentFactory uiComponentFactory;
  protected final DialogProvider dialogProvider;
  private final ResourceBundle resourceBundle;
  private JButton buttonChooseFile;
  private JButton buttonReset;
  private JCheckBox checkBoxShowFileInSystem;
  private JLabel labelBrowseCurrentLink;
  private JPanel optionPanel;
  private JTextField textFieldFilePath;

  private JPanel panel;

  public AbstractFileEditPanel(
      final UIComponentFactory uiComponentFactory,
      final DialogProvider dialogProvider,
      final File projectFolder,
      final DataContainer initialData
  ) {
    this.uiComponentFactory = uiComponentFactory;
    this.dialogProvider = dialogProvider;
    this.resourceBundle = MmcI18n.getInstance().findBundle();

    initComponents();
    this.projectFolder = projectFolder;
    this.textFieldFilePath.setText(initialData.getFilePathWithLine().toString());
    this.checkBoxShowFileInSystem.setSelected(
        initialData.isShowWithSystemTool());
    this.textFieldFilePath.setComponentPopupMenu(
        SwingUtils.addTextActions(UIComponentFactoryProvider.findInstance().makePopupMenu()));
    new Focuser(this.textFieldFilePath);
  }

  @Override
  public JComponent getComponentPreferredForFocus() {
    return this.textFieldFilePath;
  }

  public JPanel getPanel() {
    return this.panel;
  }

  protected Icon findIcon(final IconId id) {
    return null;
  }

  public DataContainer getData() {
    return new DataContainer(this.textFieldFilePath.getText().trim(),
        this.checkBoxShowFileInSystem.isSelected());
  }

  private void initComponents() {
    this.panel = this.uiComponentFactory.makePanel();

    GridBagConstraints gridBagConstraints;

    this.labelBrowseCurrentLink = this.uiComponentFactory.makeLabel();
    this.textFieldFilePath = this.uiComponentFactory.makeTextField();
    this.textFieldFilePath.setColumns(24);

    this.buttonChooseFile = this.uiComponentFactory.makeButton();
    this.buttonReset = this.uiComponentFactory.makeButton();
    this.optionPanel = this.uiComponentFactory.makePanel();
    this.checkBoxShowFileInSystem = this.uiComponentFactory.makeCheckBox();

    this.panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 1, 10));
    this.panel.setLayout(new java.awt.GridBagLayout());

    this.labelBrowseCurrentLink.setIcon(this.findIcon(IconId.LABEL_BROWSE));
    this.labelBrowseCurrentLink.setToolTipText(
        this.resourceBundle.getString("panelFileEdit.clickIcon.tooltip"));
    this.labelBrowseCurrentLink.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    this.labelBrowseCurrentLink.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    this.labelBrowseCurrentLink.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        labelBrowseCurrentLinkMouseClicked(evt);
      }
    });
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    gridBagConstraints.ipadx = 10;
    this.panel.add(this.labelBrowseCurrentLink, gridBagConstraints);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1000.0;
    this.panel.add(this.textFieldFilePath, gridBagConstraints);

    this.buttonChooseFile.setIcon(this.findIcon(IconId.BUTTON_CHOOSE));
    this.buttonChooseFile.setToolTipText(
        this.resourceBundle.getString("panelFileEdit.tooltipSelectFile"));
    this.buttonChooseFile.addActionListener(this::buttonChooseFileActionPerformed);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    this.panel.add(this.buttonChooseFile, gridBagConstraints);

    this.buttonReset.setIcon(this.findIcon(IconId.BUTTON_RESET));
    this.buttonReset.setToolTipText(
        this.resourceBundle.getString("panelFileEdit.tooltipClearValue"));
    this.buttonReset.addActionListener(this::buttonResetActionPerformed);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    this.panel.add(buttonReset, gridBagConstraints);

    this.optionPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

    this.checkBoxShowFileInSystem.setText(
        this.resourceBundle.getString("panelFileEdit.checkboxOpenInSystemBrowser"));
    this.optionPanel.add(this.checkBoxShowFileInSystem);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 4;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    gridBagConstraints.ipadx = 2;
    this.panel.add(this.optionPanel, gridBagConstraints);
  }

  private void labelBrowseCurrentLinkMouseClicked(java.awt.event.MouseEvent evt) {
    if (evt.getClickCount() > 1) {
      final File file = new File(this.textFieldFilePath.getText().trim());
      this.openFileInSystemViewer(file);
    }
  }

  protected abstract void openFileInSystemViewer(final File file);

  private void buttonChooseFileActionPerformed(final ActionEvent evt) {
    final File theFile = new File(this.textFieldFilePath.getText().trim());
    final File parent = theFile.getParentFile();

    final File file = this.dialogProvider.msgOpenFileDialog(this.panel, null, this.getClass().getName(),
        this.resourceBundle.getString("FileEditPanel.fileChooser.title"),
        this.projectFolder, false, new FileFilter[0],
        this.resourceBundle.getString("FileEditPanel.fileChooser.approve"));
    if (file != null) {
      this.textFieldFilePath.setText(file.getAbsolutePath());
    }
  }

  private void buttonResetActionPerformed(ActionEvent evt) {
    this.textFieldFilePath.setText("");
  }

  public enum IconId {
    LABEL_BROWSE,
    BUTTON_CHOOSE,
    BUTTON_RESET,
  }

  public static final class DataContainer {

    private final FilePathWithLine filePathWithLine;
    private final boolean showWithSystemTool;

    public DataContainer(final String path, final boolean showWithSystemTool) {
      this.filePathWithLine = new FilePathWithLine(path);
      this.showWithSystemTool = showWithSystemTool;
    }

    public FilePathWithLine getFilePathWithLine() {
      return this.filePathWithLine;
    }

    public boolean isShowWithSystemTool() {
      return this.showWithSystemTool;
    }

    public boolean isEmptyOrOnlySpaces() {
      return this.filePathWithLine.isEmptyOrOnlySpaces();
    }

    public boolean isValid() {
      try {
        return this.filePathWithLine.isEmptyOrOnlySpaces() ||
            new File(this.filePathWithLine.getPath()).exists();
      } catch (Exception ex) {
        return false;
      }
    }

  }
}
