package com.igormaznitsa.ideamindmap.swing;

import com.igormaznitsa.ideamindmap.utils.AllIcons;
import com.igormaznitsa.mindmap.ide.commons.editors.AbstractMindMapTreePanel;
import com.igormaznitsa.mindmap.model.ExtraTopic;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactory;
import com.intellij.ui.ColoredTreeCellRenderer;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.util.function.BiFunction;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;

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

    @Override
    protected TreeCellRenderer makeTreeCellRenderer(final BiFunction<Object, Component, Component> cellRenderFunction) {
        return new ColoredTreeCellRenderer(){
            @Override
            public void customizeCellRenderer(final JTree tree, final Object value, final boolean selected, final boolean expanded, final boolean leaf, final int row, final boolean focus) {
                if (value instanceof Topic) {
                    final Topic topic = (Topic) value;
                    this.setIcon(getIconForTopic(topic));
                    this.append(extractTextFromTopic(topic));
                }
            }
        };
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
