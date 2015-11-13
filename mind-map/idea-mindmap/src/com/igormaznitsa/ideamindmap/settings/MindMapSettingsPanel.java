package com.igormaznitsa.ideamindmap.settings;

import com.igormaznitsa.ideamindmap.swing.ColorChooserButton;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;

public class MindMapSettingsPanel {
  private JPanel mainPanel;
  private JSpinner spinnerGridStep;
  private JCheckBox checkBoxShowGrid;
  private ColorChooserButton colorButtonGridColor;
  private ColorChooserButton colorButtonBackgroundColor;

  private final MindMapSettingsComponent controller;

  private final MindMapPanelConfig etalon = new MindMapPanelConfig();

  public MindMapSettingsPanel(final MindMapSettingsComponent controller){
    this.controller = controller;
  }

  public void reset(final MindMapPanelConfig config){
    this.etalon.makeFullCopyOf(config,false,false);

    this.colorButtonBackgroundColor.setValue(this.etalon.getPaperColor());
    this.colorButtonGridColor.setValue(this.etalon.getGridColor());
    this.checkBoxShowGrid.setSelected(this.etalon.isShowGrid());
    this.spinnerGridStep.setValue(this.etalon.getGridStep());
  }

  public JPanel getPanel(){
    return this.mainPanel;
  }

  private void createUIComponents() {
    colorButtonGridColor = new ColorChooserButton();
    colorButtonBackgroundColor = new ColorChooserButton();
  }

  public MindMapPanelConfig makeConfig() {
    final MindMapPanelConfig result = new MindMapPanelConfig(this.etalon,false);

    result.setGridStep((int)this.spinnerGridStep.getValue());
    result.setShowGrid(this.checkBoxShowGrid.isSelected());
    result.setPaperColor(this.colorButtonBackgroundColor.getValue());
    result.setGridColor(this.colorButtonGridColor.getValue());

    return result;
  }

  public boolean isModified() {
    final MindMapPanelConfig current = makeConfig();
    return this.etalon.hasDifferenceInParameters(current);
  }
}
