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

package com.igormaznitsa.mindmap.ide.commons.preferences;

import static java.util.Objects.requireNonNull;

import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.mindmap.swing.panel.utils.KeyShortcut;
import com.igormaznitsa.mindmap.swing.panel.utils.MouseButton;
import com.igormaznitsa.mindmap.swing.panel.utils.RenderQuality;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactory;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Supplier;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;

public abstract class AbstractPreferencesPanel {
  private static final int GRID_GAP = 2;

  private final UIComponentFactory uiComponentFactory;
  private final JSpinner spinnerGridStep;
  private final ColorSelectButton buttonColorGrid;
  private final ColorSelectButton buttonColorPaper;
  private final ColorSelectButton buttonColorSelectFrame;
  private final ColorSelectButton buttonColorRootFill;
  private final ColorSelectButton buttonColorRootText;
  private final ColorSelectButton buttonColorLevel1Fill;
  private final ColorSelectButton buttonColorLevel2Fill;
  private final ColorSelectButton buttonColorLevel1Text;
  private final ColorSelectButton buttonColorLevel2Text;
  private final JCheckBox checkBoxShowGrid;
  private final JCheckBox checkBoxDropShadow;
  private final JSpinner spinnerBorderWidth;
  private final JSlider sliderLevel1HorzGap;
  private final JSlider sliderLevel1VertGap;
  private final JSlider sliderLevel2HorzGap;
  private final JSlider sliderLevel2VertGap;

  private final JSpinner spinnerConnectorWidth;
  private final JSpinner spinnerJumpLinkWidth;
  private final JSpinner spinnerCollapsatorSize;
  private final JSpinner spinnerCollapsatorWidth;
  private final JSpinner spinnerSelectionFrameWidth;
  private final JSpinner spinnerSelectionFrameGap;
  private final ColorSelectButton buttonColorCollapsatorFill;
  private final ColorSelectButton buttonColorCollapsatorBorder;
  private final ColorSelectButton buttonColorJumpLink;
  private final ColorSelectButton buttonColorConnector;

  private final ColorSelectButton buttonFastNavigationPaper;
  private final ColorSelectButton buttonFastNavigationInk;

  private final FontSelectPanel fontChooserPanelMindMapTopicTitleFont;
  private final JPanel panel;
  protected final DialogProvider dialogProvider;

  private final JComboBox<RenderQuality> comboBoxRenderQuality;
  private final JComboBox<MouseButton> comboBoxFastNavigationMouse;
  private final KeyModifiersSelector keyModifiersWheelScale;
  private final KeyModifiersSelector keyModifiersFastNavigation;

  private final JButton buttonKeyShortcutEditor;

  private final JCheckBox checkBoxSmartTextPaste;

  public static class ButtonInfo {
    private final Image icon;
    private final String title;

    private final ActionListener actionListener;

    private final boolean splitter;
    private final String tooltip;

    private final Supplier<JButton> supplier;

    private ButtonInfo(){
      this.splitter = true;
      this.icon = null;
      this.title = null;
      this.tooltip = null;
      this.actionListener = null;
      this.supplier = null;
    }

    private ButtonInfo(final Image icon, final String title, final String tooltip, final ActionListener actionListener, final Supplier<JButton> supplier) {
      this.splitter = false;
      this.icon = icon;
      this.title = title;
      this.tooltip = tooltip;
      this.actionListener = actionListener;
      this.supplier = supplier;
    }

    public static ButtonInfo splitter() {
      return new ButtonInfo();
    }

    public static ButtonInfo from(final Image icon, final String title, final ActionListener actionListener) {
      return new ButtonInfo(icon, title, null, actionListener, null);
    }

    public static ButtonInfo from(final Image icon, final String title, final String tooltip, final ActionListener actionListener) {
      return new ButtonInfo(icon, title, tooltip, actionListener, null);
    }

    public static ButtonInfo from(final Image icon, final String title, final String tooltip, final ActionListener actionListener, final Supplier<JButton> supplier) {
      return new ButtonInfo(icon, title, tooltip, actionListener, supplier);
    }
  }

