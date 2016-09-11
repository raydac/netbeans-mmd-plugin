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
package com.igormaznitsa.sciareto.ui.misc;

import java.awt.Component;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import org.apache.commons.io.FilenameUtils;
import com.igormaznitsa.sciareto.ui.Icons;
import com.igormaznitsa.sciareto.ui.editors.PictureViewer;
import com.igormaznitsa.sciareto.ui.tree.NodeFileOrFolder;
import com.igormaznitsa.sciareto.ui.tree.NodeProject;
import com.igormaznitsa.sciareto.ui.tree.TreeCellRenderer;

public final class NodeListRenderer extends DefaultListCellRenderer {
  
  private static final long serialVersionUID = 3875614392486198647L;

  public NodeListRenderer() {
    super();
  }

  @Nonnull
  private static String makeTextForNode(@Nonnull final NodeFileOrFolder node) {
    final NodeProject project = node.findProject();
    if (project == null) {
      return node.toString();
    } else {
      final String projectName = project.toString();
      return node.toString() + " (found in " + projectName + ')';
    }
  }

  @Override
  @Nonnull
  public Component getListCellRendererComponent(@Nonnull final JList<?> list, @Nonnull final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    final NodeFileOrFolder node = (NodeFileOrFolder) value;
    final String ext = FilenameUtils.getExtension(node.toString()).toLowerCase(Locale.ENGLISH);
    if (node instanceof NodeProject || !node.isLeaf()) {
      this.setIcon(TreeCellRenderer.DEFAULT_FOLDER_CLOSED);
    } else if (ext.equals("mmd")) {
      this.setIcon(Icons.DOCUMENT.getIcon());
    } else if (PictureViewer.SUPPORTED_FORMATS.contains(ext)) {
      this.setIcon(TreeCellRenderer.ICON_IMAGE);
    } else {
      this.setIcon(TreeCellRenderer.DEFAULT_FILE);
    }
    this.setText(makeTextForNode(node));
    return this;
  }
  
}
