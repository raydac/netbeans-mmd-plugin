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
package com.igormaznitsa.sciareto.ui.editors.mmeditors;

import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import com.igormaznitsa.sciareto.ui.Icons;
import java.awt.Component;
import javax.annotation.Nonnull;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

public class MindMapTreeCellRenderer extends DefaultTreeCellRenderer {
  private static final long serialVersionUID = 8359308344417939815L;

  @Override
  @Nonnull
  public Component getTreeCellRendererComponent(@Nonnull final JTree tree, @Nonnull final Object value, final boolean sel, final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
    final DefaultTreeCellRenderer result = (DefaultTreeCellRenderer) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
    if (value instanceof Topic) {
      result.setIcon(getIconForTopic((Topic) value));
      result.setText(extractTextFromTopic((Topic) value));
    }
    return result;
  }

  @Nonnull
  private Icon getIconForTopic(@Nonnull final Topic topic) {
    switch (topic.getTopicLevel()) {
      case 0:
        return Icons.DOCUMENT.getIcon();
      case 1:
        return Icons.BLUEBALL.getIcon();
      default:
        return Icons.GOLDBALL.getIcon();
    }
  }

  @Nonnull
  private String extractTextFromTopic(@Nonnull final Topic topic) {
    return Utils.makeShortTextVersion(Utils.getFirstLine(topic.getText()), 20);
  }
}
