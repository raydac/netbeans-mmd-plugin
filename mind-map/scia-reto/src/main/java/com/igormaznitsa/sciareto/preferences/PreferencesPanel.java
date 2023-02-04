/*
 * Copyright (C) 2015-2022 Igor A. Maznitsa
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package com.igormaznitsa.sciareto.preferences;

import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.mindmap.ide.commons.preferences.AbstractPreferencesPanel;
import com.igormaznitsa.mindmap.ide.commons.preferences.FontSelectPanel;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.mindmap.swing.panel.utils.PropertiesPreferences;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactory;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactoryProvider;
import com.igormaznitsa.sciareto.SciaRetoStarter;
import com.igormaznitsa.sciareto.ui.SrI18n;
import com.igormaznitsa.sciareto.ui.UiUtils;
import com.igormaznitsa.sciareto.ui.editors.PlantUmlSecurityProfile;
import com.igormaznitsa.sciareto.ui.editors.ScalableRsyntaxTextArea;
import com.igormaznitsa.sciareto.ui.misc.AboutPanel;
import com.igormaznitsa.sciareto.ui.misc.DonateButton;
import com.igormaznitsa.sciareto.ui.misc.SysFileExtensionEditorPanel;
import java.awt.BorderLayout;
import java.awt.Image;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import org.apache.commons.io.FileUtils;

public final class PreferencesPanel extends AbstractPreferencesPanel
    implements AdditionalPreferences {

  private static final Logger LOGGER = LoggerFactory.getLogger(PreferencesPanel.class);
  private FontSelectPanel fontSelectPanelTextEditor;
  private JComboBox<PlantUmlSecurityProfile> comboBoxPlantUmlSecurityProfile;
  private JCheckBox checkBoxShowHiddenFiles;
  private JCheckBox checkboxTrimTopicText;
  private JCheckBox checkboxUseInsideBrowser;
  private JCheckBox checkboxEnableMetricsUpload;
  private JCheckBox checkboxBackupLastEdit;
  private JCheckBox checkboxRelativePathsForFilesInTheProject;

  private JCheckBox checkBoxUnfoldCollapsedTarget;
  private JCheckBox checkBoxCopyColorInfoToNewAllowed;
  private JCheckBox checkBoxKnowledgeFolderAutogenerationAllowed;
  private JTextField textFieldGraphvizDotPath;

  private String systemFileExtensions;

  public PreferencesPanel(@Nonnull final UIComponentFactory uiComponentFactory,
                          @Nonnull final DialogProvider dialogProvider) {
    super(uiComponentFactory, dialogProvider);
  }

  @Override
  protected void beforePanelsCreate(@Nonnull final UIComponentFactory uiComponentFactory) {
    final ResourceBundle bundle = SrI18n.getInstance().findBundle();
    this.fontSelectPanelTextEditor = new FontSelectPanel(this::getPanel,
        bundle.getString("PreferencesPanel.FontForEditor.title"), uiComponentFactory,
        dialogProvider, ScalableRsyntaxTextArea.DEFAULT_FONT);

    this.textFieldGraphvizDotPath = uiComponentFactory.makeTextField();
    this.textFieldGraphvizDotPath.setColumns(16);

    this.comboBoxPlantUmlSecurityProfile =
        uiComponentFactory.makeComboBox(PlantUmlSecurityProfile.class);
    this.comboBoxPlantUmlSecurityProfile.setModel(
        new DefaultComboBoxModel<>(PlantUmlSecurityProfile.values()));

    this.checkBoxShowHiddenFiles = uiComponentFactory.makeCheckBox();
    this.checkBoxShowHiddenFiles.setText(bundle.getString("PreferencesPanel.checkShowHiddenFiles"));

    this.checkboxTrimTopicText = uiComponentFactory.makeCheckBox();
    this.checkboxTrimTopicText.setText(bundle.getString("PreferencesPanel.checkTrimTopicText"));

    this.checkboxUseInsideBrowser = uiComponentFactory.makeCheckBox();
    this.checkboxUseInsideBrowser.setText(
        bundle.getString("PreferencesPanel.checkPreferInternalBrowser"));

    this.checkboxEnableMetricsUpload = uiComponentFactory.makeCheckBox();
    this.checkboxEnableMetricsUpload.setText(
        bundle.getString("PreferencesPanel.checkEnableMetricsUpload"));

    this.checkboxBackupLastEdit = uiComponentFactory.makeCheckBox();
    this.checkboxBackupLastEdit.setText(
        bundle.getString("PreferencesPanel.checkAutoBackupLastEdit"));

    this.checkboxRelativePathsForFilesInTheProject = uiComponentFactory.makeCheckBox();
    this.checkboxRelativePathsForFilesInTheProject.setText(
        bundle.getString("PreferencesPanel.checkUseRelativePaths"));

    this.checkBoxUnfoldCollapsedTarget = uiComponentFactory.makeCheckBox();
    this.checkBoxUnfoldCollapsedTarget.setText(
        bundle.getString("PreferencesPanel.checkUnfoldCollapsedDropTarget"));

    this.checkBoxCopyColorInfoToNewAllowed = uiComponentFactory.makeCheckBox();
    this.checkBoxCopyColorInfoToNewAllowed.setText(
        bundle.getString("PreferencesPanel.checkCopyParentColorIntoNewChild"));

    this.checkBoxKnowledgeFolderAutogenerationAllowed = uiComponentFactory.makeCheckBox();
    this.checkBoxKnowledgeFolderAutogenerationAllowed.setText(
        bundle.getString("PreferencesPanel.checkEnableAutocreationKnowledgeFolder"));
  }

  @Nonnull
  @MustNotContainNull
  @Override
  protected List<JComponent> findFontAndKeyboardExtras(
      @Nonnull final UIComponentFactory uiComponentFactory) {
    final ResourceBundle bundle = SrI18n.getInstance().findBundle();
    final List<JComponent> result = new ArrayList<>();

    JPanel panel = uiComponentFactory.makePanel();
    panel.setLayout(new BorderLayout());
    panel.setBorder(
        BorderFactory.createTitledBorder(bundle.getString("PreferencesPanel.FontForEditor.title")));
    panel.add(fontSelectPanelTextEditor.asButton());

    result.add(panel);

    return result;
  }

  @Nullable
  private static Image loadIcon(@Nonnull final String path) {
    try {
      return ImageIO.read(
          Objects.requireNonNull(PreferencesPanel.class.getResource(path), "Can't find " + path));
    } catch (Exception ex) {
      LOGGER.error("Error during icon load: " + path, ex);
      return null;
    }
  }

  private void fillByDefault() {
    this.systemFileExtensions = null;
    load(new MindMapPanelConfig());
  }

  private void showAbout() {
    final JPanel aboutPanel = new AboutPanel();
    UiUtils.makeOwningDialogResizable(aboutPanel);
    final JScrollPane scrollPane = UIComponentFactoryProvider.findInstance().makeScrollPane();
    scrollPane.setViewportView(aboutPanel);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    JOptionPane.showMessageDialog(
        this.getPanel(), scrollPane,
        SrI18n.getInstance().findBundle().getString("PreferencesPanel.msgAbout.title"),
        JOptionPane.PLAIN_MESSAGE);
  }

  private void doExportIntoFile(@Nonnull final ResourceBundle bundle) {
    File file = this.dialogProvider
        .msgSaveFileDialog(this.getPanel(), null, "exportProperties",
            bundle.getString("PreferencesPanel.ExportProperties.title"), null, true,
            new FileFilter[] {new PropertiesFileFilter()},
            SrI18n.getInstance().findBundle()
                .getString("PreferencesPanel.ExportProperties.approve"));
    if (file != null) {
      if (!file.getName().toLowerCase(Locale.ENGLISH).endsWith(".properties")) { //NOI18N
        final Boolean addExt = this.dialogProvider
            .msgConfirmYesNoCancel(this.getPanel(),
                bundle.getString("PreferencesPanel.AddExtension.title"),
                bundle.getString("PreferencesPanel.AddExtension.question"));
        if (addExt == null) {
          return;
        }
        if (addExt) {
          file = new File(file.getAbsolutePath() + ".properties"); //NOI18N
        }
      }

      if (file.exists() && !this.dialogProvider
          .msgConfirmOkCancel(this.getPanel(),
              bundle.getString("PreferencesPanel.OverrideFile.title"),
              String.format(bundle.getString("PreferencesPanel.OverrideFile.question"),
                  file.getName()))) {
        return;
      }

      final PropertiesPreferences prefs = new PropertiesPreferences("SciaReto editor settings");
      final MindMapPanelConfig cfg = save();
      cfg.saveTo(prefs);
      try {
        FileUtils.write(file, prefs.toString(), StandardCharsets.UTF_8);
      } catch (final Exception ex) {
        LOGGER.error("Can't export settings", ex); //NOI18N
        this.dialogProvider.msgError(this.getPanel(),
            SrI18n.getInstance().findBundle().getString("PreferencesPanel.ExportProperties.error") +
                ex.getMessage()); //NOI18N
      }
    }
  }

  private void doImportFromFile(@Nonnull final ResourceBundle bundle) {
    final File file = this.dialogProvider
        .msgOpenFileDialog(this.getPanel(), null, "importProperties",
            bundle.getString("PreferencesPanel.ImportSettings.title"), null, true,
            new FileFilter[] {new PropertiesFileFilter()},
            bundle.getString("PreferencesPanel.ImportSettings.approve"));
    if (file != null) {
      try {
        final MindMapPanelConfig loadedConfig = new MindMapPanelConfig();
        loadedConfig.loadFrom(new PropertiesPreferences("SciaReto",
            FileUtils.readFileToString(file, StandardCharsets.UTF_8)));
        load(loadedConfig);
      } catch (final Exception ex) {
        LOGGER.error("Can't import settings", ex);
        this.dialogProvider
            .msgError(SciaRetoStarter.getApplicationFrame(),
                SrI18n.getInstance().findBundle()
                    .getString("PreferencesPanel.ImportSettings.error") + ex.getMessage());
      }
    }
  }


  @Nonnull
  @MustNotContainNull
  @Override
  public List<ButtonInfo> findButtonInfo() {
    final ResourceBundle bundle = SrI18n.getInstance().findBundle();
    final List<ButtonInfo> list = new ArrayList<>();

    list.add(ButtonInfo.from(null, bundle.getString("PreferencesPanel.buttonDonate"),
        bundle.getString("PreferencesPanel.buttonDonate.tooltip"), null, DonateButton::new));
    list.add(ButtonInfo.from(loadIcon("/icons/info16.png"),
        bundle.getString("PreferencesPanel.buttonAbout"), a -> this.showAbout()));

    list.add(ButtonInfo.splitter());

    list.add(ButtonInfo.from(loadIcon("/icons/document_export16.png"),
        bundle.getString("PreferencesPanel.buttonExportToFile"),
        a -> this.doExportIntoFile(bundle)));

    list.add(ButtonInfo.from(loadIcon("/icons/document_import16.png"),
        bundle.getString("PreferencesPanel.buttonImportFromFile"),
        a -> this.doImportFromFile(bundle)));

    list.add(ButtonInfo.from(loadIcon("/icons/file_manager.png"),
        bundle.getString("PreferencesPanel.buttonSystemFileExtensions"),
        bundle.getString("PreferencesPanel.buttonSystemFileExtensions.tooltip"),
        a -> this.editExtensionsOpenInSystem(bundle)));

    list.add(ButtonInfo.splitter());

    list.add(ButtonInfo.from(loadIcon("/menu_icons/stop.png"),
        bundle.getString("PreferencesPanel.buttonResetToDefault"), a -> this.fillByDefault()));

    return list;
  }

  private void editExtensionsOpenInSystem(@Nonnull final ResourceBundle bundle) {
    final SysFileExtensionEditorPanel
        dataPanel = new SysFileExtensionEditorPanel(this.systemFileExtensions);
    if (this.dialogProvider.msgOkCancel(this.getPanel(),
        bundle.getString("PreferencesPanel.SystemFileExtensions.title"), dataPanel)) {
      this.systemFileExtensions = dataPanel.getValuerNullIfDefault();
    }
  }

  @Nonnull
  @MustNotContainNull
  @Override
  public List<JComponent> findMiscComponents(@Nonnull final UIComponentFactory componentFactory) {
    final ResourceBundle bundle = SrI18n.getInstance().findBundle();
    final List<JComponent> result = new ArrayList<>();

    JPanel panel = componentFactory.makePanel();
    panel.setLayout(new BorderLayout());
    panel.setBorder(BorderFactory.createTitledBorder(
        bundle.getString("PreferencesPanel.panelPlantUmlSecurity.title")));
    panel.add(this.comboBoxPlantUmlSecurityProfile);
    result.add(panel);

    panel = componentFactory.makePanel();
    panel.setLayout(new BorderLayout());
    panel.setBorder(BorderFactory.createTitledBorder(
        bundle.getString("PreferencesPanel.panelGraphvizDot.title")));

    panel.add(this.textFieldGraphvizDotPath, BorderLayout.CENTER);
    final JButton buttonSelectPath = componentFactory.makeButton();
    buttonSelectPath.setToolTipText("Select path to DOT executable file");
    buttonSelectPath.setText("...");
    buttonSelectPath.addActionListener(a -> {
      final JFileChooser fileChooser = new JFileChooser(this.textFieldGraphvizDotPath.getText());
      fileChooser.setDialogTitle(bundle.getString("PreferencesPanel.SelectGraphVizDot.title"));
      fileChooser.setApproveButtonText(
          bundle.getString("PreferencesPanel.SelectGraphVizDot.approve"));
      fileChooser.setAcceptAllFileFilterUsed(true);
      fileChooser.setMultiSelectionEnabled(false);

      fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

      if (fileChooser.showOpenDialog(this.getPanel()) == JFileChooser.APPROVE_OPTION) {
        final File file = fileChooser.getSelectedFile();
        this.textFieldGraphvizDotPath.setText(file.getAbsolutePath());
      }
    });
    panel.add(buttonSelectPath, BorderLayout.EAST);

    result.add(panel);

    return result;
  }

  @Nonnull
  @MustNotContainNull
  @Override
  public List<JComponent> findFeaturesComponents(
      @Nonnull final UIComponentFactory componentFactory) {
    final List<JComponent> components = new ArrayList<>();

    components.add(this.checkBoxShowHiddenFiles);
    components.add(this.checkboxTrimTopicText);
    components.add(this.checkboxUseInsideBrowser);
    components.add(this.checkboxEnableMetricsUpload);
    components.add(this.checkboxBackupLastEdit);
    components.add(this.checkboxRelativePathsForFilesInTheProject);
    components.add(this.checkBoxUnfoldCollapsedTarget);
    components.add(this.checkBoxCopyColorInfoToNewAllowed);
    components.add(this.checkBoxKnowledgeFolderAutogenerationAllowed);

    return components;
  }

  @Override
  public void onSave(@Nonnull final MindMapPanelConfig config) {
    config.setOptionalProperty(PROPERTY_TEXT_EDITOR_FONT,
        this.fontSelectPanelTextEditor.getValue());
    config.setOptionalProperty(PROPERTY_PLANTUML_SECURITY_PROFILE,
        (PlantUmlSecurityProfile) this.comboBoxPlantUmlSecurityProfile.getSelectedItem());

    config.setOptionalProperty(PROPERTY_SHOW_HIDDEN_FILES,
        this.checkBoxShowHiddenFiles.isSelected());
    config.setOptionalProperty(PROPERTY_BACKUP_LAST_EDIT_BEFORE_SAVE,
        this.checkboxBackupLastEdit.isSelected());
    config.setOptionalProperty(PROPERTY_TRIM_TOPIC_TEXT, this.checkboxTrimTopicText.isSelected());
    config.setOptionalProperty(PROPERTY_USE_INTERNAL_BROWSER,
        this.checkboxUseInsideBrowser.isSelected());
    config.setOptionalProperty(PROPERTY_METRICS_SENDING_FLAG,
        this.checkboxEnableMetricsUpload.isSelected());
    config.setOptionalProperty(PROPERTY_MAKE_RELATIVE_PATHS_TO_PROJECT_ROOT,
        this.checkboxRelativePathsForFilesInTheProject.isSelected());
    config.setOptionalProperty(PROPERTY_UNFOLD_COLLAPSED_DROP_TARGET,
        this.checkBoxUnfoldCollapsedTarget.isSelected());
    config.setOptionalProperty(PROPERTY_COPY_PARENT_COLORS_TO_NEW_CHILD,
        this.checkBoxCopyColorInfoToNewAllowed.isSelected());
    config.setOptionalProperty(PROPERTY_KNOWLEDGE_FOLDER_ALLOWED,
        this.checkBoxKnowledgeFolderAutogenerationAllowed.isSelected());

    config.setOptionalProperty(PROPERTY_EXTENSIONS_TO_BE_OPENED_IN_SYSTEM,
        this.systemFileExtensions);

    final String pathToDotFile = this.textFieldGraphvizDotPath.getText();
    config.setOptionalProperty(PROPERTY_PLANTUML_DOT_PATH,
        pathToDotFile.trim().isEmpty() ? null : pathToDotFile);

    SystemFileExtensionManager.getInstance()
        .setExtensionsAsCommaSeparatedString(this.systemFileExtensions);
  }

  @Override
  public void onLoad(final MindMapPanelConfig config) {
    this.textFieldGraphvizDotPath.setText(
        config.getOptionalProperty(PROPERTY_PLANTUML_DOT_PATH, ""));

    this.fontSelectPanelTextEditor.setValue(config.getOptionalProperty(PROPERTY_TEXT_EDITOR_FONT,
        ScalableRsyntaxTextArea.DEFAULT_FONT));
    this.comboBoxPlantUmlSecurityProfile.setSelectedItem(
        config.getOptionalProperty(PROPERTY_PLANTUML_SECURITY_PROFILE,
            PlantUmlSecurityProfile.LEGACY));

    this.checkboxEnableMetricsUpload.setSelected(
        config.getOptionalProperty(PROPERTY_METRICS_SENDING_FLAG, true));

    this.checkboxBackupLastEdit.setSelected(
        config.getOptionalProperty(PROPERTY_BACKUP_LAST_EDIT_BEFORE_SAVE, true));

    this.checkBoxShowHiddenFiles.setSelected(
        config.getOptionalProperty(PROPERTY_SHOW_HIDDEN_FILES, true));
    this.checkboxTrimTopicText.setSelected(
        config.getOptionalProperty(PROPERTY_TRIM_TOPIC_TEXT, false));
    this.checkboxUseInsideBrowser.setSelected(
        config.getOptionalProperty(PROPERTY_USE_INTERNAL_BROWSER, true));
    this.checkboxRelativePathsForFilesInTheProject.setSelected(
        config.getOptionalProperty(PROPERTY_MAKE_RELATIVE_PATHS_TO_PROJECT_ROOT, true));
    this.checkBoxUnfoldCollapsedTarget.setSelected(
        config.getOptionalProperty(PROPERTY_UNFOLD_COLLAPSED_DROP_TARGET, true));
    this.checkBoxCopyColorInfoToNewAllowed.setSelected(
        config.getOptionalProperty(PROPERTY_COPY_PARENT_COLORS_TO_NEW_CHILD, true));
    this.checkBoxKnowledgeFolderAutogenerationAllowed.setSelected(
        config.getOptionalProperty(PROPERTY_KNOWLEDGE_FOLDER_ALLOWED, false));

    this.systemFileExtensions =
        config.getOptionalProperty(PROPERTY_EXTENSIONS_TO_BE_OPENED_IN_SYSTEM,
            SystemFileExtensionManager.getInstance()
                .getDefaultExtensionsAsCommaSeparatedString());

  }

  private static final class PropertiesFileFilter extends FileFilter {

    @Override
    public boolean accept(@Nonnull final File f) {
      return f.isDirectory() || f.getName().toLowerCase(Locale.ENGLISH).endsWith(".properties");
    }

    @Override
    public String getDescription() {
      return SrI18n.getInstance().findBundle()
          .getString("PreferencesPanel.fileChooser.filter.text");
    }

  }

}