  public AbstractPreferencesPanel(final UIComponentFactory uiComponentFactory,
                                   final DialogProvider dialogProvider) {
    final ResourceBundle bundle = MmcI18n.getInstance().findBundle();

    this.dialogProvider = dialogProvider;
    this.uiComponentFactory = uiComponentFactory;
    this.panel = this.uiComponentFactory.makePanel();
    this.panel.setLayout(new BorderLayout());

    this.buttonFastNavigationPaper = new ColorSelectButton(this.panel, this.uiComponentFactory, dialogProvider, c -> new Color(0x90000000 | (c.getRGB() & 0xFFFFFF), true));
    this.buttonFastNavigationInk = new ColorSelectButton(this.panel, this.uiComponentFactory, dialogProvider, c -> new Color(0x90000000 | (c.getRGB() & 0xFFFFFF), true));

    this.spinnerGridStep = this.uiComponentFactory.makeSpinner();
    this.spinnerGridStep.setModel(new SpinnerNumberModel(32, 8, 1024, 8));
    this.buttonColorGrid =
        new ColorSelectButton(this.panel, this.uiComponentFactory, dialogProvider);
    this.buttonColorPaper =
        new ColorSelectButton(this.panel, this.uiComponentFactory, dialogProvider);
    this.checkBoxShowGrid = this.uiComponentFactory.makeCheckBox();

    this.buttonColorSelectFrame =
        new ColorSelectButton(this.panel, this.uiComponentFactory, dialogProvider);
    this.buttonColorRootFill =
        new ColorSelectButton(this.panel, this.uiComponentFactory, dialogProvider);
    this.buttonColorRootText =
        new ColorSelectButton(this.panel, this.uiComponentFactory, dialogProvider);
    this.buttonColorLevel1Fill =
        new ColorSelectButton(this.panel, this.uiComponentFactory, dialogProvider);
    this.buttonColorLevel2Fill =
        new ColorSelectButton(this.panel, this.uiComponentFactory, dialogProvider);
    this.buttonColorLevel1Text =
        new ColorSelectButton(this.panel, this.uiComponentFactory, dialogProvider);
    this.buttonColorLevel2Text =
        new ColorSelectButton(this.panel, this.uiComponentFactory, dialogProvider);

    this.spinnerConnectorWidth = this.uiComponentFactory.makeSpinner();
    this.spinnerConnectorWidth.setModel(new SpinnerNumberModel(1.5d, 0.1d, 5.0d, 0.01d));
    this.spinnerJumpLinkWidth = this.uiComponentFactory.makeSpinner();
    this.spinnerJumpLinkWidth.setModel(new SpinnerNumberModel(1.5d, 0.1d, 5.0d, 0.01d));
    this.spinnerCollapsatorSize = this.uiComponentFactory.makeSpinner();
    this.spinnerCollapsatorSize.setModel(new SpinnerNumberModel(16, 3, 1024, 1));
    this.spinnerCollapsatorWidth = this.uiComponentFactory.makeSpinner();
    this.spinnerCollapsatorWidth.setModel(new SpinnerNumberModel(16, 3, 1024, 1));
    this.buttonColorCollapsatorFill =
        new ColorSelectButton(this.panel, this.uiComponentFactory, dialogProvider);
    this.buttonColorCollapsatorBorder =
        new ColorSelectButton(this.panel, this.uiComponentFactory, dialogProvider);
    this.buttonColorConnector =
        new ColorSelectButton(this.panel, this.uiComponentFactory, dialogProvider);
    this.buttonColorJumpLink =
        new ColorSelectButton(this.panel, this.uiComponentFactory, dialogProvider);
    this.fontChooserPanelMindMapTopicTitleFont =
        new FontSelectPanel(()->this.panel, bundle.getString("PreferencesPanel.fontSelectPanel.description"), this.uiComponentFactory, dialogProvider,
            this.panel.getFont());

    this.checkBoxSmartTextPaste = this.uiComponentFactory.makeCheckBox();

    this.comboBoxRenderQuality = this.uiComponentFactory.makeComboBox(RenderQuality.class);
    this.comboBoxRenderQuality.setModel(new DefaultComboBoxModel<>(RenderQuality.values()));

    this.comboBoxFastNavigationMouse = this.uiComponentFactory.makeComboBox(MouseButton.class);
    this.comboBoxFastNavigationMouse.setModel(new DefaultComboBoxModel<>(MouseButton.values()));

    this.keyModifiersWheelScale = new KeyModifiersSelector(this.uiComponentFactory);
    this.keyModifiersFastNavigation = new KeyModifiersSelector(this.uiComponentFactory);

    this.checkBoxDropShadow = this.uiComponentFactory.makeCheckBox();

    this.spinnerBorderWidth = this.uiComponentFactory.makeSpinner();
    this.spinnerBorderWidth.setModel(new SpinnerNumberModel(1.0d, 0.1d, 64.0d, 0.1d));

    this.sliderLevel1HorzGap = this.uiComponentFactory.makeSlider();
    this.sliderLevel1HorzGap.setMajorTickSpacing(30);
    this.sliderLevel1HorzGap.setPaintLabels(true);
    this.sliderLevel1HorzGap.setPaintTicks(true);
    this.sliderLevel1HorzGap.setMinimum(10);
    this.sliderLevel1HorzGap.setMaximum(250);

    this.sliderLevel1VertGap = this.uiComponentFactory.makeSlider();
    this.sliderLevel1VertGap.setMajorTickSpacing(30);
    this.sliderLevel1VertGap.setPaintLabels(true);
    this.sliderLevel1VertGap.setPaintTicks(true);
    this.sliderLevel1VertGap.setMinimum(10);
    this.sliderLevel1VertGap.setMaximum(250);

    this.sliderLevel2HorzGap = this.uiComponentFactory.makeSlider();
    this.sliderLevel2HorzGap.setMajorTickSpacing(30);
    this.sliderLevel2HorzGap.setPaintLabels(true);
    this.sliderLevel2HorzGap.setPaintTicks(true);
    this.sliderLevel2HorzGap.setMinimum(10);
    this.sliderLevel2HorzGap.setMaximum(250);

    this.sliderLevel2VertGap = this.uiComponentFactory.makeSlider();
    this.sliderLevel2VertGap.setMajorTickSpacing(30);
    this.sliderLevel2VertGap.setPaintLabels(true);
    this.sliderLevel2VertGap.setPaintTicks(true);
    this.sliderLevel2VertGap.setMinimum(10);
    this.sliderLevel2VertGap.setMaximum(250);

    this.spinnerSelectionFrameWidth = this.uiComponentFactory.makeSpinner();
    this.spinnerSelectionFrameWidth.setModel(new SpinnerNumberModel(1.0d, 0.1d, 64.0d, 0.1d));

    this.spinnerSelectionFrameGap = this.uiComponentFactory.makeSpinner();
    this.spinnerSelectionFrameGap.setModel(new SpinnerNumberModel(1, 1, 54, 1));

    this.buttonKeyShortcutEditor = this.uiComponentFactory.makeButton();
    this.buttonKeyShortcutEditor.setText(bundle.getString("PreferencesPanel.buttonEditKeyShortcuts.text"));
    this.buttonKeyShortcutEditor.addActionListener(a -> {
      final KeyShortCutEditor shortCutEditor = new KeyShortCutEditor(this.uiComponentFactory,
          new ArrayList<>(this.keyShortcutMap.values()),
          false);
      if (this.dialogProvider.msgOkCancel(this.panel, bundle.getString("PreferencesPanel.ShortcutEditor.title"), shortCutEditor.asPanel())) {
        for (final KeyShortcut s : shortCutEditor.getResult()) {
          this.keyShortcutMap.put(s.getID(), s);
        }
      }
    });

    this.beforePanelsCreate(uiComponentFactory);

    final JScrollPane scrollPane = this.uiComponentFactory.makeScrollPane();
    scrollPane.setViewportView(makeOptionsPanel(bundle));

    this.panel.add(scrollPane, BorderLayout.CENTER);

    final List<ButtonInfo> buttons = this.findButtonInfo();
    if (!buttons.isEmpty()) {
      this.panel.add(makeButtonPanel(buttons), BorderLayout.EAST);
    }
  }

