package com.igormaznitsa.ideamindmap.facet;

import com.igormaznitsa.ideamindmap.swing.AboutForm;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MindMapFacetPanel {
  private JCheckBox checkBoxUseInternalBrowser;
  private JCheckBox checkBoxMakeRelativePath;
  private JPanel mainPanel;
  private JCheckBox checkBoxCopyColorInfoFromParent;
  private JCheckBox checkBoxUnfoldCollapsedDropTarget;
  private JButton buttonAbout;

  private final MindMapFacetEditorTab controller;

  public MindMapFacetPanel(final MindMapFacetEditorTab controller) {
    this.controller = controller;
    buttonAbout.addActionListener(new ActionListener() {
      @Override public void actionPerformed(ActionEvent e) {
        AboutForm.show(mainPanel);
      }
    });
  }

  public JPanel getPanel(){
    return mainPanel;
  }

  public boolean isChanged(){
    boolean result = false;

    result |= this.controller.getConfiguration().isUseInsideBrowser() ^ this.checkBoxUseInternalBrowser.isSelected();
    result |= this.controller.getConfiguration().isMakeRelativePath() ^ this.checkBoxMakeRelativePath.isSelected();
    result |= this.controller.getConfiguration().isCopyColorInformationFromParent() ^ this.checkBoxCopyColorInfoFromParent.isSelected();
    result |= this.controller.getConfiguration().isUnfoldTopicWhenItIsDropTarget() ^ this.checkBoxUnfoldCollapsedDropTarget.isSelected();

    return result;
  }

  public void save(){
    this.controller.getConfiguration().setMakeRelativePath(this.checkBoxMakeRelativePath.isSelected());
    this.controller.getConfiguration().setUseInsideBrowser(this.checkBoxUseInternalBrowser.isSelected());
    this.controller.getConfiguration().setCopyColorInformationFromParent(this.checkBoxCopyColorInfoFromParent.isSelected());
    this.controller.getConfiguration().setUnfoldTopicWhenItIsDropTarget(this.checkBoxUnfoldCollapsedDropTarget.isSelected());
  }

  public void reset(){
    this.checkBoxUseInternalBrowser.setSelected(this.controller.getConfiguration().isUseInsideBrowser());
    this.checkBoxMakeRelativePath.setSelected(this.controller.getConfiguration().isMakeRelativePath());
    this.checkBoxCopyColorInfoFromParent.setSelected(this.controller.getConfiguration().isCopyColorInformationFromParent());
    this.checkBoxUnfoldCollapsedDropTarget.setSelected(this.controller.getConfiguration().isUnfoldTopicWhenItIsDropTarget());
  }

}
