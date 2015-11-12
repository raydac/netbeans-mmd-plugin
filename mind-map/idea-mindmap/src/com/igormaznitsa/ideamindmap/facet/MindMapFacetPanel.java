package com.igormaznitsa.ideamindmap.facet;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

public class MindMapFacetPanel {
  private JCheckBox checkBoxUseInternalBrowser;
  private JCheckBox comboBoxMakeRelativePath;
  private JPanel mainPanel;

  private final MindMapFacetEditorTab controller;

  public MindMapFacetPanel(final MindMapFacetEditorTab controller) {
    this.controller = controller;
  }

  public JPanel getPanel(){
    return mainPanel;
  }

  public boolean isChanged(){
    boolean result = false;

    result |= this.controller.getConfiguration().isUseInsideBrowser() ^ this.checkBoxUseInternalBrowser.isSelected();
    result |= this.controller.getConfiguration().isMakeRelativePath() ^ this.comboBoxMakeRelativePath.isSelected();

    return result;
  }

  public void save(){
    this.controller.getConfiguration().setMakeRelativePath(this.comboBoxMakeRelativePath.isSelected());
    this.controller.getConfiguration().setUseInsideBrowser(this.checkBoxUseInternalBrowser.isSelected());
  }

  public void reset(){
    this.checkBoxUseInternalBrowser.setSelected(this.controller.getConfiguration().isUseInsideBrowser());
    this.comboBoxMakeRelativePath.setSelected(this.controller.getConfiguration().isMakeRelativePath());
  }

}
