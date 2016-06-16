/*
 * Copyright 2016 Igor Maznitsa.
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
package com.igormaznitsa.sciareto.ui;

import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import javax.annotation.Nonnull;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.TransferHandler;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import com.igormaznitsa.meta.common.utils.Assertions;
import com.igormaznitsa.sciareto.Context;
import com.igormaznitsa.sciareto.tree.FileTreeNode;
import com.igormaznitsa.sciareto.tree.ProjectGroupTree;
import com.igormaznitsa.sciareto.tree.ProjectTree;

public final class ExplorerTree extends JScrollPane {

  private static final long serialVersionUID = 3894835807758698784L;

  private final JTree projectTree;

  public ExplorerTree(@Nonnull final Context context) {
    super();
    this.projectTree = new JTree() {
      @Override
      public String getToolTipText(@Nonnull final MouseEvent evt) {
        if (getRowForLocation(evt.getX(), evt.getY()) == -1) {
          return null;
        }
        final TreePath curPath = getPathForLocation(evt.getX(), evt.getY());
        final Object lastElement = curPath.getLastPathComponent();
        if (lastElement instanceof FileTreeNode) {
          final File file = ((FileTreeNode) lastElement).getFile();
          return file == null ? null : file.getAbsolutePath();
        } else {
          return null;
        }
      }
    };
    
    this.projectTree.setDragEnabled(true);
    
    ToolTipManager.sharedInstance().registerComponent(this.projectTree);
    
    this.projectTree.setCellRenderer(new TreeCellRenderer());
    this.projectTree.setModel(new ProjectGroupTree("."));
    this.projectTree.setRootVisible(false);
    this.setViewportView(this.projectTree);

    this.projectTree.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(@Nonnull final MouseEvent e) {
        if (e.getClickCount() > 1) {
          final int selRow = projectTree.getRowForLocation(e.getX(), e.getY());
          final TreePath selPath = projectTree.getPathForLocation(e.getX(), e.getY());
          if (selRow >= 0) {
            final FileTreeNode node = (FileTreeNode) selPath.getLastPathComponent();
            if (node != null) {
              final File file = node.getFile();
              if (!context.openFileAsTab(file)){
                UiUtils.openInSystemViewer(file);
              }
            }
          }
        }
      }

    });
  }

  @Nonnull
  public ProjectGroupTree getCurrentGroup() {
    return (ProjectGroupTree) this.projectTree.getModel();
  }

  public void setModel(@Nonnull final ProjectGroupTree model, final boolean expandFirst) {
    this.projectTree.setModel(Assertions.assertNotNull(model));
    if (expandFirst && model.getChildCount() > 0) {
      this.projectTree.expandPath(new TreePath(new Object[]{model, model.getChildAt(0)}));
    }
  }

}
