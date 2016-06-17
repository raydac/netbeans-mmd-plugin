/*
 * Copyright 2015 Igor Maznitsa.
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