  protected void beforePanelsCreate(final UIComponentFactory uiComponentFactory) {

  }

  public abstract List<ButtonInfo> findButtonInfo();

  private JPanel makeButtonPanel(final List<ButtonInfo> buttons){
    final JPanel panel = this.uiComponentFactory.makePanel();
    panel.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
    panel.setLayout(new GridBagLayout());
    final GridBagConstraints constraints = new GridBagConstraints();
    constraints.insets = new Insets(GRID_GAP, GRID_GAP, GRID_GAP, GRID_GAP);
    constraints.gridx = 0;
    constraints.weightx = 1;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.WEST;

    buttons.forEach(x -> {
      if (x.splitter) {
        panel.add(Box.createVerticalStrut(16),constraints);
      } else {
        final JButton button = x.supplier == null ? this.uiComponentFactory.makeButton() : x.supplier.get();
        button.setHorizontalAlignment(JButton.LEFT);
        button.setText(x.title);
        button.setToolTipText(x.tooltip);
        if (x.actionListener != null) {
          button.addActionListener(x.actionListener);
        }
        if (x.icon!=null) {
          button.setIcon(new ImageIcon(x.icon));
        }
        panel.add(button, constraints);
      }
    });

    constraints.weighty = 1000;
    panel.add(Box.createVerticalGlue(), constraints);

    return panel;
  }


  private void addGridBagRow(final JPanel panel, final String text,
                             final JComponent component) {
    final JLabel label = this.uiComponentFactory.makeLabel();
    label.setText(text);
    label.setHorizontalAlignment(JLabel.RIGHT);

    final GridBagConstraints gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.EAST;

    panel.add(label, gridBagConstraints);

    gridBagConstraints.gridx = 1;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.weighty = 10000.0d;

    panel.add(component, gridBagConstraints);
  }

  private JPanel makeColumn(final Component... component) {
    final JPanel result = new JPanel(new GridBagLayout());

    final GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 0;
    constraints.anchor = GridBagConstraints.CENTER;
    constraints.fill = GridBagConstraints.HORIZONTAL;

    for (final Component c : component) {
      if (c != null) {
        result.add(c, constraints);
      }
    }

    constraints.weighty = 10000.0d;
    result.add(Box.createVerticalGlue(), constraints);

    return result;
  }

