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

package com.igormaznitsa.ideamindmap.facet;

import com.igormaznitsa.ideamindmap.swing.AboutForm;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MindMapFacetPanel {
  private final MindMapFacetEditorTab controller;
  private JCheckBox checkBoxUseInternalBrowser;
  private JCheckBox checkBoxMakeRelativePath;
  private JCheckBox checkBoxTrimTopicTextBeforeSet;
  private JPanel mainPanel;
  private JCheckBox checkBoxCopyColorInfoFromParent;
  private JCheckBox checkBoxUnfoldCollapsedDropTarget;
  private JButton buttonAbout;
  private JCheckBox checkBoxDisableAutocreateProjectKnowledge;
  private JCheckBox checkBoxUseProjectBaseFoderAsRoot;

  public MindMapFacetPanel(final MindMapFacetEditorTab controller) {
    this.controller = controller;
    buttonAbout.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        AboutForm.show(mainPanel);
      }
    });
  }

  public JPanel getPanel() {
    return mainPanel;
  }

  public boolean isChanged() {
    boolean result = false;

    result |= this.controller.getConfiguration().isUseInsideBrowser() ^ this.checkBoxUseInternalBrowser.isSelected();
    result |= this.controller.getConfiguration().isTrimTopicTextBeforeSet() ^ this.checkBoxTrimTopicTextBeforeSet.isSelected();
    result |= this.controller.getConfiguration().isUseProjectBaseFolderAsRoot() ^ this.checkBoxUseProjectBaseFoderAsRoot.isSelected();
    result |= this.controller.getConfiguration().isMakeRelativePath() ^ this.checkBoxMakeRelativePath.isSelected();
    result |= this.controller.getConfiguration().isCopyColorInformationFromParent() ^ this.checkBoxCopyColorInfoFromParent.isSelected();
    result |= this.controller.getConfiguration().isUnfoldTopicWhenItIsDropTarget() ^ this.checkBoxUnfoldCollapsedDropTarget.isSelected();
    result |= this.controller.getConfiguration().isDisableAutoCreateProjectKnowledgeFolder() ^ this.checkBoxDisableAutocreateProjectKnowledge.isSelected();

    return result;
  }

  public void save() {
    this.controller.getConfiguration().setMakeRelativePath(this.checkBoxMakeRelativePath.isSelected());
    this.controller.getConfiguration().setTrimTopicTextBeforeSet(this.checkBoxTrimTopicTextBeforeSet.isSelected());
    this.controller.getConfiguration().setUseProjectBaseFolderAsRoot(this.checkBoxUseProjectBaseFoderAsRoot.isSelected());
    this.controller.getConfiguration().setUseInsideBrowser(this.checkBoxUseInternalBrowser.isSelected());
    this.controller.getConfiguration().setCopyColorInformationFromParent(this.checkBoxCopyColorInfoFromParent.isSelected());
    this.controller.getConfiguration().setUnfoldTopicWhenItIsDropTarget(this.checkBoxUnfoldCollapsedDropTarget.isSelected());
    this.controller.getConfiguration().setDisableAutoCreateProjectKnowledgeFolder(this.checkBoxDisableAutocreateProjectKnowledge.isSelected());
  }

  public void reset() {
    this.checkBoxUseInternalBrowser.setSelected(this.controller.getConfiguration().isUseInsideBrowser());
    this.checkBoxTrimTopicTextBeforeSet.setSelected(this.controller.getConfiguration().isTrimTopicTextBeforeSet());
    this.checkBoxUseProjectBaseFoderAsRoot.setSelected(this.controller.getConfiguration().isUseProjectBaseFolderAsRoot());
    this.checkBoxMakeRelativePath.setSelected(this.controller.getConfiguration().isMakeRelativePath());
    this.checkBoxCopyColorInfoFromParent.setSelected(this.controller.getConfiguration().isCopyColorInformationFromParent());
    this.checkBoxUnfoldCollapsedDropTarget.setSelected(this.controller.getConfiguration().isUnfoldTopicWhenItIsDropTarget());
    this.checkBoxDisableAutocreateProjectKnowledge.setSelected(this.controller.getConfiguration().isDisableAutoCreateProjectKnowledgeFolder());
  }

}
