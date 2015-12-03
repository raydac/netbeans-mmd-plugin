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
package com.igormaznitsa.ideamindmap.settings;

import com.igormaznitsa.ideamindmap.swing.AboutForm;
import com.igormaznitsa.ideamindmap.swing.ColorChooserButton;
import com.igormaznitsa.ideamindmap.swing.FontSelector;
import com.igormaznitsa.ideamindmap.swing.KeyShortCutEditPanel;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.mindmap.swing.panel.utils.KeyShortcut;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MindMapSettingsPanel {
  private JPanel mainPanel;
  private JSpinner spinnerGridStep;
  private JCheckBox checkBoxShowGrid;
  private ColorChooserButton colorButtonGridColor;
  private ColorChooserButton colorButtonBackgroundColor;
  private JSpinner spinnerConnectorWidth;
  private JSpinner spinnerJumpLinkWidth;
  private JSpinner spinnerCollapsatorSize;
  private JSpinner spinnerCollapsatorWidth;
  private ColorChooserButton colorButtonCollapsatorFill;
  private ColorChooserButton colorButtonJumpLink;
  private ColorChooserButton colorButtonCollapsatorBorder;
  private ColorChooserButton colorButtonConnectorColor;
  private JSpinner spinnerSelectionFrameWidth;
  private JSpinner spinnerSelectionFrameGap;
  private ColorChooserButton colorButtonSelectFrameColor;
  private JCheckBox checkBoxDropShadow;
  private JSlider slider1stLevelHorzGap;
  private JSlider slider1stLevelVertGap;
  private JSlider slider2ndLevelHorzGap;
  private JSlider slider2ndLevelVertGap;
  private JSpinner spinnerBorderWidth;
  private JButton buttonFont;
  private JButton buttonAbout;
  private ColorChooserButton colorButtonRootFill;
  private ColorChooserButton colorButtonRootText;
  private ColorChooserButton colorButton1stLevelFill;
  private ColorChooserButton colorButton1stLevelText;
  private ColorChooserButton colorButton2ndLevelFill;
  private ColorChooserButton colorButton2ndLevelText;
  private JButton buttonEditKeyShortcuts;
  private JCheckBox checkBoxScalingModifierALT;
  private JCheckBox checkBoxScalingModifierCTRL;
  private JCheckBox checkBoxScalingModifierSHFT;
  private JCheckBox checkBoxScalingModifierMETA;

  private static final Logger LOGGER = LoggerFactory.getLogger(MindMapSettingsPanel.class);

  private final MindMapSettingsComponent controller;

  private final MindMapPanelConfig etalon = new MindMapPanelConfig();
  private Font theFont;

  private final Map<String,KeyShortcut> mapKeyShortCuts = new TreeMap<String,KeyShortcut>(new Comparator<String>(){
    @Override public int compare(String o1, String o2) {
      return o1.compareTo(o2);
    }
  });

  private static class DialogComponent extends DialogWrapper {
    private final JComponent component;

    public DialogComponent(final Component parent, final String title, final Component component) {
      super(parent, true);
      final JPanel panel = new JPanel(new BorderLayout(0, 0));
      panel.add(component, BorderLayout.CENTER);
      this.component = panel;
      init();
      setTitle(title);
      pack();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
      return this.component;
    }
  }

  public MindMapSettingsPanel(final MindMapSettingsComponent controller) {
    this.controller = controller;

    this.spinnerGridStep.setModel(makeIntSpinnerModel(2, 500, 1));
    this.spinnerConnectorWidth.setModel(makeFloatSpinnerModel(0.05f, 20.0f, 0.01f));
    this.spinnerJumpLinkWidth.setModel(makeFloatSpinnerModel(0.05f, 20.0f, 0.01f));
    this.spinnerCollapsatorSize.setModel(makeIntSpinnerModel(3, 500, 1));
    this.spinnerCollapsatorWidth.setModel(makeFloatSpinnerModel(0.01f, 100.0f, 0.1f));
    this.spinnerSelectionFrameWidth.setModel(makeFloatSpinnerModel(0.02f, 100.0f, 0.1f));
    this.spinnerSelectionFrameGap.setModel(makeIntSpinnerModel(1, 500, 1));
    this.spinnerBorderWidth.setModel(makeFloatSpinnerModel(0.05f, 50.0f, 0.1f));

    buttonAbout.addActionListener(new ActionListener() {
      @Override public void actionPerformed(final ActionEvent e) {
        AboutForm.show(mainPanel);
      }
    });

    buttonFont.addActionListener(new ActionListener() {
      @Override public void actionPerformed(ActionEvent e) {
        final FontSelector fontSelector = new FontSelector(theFont);
        if (new DialogComponent(mainPanel, "Select font", fontSelector.getPanel()).showAndGet()) {
          theFont = fontSelector.getValue();
          updateFontButton();
        }

      }
    });
    buttonEditKeyShortcuts.addActionListener(new ActionListener() {
      @Override public void actionPerformed(ActionEvent evt) {
        final List<KeyShortcut> list = new ArrayList<KeyShortcut>();
        for(final Map.Entry<String,KeyShortcut> e : mapKeyShortCuts.entrySet()){
          list.add(e.getValue());
        }

        final KeyShortCutEditPanel editPanel = new KeyShortCutEditPanel(list);
        if (new DialogComponent(mainPanel, "Edit shortcuts", editPanel).showAndGet()){
          for(final KeyShortcut k : editPanel.getResult()){
            mapKeyShortCuts.put(k.getID(),k);
          }
        }
      }
    });
  }

  private static double toDouble(final Number number){
    return number.doubleValue();
  }

  public void reset(final MindMapPanelConfig config) {
    this.etalon.makeFullCopyOf(config, false, false);

    this.colorButtonBackgroundColor.setValue(this.etalon.getPaperColor());
    this.colorButtonGridColor.setValue(this.etalon.getGridColor());
    this.colorButtonCollapsatorFill.setValue(this.etalon.getCollapsatorBackgroundColor());
    this.colorButtonCollapsatorBorder.setValue(this.etalon.getCollapsatorBorderColor());
    this.colorButtonConnectorColor.setValue(this.etalon.getConnectorColor());
    this.colorButtonJumpLink.setValue(this.etalon.getJumpLinkColor());
    this.colorButtonSelectFrameColor.setValue(this.etalon.getSelectLineColor());

    this.colorButtonRootFill.setValue(this.etalon.getRootBackgroundColor());
    this.colorButtonRootText.setValue(this.etalon.getRootTextColor());
    this.colorButton1stLevelFill.setValue(this.etalon.getFirstLevelBackgroundColor());
    this.colorButton1stLevelText.setValue(this.etalon.getFirstLevelTextColor());
    this.colorButton2ndLevelFill.setValue(this.etalon.getOtherLevelBackgroundColor());
    this.colorButton2ndLevelText.setValue(this.etalon.getOtherLevelTextColor());

    this.checkBoxShowGrid.setSelected(this.etalon.isShowGrid());
    this.checkBoxDropShadow.setSelected(this.etalon.isDropShadow());

    this.checkBoxScalingModifierALT.setSelected((this.etalon.getScaleModifiers() & KeyEvent.ALT_MASK)!=0);
    this.checkBoxScalingModifierCTRL.setSelected((this.etalon.getScaleModifiers() & KeyEvent.CTRL_MASK)!=0);
    this.checkBoxScalingModifierMETA.setSelected((this.etalon.getScaleModifiers() & KeyEvent.META_MASK)!=0);
    this.checkBoxScalingModifierSHFT.setSelected((this.etalon.getScaleModifiers() & KeyEvent.SHIFT_MASK)!=0);

    this.spinnerGridStep.setValue(this.etalon.getGridStep());
    this.spinnerCollapsatorSize.setValue(this.etalon.getCollapsatorSize());
    this.spinnerCollapsatorWidth.setValue(toDouble(this.etalon.getCollapsatorBorderWidth()));
    this.spinnerConnectorWidth.setValue(toDouble(this.etalon.getConnectorWidth()));
    this.spinnerJumpLinkWidth.setValue(toDouble(this.etalon.getJumpLinkWidth()));
    this.spinnerSelectionFrameWidth.setValue(toDouble(this.etalon.getSelectLineWidth()));
    this.spinnerSelectionFrameGap.setValue(this.etalon.getSelectLineGap());
    this.spinnerBorderWidth.setValue(toDouble(this.etalon.getElementBorderWidth()));

    this.slider1stLevelHorzGap.setValue(this.etalon.getFirstLevelHorizontalInset());
    this.slider1stLevelVertGap.setValue(this.etalon.getFirstLevelVerticalInset());

    this.slider2ndLevelHorzGap.setValue(this.etalon.getOtherLevelHorizontalInset());
    this.slider2ndLevelVertGap.setValue(this.etalon.getOtherLevelVerticalInset());

    this.theFont = this.etalon.getFont();

    this.mapKeyShortCuts.clear();
    for(final Map.Entry<String,KeyShortcut> e : this.etalon.getKeyShortcutMap().entrySet()){
      this.mapKeyShortCuts.put(e.getKey(),e.getValue());
    }

    updateFontButton();
  }

  private void updateFontButton() {
    final String strStyle;

    if (theFont.isBold()) {
      strStyle = theFont.isItalic() ? "bolditalic" : "bold";
    }
    else {
      strStyle = theFont.isItalic() ? "italic" : "plain";
    }

    this.buttonFont.setText(theFont.getFamily() + ", " + strStyle + ", " + theFont.getSize());
  }

  private static int getInt(final JSpinner spinner) {
    return ((Number) spinner.getValue()).intValue();
  }

  private static float getFloat(final JSpinner spinner) {
    return ((Number) spinner.getValue()).floatValue();
  }

  private static SpinnerModel makeIntSpinnerModel(final int min, final int max, final int step) {
    return new SpinnerNumberModel(min, min, max, step);
  }

  private static SpinnerModel makeFloatSpinnerModel(final double min, final double max, final double step) {
    return new SpinnerNumberModel(min, min, max, step);
  }

  public JPanel getPanel() {
    return this.mainPanel;
  }

  private void createUIComponents() {
    colorButtonBackgroundColor = new ColorChooserButton();
    colorButtonGridColor = new ColorChooserButton();
    colorButtonCollapsatorBorder = new ColorChooserButton();
    colorButtonCollapsatorFill = new ColorChooserButton();
    colorButtonJumpLink = new ColorChooserButton();
    colorButtonConnectorColor = new ColorChooserButton();
    colorButtonSelectFrameColor = new ColorChooserButton();

    colorButtonRootFill = new ColorChooserButton();
    colorButtonRootText = new ColorChooserButton();
    colorButton1stLevelFill = new ColorChooserButton();
    colorButton1stLevelText = new ColorChooserButton();
    colorButton2ndLevelFill = new ColorChooserButton();
    colorButton2ndLevelText = new ColorChooserButton();
  }

  public MindMapPanelConfig makeConfig() {
    final MindMapPanelConfig result = new MindMapPanelConfig(this.etalon, false);

    result.setPaperColor(this.colorButtonBackgroundColor.getValue());
    result.setGridColor(this.colorButtonGridColor.getValue());
    result.setCollapsatorBackgroundColor(this.colorButtonCollapsatorFill.getValue());
    result.setCollapsatorBorderColor(this.colorButtonCollapsatorBorder.getValue());
    result.setConnectorColor(this.colorButtonConnectorColor.getValue());
    result.setJumpLinkColor(this.colorButtonJumpLink.getValue());
    result.setSelectLineColor(this.colorButtonSelectFrameColor.getValue());

    result.setRootBackgroundColor(this.colorButtonRootFill.getValue());
    result.setRootTextColor(this.colorButtonRootText.getValue());
    result.setFirstLevelBackgroundColor(this.colorButton1stLevelFill.getValue());
    result.setFirstLevelTextColor(this.colorButton1stLevelText.getValue());
    result.setOtherLevelBackgroundColor(this.colorButton2ndLevelFill.getValue());
    result.setOtherLevelTextColor(this.colorButton2ndLevelText.getValue());

    result.setGridStep(getInt(this.spinnerGridStep));
    result.setConnectorWidth(getFloat(this.spinnerConnectorWidth));
    result.setCollapsatorSize(getInt(this.spinnerCollapsatorSize));
    result.setCollapsatorBorderWidth(getFloat(this.spinnerCollapsatorWidth));
    result.setJumpLinkWidth(getFloat(this.spinnerJumpLinkWidth));
    result.setSelectLineWidth(getFloat(this.spinnerSelectionFrameWidth));
    result.setSelectLineGap(getInt(this.spinnerSelectionFrameGap));
    result.setElementBorderWidth(getFloat(this.spinnerBorderWidth));

    result.setShowGrid(this.checkBoxShowGrid.isSelected());
    result.setDropShadow(this.checkBoxDropShadow.isSelected());

    result.setFirstLevelHorizontalInset(this.slider1stLevelHorzGap.getValue());
    result.setFirstLevelVerticalInset(this.slider1stLevelVertGap.getValue());

    result.setOtherLevelHorizontalInset(this.slider2ndLevelHorzGap.getValue());
    result.setOtherLevelVerticalInset(this.slider2ndLevelVertGap.getValue());

    result.setFont(this.theFont);

    final int scaleModifier = (this.checkBoxScalingModifierALT.isSelected() ? KeyEvent.ALT_MASK : 0)
      | (this.checkBoxScalingModifierCTRL.isSelected() ? KeyEvent.CTRL_MASK: 0)
      | (this.checkBoxScalingModifierSHFT.isSelected() ? KeyEvent.SHIFT_MASK: 0)
      | (this.checkBoxScalingModifierMETA.isSelected() ? KeyEvent.META_MASK: 0);

    result.setScaleModifiers(scaleModifier);

    for(final Map.Entry<String,KeyShortcut> e : this.mapKeyShortCuts.entrySet()){
      result.setKeyShortCut(e.getValue());
    }

    return result;
  }

  public boolean isModified() {
    final MindMapPanelConfig current = makeConfig();
    return this.etalon.hasDifferenceInParameters(current);
  }
}