  private JPanel makeOptionsPanel(final ResourceBundle bundle) {
    final JPanel panel = this.uiComponentFactory.makePanel();

    panel.setLayout(new GridBagLayout());
    final GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridy = 0;
    constraints.anchor = GridBagConstraints.NORTHWEST;
    constraints.fill = GridBagConstraints.BOTH;

    panel.add(makeColumn(
            makePaperOptions(bundle),
            makeConnectorAndCollapsatorOptions(bundle),
            makeFontAndKeyboardPanel(bundle),
            makeFeaturesOptions(bundle)
        ), constraints
    );

    panel.add(makeColumn(
            makeElementOptions(bundle),
            makeSelectionFrameOptions(bundle),
            makeMiscOptions(bundle)
        ), constraints
    );

    constraints.weightx = 1000.0d;
    panel.add(Box.createHorizontalGlue(), constraints);

    return panel;
  }

  private JPanel makePaperOptions(final ResourceBundle bundle) {
    final JPanel panel = this.uiComponentFactory.makePanel();
    panel.setBorder(BorderFactory.createTitledBorder(bundle.getString("PreferencesPanel.paperOptions.title")));

    panel.setLayout(new GridBagLayout());
    final GridBagConstraints constraints = new GridBagConstraints();
    constraints.weightx = 1;
    constraints.gridx = 0;
    constraints.anchor = GridBagConstraints.WEST;
    constraints.fill = GridBagConstraints.HORIZONTAL;

    final JPanel panelGridStep = new JPanel(new FlowLayout());
    final JLabel labelGridStep = this.uiComponentFactory.makeLabel();
    labelGridStep.setText(bundle.getString("PreferencesPanel.paperOptions.gridStep"));
    panelGridStep.add(labelGridStep);
    panelGridStep.add(this.spinnerGridStep);

    panel.add(panelGridStep, constraints);

    this.checkBoxShowGrid.setText(bundle.getString("PreferencesPanel.paperOptions.showGrid"));

    panel.add(this.checkBoxShowGrid, constraints);

    constraints.gridx = 1;
    panel.add(this.buttonColorGrid.setText(bundle.getString("PreferencesPanel.paperOptions.gridColor")).asButton(), constraints);
    panel.add(this.buttonColorPaper.setText(bundle.getString("PreferencesPanel.paperOptions.backColor")).asButton(), constraints);

    final JPanel panelRenderQuality = this.uiComponentFactory.makePanel();
    panelRenderQuality.setLayout(new BorderLayout());
    panelRenderQuality.setBorder(BorderFactory.createTitledBorder(bundle.getString("PreferencesPanel.paperOptions.renderQuality")));
    panelRenderQuality.add(this.comboBoxRenderQuality, BorderLayout.CENTER);

    constraints.gridx = 0;
    constraints.gridwidth = 2;
    panel.add(panelRenderQuality, constraints);

    return panel;
  }

  private JPanel makeConnectorAndCollapsatorOptions(final ResourceBundle bundle) {
    final JPanel panel = this.uiComponentFactory.makePanel();
    panel.setBorder(BorderFactory.createTitledBorder(bundle.getString("PreferencesPanel.conAndColOptions.title")));
    panel.setLayout(new GridBagLayout());

    addGridBagRow(panel, bundle.getString("PreferencesPanel.conAndColOptions.connectorWidth"), this.spinnerConnectorWidth);
    addGridBagRow(panel, bundle.getString("PreferencesPanel.conAndColOptions.jumpLinkWidth"), this.spinnerJumpLinkWidth);
    addGridBagRow(panel, bundle.getString("PreferencesPanel.conAndColOptions.collapsatorSize"), this.spinnerCollapsatorSize);
    addGridBagRow(panel, bundle.getString("PreferencesPanel.conAndColOptions.collapsatorWidth"), this.spinnerCollapsatorWidth);

    final JPanel colorButtons = this.uiComponentFactory.makePanel();
    colorButtons.setLayout(new GridLayout(2, 2, GRID_GAP, GRID_GAP));
    colorButtons.add(this.buttonColorCollapsatorFill.setText(bundle.getString("PreferencesPanel.conAndColOptions.collapsatorFill")).asButton());
    colorButtons.add(this.buttonColorCollapsatorBorder.setText(bundle.getString("PreferencesPanel.conAndColOptions.collapsatorBorder")).asButton());
    colorButtons.add(this.buttonColorJumpLink.setText(bundle.getString("PreferencesPanel.conAndColOptions.jumpLink")).asButton());
    colorButtons.add(this.buttonColorConnector.setText(bundle.getString("PreferencesPanel.conAndColOptions.connectorColor")).asButton());

    final GridBagConstraints constraints = new GridBagConstraints();
    constraints.insets = new Insets(GRID_GAP, GRID_GAP, GRID_GAP, GRID_GAP);
    constraints.anchor = GridBagConstraints.CENTER;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.weightx = 1;
    constraints.gridx = 0;
    constraints.gridwidth = 2;

    panel.add(colorButtons, constraints);

    return panel;
  }

