package com.igormaznitsa.ideamindmap.swing;

import com.igormaznitsa.ideamindmap.utils.AllIcons;
import com.igormaznitsa.mindmap.ide.commons.editors.AbstractMindMapTreePanel;
import com.igormaznitsa.mindmap.model.ExtraTopic;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactory;
import java.awt.event.ActionListener;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.Icon;

public class MindMapTreePanel extends AbstractMindMapTreePanel {

    public MindMapTreePanel(
            @Nonnull final UIComponentFactory uiComponentFactoryProvider,
            @Nonnull final MindMap map,
            @Nullable final ExtraTopic selectedTopicUid,
            final boolean expandAll,
            @Nullable final ActionListener listener
    ) {
        super(uiComponentFactoryProvider, map, selectedTopicUid, expandAll, listener);
    }

    @Nullable
    @Override
    protected Icon findIcon(@Nonnull final IconId iconId) {
        switch (iconId) {
            case BALL_BLUE:
                return AllIcons.Tree.BLUEBALL;
            case BALL_GOLD:
                return AllIcons.Tree.GOLDBALL;
            case TREE_DOCUMENT:
                return AllIcons.Tree.DOCUMENT;
            case BUTTON_FOLD_ALL:
                return AllIcons.Buttons.COLLAPSEALL;
            case BUTTON_SELECT_NONE:
                return AllIcons.Buttons.SELECT;
            case BUTTON_UNFOLD_ALL:
                return AllIcons.Buttons.EXPANDALL;
            default:
                return null;
        }
    }
}
