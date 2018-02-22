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
package com.igormaznitsa.ideamindmap.view;

import com.igormaznitsa.ideamindmap.utils.AllIcons;
import com.intellij.ide.DataManager;
import com.intellij.ide.PsiCopyPasteManager;
import com.intellij.ide.SelectInTarget;
import com.intellij.ide.dnd.aware.DnDAwareTree;
import com.intellij.ide.impl.ProjectPaneSelectInTarget;
import com.intellij.ide.projectView.BaseProjectTreeBuilder;
import com.intellij.ide.projectView.impl.AbstractProjectViewPane;
import com.intellij.ide.projectView.impl.ProjectViewTree;
import com.intellij.ide.ui.customization.CustomizationUtil;
import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.ide.util.treeView.AbstractTreeStructure;
import com.intellij.ide.util.treeView.AbstractTreeUpdater;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.util.EditSourceOnDoubleClickHandler;
import com.intellij.util.OpenSourceUtil;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NonNls;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class KnowledgeViewPane extends AbstractProjectViewPane {
  @NonNls public static final String ID = "NBKnowledgePane";
  private JScrollPane myComponent;

  public KnowledgeViewPane(@Nonnull Project project) {
    super(project);
  }

  @Override public String getTitle() {
    return "Knowledge";
  }

  @Override public Icon getIcon() {
    return AllIcons.Logo.MINDMAP;
  }

  @Nonnull @Override public String getId() {
    return ID;
  }

  @Override public JComponent createComponent() {
    if (myComponent != null)
      return myComponent;

    DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(null);
    DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
    myTree = createTree(treeModel);
    enableDnD();
    myComponent = ScrollPaneFactory.createScrollPane(myTree);
    myTreeStructure = createStructure();
    setTreeBuilder(createBuilder(treeModel));

    installComparator();
    initTree();

    return myComponent;
  }

  private DnDAwareTree createTree(final TreeModel treeModel) {
    return new ProjectViewTree(myProject, treeModel) {
      public String toString() {
        return getTitle() + " " + super.toString();
      }

      @Override
      public void setFont(Font font) {
        if (Registry.is("bigger.font.in.project.view")) {
          font = font.deriveFont(font.getSize() + 1.0f);
        }
        super.setFont(font);
      }

      @Override
      public DefaultMutableTreeNode getSelectedNode() {
        return null;
      }
    };
  }

  private AbstractTreeStructure createStructure() {
    return new KnowledgeViewPanelTreeStructure(this.myProject);
  }

  private void initTree() {
    myTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
    UIUtil.setLineStyleAngled(myTree);
    myTree.setRootVisible(false);
    myTree.setShowsRootHandles(true);
    myTree.expandPath(new TreePath(myTree.getModel().getRoot()));
    myTree.setSelectionPath(new TreePath(myTree.getModel().getRoot()));

    EditSourceOnDoubleClickHandler.install(myTree);

    ToolTipManager.sharedInstance().registerComponent(myTree);
    TreeUtil.installActions(myTree);

    myTree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
      @Override
      public void valueChanged(TreeSelectionEvent e) {
        fireTreeChangeListener();
      }
    });
    myTree.getModel().addTreeModelListener(new TreeModelListener() {
      @Override
      public void treeNodesChanged(TreeModelEvent e) {
        fireTreeChangeListener();
      }

      @Override
      public void treeNodesInserted(TreeModelEvent e) {
        fireTreeChangeListener();
      }

      @Override
      public void treeNodesRemoved(TreeModelEvent e) {
        fireTreeChangeListener();
      }

      @Override
      public void treeStructureChanged(TreeModelEvent e) {
        fireTreeChangeListener();
      }
    });

    new SpeedSearch(myTree);

    myTree.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        if (KeyEvent.VK_ENTER == e.getKeyCode()) {

          final DefaultMutableTreeNode selectedNode = ((ProjectViewTree) myTree).getSelectedNode();
          if (selectedNode != null && !selectedNode.isLeaf()) {
            return;
          }

          DataContext dataContext = DataManager.getInstance().getDataContext(myTree);
          OpenSourceUtil.openSourcesFrom(dataContext, false);
        }
        else if (KeyEvent.VK_ESCAPE == e.getKeyCode()) {
          if (e.isConsumed())
            return;
          PsiCopyPasteManager copyPasteManager = PsiCopyPasteManager.getInstance();
          boolean[] isCopied = new boolean[1];
          if (copyPasteManager.getElements(isCopied) != null && !isCopied[0]) {
            copyPasteManager.clear();
            e.consume();
          }
        }
      }
    });
    CustomizationUtil.installPopupHandler(myTree, IdeActions.GROUP_PROJECT_VIEW_POPUP, ActionPlaces.PROJECT_VIEW_POPUP);
  }

  private AbstractTreeUpdater createTreeUpdater(final AbstractTreeBuilder builder) {
    return new AbstractTreeUpdater(builder);
  }

  @Nonnull
  protected BaseProjectTreeBuilder createBuilder(DefaultTreeModel treeModel) {
    return new KnowledgeViewTreeBuilder(myProject, myTree, treeModel, null, (KnowledgeViewPanelTreeStructure) myTreeStructure) {
      @Override
      protected AbstractTreeUpdater createUpdater() {
        return createTreeUpdater(this);
      }
    };
  }

  @Nonnull @Override public ActionCallback updateFromRoot(boolean restoreExpandedPaths) {
    return ActionCallback.DONE;
  }

  @Override public void select(Object element, VirtualFile file, boolean requestFocus) {
    if (file != null) {
      ((KnowledgeViewTreeBuilder) getTreeBuilder()).select(element, file, requestFocus);
    }
  }

  @Override public int getWeight() {
    return 10;
  }

  @Override public JTree getTree() {
    return this.myTree;
  }

  @Override public SelectInTarget createSelectInTarget() {
    return new ProjectPaneSelectInTarget(myProject);
  }

}