  private JPanel makeMiscOptions(final ResourceBundle bundle) {
    final List<JComponent> components = this.findMiscComponents(this.uiComponentFactory);

    if (!components.isEmpty()) {
      final JPanel panel = this.uiComponentFactory.makePanel();
      panel.setBorder(BorderFactory.createTitledBorder(bundle.getString("PreferencesPanel.panelMisc.title")));
      panel.setLayout(new GridBagLayout());

      final GridBagConstraints constraints = new GridBagConstraints();
      constraints.anchor = GridBagConstraints.WEST;
      constraints.fill = GridBagConstraints.HORIZONTAL;
      constraints.weightx = 1;
      constraints.gridx = 0;

      components.forEach(x -> panel.add(x, constraints));

      return panel;
    } else {
      return null;
    }
  }

  public abstract List<JComponent> findMiscComponents(final UIComponentFactory componentFactory);

  private JPanel makeFeaturesOptions(final ResourceBundle bundle) {
    final JPanel panel = this.uiComponentFactory.makePanel();
    panel.setBorder(BorderFactory.createTitledBorder(bundle.getString("PreferencesPanel.features.title")));
    panel.setLayout(new GridBagLayout());

    final GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 0;
    constraints.weightx = 1;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.WEST;

    this.checkBoxSmartTextPaste.setText(bundle.getString("PreferencesPanel.checkSmartTextPaste"));
    panel.add(this.checkBoxSmartTextPaste, constraints);

    this.findFeaturesComponents(this.uiComponentFactory).forEach(c -> panel.add(c, constraints));

    return panel;
  }

  public abstract List<JComponent> findFeaturesComponents(final UIComponentFactory componentFactory);

  private JPanel makeFontAndKeyboardPanel(final ResourceBundle bundle) {
    final JPanel panel = this.uiComponentFactory.makePanel();
    panel.setBorder(BorderFactory.createTitledBorder(bundle.getString("PreferencesPanel.fontsandshorcuts.title")));

    panel.setLayout(new GridBagLayout());
    final GridBagConstraints constraints = new GridBagConstraints();
    constraints.anchor = GridBagConstraints.NORTH;
    constraints.gridx = 0;
    constraints.weightx = 1;
    constraints.fill = GridBagConstraints.HORIZONTAL;

    panel.add(this.buttonKeyShortcutEditor, constraints);

    JPanel panelInternal = this.uiComponentFactory.makePanel();
    panelInternal.setBorder(new TitledBorder(bundle.getString("PreferencesPanel.topictextfont.title")));
    panelInternal.setLayout(new BorderLayout());
    panelInternal.add(this.fontChooserPanelMindMapTopicTitleFont.asButton(), BorderLayout.CENTER);

    panel.add(panelInternal, constraints);

    panelInternal = this.keyModifiersWheelScale.asPanel();
    panelInternal.setBorder(new TitledBorder(bundle.getString("PreferencesPanel.activatorwheelscale.title")));
    panel.add(panelInternal, constraints);

    panelInternal = this.uiComponentFactory.makePanel();
    panelInternal.setBorder(new TitledBorder(bundle.getString("PreferencesPanel.fastnavigation.title")));
    panelInternal.setLayout(new BorderLayout());
    panelInternal.add(this.comboBoxFastNavigationMouse, BorderLayout.NORTH);
    panelInternal.add(this.keyModifiersFastNavigation.asPanel(), BorderLayout.CENTER);

    final JPanel buttonPanel = this.uiComponentFactory.makePanel();
    buttonPanel.setLayout(new GridLayout(1, 2, GRID_GAP, GRID_GAP));
    JButton button = this.buttonFastNavigationInk.asButton();
    button.setText(bundle.getString("PreferencesPanel.fastnavigation.buttonInk"));
    buttonPanel.add(button);
    button = this.buttonFastNavigationPaper.asButton();
    button.setText(bundle.getString("PreferencesPanel.fastnavigation.buttonPaper"));
    buttonPanel.add(button);
    panelInternal.add(buttonPanel, BorderLayout.SOUTH);

    panel.add(panelInternal, constraints);

    this.findFontAndKeyboardExtras(this.uiComponentFactory).forEach(x -> panel.add(x, constraints));

    return panel;
  }

  protected List<JComponent> findFontAndKeyboardExtras(final UIComponentFactory uiComponentFactory) {
    return Collections.emptyList();
  }

