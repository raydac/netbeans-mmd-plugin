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
import com.igormaznitsa.meta.common.utils.GetUtils;
import com.igormaznitsa.sciareto.ui.Icons;
import com.igormaznitsa.sciareto.ui.UiUtils;
import com.igormaznitsa.sciareto.ui.editors.PictureViewer;

public class TreeCellRenderer extends DefaultTreeCellRenderer {

  private static final long serialVersionUID = -6283018126496160094L;

  private static final Image PROJECT_BADGE = UiUtils.loadIcon("project_badge.png"); //NOI18N
  private static final Image KF_BADGE = UiUtils.loadIcon("mmdbadge.png"); //NOI18N
  private static final Image READONLY_BADGE = UiUtils.loadIcon("ro.png"); //NOI18N
  private static final Icon ICON_IMAGE_RO;

  public static final Icon ICON_IMAGE = new ImageIcon(UiUtils.loadIcon("image16.png")); //NOI18N
  public static final Icon DEFAULT_FOLDER_CLOSED = new ImageIcon(UiUtils.loadIcon("folder16.gif")); //NOI18N
  public static final Icon DEFAULT_FOLDER_OPENED = new ImageIcon(UiUtils.loadIcon("folderOpen16.gif")); //NOI18N
  public static final Icon DEFAULT_FILE = new ImageIcon(UiUtils.loadIcon("document_empty16.png")); //NOI18N

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

  private Icon FOLDER_KF_CLOSED;
  private Icon FOLDER_KF_CLOSED_RO;

  private Icon FOLDER_OPENED;
  private Icon FOLDER_OPENED_RO;

  private Icon FOLDER_KF_OPENED;
  private Icon FOLDER_KF_OPENED_RO;

  private Icon LEAF_MINDMAP;
  private Icon LEAF_MINDMAP_RO;

  public TreeCellRenderer() {
    super();

  }

  private void ensureIcons(@Nonnull final JTree tree) {
    if (PROJECT_CLOSED == null) {
      PROJECT_CLOSED = new ImageIcon(UiUtils.makeBadgedRightBottom(UiUtils.iconToImage(tree, GetUtils.ensureNonNull(UIManager.getIcon("Tree.closedIcon"), DEFAULT_FOLDER_CLOSED)), PROJECT_BADGE)); //NOI18N
      PROJECT_CLOSED_RO = new ImageIcon(UiUtils.makeBadgedRightTop(((ImageIcon) PROJECT_CLOSED).getImage(), READONLY_BADGE));
    }

    if (PROJECT_OPENED == null) {
      PROJECT_OPENED = new ImageIcon(UiUtils.makeBadgedRightBottom(UiUtils.iconToImage(tree, GetUtils.ensureNonNull(UIManager.getIcon("Tree.openIcon"), DEFAULT_FOLDER_OPENED)), PROJECT_BADGE)); //NOI18N
      PROJECT_OPENED_RO = new ImageIcon(UiUtils.makeBadgedRightTop(((ImageIcon) PROJECT_OPENED).getImage(), READONLY_BADGE));
    }

    if (FOLDER_CLOSED == null) {
      FOLDER_CLOSED = new ImageIcon(UiUtils.iconToImage(tree, GetUtils.ensureNonNull(UIManager.getIcon("Tree.closedIcon"), DEFAULT_FOLDER_CLOSED))); //NOI18N
      FOLDER_CLOSED_RO = new ImageIcon(UiUtils.makeBadgedRightTop(((ImageIcon) FOLDER_CLOSED).getImage(), READONLY_BADGE));
    }

    if (FOLDER_OPENED == null) {
      FOLDER_OPENED = new ImageIcon(UiUtils.iconToImage(tree, GetUtils.ensureNonNull(UIManager.getIcon("Tree.openIcon"), DEFAULT_FOLDER_OPENED))); //NOI18N
      FOLDER_OPENED_RO = new ImageIcon(UiUtils.makeBadgedRightTop(((ImageIcon) FOLDER_OPENED).getImage(), READONLY_BADGE));
    }

    if (FOLDER_KF_CLOSED == null) {
      FOLDER_KF_CLOSED = new ImageIcon(UiUtils.makeBadgedRightBottom(UiUtils.iconToImage(tree, GetUtils.ensureNonNull(UIManager.getIcon("Tree.closedIcon"), DEFAULT_FOLDER_CLOSED)), KF_BADGE)); //NOI18N
      FOLDER_KF_CLOSED_RO = new ImageIcon(UiUtils.makeBadgedRightTop(((ImageIcon) FOLDER_KF_CLOSED).getImage(), READONLY_BADGE));
    }

    if (FOLDER_KF_OPENED == null) {
      FOLDER_KF_OPENED = new ImageIcon(UiUtils.makeBadgedRightBottom(UiUtils.iconToImage(tree, GetUtils.ensureNonNull(UIManager.getIcon("Tree.openIcon"), DEFAULT_FOLDER_OPENED)), KF_BADGE)); //NOI18N
      FOLDER_KF_OPENED_RO = new ImageIcon(UiUtils.makeBadgedRightTop(((ImageIcon) FOLDER_KF_OPENED).getImage(), READONLY_BADGE));
    }

    if (LEAF == null) {
      LEAF = new ImageIcon(UiUtils.iconToImage(tree, GetUtils.ensureNonNull(UIManager.getIcon("Tree.leafIcon"), DEFAULT_FILE))); //NOI18N
      LEAF_RO = new ImageIcon(UiUtils.makeBadgedRightTop(((ImageIcon) LEAF).getImage(), READONLY_BADGE));
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
          if (ext.equals("mmd")) { //NOI18N
            this.setIcon(node.isReadOnly() ? LEAF_MINDMAP_RO : LEAF_MINDMAP);
          } else if (PictureViewer.SUPPORTED_FORMATS.contains(ext)) {
            this.setIcon(node.isReadOnly() ? ICON_IMAGE_RO : ICON_IMAGE);
          } else {
            this.setIcon(node.isReadOnly() ? LEAF_RO : LEAF);
          }
        } else if (node.isProjectKnowledgeFolder()) {
          this.setText("Knowledge");
          if (node.isReadOnly()) {
            this.setIcon(expanded ? FOLDER_KF_OPENED_RO : FOLDER_KF_CLOSED_RO);
          } else {
            this.setIcon(expanded ? FOLDER_KF_OPENED : FOLDER_KF_CLOSED);
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
