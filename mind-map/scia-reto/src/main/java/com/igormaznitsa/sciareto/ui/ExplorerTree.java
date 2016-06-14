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

import javax.annotation.Nonnull;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import com.igormaznitsa.sciareto.tree.ProjectGroupTree;

public final class ExplorerTree extends JScrollPane {

  private static final long serialVersionUID = 3894835807758698784L;

  private final JTree projectTree;

  public ExplorerTree() {
    super();
    this.projectTree = new JTree();
    this.projectTree.setCellRenderer(new TreeCellRenderer());
    this.projectTree.setModel(new ProjectGroupTree("."));
    this.projectTree.setRootVisible(false);
    this.setViewportView(this.projectTree);
  }

  public void setModel(@Nonnull final ProjectGroupTree model, final boolean expandFirst) {
    this.projectTree.setModel(model);
    if (expandFirst && model.getChildCount() > 0) {
      this.projectTree.expandPath(new TreePath(new Object[]{model, model.getChildAt(0)}));
    }
  }

}
