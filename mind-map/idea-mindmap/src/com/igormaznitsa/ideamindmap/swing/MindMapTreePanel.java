/*
 * Copyright 2015-2018 Igor Maznitsa.
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

package com.igormaznitsa.ideamindmap.swing;

import com.igormaznitsa.ideamindmap.utils.AllIcons;
import com.igormaznitsa.mindmap.ide.commons.SortedTreeModelWrapper;
import com.igormaznitsa.mindmap.model.ExtraTopic;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.swing.panel.HasPreferredFocusComponent;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Comparator;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public final class MindMapTreePanel extends javax.swing.JPanel implements Comparator<Object>, HasPreferredFocusComponent {

  private static final long serialVersionUID = 2652308291444091807L;
  private static final ResourceBundle BUNDLE = java.util.ResourceBundle.getBundle("i18n/Bundle");
  private final MindMapTreeCellRenderer cellRenderer = new MindMapTreeCellRenderer();
  private final SortedTreeModelWrapper sortedModel;
  private JButton buttonCollapseAll;
  private JButton buttonExpandAll;
  private JButton buttonUnselect;
  private JToolBar toolBar;
  private JTree treeMindMap;
  private JBScrollPane treeScrollPane;

  public MindMapTreePanel(final MindMap map, final ExtraTopic selectedTopicUid, final boolean expandAll, final ActionListener listener) {
    initComponents();
    this.treeMindMap.setCellRenderer(this.cellRenderer);
    this.treeMindMap.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    if (map != null) {
      this.sortedModel = new SortedTreeModelWrapper(map, this);
      this.treeMindMap.setModel(this.sortedModel);
      if (selectedTopicUid != null) {
        final Topic topic = map.findTopicForLink(selectedTopicUid);
        if (topic != null) {
          this.treeMindMap.setSelectionPath(new TreePath(topic.getPath()));
        }
      }
    } else {
      this.sortedModel = null;
    }

    this.treeMindMap.addMouseListener(new MouseAdapter() {

      @Override
      public void mouseClicked(final MouseEvent e) {
        if (!e.isPopupTrigger() && e.getClickCount() > 1) {
          if (listener != null) {
            listener.actionPerformed(new ActionEvent(this, 0, "doubleClick"));
          }
        }
      }
    });

    this.setPreferredSize(new Dimension(450, 400));

    this.treeMindMap.requestFocus();

    if (expandAll) {
      expandAll();
    }
  }

  @Override
  public JComponent getComponentPreferredForFocus() {
    return this.treeMindMap;
  }

  public void expandAll() {
    Utils.foldUnfoldTree(this.treeMindMap, true);
  }

  public void collapseAll() {
    Utils.foldUnfoldTree(this.treeMindMap, false);
  }

  public JTree getTree() {
    return this.treeMindMap;
  }

  public Topic getSelectedTopic() {
    final TreePath selected = this.treeMindMap.getSelectionPath();
    return selected == null ? null : (Topic) selected.getLastPathComponent();
  }

  private void initComponents() {

    treeScrollPane = new JBScrollPane();
    treeMindMap = new Tree();
    toolBar = new javax.swing.JToolBar();
    buttonExpandAll = new javax.swing.JButton();
    buttonCollapseAll = new javax.swing.JButton();
    buttonUnselect = new javax.swing.JButton();

    setLayout(new java.awt.BorderLayout());

    treeScrollPane.setViewportView(treeMindMap);

    add(treeScrollPane, java.awt.BorderLayout.CENTER);

    toolBar.setFloatable(false);
    toolBar.setRollover(true);

    buttonExpandAll.setIcon(AllIcons.Buttons.EXPANDALL);
    java.util.ResourceBundle bundle = BUNDLE;
    buttonExpandAll.setText(bundle.getString("MindMapTreePanel.buttonExpandAll.text")); // NOI18N
    buttonExpandAll.setFocusable(false);
    buttonExpandAll.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    buttonExpandAll.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    buttonExpandAll.addActionListener(this::buttonExpandAllActionPerformed);
    toolBar.add(buttonExpandAll);

    buttonCollapseAll.setIcon(AllIcons.Buttons.COLLAPSEALL);
    buttonCollapseAll.setText(bundle.getString("MindMapTreePanel.buttonCollapseAll.text")); // NOI18N
    buttonCollapseAll.setFocusable(false);
    buttonCollapseAll.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    buttonCollapseAll.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    buttonCollapseAll.addActionListener(this::buttonCollapseAllActionPerformed);
    toolBar.add(buttonCollapseAll);

    buttonUnselect.setIcon(AllIcons.Buttons.SELECT);
    buttonUnselect.setText(bundle.getString("MindMapTreePanel.buttonUnselect.text")); // NOI18N
    buttonUnselect.setFocusable(false);
    buttonUnselect.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    buttonUnselect.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    buttonUnselect.addActionListener(this::buttonUnselectActionPerformed);
    toolBar.add(buttonUnselect);

    add(toolBar, java.awt.BorderLayout.PAGE_START);
  }

  private void buttonUnselectActionPerformed(java.awt.event.ActionEvent evt) {
    this.treeMindMap.setSelectionPath(null);
  }

  private void buttonExpandAllActionPerformed(java.awt.event.ActionEvent evt) {
    expandAll();
  }

  private void buttonCollapseAllActionPerformed(java.awt.event.ActionEvent evt) {
    collapseAll();
  }

  public void dispose() {
    if (this.sortedModel != null) {
      this.sortedModel.dispose();
    }
  }

  @Override
  public int compare(final Object o1, final Object o2) {
    return String.CASE_INSENSITIVE_ORDER.compare(String.valueOf(o1), String.valueOf(o2));
  }
}
