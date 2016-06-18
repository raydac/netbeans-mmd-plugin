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
package com.igormaznitsa.sciareto.ui.tree;

import java.awt.Component;
import java.awt.Image;
import java.io.File;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.apache.commons.io.FilenameUtils;
import com.igormaznitsa.sciareto.ui.Icons;
import com.igormaznitsa.sciareto.ui.MainFrame;
import com.igormaznitsa.sciareto.ui.UiUtils;
import com.igormaznitsa.sciareto.ui.editors.PictureViewer;

public class TreeCellRenderer extends DefaultTreeCellRenderer {

  private static final long serialVersionUID = -6283018126496160094L;

  private static final Image PROJECT_BADGE = UiUtils.loadImage("project_badge.png");
  private static final Icon ICON_IMAGE = new ImageIcon(UiUtils.loadImage("image16.png"));

  private Icon PROJECT_CLOSED;
  private Icon PROJECT_OPENED;

  public TreeCellRenderer() {
    super();

  }

  private void ensureIcons(@Nonnull final JTree tree) {
    if (PROJECT_CLOSED == null) {
      PROJECT_CLOSED = new ImageIcon(UiUtils.makeBadged(UiUtils.iconToImage(tree, UIManager.getIcon("Tree.closedIcon")), PROJECT_BADGE));
    }
    if (PROJECT_OPENED == null) {
      PROJECT_OPENED = new ImageIcon(UiUtils.makeBadged(UiUtils.iconToImage(tree, UIManager.getIcon("Tree.openIcon")), PROJECT_BADGE));
    }
  }

  @Override
  @Nonnull
  public Component getTreeCellRendererComponent(@Nonnull final JTree tree, @Nullable final Object value, final boolean selected, final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
    ensureIcons(tree);
    super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
    if (value != null) {
      if (value instanceof NodeFileOrFolder) {
        final NodeFileOrFolder node = (NodeFileOrFolder) value;
        if (node instanceof NodeProject) {
          this.setIcon(expanded ? PROJECT_OPENED : PROJECT_CLOSED);
        } else {
          final File file = node.getFile();
          final String ext = file == null ? null : FilenameUtils.getExtension(file.getName()).toLowerCase(Locale.ENGLISH);
          if (ext != null){
            if (ext.equals("mmd")){
            this.setIcon(Icons.DOCUMENT.getIcon());
            }else if (PictureViewer.SUPPORTED_FORMATS.contains(ext)){
              this.setIcon(ICON_IMAGE);
            }
          }
        }
      }
    }
    return this;
  }
}
