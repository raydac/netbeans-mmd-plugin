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

import com.igormaznitsa.meta.annotation.ReturnsOriginal;
import com.igormaznitsa.meta.common.utils.Assertions;
import com.igormaznitsa.meta.common.utils.GetUtils;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.mindmap.swing.panel.utils.KeyShortcut;
import com.igormaznitsa.mindmap.swing.panel.utils.MouseButton;
import com.igormaznitsa.mindmap.swing.panel.utils.PropertiesPreferences;
import com.igormaznitsa.mindmap.swing.panel.utils.RenderQuality;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactoryProvider;
import com.igormaznitsa.sciareto.Context;
import com.igormaznitsa.sciareto.SciaRetoStarter;
import com.igormaznitsa.sciareto.metrics.MetricsService;
import static com.igormaznitsa.sciareto.preferences.PrefUtils.PROPERTY_PLANTUML_DOT_PATH;
import static com.igormaznitsa.sciareto.preferences.PrefUtils.PROPERTY_PLANTUML_SECURITY_PROFILE;
import static java.util.Comparator.naturalOrder;

import com.igormaznitsa.sciareto.ui.DialogProviderManager;
import com.igormaznitsa.sciareto.ui.SrI18n;
import com.igormaznitsa.sciareto.ui.UiUtils;
import com.igormaznitsa.sciareto.ui.editors.PlantUmlSecurityProfile;
import com.igormaznitsa.sciareto.ui.editors.ScalableRsyntaxTextArea;
import com.igormaznitsa.sciareto.ui.editors.mmeditors.FontSelector;
import com.igormaznitsa.sciareto.ui.editors.mmeditors.KeyShortCutEditPanel;
import com.igormaznitsa.sciareto.ui.misc.AboutPanel;
import com.igormaznitsa.sciareto.ui.misc.SysFileExtensionEditorPanel;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;
import java.util.prefs.Preferences;

public final class PreferencesPanel extends javax.swing.JPanel {

  private static final long serialVersionUID = -1090601330630026253L;

  private static final Logger LOGGER = LoggerFactory.getLogger(PreferencesPanel.class);

  private static final class PropertiesFileFilter extends FileFilter {

    @Override
    public boolean accept(@Nonnull final File f) {
      return f.isDirectory() || f.getName().toLowerCase(Locale.ENGLISH).endsWith(".properties"); //NOI18N
    }

    @Override
    public String getDescription() {
        return SrI18n.getInstance().findBundle().getString("PreferencesPanel.fileChooser.filter.text");  //NOI18N
    }

  }

  private volatile boolean changeNotificationAllowed = true;

  private final Context context;

  public static final String PREFERENCE_KEY_KNOWLEDGEFOLDER_ALLOWED = "knowledgeFolderGenerationAllowed"; //NOI18N

  private boolean changed;

  private Font fontTextEditor = ScalableRsyntaxTextArea.DEFAULT_FONT;
  private Font fontMindMapEditor = ScalableRsyntaxTextArea.DEFAULT_FONT;

  private final MindMapPanelConfig config = new MindMapPanelConfig();
  private final transient Map<String, KeyShortcut> mapKeyShortCuts = new TreeMap<>(naturalOrder());

  public PreferencesPanel(final Context context) {
    this.context = context;
    initComponents();

    this.textFieldPathToGraphvizDot.getDocument().addDocumentListener(new DocumentListener() {

      private void onChange() {
        if (changeNotificationAllowed) {
          changed = true;
        }
      }

      @Override
      public void insertUpdate(@Nonnull final DocumentEvent e) {
        onChange();
      }

      @Override
      public void removeUpdate(@Nonnull final DocumentEvent e) {
        onChange();
      }

      @Override
      public void changedUpdate(@Nonnull final DocumentEvent e) {
        onChange();
      }
    });
  }

