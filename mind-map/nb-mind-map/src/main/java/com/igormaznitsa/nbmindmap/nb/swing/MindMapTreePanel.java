package com.igormaznitsa.nbmindmap.nb.swing;

import static java.util.Objects.requireNonNull;

import com.igormaznitsa.mindmap.ide.commons.editors.AbstractMindMapTreePanel;
import com.igormaznitsa.mindmap.model.ExtraTopic;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactory;
import com.igormaznitsa.nbmindmap.utils.Icons;
import java.awt.event.ActionListener;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.Icon;
import javax.swing.ImageIcon;

public class MindMapTreePanel extends AbstractMindMapTreePanel {
  public MindMapTreePanel(
      @Nonnull final UIComponentFactory uiComponentFactoryProvider,
      @Nullable final MindMap map,
      @Nullable final ExtraTopic selectedTopicUid,
      final boolean expandAll,
      @Nullable final ActionListener listener) {
    super(uiComponentFactoryProvider, map, selectedTopicUid, expandAll, listener);
  }

  @Override
  protected Icon findIcon(@Nonnull final IconId iconId) {
    switch (iconId) {
      case TREE_DOCUMENT:
        return Icons.DOCUMENT.getIcon();
      case BALL_BLUE:
        return Icons.BLUEBALL.getIcon();
      case BALL_GOLD:
        return Icons.GOLDBALL.getIcon();
      case BUTTON_FOLD_ALL:
        return new ImageIcon(requireNonNull(
            getClass().getResource("/com/igormaznitsa/nbmindmap/icons/toggle16.png")));
      case BUTTON_UNFOLD_ALL:
        return new ImageIcon(requireNonNull(
            getClass().getResource("/com/igormaznitsa/nbmindmap/icons/toggle_expand16.png")));
      case BUTTON_SELECT_NONE:
        return new ImageIcon(requireNonNull(
            getClass().getResource("/com/igormaznitsa/nbmindmap/icons/select16.png")));
      default:
        return null;
    }
  }
}
