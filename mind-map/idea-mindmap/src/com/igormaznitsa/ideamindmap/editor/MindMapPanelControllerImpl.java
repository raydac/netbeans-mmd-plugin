package com.igormaznitsa.ideamindmap.editor;

import com.igormaznitsa.mindmap.model.MindMapController;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelController;
import com.igormaznitsa.mindmap.swing.panel.ui.AbstractElement;
import com.igormaznitsa.mindmap.swing.panel.ui.ElementPart;

import javax.swing.*;
import java.awt.*;

public class MindMapPanelControllerImpl implements MindMapPanelController {

    private final MindMapDocumentEditor editor;

    public MindMapPanelControllerImpl(final MindMapDocumentEditor editor){
        this.editor = editor;
    }

    public MindMapDocumentEditor getEditor(){
        return this.editor;
    }

    @Override
    public boolean isUnfoldCollapsedTopicDropTarget(MindMapPanel mindMapPanel) {
        return false;
    }

    @Override
    public boolean isCopyColorInfoFromParentToNewChildAllowed(MindMapPanel mindMapPanel) {
        return false;
    }

    @Override
    public boolean isSelectionAllowed(MindMapPanel mindMapPanel) {
        return true;
    }

    @Override
    public boolean isElementDragAllowed(MindMapPanel mindMapPanel) {
        return true;
    }

    @Override
    public boolean isMouseMoveProcessingAllowed(MindMapPanel mindMapPanel) {
        return true;
    }

    @Override
    public boolean isMouseWheelProcessingAllowed(MindMapPanel mindMapPanel) {
        return true;
    }

    @Override
    public boolean isMouseClickProcessingAllowed(MindMapPanel mindMapPanel) {
        return true;
    }

    @Override
    public MindMapPanelConfig provideConfigForMindMapPanel(MindMapPanel mindMapPanel) {
        return null;
    }

    @Override
    public JPopupMenu makePopUpForMindMapPanel(MindMapPanel mindMapPanel, Point point, AbstractElement abstractElement, ElementPart elementPart) {
        return null;
    }

    @Override
    public DialogProvider getDialogProvider(MindMapPanel mindMapPanel) {
        return null;
    }

    @Override
    public boolean processDropTopicToAnotherTopic(MindMapPanel mindMapPanel, Point point, Topic topic, Topic topic1) {
        return false;
    }
}