  private JPanel makeElementOptions(final ResourceBundle bundle) {
    final JPanel panel = this.uiComponentFactory.makePanel();
    panel.setLayout(new GridBagLayout());
    panel.setBorder(BorderFactory.createTitledBorder(bundle.getString("PreferencesPanel.elementioptions.title")));

    final GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 0;
    constraints.weightx = 1;
    constraints.anchor = GridBagConstraints.NORTHWEST;
    constraints.fill = GridBagConstraints.HORIZONTAL;

    final JPanel panelShadowAndBorder = new JPanel(new FlowLayout());
    this.checkBoxDropShadow.setText(bundle.getString("PreferencesPanel.dropShadow.text"));
    panelShadowAndBorder.add(this.checkBoxDropShadow);
    panelShadowAndBorder.add(Box.createHorizontalStrut(16));
    JLabel label = this.uiComponentFactory.makeLabel();
    label.setText(bundle.getString("PreferencesPanel.labelBorderWidth.text"));
    panelShadowAndBorder.add(label);
    panelShadowAndBorder.add(this.spinnerBorderWidth);

    panel.add(panelShadowAndBorder, constraints);

    JPanel intPanel = this.uiComponentFactory.makePanel();
    intPanel.setLayout(new GridLayout(3, 2, GRID_GAP, GRID_GAP));
    intPanel.add(this.buttonColorRootFill.setText(bundle.getString("PreferencesPanel.buttonRootFill.text")).asButton());
    intPanel.add(this.buttonColorRootText.setText(bundle.getString("PreferencesPanel.buttonRootText.text")).asButton());
    intPanel.add(this.buttonColorLevel1Fill.setText(bundle.getString("PreferencesPanel.button1levelFill.text")).asButton());
    intPanel.add(this.buttonColorLevel1Text.setText(bundle.getString("PreferencesPanel.button1levelText.text")).asButton());
    intPanel.add(this.buttonColorLevel2Fill.setText(bundle.getString("PreferencesPanel.button2levelFill.text")).asButton());
    intPanel.add(this.buttonColorLevel2Text.setText(bundle.getString("PreferencesPanel.button2levelText.text")).asButton());

    panel.add(intPanel, constraints);

    intPanel = this.uiComponentFactory.makePanel();
    intPanel.setBorder(BorderFactory.createTitledBorder(bundle.getString("PreferencesPanel.gap1levelHorz.title")));
    intPanel.setLayout(new BorderLayout());
    intPanel.add(this.sliderLevel1HorzGap, BorderLayout.CENTER);

    panel.add(intPanel, constraints);

    intPanel = this.uiComponentFactory.makePanel();
    intPanel.setBorder(BorderFactory.createTitledBorder(bundle.getString("PreferencesPanel.gap1levelVert.title")));
    intPanel.setLayout(new BorderLayout());
    intPanel.add(this.sliderLevel1VertGap, BorderLayout.CENTER);

    panel.add(intPanel, constraints);

    intPanel = this.uiComponentFactory.makePanel();
    intPanel.setBorder(BorderFactory.createTitledBorder(bundle.getString("PreferencesPanel.gap2levelHorz.title")));
    intPanel.setLayout(new BorderLayout());
    intPanel.add(this.sliderLevel2HorzGap, BorderLayout.CENTER);

    panel.add(intPanel, constraints);

    intPanel = this.uiComponentFactory.makePanel();
    intPanel.setBorder(BorderFactory.createTitledBorder(bundle.getString("PreferencesPanel.gap2levelVert.title")));
    intPanel.setLayout(new BorderLayout());
    intPanel.add(this.sliderLevel2VertGap, BorderLayout.CENTER);

    panel.add(intPanel, constraints);

    return panel;
  }

  private JPanel makeSelectionFrameOptions(final ResourceBundle bundle) {
    final JPanel panel = this.uiComponentFactory.makePanel();
    panel.setLayout(new GridBagLayout());
    panel.setBorder(BorderFactory.createTitledBorder(bundle.getString("PreferencesPanel.selectionFrameOptions.title")));

    final GridBagConstraints constraints = new GridBagConstraints();
    constraints.insets = new Insets(GRID_GAP, GRID_GAP, GRID_GAP, GRID_GAP);
    constraints.anchor = GridBagConstraints.NORTHWEST;
    constraints.weightx = 1;
    constraints.fill = GridBagConstraints.HORIZONTAL;

    constraints.gridx = 0;
    panel.add(Box.createHorizontalGlue());
    constraints.gridx = 1;
    panel.add(this.buttonColorSelectFrame.setText(bundle.getString("PreferencesPanel.selectionFrameOptions.colorSelectFrame")).asButton(), constraints);

    constraints.gridx = 0;
    JLabel label = this.uiComponentFactory.makeLabel();
    label.setHorizontalAlignment(JLabel.RIGHT);
    label.setText(bundle.getString("PreferencesPanel.selectionFrameOptions.frameWidth"));
    panel.add(label, constraints);
    constraints.gridx = 1;
    panel.add(this.spinnerSelectionFrameWidth, constraints);

    constraints.gridx = 0;
    label = this.uiComponentFactory.makeLabel();
    label.setHorizontalAlignment(JLabel.RIGHT);
    label.setText(bundle.getString("PreferencesPanel.selectionFrameOptions.frameGap"));
    panel.add(label, constraints);
    constraints.gridx = 1;
    panel.add(this.spinnerSelectionFrameGap, constraints);

    return panel;
  }