  public boolean isChanged() {
    return this.changed;
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel8 = new javax.swing.JLabel();
        MainScrollPanel = new javax.swing.JScrollPane();
        jPanel6 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        spinnerConnectorWidth = new javax.swing.JSpinner();
        jLabel5 = new javax.swing.JLabel();
        spinnerCollapsatorSize = new javax.swing.JSpinner();
        jLabel6 = new javax.swing.JLabel();
        spinnerCollapsatorWidth = new javax.swing.JSpinner();
        jLabel7 = new javax.swing.JLabel();
        spinnerJumpLinkWidth = new javax.swing.JSpinner();
        jPanel15 = new javax.swing.JPanel();
        colorChooserCollapsatorBackground = new com.igormaznitsa.sciareto.ui.misc.ColorChooserButton();
        colorChooserCollapsatorBorder = new com.igormaznitsa.sciareto.ui.misc.ColorChooserButton();
        colorChooserJumpLink = new com.igormaznitsa.sciareto.ui.misc.ColorChooserButton();
        colorChooserConnectorColor = new com.igormaznitsa.sciareto.ui.misc.ColorChooserButton();
        jPanel4 = new javax.swing.JPanel();
        colorChooserPaperColor = new com.igormaznitsa.sciareto.ui.misc.ColorChooserButton();
        checkBoxShowGrid = new javax.swing.JCheckBox();
        colorChooserGridColor = new com.igormaznitsa.sciareto.ui.misc.ColorChooserButton();
        spinnerGridStep = new javax.swing.JSpinner();
        jLabel1 = new javax.swing.JLabel();
        jPanel13 = new javax.swing.JPanel();
        comboBoxRenderQuality = new javax.swing.JComboBox<>();
        jPanel2 = new javax.swing.JPanel();
        colorChooserRootBackground = new com.igormaznitsa.sciareto.ui.misc.ColorChooserButton();
        colorChooserRootText = new com.igormaznitsa.sciareto.ui.misc.ColorChooserButton();
        colorChooser1stBackground = new com.igormaznitsa.sciareto.ui.misc.ColorChooserButton();
        colorChooser1stText = new com.igormaznitsa.sciareto.ui.misc.ColorChooserButton();
        colorChooser2ndBackground = new com.igormaznitsa.sciareto.ui.misc.ColorChooserButton();
        colorChooser2ndText = new com.igormaznitsa.sciareto.ui.misc.ColorChooserButton();
        jPanel5 = new javax.swing.JPanel();
        colorChooserSelectLine = new com.igormaznitsa.sciareto.ui.misc.ColorChooserButton();
        jLabel3 = new javax.swing.JLabel();
        spinnerSelectLineWidth = new javax.swing.JSpinner();
        jLabel4 = new javax.swing.JLabel();
        spinnerSelectLineGap = new javax.swing.JSpinner();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 32767));
        jPanel10 = new javax.swing.JPanel();
        buttonFontForEditor = new javax.swing.JButton();
        jPanel11 = new javax.swing.JPanel();
        checkBoxDropShadow = new javax.swing.JCheckBox();
        jPanel12 = new javax.swing.JPanel();
        spinnerElementBorderWidth = new javax.swing.JSpinner();
        labelBorderWidth = new javax.swing.JLabel();
        filler4 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 32767));
        panelGraphvizDot = new javax.swing.JPanel();
        textFieldPathToGraphvizDot = new javax.swing.JTextField();
        buttonGraphvizDotFile = new javax.swing.JButton();
        filler5 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 32767));
        jPanel16 = new javax.swing.JPanel();
        slider1stLevelHorzGap = new javax.swing.JSlider();
        jPanel17 = new javax.swing.JPanel();
        slider1stLevelVertGap = new javax.swing.JSlider();
        jPanel18 = new javax.swing.JPanel();
        slider2ndLevelHorzGap = new javax.swing.JSlider();
        jPanel19 = new javax.swing.JPanel();
        slider2ndLevelVertGap = new javax.swing.JSlider();
        panelPlantUmlSecurityProfile = new javax.swing.JPanel();
        comboBoxPlantUmlSecurityProfile = new javax.swing.JComboBox<>();
        jPanel1 = new javax.swing.JPanel();
        checkboxUseInsideBrowser = new javax.swing.JCheckBox();
        checkboxRelativePathsForFilesInTheProject = new javax.swing.JCheckBox();
        checkBoxUnfoldCollapsedTarget = new javax.swing.JCheckBox();
        checkBoxCopyColorInfoToNewAllowed = new javax.swing.JCheckBox();
        checkBoxKnowledgeFolderAutogenerationAllowed = new javax.swing.JCheckBox();
        jPanel7 = new javax.swing.JPanel();
        buttonFont = new javax.swing.JButton();
        jPanel8 = new javax.swing.JPanel();
        buttonOpenShortcutEditor = new javax.swing.JButton();
        panelScalingModifiers = new javax.swing.JPanel();
        checkBoxScalingCTRL = new javax.swing.JCheckBox();
        checkBoxScalingALT = new javax.swing.JCheckBox();
        checkBoxScalingSHIFT = new javax.swing.JCheckBox();
        checkBoxScalingMETA = new javax.swing.JCheckBox();
        filler8 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        jPanel14 = new javax.swing.JPanel();
        jPanel20 = new javax.swing.JPanel();
        labelBirdseyeMouse = new javax.swing.JLabel();
        comboBirdseyeMouse = new javax.swing.JComboBox<>();
        jPanel21 = new javax.swing.JPanel();
        checkBoxBirdsCTRL = new javax.swing.JCheckBox();
        checkBoxBirdsALT = new javax.swing.JCheckBox();
        checkBoxBirdsSHIFT = new javax.swing.JCheckBox();
        checkBoxBirdsMETA = new javax.swing.JCheckBox();
        filler9 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        checkboxMetricsAllowed = new javax.swing.JCheckBox();
        checkboxTrimTopicText = new javax.swing.JCheckBox();
        checkBoxShowHiddenFiles = new javax.swing.JCheckBox();
        checkBoxSmartTextPaste = new javax.swing.JCheckBox();
        checkBoxBackupLastEdit = new javax.swing.JCheckBox();
        jPanel9 = new javax.swing.JPanel();
        buttonAbout = new javax.swing.JButton();
        donateButton1 = new com.igormaznitsa.sciareto.ui.misc.DonateButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 32767));
        buttonResetToDefault = new javax.swing.JButton();
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 16), new java.awt.Dimension(0, 16), new java.awt.Dimension(32767, 16));
        buttonExportToFile = new javax.swing.JButton();
        buttonImportFromFile = new javax.swing.JButton();
        buttonExtensionsOpenInSystem = new javax.swing.JButton();
        filler6 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 16), new java.awt.Dimension(0, 16), new java.awt.Dimension(32767, 16));
        filler7 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 32767));

        jLabel8.setText("jLabel8");

        setLayout(new java.awt.BorderLayout());

        jPanel6.setLayout(new java.awt.GridBagLayout());

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.panelConnectorAndCollapsatorOptions.title"))); // NOI18N
        jPanel3.setLayout(new java.awt.GridBagLayout());

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.labelConnecorWidth")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel3.add(jLabel2, gridBagConstraints);

        spinnerConnectorWidth.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(0.1f), Float.valueOf(0.05f), Float.valueOf(20.0f), Float.valueOf(0.01f)));
        spinnerConnectorWidth.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerConnectorWidthStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 80;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanel3.add(spinnerConnectorWidth, gridBagConstraints);

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel5.setText(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.labelCollapsatorSize")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel3.add(jLabel5, gridBagConstraints);

        spinnerCollapsatorSize.setModel(new javax.swing.SpinnerNumberModel(5, 3, 500, 1));
        spinnerCollapsatorSize.setMinimumSize(new java.awt.Dimension(80, 0));
        spinnerCollapsatorSize.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerCollapsatorSizeStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanel3.add(spinnerCollapsatorSize, gridBagConstraints);

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel6.setText(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.labelCollapsatorWidth")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel3.add(jLabel6, gridBagConstraints);

        spinnerCollapsatorWidth.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(1.0f), Float.valueOf(0.01f), Float.valueOf(100.0f), Float.valueOf(0.1f)));
        spinnerCollapsatorWidth.setMinimumSize(new java.awt.Dimension(80, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanel3.add(spinnerCollapsatorWidth, gridBagConstraints);

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel7.setText(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.labelJumpLinkWidth")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel3.add(jLabel7, gridBagConstraints);

        spinnerJumpLinkWidth.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(0.1f), Float.valueOf(0.05f), Float.valueOf(20.0f), Float.valueOf(0.01f)));
        spinnerJumpLinkWidth.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerJumpLinkWidthStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 80;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanel3.add(spinnerJumpLinkWidth, gridBagConstraints);

        jPanel15.setLayout(new java.awt.GridLayout(2, 2));

        colorChooserCollapsatorBackground.setText(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.buttonCollapsatorFill")); // NOI18N
        colorChooserCollapsatorBackground.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        colorChooserCollapsatorBackground.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorChooserCollapsatorBackgroundActionPerformed(evt);
            }
        });
        jPanel15.add(colorChooserCollapsatorBackground);

        colorChooserCollapsatorBorder.setText(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.buttonCollapsatorBorder")); // NOI18N
        colorChooserCollapsatorBorder.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        colorChooserCollapsatorBorder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorChooserCollapsatorBorderActionPerformed(evt);
            }
        });
        jPanel15.add(colorChooserCollapsatorBorder);

        colorChooserJumpLink.setText(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.buttonJumpLink")); // NOI18N
        colorChooserJumpLink.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        colorChooserJumpLink.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorChooserJumpLinkActionPerformed(evt);
            }
        });
        jPanel15.add(colorChooserJumpLink);

        colorChooserConnectorColor.setText(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.buttonConnectorColor")); // NOI18N
        colorChooserConnectorColor.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        colorChooserConnectorColor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorChooserConnectorColorActionPerformed(evt);
            }
        });
        jPanel15.add(colorChooserConnectorColor);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1000.0;
        jPanel3.add(jPanel15, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel6.add(jPanel3, gridBagConstraints);

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.paperOptions.title"))); // NOI18N
        jPanel4.setLayout(new java.awt.GridBagLayout());

        colorChooserPaperColor.setText(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.buttonBackgroundFill")); // NOI18N
        colorChooserPaperColor.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        colorChooserPaperColor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorChooserPaperColorActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel4.add(colorChooserPaperColor, gridBagConstraints);

        checkBoxShowGrid.setText(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.checkboxShowGrid")); // NOI18N
        checkBoxShowGrid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxShowGridActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.ipady = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel4.add(checkBoxShowGrid, gridBagConstraints);

        colorChooserGridColor.setText(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.buttonGridColor")); // NOI18N
        colorChooserGridColor.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        colorChooserGridColor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorChooserGridColorActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel4.add(colorChooserGridColor, gridBagConstraints);

        spinnerGridStep.setModel(new javax.swing.SpinnerNumberModel(15, 2, 500, 1));
        spinnerGridStep.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerGridStepStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        jPanel4.add(spinnerGridStep, gridBagConstraints);

        jLabel1.setText(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.label.gridStep")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipady = 5;
        jPanel4.add(jLabel1, gridBagConstraints);

        jPanel13.setBorder(javax.swing.BorderFactory.createTitledBorder(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.panelRenderQuality.title"))); // NOI18N
        jPanel13.setLayout(new java.awt.BorderLayout());

        comboBoxRenderQuality.setModel(new DefaultComboBoxModel<RenderQuality>(RenderQuality.values()));
        comboBoxRenderQuality.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxRenderQualityActionPerformed(evt);
            }
        });
        jPanel13.add(comboBoxRenderQuality, java.awt.BorderLayout.NORTH);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1000.0;
        jPanel4.add(jPanel13, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 73;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel6.add(jPanel4, gridBagConstraints);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.panelElementOptrions.title"))); // NOI18N
        jPanel2.setLayout(new java.awt.GridBagLayout());

        colorChooserRootBackground.setText(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.buttonRootFill")); // NOI18N
        colorChooserRootBackground.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        colorChooserRootBackground.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorChooserRootBackgroundActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel2.add(colorChooserRootBackground, gridBagConstraints);

        colorChooserRootText.setText(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.buttonRootText")); // NOI18N
        colorChooserRootText.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        colorChooserRootText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorChooserRootTextActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel2.add(colorChooserRootText, gridBagConstraints);

        colorChooser1stBackground.setText(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.button1levelFill")); // NOI18N
        colorChooser1stBackground.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        colorChooser1stBackground.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorChooser1stBackgroundActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel2.add(colorChooser1stBackground, gridBagConstraints);

        colorChooser1stText.setText(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.button1levelTextl")); // NOI18N
        colorChooser1stText.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        colorChooser1stText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorChooser1stTextActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel2.add(colorChooser1stText, gridBagConstraints);

        colorChooser2ndBackground.setText(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.button2levelFill")); // NOI18N
        colorChooser2ndBackground.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        colorChooser2ndBackground.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorChooser2ndBackgroundActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel2.add(colorChooser2ndBackground, gridBagConstraints);

        colorChooser2ndText.setText(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.button2levelTextl")); // NOI18N
        colorChooser2ndText.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        colorChooser2ndText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorChooser2ndTextActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel2.add(colorChooser2ndText, gridBagConstraints);

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.panelSelectionFrame.title"))); // NOI18N
        jPanel5.setLayout(new java.awt.GridBagLayout());

        colorChooserSelectLine.setText(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.buttonSelectFrameColor")); // NOI18N
        colorChooserSelectLine.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        colorChooserSelectLine.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorChooserSelectLineActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel5.add(colorChooserSelectLine, gridBagConstraints);

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.labelSelectionFrameWidth")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel5.add(jLabel3, gridBagConstraints);

        spinnerSelectLineWidth.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(1.0f), Float.valueOf(0.02f), Float.valueOf(100.0f), Float.valueOf(0.1f)));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel5.add(spinnerSelectLineWidth, gridBagConstraints);

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel4.setText(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.labelSelectionFrameGap")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel5.add(jLabel4, gridBagConstraints);

        spinnerSelectLineGap.setModel(new javax.swing.SpinnerNumberModel(1, 1, 500, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel5.add(spinnerSelectLineGap, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel2.add(jPanel5, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weighty = 1000.0;
        jPanel2.add(filler2, gridBagConstraints);

        jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.panelTextEditorFont.title"))); // NOI18N
        jPanel10.setLayout(new java.awt.BorderLayout());

        buttonFontForEditor.setText("...");
        buttonFontForEditor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonFontForEditorActionPerformed(evt);
            }
        });
        jPanel10.add(buttonFontForEditor, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel2.add(jPanel10, gridBagConstraints);

        jPanel11.setLayout(new java.awt.BorderLayout());

        checkBoxDropShadow.setText(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.checkDropShadow")); // NOI18N
        checkBoxDropShadow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxDropShadowActionPerformed(evt);
            }
        });
        jPanel11.add(checkBoxDropShadow, java.awt.BorderLayout.CENTER);

        jPanel12.setLayout(new java.awt.BorderLayout());

        spinnerElementBorderWidth.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(0.5f), Float.valueOf(0.05f), Float.valueOf(50.0f), Float.valueOf(0.1f)));
        spinnerElementBorderWidth.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerElementBorderWidthStateChanged(evt);
            }
        });
        jPanel12.add(spinnerElementBorderWidth, java.awt.BorderLayout.CENTER);

        labelBorderWidth.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelBorderWidth.setText(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.labelBorderWidth")); // NOI18N
        jPanel12.add(labelBorderWidth, java.awt.BorderLayout.WEST);

        jPanel11.add(jPanel12, java.awt.BorderLayout.EAST);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        jPanel2.add(jPanel11, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weighty = 1000.0;
        jPanel2.add(filler4, gridBagConstraints);

        panelGraphvizDot.setBorder(javax.swing.BorderFactory.createTitledBorder(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.panelGraphvizDot.title"))); // NOI18N
        panelGraphvizDot.setLayout(new java.awt.GridBagLayout());

        textFieldPathToGraphvizDot.setColumns(3);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1000.0;
        panelGraphvizDot.add(textFieldPathToGraphvizDot, gridBagConstraints);

        buttonGraphvizDotFile.setText("...");
        buttonGraphvizDotFile.setToolTipText("select file");
        buttonGraphvizDotFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonGraphvizDotFileActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        panelGraphvizDot.add(buttonGraphvizDotFile, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel2.add(panelGraphvizDot, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1000.0;
        jPanel2.add(filler5, gridBagConstraints);

        jPanel16.setBorder(javax.swing.BorderFactory.createTitledBorder(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.panel1levelHorztGap.title"))); // NOI18N
        jPanel16.setLayout(new java.awt.BorderLayout());

        slider1stLevelHorzGap.setMajorTickSpacing(30);
        slider1stLevelHorzGap.setMaximum(250);
        slider1stLevelHorzGap.setMinimum(10);
        slider1stLevelHorzGap.setPaintLabels(true);
        slider1stLevelHorzGap.setPaintTicks(true);
        slider1stLevelHorzGap.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                slider1stLevelHorzGapStateChanged(evt);
            }
        });
        jPanel16.add(slider1stLevelHorzGap, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel2.add(jPanel16, gridBagConstraints);

        jPanel17.setBorder(javax.swing.BorderFactory.createTitledBorder(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.panel1levelVertGap.title"))); // NOI18N
        jPanel17.setLayout(new java.awt.BorderLayout());

        slider1stLevelVertGap.setMajorTickSpacing(30);
        slider1stLevelVertGap.setMaximum(250);
        slider1stLevelVertGap.setMinimum(10);
        slider1stLevelVertGap.setPaintLabels(true);
        slider1stLevelVertGap.setPaintTicks(true);
        slider1stLevelVertGap.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                slider1stLevelVertGapStateChanged(evt);
            }
        });
        jPanel17.add(slider1stLevelVertGap, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel2.add(jPanel17, gridBagConstraints);

        jPanel18.setBorder(javax.swing.BorderFactory.createTitledBorder(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.panel2levelHorzGap.title"))); // NOI18N
        jPanel18.setLayout(new java.awt.BorderLayout());

        slider2ndLevelHorzGap.setMajorTickSpacing(30);
        slider2ndLevelHorzGap.setMaximum(250);
        slider2ndLevelHorzGap.setMinimum(10);
        slider2ndLevelHorzGap.setPaintLabels(true);
        slider2ndLevelHorzGap.setPaintTicks(true);
        slider2ndLevelHorzGap.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                slider2ndLevelHorzGapStateChanged(evt);
            }
        });
        jPanel18.add(slider2ndLevelHorzGap, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel2.add(jPanel18, gridBagConstraints);

        jPanel19.setBorder(javax.swing.BorderFactory.createTitledBorder(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.panel2levelVertGap.title"))); // NOI18N
        jPanel19.setLayout(new java.awt.BorderLayout());

        slider2ndLevelVertGap.setMajorTickSpacing(30);
        slider2ndLevelVertGap.setMaximum(250);
        slider2ndLevelVertGap.setMinimum(10);
        slider2ndLevelVertGap.setPaintLabels(true);
        slider2ndLevelVertGap.setPaintTicks(true);
        slider2ndLevelVertGap.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                slider2ndLevelVertGapStateChanged(evt);
            }
        });
        jPanel19.add(slider2ndLevelVertGap, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel2.add(jPanel19, gridBagConstraints);

        panelPlantUmlSecurityProfile.setBorder(javax.swing.BorderFactory.createTitledBorder(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.panelPlantUmlSecurity.title"))); // NOI18N
        panelPlantUmlSecurityProfile.setLayout(new java.awt.BorderLayout());

        comboBoxPlantUmlSecurityProfile.setModel(new DefaultComboBoxModel<PlantUmlSecurityProfile>(PlantUmlSecurityProfile.values()));
        panelPlantUmlSecurityProfile.add(comboBoxPlantUmlSecurityProfile, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel2.add(panelPlantUmlSecurityProfile, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel6.add(jPanel2, gridBagConstraints);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.panelMisc.title"))); // NOI18N
        jPanel1.setLayout(new java.awt.GridBagLayout());

        checkboxUseInsideBrowser.setText(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.checkPreferInternalBrowser")); // NOI18N
        checkboxUseInsideBrowser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkboxUseInsideBrowserActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel1.add(checkboxUseInsideBrowser, gridBagConstraints);

        checkboxRelativePathsForFilesInTheProject.setText(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.checkUseRelativePaths")); // NOI18N
        checkboxRelativePathsForFilesInTheProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkboxRelativePathsForFilesInTheProjectActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel1.add(checkboxRelativePathsForFilesInTheProject, gridBagConstraints);

        checkBoxUnfoldCollapsedTarget.setText(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.checkUnfoldCollapsedDropTarget")); // NOI18N
        checkBoxUnfoldCollapsedTarget.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxUnfoldCollapsedTargetActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel1.add(checkBoxUnfoldCollapsedTarget, gridBagConstraints);

        checkBoxCopyColorInfoToNewAllowed.setText(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.checkCopyParentColorIntoNewChild")); // NOI18N
        checkBoxCopyColorInfoToNewAllowed.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxCopyColorInfoToNewAllowedActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel1.add(checkBoxCopyColorInfoToNewAllowed, gridBagConstraints);

        checkBoxKnowledgeFolderAutogenerationAllowed.setText(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.checkEnableAutocreationKnowledgeFolder")); // NOI18N
        checkBoxKnowledgeFolderAutogenerationAllowed.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxKnowledgeFolderAutogenerationAllowedActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel1.add(checkBoxKnowledgeFolderAutogenerationAllowed, gridBagConstraints);

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.panelTextFont.title"))); // NOI18N
        jPanel7.setLayout(new java.awt.BorderLayout());

        buttonFont.setText("..."); // NOI18N
        buttonFont.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonFontActionPerformed(evt);
            }
        });
        jPanel7.add(buttonFont, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1000.0;
        jPanel1.add(jPanel7, gridBagConstraints);

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("MMDCfgPanel.ShortCutsTitle"))); // NOI18N
        jPanel8.setLayout(new java.awt.GridBagLayout());

        buttonOpenShortcutEditor.setText(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("MMDCfgPanel.ShortCutsButtonText")); // NOI18N
        buttonOpenShortcutEditor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonOpenShortcutEditorActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel8.add(buttonOpenShortcutEditor, gridBagConstraints);

        panelScalingModifiers.setBorder(javax.swing.BorderFactory.createTitledBorder(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.panelScaleWithWheel.title"))); // NOI18N
        panelScalingModifiers.setLayout(new java.awt.GridLayout(1, 0));

        checkBoxScalingCTRL.setText("CTRL"); // NOI18N
        checkBoxScalingCTRL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxScalingCTRLActionPerformed(evt);
            }
        });
        panelScalingModifiers.add(checkBoxScalingCTRL);

        checkBoxScalingALT.setText("ALT"); // NOI18N
        checkBoxScalingALT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxScalingALTActionPerformed(evt);
            }
        });
        panelScalingModifiers.add(checkBoxScalingALT);

        checkBoxScalingSHIFT.setText("SHIFT"); // NOI18N
        checkBoxScalingSHIFT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxScalingSHIFTActionPerformed(evt);
            }
        });
        panelScalingModifiers.add(checkBoxScalingSHIFT);

        checkBoxScalingMETA.setText("META"); // NOI18N
        checkBoxScalingMETA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxScalingMETAActionPerformed(evt);
            }
        });
        panelScalingModifiers.add(checkBoxScalingMETA);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel8.add(panelScalingModifiers, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.weightx = 1999.0;
        jPanel8.add(filler8, gridBagConstraints);

        jPanel14.setBorder(javax.swing.BorderFactory.createTitledBorder(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.panelBirdsEye.title"))); // NOI18N
        jPanel14.setLayout(new java.awt.GridBagLayout());

        jPanel20.setLayout(new java.awt.BorderLayout());

        labelBirdseyeMouse.setText(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.panelBirdsEye.labelMouseButton")); // NOI18N
        jPanel20.add(labelBirdseyeMouse, java.awt.BorderLayout.LINE_START);

        comboBirdseyeMouse.setModel(new DefaultComboBoxModel<MouseButton>(MouseButton.values())
        );
        jPanel20.add(comboBirdseyeMouse, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel14.add(jPanel20, gridBagConstraints);

        jPanel21.setLayout(new java.awt.GridLayout(1, 4));

        checkBoxBirdsCTRL.setText("CTRL");
        jPanel21.add(checkBoxBirdsCTRL);

        checkBoxBirdsALT.setText("ALT");
        jPanel21.add(checkBoxBirdsALT);

        checkBoxBirdsSHIFT.setText("SHIFT");
        jPanel21.add(checkBoxBirdsSHIFT);

        checkBoxBirdsMETA.setText("META");
        jPanel21.add(checkBoxBirdsMETA);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel14.add(jPanel21, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1000.0;
        jPanel14.add(filler9, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel8.add(jPanel14, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add(jPanel8, gridBagConstraints);

        checkboxMetricsAllowed.setText(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.checkEnableMetricsUpload")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel1.add(checkboxMetricsAllowed, gridBagConstraints);

        checkboxTrimTopicText.setText(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.checkTrimTopicText")); // NOI18N
        checkboxTrimTopicText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkboxTrimTopicTextActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel1.add(checkboxTrimTopicText, gridBagConstraints);

        checkBoxShowHiddenFiles.setText(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.checkShowHiddenFiles")); // NOI18N
        checkBoxShowHiddenFiles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxShowHiddenFilesActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel1.add(checkBoxShowHiddenFiles, gridBagConstraints);

        checkBoxSmartTextPaste.setText(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.checkSmartTextPaste")); // NOI18N
        checkBoxSmartTextPaste.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxSmartTextPasteActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel1.add(checkBoxSmartTextPaste, gridBagConstraints);

        checkBoxBackupLastEdit.setText(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.checkAutoBackupLastEdit")); // NOI18N
        checkBoxBackupLastEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxBackupLastEditActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel1.add(checkBoxBackupLastEdit, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel6.add(jPanel1, gridBagConstraints);

        jPanel9.setLayout(new java.awt.GridBagLayout());

        buttonAbout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/info16.png"))); // NOI18N
        buttonAbout.setText(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.buttonAbout")); // NOI18N
        buttonAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonAboutActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel9.add(buttonAbout, gridBagConstraints);

        donateButton1.setText(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.buttonDonate")); // NOI18N
        donateButton1.setToolTipText(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.buttonDonate.tooltip")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel9.add(donateButton1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.weighty = 1000.0;
        jPanel9.add(filler1, gridBagConstraints);

        buttonResetToDefault.setIcon(new javax.swing.ImageIcon(getClass().getResource("/menu_icons/stop.png"))); // NOI18N
        buttonResetToDefault.setText(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.buttonResetToDefault")); // NOI18N
        buttonResetToDefault.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonResetToDefaultActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel9.add(buttonResetToDefault, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        jPanel9.add(filler3, gridBagConstraints);

        buttonExportToFile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/document_export16.png"))); // NOI18N
        buttonExportToFile.setText(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.buttonExportToFile")); // NOI18N
        buttonExportToFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonExportToFileActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel9.add(buttonExportToFile, gridBagConstraints);

        buttonImportFromFile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/document_import16.png"))); // NOI18N
        buttonImportFromFile.setText(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.buttonImportFromFile")); // NOI18N
        buttonImportFromFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonImportFromFileActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel9.add(buttonImportFromFile, gridBagConstraints);

        buttonExtensionsOpenInSystem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/file_manager.png"))); // NOI18N
        buttonExtensionsOpenInSystem.setText(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.buttonSystemFileExtensions")); // NOI18N
        buttonExtensionsOpenInSystem.setToolTipText(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("PreferencesPanel.buttonSystemFileExtensions.tooltip")); // NOI18N
        buttonExtensionsOpenInSystem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonExtensionsOpenInSystemActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel9.add(buttonExtensionsOpenInSystem, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        jPanel9.add(filler6, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.weighty = 1000.0;
        jPanel9.add(filler7, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(16, 5, 0, 5);
        jPanel6.add(jPanel9, gridBagConstraints);

        MainScrollPanel.setViewportView(jPanel6);

        add(MainScrollPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

  private void checkBoxShowGridActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxShowGridActionPerformed
    if (this.changeNotificationAllowed) {
      this.changed = true;
    }
  }//GEN-LAST:event_checkBoxShowGridActionPerformed

  private void colorChooserPaperColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorChooserPaperColorActionPerformed
    if (this.changeNotificationAllowed) {
      this.changed = true;
    }
  }//GEN-LAST:event_colorChooserPaperColorActionPerformed

  private void colorChooserGridColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorChooserGridColorActionPerformed
    if (this.changeNotificationAllowed) {
      this.changed = true;
    }
  }//GEN-LAST:event_colorChooserGridColorActionPerformed

  private void spinnerGridStepStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerGridStepStateChanged
    if (this.changeNotificationAllowed) {
      this.changed = true;
    }
  }//GEN-LAST:event_spinnerGridStepStateChanged

  private void spinnerConnectorWidthStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerConnectorWidthStateChanged
    if (this.changeNotificationAllowed) {
      this.changed = true;
    }
  }//GEN-LAST:event_spinnerConnectorWidthStateChanged

  private void colorChooserConnectorColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorChooserConnectorColorActionPerformed
    if (this.changeNotificationAllowed) {
      this.changed = true;
    }
  }//GEN-LAST:event_colorChooserConnectorColorActionPerformed

  private void colorChooserSelectLineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorChooserSelectLineActionPerformed
    if (this.changeNotificationAllowed) {
      this.changed = true;
    }
  }//GEN-LAST:event_colorChooserSelectLineActionPerformed

  private void colorChooserCollapsatorBorderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorChooserCollapsatorBorderActionPerformed
    if (this.changeNotificationAllowed) {
      this.changed = true;
    }
  }//GEN-LAST:event_colorChooserCollapsatorBorderActionPerformed

  private void colorChooserCollapsatorBackgroundActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorChooserCollapsatorBackgroundActionPerformed
    if (this.changeNotificationAllowed) {
      this.changed = true;
    }
  }//GEN-LAST:event_colorChooserCollapsatorBackgroundActionPerformed

  private void spinnerCollapsatorSizeStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerCollapsatorSizeStateChanged
    if (this.changeNotificationAllowed) {
      this.changed = true;
    }
  }//GEN-LAST:event_spinnerCollapsatorSizeStateChanged

  private void updateFontButton(@Nonnull final JButton button, @Nonnull final Font font) {
    final String strStyle;
    if (font.isBold()) {
      strStyle = font.isItalic() ? "bolditalic" : "bold"; //NOI18N
    } else {
      strStyle = font.isItalic() ? "italic" : "plain"; //NOI18N
    }

    button.setText(font.getName() + ", " + strStyle + ", " + font.getSize()); //NOI18N
  }

  private void buttonFontActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonFontActionPerformed
    final FontSelector fontSelector = new FontSelector(this.config.getFont());

    if (DialogProviderManager.getInstance().getDialogProvider().msgOkCancel(this, SrI18n.getInstance().findBundle().getString("PreferencesPanel.msgSelectFont.title"), fontSelector)) {
      final Font selectedFont = fontSelector.getValue();
      this.config.setFont(selectedFont);
      this.fontMindMapEditor = selectedFont;
      updateFontButton(this.buttonFont, selectedFont);
      if (this.changeNotificationAllowed) {
        this.changed = true;
      }
    }
  }//GEN-LAST:event_buttonFontActionPerformed

  private void buttonAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonAboutActionPerformed
      final JPanel aboutPanel = new AboutPanel();
      UiUtils.makeOwningDialogResizable(aboutPanel);
      final JScrollPane scrollPane = UIComponentFactoryProvider.findInstance().makeScrollPane();
      scrollPane.setViewportView(aboutPanel);
      scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

      JOptionPane.showMessageDialog(SciaRetoStarter.getApplicationFrame(), scrollPane, SrI18n.getInstance().findBundle().getString("PreferencesPanel.msgAbout.title"), JOptionPane.PLAIN_MESSAGE);
  }//GEN-LAST:event_buttonAboutActionPerformed

  private void colorChooserJumpLinkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorChooserJumpLinkActionPerformed
    if (this.changeNotificationAllowed) {
      this.changed = true;
    }
  }//GEN-LAST:event_colorChooserJumpLinkActionPerformed

  private void spinnerJumpLinkWidthStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerJumpLinkWidthStateChanged
    if (this.changeNotificationAllowed) {
      this.changed = true;
    }
  }//GEN-LAST:event_spinnerJumpLinkWidthStateChanged

  private void buttonOpenShortcutEditorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonOpenShortcutEditorActionPerformed
    final List<KeyShortcut> list = new ArrayList<>();
    for (final Map.Entry<String, KeyShortcut> e : this.mapKeyShortCuts.entrySet()) {
      list.add(e.getValue());
    }
    final KeyShortCutEditPanel panel = new KeyShortCutEditPanel(list);
    if (DialogProviderManager.getInstance().getDialogProvider().msgOkCancel(this, SrI18n.getInstance().findBundle().getString("PreferencesPanel.ShortcutEditor.title"), panel)) {
      for (final KeyShortcut s : panel.getResult()) {
        this.mapKeyShortCuts.put(s.getID(), s);
      }
      if (this.changeNotificationAllowed) {
        this.changed = true;
      }
    }
  }//GEN-LAST:event_buttonOpenShortcutEditorActionPerformed

  private void checkBoxScalingCTRLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxScalingCTRLActionPerformed
    if (this.changeNotificationAllowed) {
      this.changed = true;
    }
  }//GEN-LAST:event_checkBoxScalingCTRLActionPerformed

  private void checkBoxScalingALTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxScalingALTActionPerformed
    if (this.changeNotificationAllowed) {
      this.changed = true;
    }
  }//GEN-LAST:event_checkBoxScalingALTActionPerformed

  private void checkBoxScalingSHIFTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxScalingSHIFTActionPerformed
    if (this.changeNotificationAllowed) {
      this.changed = true;
    }
  }//GEN-LAST:event_checkBoxScalingSHIFTActionPerformed

  private void checkBoxScalingMETAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxScalingMETAActionPerformed
    if (this.changeNotificationAllowed) {
      this.changed = true;
    }
  }//GEN-LAST:event_checkBoxScalingMETAActionPerformed

  private void checkBoxKnowledgeFolderAutogenerationAllowedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxKnowledgeFolderAutogenerationAllowedActionPerformed
    if (this.changeNotificationAllowed) {
      this.changed = true;
    }
  }//GEN-LAST:event_checkBoxKnowledgeFolderAutogenerationAllowedActionPerformed

  private void checkBoxCopyColorInfoToNewAllowedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxCopyColorInfoToNewAllowedActionPerformed
    if (this.changeNotificationAllowed) {
      this.changed = true;
    }
  }//GEN-LAST:event_checkBoxCopyColorInfoToNewAllowedActionPerformed

  private void checkBoxUnfoldCollapsedTargetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxUnfoldCollapsedTargetActionPerformed
    if (this.changeNotificationAllowed) {
      this.changed = true;
    }
  }//GEN-LAST:event_checkBoxUnfoldCollapsedTargetActionPerformed

  private void checkboxRelativePathsForFilesInTheProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkboxRelativePathsForFilesInTheProjectActionPerformed
    if (this.changeNotificationAllowed) {
      this.changed = true;
    }
  }//GEN-LAST:event_checkboxRelativePathsForFilesInTheProjectActionPerformed

  private void checkboxUseInsideBrowserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkboxUseInsideBrowserActionPerformed
    if (this.changeNotificationAllowed) {
      this.changed = true;
    }
  }//GEN-LAST:event_checkboxUseInsideBrowserActionPerformed

  private void spinnerElementBorderWidthStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerElementBorderWidthStateChanged
    if (this.changeNotificationAllowed) {
      this.changed = true;
    }
  }//GEN-LAST:event_spinnerElementBorderWidthStateChanged

  private void slider2ndLevelVertGapStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_slider2ndLevelVertGapStateChanged
    if (this.changeNotificationAllowed) {
      this.changed = true;
    }
  }//GEN-LAST:event_slider2ndLevelVertGapStateChanged

  private void slider2ndLevelHorzGapStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_slider2ndLevelHorzGapStateChanged
    if (this.changeNotificationAllowed) {
      this.changed = true;
    }
  }//GEN-LAST:event_slider2ndLevelHorzGapStateChanged

  private void slider1stLevelVertGapStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_slider1stLevelVertGapStateChanged
    if (this.changeNotificationAllowed) {
      this.changed = true;
    }
  }//GEN-LAST:event_slider1stLevelVertGapStateChanged

  private void slider1stLevelHorzGapStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_slider1stLevelHorzGapStateChanged
    if (this.changeNotificationAllowed) {
      this.changed = true;
    }
  }//GEN-LAST:event_slider1stLevelHorzGapStateChanged

  private void colorChooser2ndTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorChooser2ndTextActionPerformed
    if (this.changeNotificationAllowed) {
      this.changed = true;
    }
  }//GEN-LAST:event_colorChooser2ndTextActionPerformed

  private void colorChooser2ndBackgroundActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorChooser2ndBackgroundActionPerformed
    if (this.changeNotificationAllowed) {
      this.changed = true;
    }
  }//GEN-LAST:event_colorChooser2ndBackgroundActionPerformed

  private void colorChooser1stTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorChooser1stTextActionPerformed
    if (this.changeNotificationAllowed) {
      this.changed = true;
    }
  }//GEN-LAST:event_colorChooser1stTextActionPerformed

  private void colorChooser1stBackgroundActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorChooser1stBackgroundActionPerformed
    if (this.changeNotificationAllowed) {
      this.changed = true;
    }
  }//GEN-LAST:event_colorChooser1stBackgroundActionPerformed

  private void colorChooserRootTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorChooserRootTextActionPerformed
    if (this.changeNotificationAllowed) {
      this.changed = true;
    }
  }//GEN-LAST:event_colorChooserRootTextActionPerformed

  private void colorChooserRootBackgroundActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorChooserRootBackgroundActionPerformed
    if (this.changeNotificationAllowed) {
      this.changed = true;
    }
  }//GEN-LAST:event_colorChooserRootBackgroundActionPerformed

  private void checkBoxDropShadowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxDropShadowActionPerformed
    if (this.changeNotificationAllowed) {
      this.changed = true;
    }
  }//GEN-LAST:event_checkBoxDropShadowActionPerformed

  private void buttonResetToDefaultActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonResetToDefaultActionPerformed
    loadFrom(new MindMapPanelConfig(), PreferencesManager.getInstance().getPreferences());
    this.fontTextEditor = ScalableRsyntaxTextArea.DEFAULT_FONT;
    this.textFieldPathToGraphvizDot.setText("");
    this.comboBoxPlantUmlSecurityProfile.setSelectedItem(PlantUmlSecurityProfile.LEGACY);
    updateFontButton(this.buttonFontForEditor, this.fontTextEditor);
  }//GEN-LAST:event_buttonResetToDefaultActionPerformed

  private void buttonFontForEditorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonFontForEditorActionPerformed
    final FontSelector fontSelector = new FontSelector(this.fontTextEditor);
    if (DialogProviderManager.getInstance().getDialogProvider().msgOkCancel(this, SrI18n.getInstance().findBundle().getString("PreferencesPanel.FontForEditor.title"), fontSelector)) {
      this.fontTextEditor = fontSelector.getValue();
      updateFontButton(this.buttonFontForEditor, fontSelector.getValue());
      if (this.changeNotificationAllowed) {
        this.changed = true;
      }
    }
  }//GEN-LAST:event_buttonFontForEditorActionPerformed

  private void buttonExportToFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonExportToFileActionPerformed
    File file = DialogProviderManager.getInstance()
            .getDialogProvider()
            .msgSaveFileDialog(this, null,"exportProperties", SrI18n.getInstance().findBundle().getString("PreferencesPanel.ExportProperties.title"), null, true, new FileFilter[]{new PropertiesFileFilter()},
                SrI18n.getInstance().findBundle().getString("PreferencesPanel.ExportProperties.approve"));
    if (file != null) {
      if (!file.getName().toLowerCase(Locale.ENGLISH).endsWith(".properties")) { //NOI18N
        final Boolean addExt = DialogProviderManager.getInstance().getDialogProvider().msgConfirmYesNoCancel(this,
            SrI18n.getInstance().findBundle().getString("PreferencesPanel.AddExtension.title"), SrI18n.getInstance().findBundle().getString("PreferencesPanel.AddExtension.question"));
        if (addExt == null) {
          return;
        }
        if (addExt) {
          file = new File(file.getAbsolutePath() + ".properties"); //NOI18N
        }
      }

      if (file.exists() && !DialogProviderManager.getInstance().getDialogProvider()
              .msgConfirmOkCancel(this, SrI18n.getInstance().findBundle().getString("PreferencesPanel.OverrideFile.title"),
                      String.format(SrI18n.getInstance().findBundle().getString("PreferencesPanel.OverrideFile.question"), file.getName()))) {
        return;
      }

      final PropertiesPreferences prefs = new PropertiesPreferences("SciaReto editor settings");
      final MindMapPanelConfig cfg = fillBySettings(new MindMapPanelConfig(), prefs);
      cfg.saveTo(prefs);
      try {
        FileUtils.write(file, prefs.toString(), StandardCharsets.UTF_8);
      } catch (final Exception ex) {
        LOGGER.error("Can't export settings", ex); //NOI18N
        DialogProviderManager.getInstance().getDialogProvider().msgError(this,
            SrI18n.getInstance().findBundle().getString("PreferencesPanel.ExportProperties.error") + ex.getMessage()); //NOI18N
      }
    }
  }//GEN-LAST:event_buttonExportToFileActionPerformed

  private void buttonImportFromFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonImportFromFileActionPerformed
    final File file = DialogProviderManager
            .getInstance()
            .getDialogProvider()
            .msgOpenFileDialog(this, null, "importProperties",
                SrI18n.getInstance().findBundle().getString("PreferencesPanel.ImportSettings.title"), null, true, new FileFilter[]{new PropertiesFileFilter()},
                SrI18n.getInstance().findBundle().getString("PreferencesPanel.ImportSettings.approve"));
    if (file != null) {
      try {
        load(new PropertiesPreferences("SciaReto", FileUtils.readFileToString(file, StandardCharsets.UTF_8)));
      } catch (final Exception ex) {
        LOGGER.error("Can't import settings", ex); //NOI18N
        DialogProviderManager.getInstance().getDialogProvider().msgError(SciaRetoStarter.getApplicationFrame(),
            SrI18n.getInstance().findBundle().getString("PreferencesPanel.ImportSettings.error") + ex.getMessage());
      }
    }
  }//GEN-LAST:event_buttonImportFromFileActionPerformed

  private void checkboxTrimTopicTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkboxTrimTopicTextActionPerformed
    if (this.changeNotificationAllowed) {
      this.changed = true;
    }
  }//GEN-LAST:event_checkboxTrimTopicTextActionPerformed

    private void comboBoxRenderQualityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBoxRenderQualityActionPerformed
      if (this.changeNotificationAllowed) {
        this.changed = true;
      }
    }//GEN-LAST:event_comboBoxRenderQualityActionPerformed

    private void checkBoxShowHiddenFilesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxShowHiddenFilesActionPerformed
      if (this.changeNotificationAllowed) {
        this.changed = true;
      }
    }//GEN-LAST:event_checkBoxShowHiddenFilesActionPerformed

    private void buttonGraphvizDotFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonGraphvizDotFileActionPerformed
      final JFileChooser fileChooser = new JFileChooser(this.textFieldPathToGraphvizDot.getText());
      fileChooser.setDialogTitle(SrI18n.getInstance().findBundle().getString("PreferencesPanel.SelectGraphVizDot.title"));
      fileChooser.setApproveButtonText(SrI18n.getInstance().findBundle().getString("PreferencesPanel.SelectGraphVizDot.approve"));
      fileChooser.setAcceptAllFileFilterUsed(true);
      fileChooser.setMultiSelectionEnabled(false);

      fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

      if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
        final File file = fileChooser.getSelectedFile();
        this.textFieldPathToGraphvizDot.setText(file.getAbsolutePath());
      }
    }//GEN-LAST:event_buttonGraphvizDotFileActionPerformed

  private void buttonExtensionsOpenInSystemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonExtensionsOpenInSystemActionPerformed
    final SysFileExtensionEditorPanel dataPanel = new SysFileExtensionEditorPanel(SystemFileExtensionManager.getInstance().getExtensionsAsCommaSeparatedString());
    if (DialogProviderManager.getInstance().getDialogProvider().msgOkCancel(this,
        SrI18n.getInstance().findBundle().getString("PreferencesPanel.SystemFileExtensions.title"), dataPanel)) {
      SystemFileExtensionManager.getInstance().setExtensionsAsCommaSeparatedString(dataPanel.getValuerNullIfDefault());
    }
  }//GEN-LAST:event_buttonExtensionsOpenInSystemActionPerformed

  private void checkBoxSmartTextPasteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxSmartTextPasteActionPerformed
    if (this.changeNotificationAllowed) {
      this.changed = true;
    }
  }//GEN-LAST:event_checkBoxSmartTextPasteActionPerformed

  private void checkBoxBackupLastEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxBackupLastEditActionPerformed
    if (this.changeNotificationAllowed) {
      this.changed = true;
    }
  }//GEN-LAST:event_checkBoxBackupLastEditActionPerformed

  public void load(@Nonnull final Preferences preferences) {
    this.config.loadFrom(preferences);
    loadFrom(this.config, preferences);
    this.changeNotificationAllowed = false;
    try {
      // Common behaviour options
      this.checkBoxShowHiddenFiles.setSelected(preferences.getBoolean("showHiddenFiles", true)); //NOI18N
      this.checkboxTrimTopicText.setSelected(preferences.getBoolean("trimTopicText", false)); //NOI18N
      this.checkboxUseInsideBrowser.setSelected(preferences.getBoolean("useInsideBrowser", false)); //NOI18N
      this.checkboxRelativePathsForFilesInTheProject.setSelected(preferences.getBoolean("makeRelativePathToProject", true)); //NOI18N
      this.checkBoxUnfoldCollapsedTarget.setSelected(preferences.getBoolean("unfoldCollapsedTarget", true)); //NOI18N
      this.checkBoxCopyColorInfoToNewAllowed.setSelected(preferences.getBoolean("copyColorInfoToNewChildAllowed", true)); //NOI18N
      this.checkBoxKnowledgeFolderAutogenerationAllowed.setSelected(preferences.getBoolean(PREFERENCE_KEY_KNOWLEDGEFOLDER_ALLOWED, false));
      
      // third part options
      final String pathToGraphViz = preferences.get(PROPERTY_PLANTUML_DOT_PATH, null);
      this.textFieldPathToGraphvizDot.setText(pathToGraphViz == null ? "" : pathToGraphViz);
      this.comboBoxPlantUmlSecurityProfile.setSelectedItem(
              PlantUmlSecurityProfile.findForText(preferences.get(PROPERTY_PLANTUML_SECURITY_PROFILE, null), PlantUmlSecurityProfile.LEGACY));

      // Metrics
      this.checkboxMetricsAllowed.setSelected(MetricsService.getInstance().isEnabled());
    } finally {
      this.changeNotificationAllowed = true;
    }
  }

  private void loadFrom(@Nonnull final MindMapPanelConfig config, @Nonnull final Preferences prefs) {
    this.changeNotificationAllowed = false;
    try {
      this.checkBoxShowGrid.setSelected(config.isShowGrid());
      this.checkBoxDropShadow.setSelected(config.isDropShadow());
      this.colorChooserPaperColor.setValue(config.getPaperColor());
      this.colorChooserGridColor.setValue(config.getGridColor());
      this.colorChooserConnectorColor.setValue(config.getConnectorColor());
      this.colorChooserJumpLink.setValue(config.getJumpLinkColor());

      this.colorChooserRootBackground.setValue(config.getRootBackgroundColor());
      this.colorChooserRootText.setValue(config.getRootTextColor());

      this.colorChooser1stBackground.setValue(config.getFirstLevelBackgroundColor());
      this.colorChooser1stText.setValue(config.getFirstLevelTextColor());

      this.colorChooser2ndBackground.setValue(config.getOtherLevelBackgroundColor());
      this.colorChooser2ndText.setValue(config.getOtherLevelTextColor());

      this.colorChooserSelectLine.setValue(config.getSelectLineColor());

      this.spinnerGridStep.setValue(config.getGridStep());
      this.spinnerSelectLineGap.setValue(config.getSelectLineGap());
      this.spinnerConnectorWidth.setValue(config.getConnectorWidth());
      this.spinnerJumpLinkWidth.setValue(config.getJumpLinkWidth());
      this.spinnerCollapsatorWidth.setValue(config.getCollapsatorBorderWidth());
      this.spinnerCollapsatorSize.setValue(config.getCollapsatorSize());
      this.spinnerElementBorderWidth.setValue(config.getElementBorderWidth());

      this.colorChooserCollapsatorBackground.setValue(config.getCollapsatorBackgroundColor());
      this.colorChooserCollapsatorBorder.setValue(config.getCollapsatorBorderColor());

      this.spinnerSelectLineWidth.setValue(config.getSelectLineWidth());

      this.slider1stLevelHorzGap.setValue(config.getFirstLevelHorizontalInset());
      this.slider1stLevelVertGap.setValue(config.getFirstLevelVerticalInset());

      this.slider2ndLevelHorzGap.setValue(config.getOtherLevelHorizontalInset());
      this.slider2ndLevelVertGap.setValue(config.getOtherLevelVerticalInset());

      this.checkBoxSmartTextPaste.setSelected(config.isSmartTextPaste());

      this.mapKeyShortCuts.clear();
      this.mapKeyShortCuts.putAll(config.getKeyShortcutMap());

      this.comboBoxRenderQuality.setSelectedItem(config.getRenderQuality());
      this.comboBirdseyeMouse.setSelectedItem(config.getBirdseyeMouseButton());

      setBirdsEyeModifiers(config.getKeyShortCut(MindMapPanelConfig.KEY_BIRDSEYE_MODIFIERS).getModifiers());
      setScalingModifiers(config.getScaleModifiers());

      this.fontMindMapEditor = config.getFont();
      updateFontButton(this.buttonFont, this.fontMindMapEditor);

      this.fontTextEditor = Assertions.assertNotNull(PreferencesManager.getInstance().getFont(prefs, SpecificKeys.PROPERTY_TEXT_EDITOR_FONT, ScalableRsyntaxTextArea.DEFAULT_FONT));
      updateFontButton(this.buttonFontForEditor, this.fontTextEditor);
      this.checkBoxBackupLastEdit.setSelected(PreferencesManager.getInstance().getFlag(prefs, SpecificKeys.PROPERTY_BACKUP_LAST_EDIT_BEFORE_SAVE, false));
    } finally {
      this.changeNotificationAllowed = true;
    }
  }

  @Nonnull
  @ReturnsOriginal
  private MindMapPanelConfig fillBySettings(@Nonnull final MindMapPanelConfig config, @Nonnull final Preferences preferences) {
    config.setShowGrid(this.checkBoxShowGrid.isSelected());
    config.setDropShadow(this.checkBoxDropShadow.isSelected());
    config.setPaperColor(Assertions.assertNotNull(this.colorChooserPaperColor.getValue()));
    config.setGridColor(Assertions.assertNotNull(this.colorChooserGridColor.getValue()));
    config.setConnectorColor(Assertions.assertNotNull(this.colorChooserConnectorColor.getValue()));
    config.setJumpLinkColor(Assertions.assertNotNull(this.colorChooserJumpLink.getValue()));
    config.setRootBackgroundColor(Assertions.assertNotNull(this.colorChooserRootBackground.getValue()));
    config.setRootTextColor(Assertions.assertNotNull(this.colorChooserRootText.getValue()));
    config.setFirstLevelBackgroundColor(Assertions.assertNotNull(this.colorChooser1stBackground.getValue()));
    config.setFirstLevelTextColor(Assertions.assertNotNull(this.colorChooser1stText.getValue()));
    config.setOtherLevelBackgroundColor(Assertions.assertNotNull(this.colorChooser2ndBackground.getValue()));
    config.setOtherLevelTextColor(Assertions.assertNotNull(this.colorChooser2ndText.getValue()));
    config.setSelectLineColor(Assertions.assertNotNull(this.colorChooserSelectLine.getValue()));
    config.setCollapsatorBackgroundColor(Assertions.assertNotNull(this.colorChooserCollapsatorBackground.getValue()));
    config.setCollapsatorBorderColor(Assertions.assertNotNull(this.colorChooserCollapsatorBorder.getValue()));
    config.setGridStep((Integer) this.spinnerGridStep.getValue());
    config.setSelectLineGap((Integer) this.spinnerSelectLineGap.getValue());
    config.setCollapsatorSize((Integer) this.spinnerCollapsatorSize.getValue());
    config.setConnectorWidth((Float) this.spinnerConnectorWidth.getValue());
    config.setJumpLinkWidth((Float) this.spinnerJumpLinkWidth.getValue());
    config.setSelectLineWidth((Float) this.spinnerSelectLineWidth.getValue());
    config.setCollapsatorBorderWidth((Float) this.spinnerCollapsatorWidth.getValue());
    config.setElementBorderWidth((Float) this.spinnerElementBorderWidth.getValue());
    config.setSmartTextPaste(this.checkBoxSmartTextPaste.isSelected());

    config.setFirstLevelHorizontalInset(this.slider1stLevelHorzGap.getValue());
    config.setFirstLevelVerticalInset(this.slider1stLevelVertGap.getValue());
    config.setOtherLevelHorizontalInset(this.slider2ndLevelHorzGap.getValue());
    config.setOtherLevelVerticalInset(this.slider2ndLevelVertGap.getValue());
    config.setFont(this.fontMindMapEditor);

    config.setRenderQuality(GetUtils.ensureNonNull((RenderQuality) this.comboBoxRenderQuality.getSelectedItem(), Utils.getDefaultRenderQialityForOs()));
    config.setBirdseyeMouseButton(GetUtils.ensureNonNull((MouseButton)this.comboBirdseyeMouse.getSelectedItem(), MouseButton.BUTTON_3));

    for (final Map.Entry<String, KeyShortcut> e : this.mapKeyShortCuts.entrySet()) {
      config.setKeyShortCut(e.getValue());
    }
    final KeyShortcut birdsEyeModifiers = config.getKeyShortCut(MindMapPanelConfig.KEY_BIRDSEYE_MODIFIERS);
    config.setKeyShortCut(new KeyShortcut(birdsEyeModifiers.getID(), getBirdsModifiers()));

    config.setScaleModifiers(getScalingModifiers());
    
    
    config.saveTo(preferences);

    // Common behaviour options
    preferences.putBoolean("useInsideBrowser", this.checkboxUseInsideBrowser.isSelected()); //NOI18N
    preferences.putBoolean("trimTopicText", this.checkboxTrimTopicText.isSelected()); //NOI18N
    preferences.putBoolean("showHiddenFiles", this.checkBoxShowHiddenFiles.isSelected()); //NOI18N
    preferences.putBoolean("makeRelativePathToProject", this.checkboxRelativePathsForFilesInTheProject.isSelected()); //NOI18N
    preferences.putBoolean("unfoldCollapsedTarget", this.checkBoxUnfoldCollapsedTarget.isSelected()); //NOI18N
    preferences.putBoolean("copyColorInfoToNewChildAllowed", this.checkBoxCopyColorInfoToNewAllowed.isSelected()); //NOI18N
    preferences.putBoolean(PREFERENCE_KEY_KNOWLEDGEFOLDER_ALLOWED, this.checkBoxKnowledgeFolderAutogenerationAllowed.isSelected());

    final String pathToGraphVizDot = textFieldPathToGraphvizDot.getText();
    if (pathToGraphVizDot.trim().isEmpty()) {
      preferences.remove(PROPERTY_PLANTUML_DOT_PATH);
    } else {
      preferences.put(PROPERTY_PLANTUML_DOT_PATH, pathToGraphVizDot);
    }
    
    preferences.put(PROPERTY_PLANTUML_SECURITY_PROFILE, comboBoxPlantUmlSecurityProfile.getSelectedItem().toString());

    PreferencesManager.getInstance().setFont(preferences, SpecificKeys.PROPERTY_TEXT_EDITOR_FONT, fontTextEditor);
    PreferencesManager.getInstance().setFlag(preferences, SpecificKeys.PROPERTY_BACKUP_LAST_EDIT_BEFORE_SAVE, this.checkBoxBackupLastEdit.isSelected());

    // Metrics
    MetricsService.getInstance().setEnabled(this.checkboxMetricsAllowed.isSelected());

    return config;
  }

  public void save() {
    final MindMapPanelConfig config = this.config;
    if (config != null) {
      try {
        fillBySettings(config, PreferencesManager.getInstance().getPreferences());
        PreferencesManager.getInstance().flush();
      } finally {
        context.notifyReloadConfig();
      }
    }
  }

  private void setScalingModifiers(final int value) {
    this.checkBoxScalingALT.setSelected((value & KeyEvent.ALT_MASK) != 0);
    this.checkBoxScalingCTRL.setSelected((value & KeyEvent.CTRL_MASK) != 0);
    this.checkBoxScalingMETA.setSelected((value & KeyEvent.META_MASK) != 0);
    this.checkBoxScalingSHIFT.setSelected((value & KeyEvent.SHIFT_MASK) != 0);
  }

  private int getScalingModifiers() {
    return (this.checkBoxScalingALT.isSelected() ? KeyEvent.ALT_MASK : 0)
            | (this.checkBoxScalingCTRL.isSelected() ? KeyEvent.CTRL_MASK : 0)
            | (this.checkBoxScalingMETA.isSelected() ? KeyEvent.META_MASK : 0)
            | (this.checkBoxScalingSHIFT.isSelected() ? KeyEvent.SHIFT_MASK : 0);
    }


  private void setBirdsEyeModifiers(final int value) {
    this.checkBoxBirdsALT.setSelected((value & KeyEvent.ALT_MASK) != 0);
    this.checkBoxBirdsCTRL.setSelected((value & KeyEvent.CTRL_MASK) != 0);
    this.checkBoxBirdsMETA.setSelected((value & KeyEvent.META_MASK) != 0);
    this.checkBoxBirdsSHIFT.setSelected((value & KeyEvent.SHIFT_MASK) != 0);
  }

  private int getBirdsModifiers() {
    return (this.checkBoxBirdsALT.isSelected() ? KeyEvent.ALT_MASK : 0)
            | (this.checkBoxBirdsCTRL.isSelected() ? KeyEvent.CTRL_MASK : 0)
            | (this.checkBoxBirdsMETA.isSelected() ? KeyEvent.META_MASK : 0)
            | (this.checkBoxBirdsSHIFT.isSelected() ? KeyEvent.SHIFT_MASK : 0);
  }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane MainScrollPanel;
    private javax.swing.JButton buttonAbout;
    private javax.swing.JButton buttonExportToFile;
    private javax.swing.JButton buttonExtensionsOpenInSystem;
    private javax.swing.JButton buttonFont;
    private javax.swing.JButton buttonFontForEditor;
    private javax.swing.JButton buttonGraphvizDotFile;
    private javax.swing.JButton buttonImportFromFile;
    private javax.swing.JButton buttonOpenShortcutEditor;
    private javax.swing.JButton buttonResetToDefault;
    private javax.swing.JCheckBox checkBoxBackupLastEdit;
    private javax.swing.JCheckBox checkBoxBirdsALT;
    private javax.swing.JCheckBox checkBoxBirdsCTRL;
    private javax.swing.JCheckBox checkBoxBirdsMETA;
    private javax.swing.JCheckBox checkBoxBirdsSHIFT;
    private javax.swing.JCheckBox checkBoxCopyColorInfoToNewAllowed;
    private javax.swing.JCheckBox checkBoxDropShadow;
    private javax.swing.JCheckBox checkBoxKnowledgeFolderAutogenerationAllowed;
    private javax.swing.JCheckBox checkBoxScalingALT;
    private javax.swing.JCheckBox checkBoxScalingCTRL;
    private javax.swing.JCheckBox checkBoxScalingMETA;
    private javax.swing.JCheckBox checkBoxScalingSHIFT;
    private javax.swing.JCheckBox checkBoxShowGrid;
    private javax.swing.JCheckBox checkBoxShowHiddenFiles;
    private javax.swing.JCheckBox checkBoxSmartTextPaste;
    private javax.swing.JCheckBox checkBoxUnfoldCollapsedTarget;
    private javax.swing.JCheckBox checkboxMetricsAllowed;
    private javax.swing.JCheckBox checkboxRelativePathsForFilesInTheProject;
    private javax.swing.JCheckBox checkboxTrimTopicText;
    private javax.swing.JCheckBox checkboxUseInsideBrowser;
    private com.igormaznitsa.sciareto.ui.misc.ColorChooserButton colorChooser1stBackground;
    private com.igormaznitsa.sciareto.ui.misc.ColorChooserButton colorChooser1stText;
    private com.igormaznitsa.sciareto.ui.misc.ColorChooserButton colorChooser2ndBackground;
    private com.igormaznitsa.sciareto.ui.misc.ColorChooserButton colorChooser2ndText;
    private com.igormaznitsa.sciareto.ui.misc.ColorChooserButton colorChooserCollapsatorBackground;
    private com.igormaznitsa.sciareto.ui.misc.ColorChooserButton colorChooserCollapsatorBorder;
    private com.igormaznitsa.sciareto.ui.misc.ColorChooserButton colorChooserConnectorColor;
    private com.igormaznitsa.sciareto.ui.misc.ColorChooserButton colorChooserGridColor;
    private com.igormaznitsa.sciareto.ui.misc.ColorChooserButton colorChooserJumpLink;
    private com.igormaznitsa.sciareto.ui.misc.ColorChooserButton colorChooserPaperColor;
    private com.igormaznitsa.sciareto.ui.misc.ColorChooserButton colorChooserRootBackground;
    private com.igormaznitsa.sciareto.ui.misc.ColorChooserButton colorChooserRootText;
    private com.igormaznitsa.sciareto.ui.misc.ColorChooserButton colorChooserSelectLine;
    private javax.swing.JComboBox<MouseButton> comboBirdseyeMouse;
    private javax.swing.JComboBox<PlantUmlSecurityProfile> comboBoxPlantUmlSecurityProfile;
    private javax.swing.JComboBox<RenderQuality> comboBoxRenderQuality;
    private com.igormaznitsa.sciareto.ui.misc.DonateButton donateButton1;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler2;
    private javax.swing.Box.Filler filler3;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler5;
    private javax.swing.Box.Filler filler6;
    private javax.swing.Box.Filler filler7;
    private javax.swing.Box.Filler filler8;
    private javax.swing.Box.Filler filler9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel21;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JLabel labelBirdseyeMouse;
    private javax.swing.JLabel labelBorderWidth;
    private javax.swing.JPanel panelGraphvizDot;
    private javax.swing.JPanel panelPlantUmlSecurityProfile;
    private javax.swing.JPanel panelScalingModifiers;
    private javax.swing.JSlider slider1stLevelHorzGap;
    private javax.swing.JSlider slider1stLevelVertGap;
    private javax.swing.JSlider slider2ndLevelHorzGap;
    private javax.swing.JSlider slider2ndLevelVertGap;
    private javax.swing.JSpinner spinnerCollapsatorSize;
    private javax.swing.JSpinner spinnerCollapsatorWidth;
    private javax.swing.JSpinner spinnerConnectorWidth;
    private javax.swing.JSpinner spinnerElementBorderWidth;
    private javax.swing.JSpinner spinnerGridStep;
    private javax.swing.JSpinner spinnerJumpLinkWidth;
    private javax.swing.JSpinner spinnerSelectLineGap;
    private javax.swing.JSpinner spinnerSelectLineWidth;
    private javax.swing.JTextField textFieldPathToGraphvizDot;
    // End of variables declaration//GEN-END:variables
}
