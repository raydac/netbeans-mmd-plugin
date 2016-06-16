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

import java.awt.Component;
import java.io.File;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.apache.commons.io.FilenameUtils;
import com.igormaznitsa.sciareto.tree.FileTreeNode;

public class TreeCellRenderer extends DefaultTreeCellRenderer {

  private static final long serialVersionUID = -6283018126496160094L;
  
  @Override
  @Nonnull
  public Component getTreeCellRendererComponent(@Nonnull final JTree tree, @Nullable final Object value, final boolean selected, final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
    super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
    if (value!=null && value instanceof FileTreeNode){
      final FileTreeNode node = (FileTreeNode)value;
      final File file = node.getFile();
      final String ext = file == null ? null : FilenameUtils.getExtension(file.getName()).toLowerCase(Locale.ENGLISH);
      if (ext != null && ext.equals("mmd")) {
        this.setIcon(Icons.DOCUMENT.getIcon());
      }
    }
    return this;
  }
}