  private final Map<String,KeyShortcut> keyShortcutMap = new HashMap<>();

  public MindMapPanelConfig save() {
    final MindMapPanelConfig config = new MindMapPanelConfig();

    config.setPaperColor(this.buttonColorPaper.getValue());
    config.setGridColor(this.buttonColorGrid.getValue());

    config.setBirdseyeFront(this.buttonFastNavigationInk.getValue());
    config.setBirdseyeBackground(this.buttonFastNavigationPaper.getValue());

    config.setShowGrid(this.checkBoxShowGrid.isSelected());
    config.setGridStep(((Number)this.spinnerGridStep.getValue()).intValue());
    config.setRenderQuality((RenderQuality) this.comboBoxRenderQuality.getSelectedItem());
    config.setConnectorWidth(((Number)this.spinnerConnectorWidth.getValue()).floatValue());
    config.setJumpLinkWidth(((Number)this.spinnerJumpLinkWidth.getValue()).floatValue());
    config.setCollapsatorBorderWidth(((Number)this.spinnerCollapsatorWidth.getValue()).floatValue());
    config.setCollapsatorSize(((Number)this.spinnerCollapsatorSize.getValue()).intValue());
    config.setCollapsatorBackgroundColor(this.buttonColorCollapsatorFill.getValue());
    config.setCollapsatorBorderColor(this.buttonColorCollapsatorBorder.getValue());
    config.setJumpLinkColor(this.buttonColorJumpLink.getValue());
    config.setConnectorColor(this.buttonColorConnector.getValue());
    config.setFont(this.fontChooserPanelMindMapTopicTitleFont.getValue());

    config.setSmartTextPaste(this.checkBoxSmartTextPaste.isSelected());
    config.setKeyShortcutMap(this.keyShortcutMap);

    config.setScaleModifiers(this.keyModifiersWheelScale.getModifiers());
    config.setBirdseyeMouseButton((MouseButton) this.comboBoxFastNavigationMouse.getSelectedItem());
    config.setKeyShortCut(new KeyShortcut(MindMapPanelConfig.KEY_BIRDSEYE_MODIFIERS, this.keyModifiersFastNavigation.getModifiers()));

    config.setDropShadow(this.checkBoxDropShadow.isSelected());
    config.setElementBorderWidth(((Number)this.spinnerBorderWidth.getValue()).floatValue());

    config.setRootBackgroundColor(this.buttonColorRootFill.getValue());
    config.setRootTextColor(this.buttonColorRootText.getValue());

    config.setFirstLevelBackgroundColor(this.buttonColorLevel1Fill.getValue());
    config.setFirstLevelTextColor(this.buttonColorLevel1Text.getValue());

    config.setOtherLevelBackgroundColor(this.buttonColorLevel2Fill.getValue());
    config.setOtherLevelTextColor(this.buttonColorLevel2Text.getValue());

    config.setFirstLevelHorizontalInset(this.sliderLevel1HorzGap.getValue());
    config.setFirstLevelVerticalInset(this.sliderLevel1VertGap.getValue());

    config.setOtherLevelHorizontalInset(this.sliderLevel2HorzGap.getValue());
    config.setOtherLevelVerticalInset(this.sliderLevel2VertGap.getValue());

    config.setSelectLineColor(this.buttonColorSelectFrame.getValue());
    config.setSelectLineGap(((Number)this.spinnerSelectionFrameGap.getValue()).intValue());
    config.setSelectLineWidth(((Number)this.spinnerSelectionFrameWidth.getValue()).floatValue());

    this.onSave(config);

    return config;
  }

  public abstract void onSave(MindMapPanelConfig config);

