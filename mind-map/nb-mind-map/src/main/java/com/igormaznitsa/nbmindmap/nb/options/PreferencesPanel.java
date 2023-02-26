/*
 * Copyright (C) 2015-2023 Igor A. Maznitsa
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

package com.igormaznitsa.nbmindmap.nb.options;

import static java.util.Objects.requireNonNull;

import com.igormaznitsa.mindmap.ide.commons.preferences.AbstractPreferencesPanel;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactory;
import com.igormaznitsa.nbmindmap.nb.swing.AboutPanel;
import com.igormaznitsa.nbmindmap.nb.swing.DonateButton;
import com.igormaznitsa.nbmindmap.utils.NbUtils;
import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

public class PreferencesPanel extends AbstractPreferencesPanel implements AdditionalPreferences {

  private final Consumer<PreferencesPanel> changeConsumer;
  private JCheckBox checkboxTrimTopicText;
  private JCheckBox checkboxPreferInternalBrowser;
  private JCheckBox checkboxUseRelativePaths;
  private JCheckBox checkboxUnfoldDropTarget;
  private JCheckBox checkboxCopyParentColors;
  private JCheckBox checkboxEnableAutoCreateKnowledgeFolder;
  private JCheckBox checkboxWatchFileRefactoring;
  private JCheckBox checkboxTurnOffProcessingWhereUsedRefactoring;

  private JLabel labelIdeShouldBeReload;

  public PreferencesPanel(
      @Nonnull final UIComponentFactory uiComponentFactory,
      @Nonnull final DialogProvider dialogProvider,
      @Nonnull final Consumer<PreferencesPanel> changeConsumer
  ) {
    super(uiComponentFactory, dialogProvider);
    this.changeConsumer = changeConsumer;
  }

  @Override
  protected void beforePanelsCreate(UIComponentFactory uiComponentFactory) {
    final ResourceBundle bundle = ResourceBundle.getBundle("com.igormaznitsa.nbmindmap.i18n.Bundle");

    this.checkboxTrimTopicText = uiComponentFactory.makeCheckBox();
    this.checkboxTrimTopicText.setText(bundle.getString("MMDCfgPanel.checkboxTrimTopicText.text"));

    this.checkboxPreferInternalBrowser = uiComponentFactory.makeCheckBox();
    this.checkboxPreferInternalBrowser.setText("MMDCfgPanel.checkboxUseInsideBrowser.text");

    this.checkboxUseRelativePaths = uiComponentFactory.makeCheckBox();
    this.checkboxUseRelativePaths.setText(bundle.getString("MMDCfgPanel.checkboxRelativePathsForFilesInTheProject.text"));

    this.checkboxUnfoldDropTarget = uiComponentFactory.makeCheckBox();
    this.checkboxUnfoldDropTarget.setText(bundle.getString("MMDCfgPanel.checkBoxUnfoldCollapsedTarget.text"));

    this.checkboxCopyParentColors = uiComponentFactory.makeCheckBox();
    this.checkboxCopyParentColors.setText(bundle.getString("MMDCfgPanel.checkBoxCopyColorInfoToNewAllowed.text"));

    this.checkboxEnableAutoCreateKnowledgeFolder = uiComponentFactory.makeCheckBox();
    this.checkboxEnableAutoCreateKnowledgeFolder.setText(bundle.getString("MMDCfgPanel.checkBoxKnowledgeFolderAutogenerationAllowed.text"));

    this.checkboxWatchFileRefactoring = uiComponentFactory.makeCheckBox();
    this.checkboxWatchFileRefactoring.setText(bundle.getString("MMDCfgPanel.checkboxWatchFileRefactoring.text"));

    this.checkboxTurnOffProcessingWhereUsedRefactoring = uiComponentFactory.makeCheckBox();
    this.checkboxTurnOffProcessingWhereUsedRefactoring.setText(bundle.getString("MMDCfgPanel.checkboxTurnOffProcessingWhereUsedRefactoring.text"));

    this.labelIdeShouldBeReload = uiComponentFactory.makeLabel();
    this.labelIdeShouldBeReload.setHorizontalAlignment(JLabel.CENTER);
    this.labelIdeShouldBeReload.setForeground(Color.RED);
    this.labelIdeShouldBeReload.setText(bundle.getString("MMDCfgPanel.labelIdeShouldBeReload.text"));
  }

  @Override
  protected boolean isFeatureAndMiscAsNewColumn() {
    return true;
  }

  @Override
  protected void onPossibleChangeNotify(@Nonnull final Component source) {
    this.changeConsumer.accept(this);
  }

  private void doShowAbout() {
    NbUtils.plainMessageOk(null,
        ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18n/Bundle")
            .getString("MMDCfgPanel.buttonAbout.Text"), new AboutPanel());
  }


  @Override
  public List<ButtonInfo> findButtonInfo(@Nonnull final UIComponentFactory componentFactory,
                                         @Nonnull final DialogProvider dialogProvider) {

    final ResourceBundle bundle = ResourceBundle.getBundle("com.igormaznitsa.nbmindmap.i18n.Bundle");
    final List<ButtonInfo> list = new ArrayList<>();

    list.add(ButtonInfo.from(this.loadImage("info16.png"), bundle.getString("MMDCfgPanel.buttonAbout.Text"), e -> this.doShowAbout()));
    list.add(ButtonInfo.from(null, null, null, null, DonateButton::new));
    list.add(ButtonInfo.splitter());
    list.add(ButtonInfo.from(this.loadImage("document_export16.png"), bundle.getString("MMDCfgPanel.buttonExport.Text"),
        e -> this.exportAsFileDialog(() -> SwingUtilities.getWindowAncestor(this.getPanel()))));
    list.add(ButtonInfo.from(this.loadImage("document_import16.png"), bundle.getString("MMDCfgPanel.buttonImport.Text"),
        e -> this.importFromFileDialog(() -> SwingUtilities.getWindowAncestor(this.getPanel()))));
    list.add(ButtonInfo.splitter());
    list.add(ButtonInfo.from(this.loadImage("stop16.png"), bundle.getString("MMDCfgPanel.buttonReset.Text"),
        e -> this.fillByDefault()));

    return list;
  }

  @Nullable
  private Image loadImage(final String icon) {
    try {
      return ImageIO.read(
          requireNonNull(getClass().getResource("/com/igormaznitsa/nbmindmap/icons/" + icon)));
    } catch (Exception ex) {
      LOGGER.error("Error during load image: " + icon, ex);
      return null;
    }
  }

  private void fillByDefault() {
    load(new MindMapPanelConfig());
    this.changeConsumer.accept(this);
  }


  @Override
  public List<JComponent> findMiscComponents(UIComponentFactory componentFactory) {
    final List<JComponent> list = new ArrayList<>();
    return list;
  }

  @Override
  public List<JComponent> findFeaturesComponents(UIComponentFactory componentFactory) {
    final List<JComponent> list = new ArrayList<>();
    list.add(this.checkboxTrimTopicText);
    list.add(this.checkboxPreferInternalBrowser);
    list.add(this.checkboxUseRelativePaths);
    list.add(this.checkboxUnfoldDropTarget);
    list.add(this.checkboxCopyParentColors);
    list.add(this.checkboxEnableAutoCreateKnowledgeFolder);
    list.add(this.checkboxWatchFileRefactoring);
    list.add(this.checkboxTurnOffProcessingWhereUsedRefactoring);
    list.add(this.labelIdeShouldBeReload);

    return list;
  }

  @Override
  public void onSave(@Nonnull final MindMapPanelConfig config) {
    config.setOptionalProperty(PROPERTY_TRIM_TOPIC_TEXT, this.checkboxTrimTopicText.isSelected());
    config.setOptionalProperty(PROPERTY_USE_INTERNAL_BROWSER, this.checkboxPreferInternalBrowser.isSelected());
    config.setOptionalProperty(PROPERTY_MAKE_RELATIVE_PATHS_TO_PROJECT_ROOT, this.checkboxUseRelativePaths.isSelected());
    config.setOptionalProperty(PROPERTY_UNFOLD_COLLAPSED_DROP_TARGET, this.checkboxUnfoldDropTarget.isSelected());
    config.setOptionalProperty(PROPERTY_COPY_PARENT_COLORS_TO_NEW_CHILD, this.checkboxCopyParentColors.isSelected());
    config.setOptionalProperty(PROPERTY_KNOWLEDGE_FOLDER_ALLOWED, this.checkboxEnableAutoCreateKnowledgeFolder.isSelected());
    config.setOptionalProperty(PROPERTY_WATCH_FILE_REFACTORING, this.checkboxWatchFileRefactoring.isSelected());
    config.setOptionalProperty(PROPERTY_TURN_OFF_PROCESSING_WHERE_USED, this.checkboxTurnOffProcessingWhereUsedRefactoring.isSelected());
  }

  @Override
  public void onLoad(@Nonnull final MindMapPanelConfig config) {
    this.checkboxTrimTopicText.setSelected(config.getOptionalProperty(PROPERTY_TRIM_TOPIC_TEXT, false));
    this.checkboxPreferInternalBrowser.setSelected(config.getOptionalProperty(PROPERTY_USE_INTERNAL_BROWSER, false));
    this.checkboxUseRelativePaths.setSelected(config.getOptionalProperty(PROPERTY_MAKE_RELATIVE_PATHS_TO_PROJECT_ROOT, true));
    this.checkboxUnfoldDropTarget.setSelected(config.getOptionalProperty(PROPERTY_UNFOLD_COLLAPSED_DROP_TARGET, true));
    this.checkboxCopyParentColors.setSelected(config.getOptionalProperty(PROPERTY_COPY_PARENT_COLORS_TO_NEW_CHILD, true));
    this.checkboxEnableAutoCreateKnowledgeFolder.setSelected(config.getOptionalProperty(PROPERTY_KNOWLEDGE_FOLDER_ALLOWED, false));
    this.checkboxWatchFileRefactoring.setSelected(config.getOptionalProperty(PROPERTY_WATCH_FILE_REFACTORING, false));
    this.checkboxTurnOffProcessingWhereUsedRefactoring.setSelected(config.getOptionalProperty(PROPERTY_TURN_OFF_PROCESSING_WHERE_USED, false));
  }
}
