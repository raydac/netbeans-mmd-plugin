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
import com.igormaznitsa.sciareto.ui.UiUtils;
import com.igormaznitsa.sciareto.ui.editors.PictureViewer;

public class TreeCellRenderer extends DefaultTreeCellRenderer {

  private static final long serialVersionUID = -6283018126496160094L;

  private static final Image PROJECT_BADGE = UiUtils.loadImage("project_badge.png");
  private static final Image READONLY_BADGE = UiUtils.loadImage("ro.png");
  private static final Icon ICON_IMAGE = new ImageIcon(UiUtils.loadImage("image16.png"));
  private static final Icon ICON_IMAGE_RO;

  static {
    ICON_IMAGE_RO = new ImageIcon(UiUtils.makeBadgedRightTop(((ImageIcon) ICON_IMAGE).getImage(), READONLY_BADGE));
  }

  private Icon PROJECT_CLOSED;
  private Icon PROJECT_OPENED;

  private Icon PROJECT_CLOSED_RO;
  private Icon PROJECT_OPENED_RO;

  private Icon LEAF;
  private Icon LEAF_RO;

  private Icon FOLDER_CLOSED;
  private Icon FOLDER_CLOSED_RO;

  private Icon FOLDER_OPENED;
  private Icon FOLDER_OPENED_RO;

  private Icon LEAF_MINDMAP;
  private Icon LEAF_MINDMAP_RO;

  public TreeCellRenderer() {
    super();

  }

  private void ensureIcons(@Nonnull final JTree tree) {
    if (PROJECT_CLOSED == null) {
      PROJECT_CLOSED = new ImageIcon(UiUtils.makeBadgedRightBottom(UiUtils.iconToImage(tree, UIManager.getIcon("Tree.closedIcon")), PROJECT_BADGE));
      PROJECT_CLOSED_RO = new ImageIcon(UiUtils.makeBadgedRightTop(((ImageIcon) PROJECT_CLOSED).getImage(), READONLY_BADGE));
    }

    if (PROJECT_OPENED == null) {
      PROJECT_OPENED = new ImageIcon(UiUtils.makeBadgedRightBottom(UiUtils.iconToImage(tree, UIManager.getIcon("Tree.openIcon")), PROJECT_BADGE));
      PROJECT_OPENED_RO = new ImageIcon(UiUtils.makeBadgedRightTop(((ImageIcon) PROJECT_OPENED).getImage(), READONLY_BADGE));
    }

    if (FOLDER_CLOSED == null) {
      FOLDER_CLOSED = new ImageIcon(UiUtils.iconToImage(tree, UIManager.getIcon("Tree.closedIcon")));
      FOLDER_CLOSED_RO = new ImageIcon(UiUtils.makeBadgedRightTop(((ImageIcon) FOLDER_CLOSED).getImage(), READONLY_BADGE));
    }

    if (FOLDER_OPENED == null) {
      FOLDER_OPENED = new ImageIcon(UiUtils.iconToImage(tree, UIManager.getIcon("Tree.openIcon")));
      FOLDER_OPENED_RO = new ImageIcon(UiUtils.makeBadgedRightTop(((ImageIcon) FOLDER_OPENED).getImage(), READONLY_BADGE));
    }

    if (LEAF == null) {
      LEAF = new ImageIcon(UiUtils.iconToImage(tree, UIManager.getIcon("Tree.leafIcon")));
      LEAF_RO = new ImageIcon(UiUtils.makeBadgedRightTop(((ImageIcon) PROJECT_OPENED).getImage(), READONLY_BADGE));
    }
    
    if (LEAF_MINDMAP == null) {
      LEAF_MINDMAP = Icons.DOCUMENT.getIcon();
      LEAF_MINDMAP_RO = new ImageIcon(UiUtils.makeBadgedRightTop(Icons.DOCUMENT.getIcon().getImage(), READONLY_BADGE));
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
          if (node.isReadOnly()) {
            this.setIcon(expanded ? PROJECT_OPENED_RO : PROJECT_CLOSED_RO);
          } else {
            this.setIcon(expanded ? PROJECT_OPENED : PROJECT_CLOSED);
          }
        } else if (node.isLeaf()) {
          final String ext = FilenameUtils.getExtension(node.toString()).toLowerCase(Locale.ENGLISH);
          if (ext.equals("mmd")) {
            this.setIcon(node.isReadOnly() ? LEAF_MINDMAP_RO : LEAF_MINDMAP);
          } else if (PictureViewer.SUPPORTED_FORMATS.contains(ext)) {
            this.setIcon(node.isReadOnly() ? ICON_IMAGE_RO : ICON_IMAGE);
          } else {
            this.setIcon(node.isReadOnly() ? LEAF_RO : LEAF);
          }
        } else if (node.isReadOnly()) {
          this.setIcon(expanded ? FOLDER_OPENED_RO : FOLDER_CLOSED_RO);
        } else {
          this.setIcon(expanded ? FOLDER_OPENED : FOLDER_CLOSED);
        }
      }
    }
    return this;
  }
}