  public AbstractPreferencesPanel load(final MindMapPanelConfig config) {
    this.buttonColorPaper.setValue(config.getPaperColor());
    this.buttonColorGrid.setValue(config.getGridColor());
    this.buttonFastNavigationInk.setValue(config.getBirdseyeFront());
    this.buttonFastNavigationPaper.setValue(config.getBirdseyeBackground());
    this.checkBoxShowGrid.setSelected(config.isShowGrid());
    this.spinnerGridStep.setValue(config.getGridStep());
    this.comboBoxRenderQuality.setSelectedItem(config.getRenderQuality());
    this.spinnerConnectorWidth.setValue(config.getConnectorWidth());
    this.spinnerJumpLinkWidth.setValue(config.getJumpLinkWidth());
    this.spinnerCollapsatorWidth.setValue(config.getCollapsatorBorderWidth());
    this.spinnerCollapsatorSize.setValue(config.getCollapsatorSize());
    this.buttonColorCollapsatorFill.setValue(config.getCollapsatorBackgroundColor());
    this.buttonColorCollapsatorBorder.setValue(config.getCollapsatorBorderColor());
    this.buttonColorJumpLink.setValue(config.getJumpLinkColor());
    this.buttonColorConnector.setValue(config.getConnectorColor());
    this.fontChooserPanelMindMapTopicTitleFont.setValue(config.getFont());

    this.checkBoxSmartTextPaste.setSelected(config.isSmartTextPaste());

    this.keyShortcutMap.clear();
    this.keyShortcutMap.putAll(config.getKeyShortcutMap());

    this.keyModifiersWheelScale.setModifiers(config.getScaleModifiers());
    this.comboBoxFastNavigationMouse.setSelectedItem(config.getBirdseyeMouseButton());
    this.keyModifiersFastNavigation.setModifiers(config.getKeyShortcutMap().get(MindMapPanelConfig.KEY_BIRDSEYE_MODIFIERS).getModifiers());

    this.checkBoxDropShadow.setSelected(config.isDropShadow());
    this.spinnerBorderWidth.setValue(config.getElementBorderWidth());

    this.buttonColorRootFill.setValue(config.getRootBackgroundColor());
    this.buttonColorRootText.setValue(config.getRootTextColor());

    this.buttonColorLevel1Fill.setValue(config.getFirstLevelBackgroundColor());
    this.buttonColorLevel1Text.setValue(config.getFirstLevelTextColor());

    this.buttonColorLevel2Fill.setValue(config.getOtherLevelBackgroundColor());
    this.buttonColorLevel2Text.setValue(config.getOtherLevelTextColor());

    this.sliderLevel1HorzGap.setValue(config.getFirstLevelHorizontalInset());
    this.sliderLevel1VertGap.setValue(config.getFirstLevelVerticalInset());

    this.sliderLevel2HorzGap.setValue(config.getOtherLevelHorizontalInset());
    this.sliderLevel2VertGap.setValue(config.getOtherLevelVerticalInset());

    this.buttonColorSelectFrame.setValue(config.getSelectLineColor());
    this.spinnerSelectionFrameGap.setValue(config.getSelectLineGap());
    this.spinnerSelectionFrameWidth.setValue(config.getSelectLineWidth());

    this.onLoad(config);

    return this;
  }

  public abstract void onLoad(MindMapPanelConfig config);

  public MindMapPanelConfig make() {
    return null;
  }

  public JPanel getPanel() {
    return this.panel;
  }

  public static class KeyModifiersSelector {
    private final JCheckBox checkCtrl;
    private final JCheckBox checkAlt;
    private final JCheckBox checkShift;
    private final JCheckBox checkMeta;
    private final JPanel panel;

    public KeyModifiersSelector(final UIComponentFactory componentFactory) {
      this.panel = componentFactory.makePanel();
      this.panel.setLayout(new GridLayout(1, 4));
      this.checkShift = componentFactory.makeCheckBox();
      this.checkShift.setText("SHIFT");
      this.checkAlt = componentFactory.makeCheckBox();
      this.checkAlt.setText("ALT");
      this.checkCtrl = componentFactory.makeCheckBox();
      this.checkCtrl.setText("CTRL");
      this.checkMeta = componentFactory.makeCheckBox();
      this.checkMeta.setText("META");
      this.panel.add(this.checkCtrl);
      this.panel.add(this.checkAlt);
      this.panel.add(this.checkShift);
      this.panel.add(this.checkMeta);
    }

    public JPanel asPanel() {
      return this.panel;
    }

    public int getModifiers() {
      return (this.checkCtrl.isSelected() ? KeyEvent.CTRL_MASK : 0)
          | (this.checkMeta.isSelected() ? KeyEvent.META_MASK : 0)
          | (this.checkAlt.isSelected() ? KeyEvent.ALT_MASK : 0)
          | (this.checkShift.isSelected() ? KeyEvent.SHIFT_MASK : 0);
    }

    public KeyModifiersSelector setModifiers(final int value) {
      this.checkCtrl.setSelected((value & KeyEvent.CTRL_MASK) != 0);
      this.checkMeta.setSelected((value & KeyEvent.META_MASK) != 0);
      this.checkAlt.setSelected((value & KeyEvent.ALT_MASK) != 0);
      this.checkShift.setSelected((value & KeyEvent.SHIFT_MASK) != 0);
      return this;
    }
  }

}
