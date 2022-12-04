/*
 * Copyright (C) 2015-2022 Igor A. Maznitsa
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.igormaznitsa.sciareto.ui.misc;

import com.igormaznitsa.sciareto.ui.SrI18n;
import java.awt.Color;
import java.awt.Component;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.UIManager;
import org.apache.commons.io.FilenameUtils;
import com.igormaznitsa.sciareto.ui.Icons;
import com.igormaznitsa.sciareto.ui.editors.PictureViewer;
import com.igormaznitsa.sciareto.ui.tree.NodeFileOrFolder;
import com.igormaznitsa.sciareto.ui.tree.NodeProject;
import com.igormaznitsa.sciareto.ui.tree.TreeCellRenderer;
import javax.swing.JLabel;

public final class NodeListRenderer extends DefaultListCellRenderer {

  private static final long serialVersionUID = 3875614392486198647L;

  private final Color COLOR_ROW_EVEN;
  private final Color COLOR_ROW_ODD;

  public NodeListRenderer() {
    super();
    final Color defaultBackground = UIManager.getLookAndFeelDefaults().getColor("List.background"); //NOI18N
    if (defaultBackground == null) {
      COLOR_ROW_EVEN = null;
      COLOR_ROW_ODD = null;
    } else {
      final float FACTOR = 0.97f;
      COLOR_ROW_EVEN = defaultBackground;
      COLOR_ROW_ODD = new Color(Math.max((int) (COLOR_ROW_EVEN.getRed() * FACTOR), 0),
          Math.max((int) (COLOR_ROW_EVEN.getGreen() * FACTOR), 0),
          Math.max((int) (COLOR_ROW_EVEN.getBlue() * FACTOR), 0),
          COLOR_ROW_EVEN.getAlpha());
    }
  }

  @Nonnull
  private static String makeTextForNode(@Nonnull final NodeFileOrFolder node) {
    final NodeProject project = node.findProject();
    if (project == null) {
      return node.toString();
    } else {
      final String projectName = project.toString();
      return String.format(SrI18n.getInstance().findBundle().getString("NodeListRenderer.textForNode"), node, projectName);
    }
  }

  @Override
  @Nonnull
  public Component getListCellRendererComponent(@Nonnull final JList<?> list, @Nonnull final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
    final JLabel result = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
   
    final NodeFileOrFolder node = (NodeFileOrFolder) value;

    final String ext = FilenameUtils.getExtension(node.toString()).toLowerCase(Locale.ENGLISH);

    if (!isSelected && COLOR_ROW_EVEN != null && COLOR_ROW_ODD != null) {
      if (index % 2 == 0) {
        result.setBackground(COLOR_ROW_EVEN);
      } else {
        result.setBackground(COLOR_ROW_ODD);
      }
    }

    if (node instanceof NodeProject || !node.isLeaf()) {
      result.setIcon(TreeCellRenderer.DEFAULT_FOLDER_CLOSED);
    } else if (ext.equals("mmd")) { //NOI18N
      result.setIcon(Icons.DOCUMENT.getIcon());
    } else if (PictureViewer.SUPPORTED_FORMATS.contains(ext)) {
      result.setIcon(TreeCellRenderer.ICON_IMAGE);
    } else {
      result.setIcon(TreeCellRenderer.DEFAULT_FILE);
    }
    result.setText(makeTextForNode(node));
    
    return result;
  }

}
