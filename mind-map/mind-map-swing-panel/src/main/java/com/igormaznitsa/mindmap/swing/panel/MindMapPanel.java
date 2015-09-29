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
package com.igormaznitsa.mindmap.swing.panel;

import com.igormaznitsa.mindmap.swing.panel.ui.MouseSelectedArea;
import com.igormaznitsa.mindmap.swing.panel.ui.ElementPart;
import com.igormaznitsa.mindmap.swing.panel.ui.ElementRoot;
import com.igormaznitsa.mindmap.swing.panel.ui.ElementLevelFirst;
import com.igormaznitsa.mindmap.swing.panel.ui.ElementLevelOther;
import com.igormaznitsa.mindmap.swing.panel.ui.AbstractElement;
import com.igormaznitsa.mindmap.swing.panel.ui.AbstractCollapsableElement;
import com.igormaznitsa.mindmap.model.*;
import com.igormaznitsa.mindmap.swing.panel.utils.MindMapUtils;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import java.awt.*;
import java.awt.RenderingHints.Key;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Dimension2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MindMapPanel extends JPanel {

  public static final long serialVersionUID = 2783412123454232L;

  public static final String ATTR_SHOW_JUMPS = "showJumps";

  private static final Logger logger = LoggerFactory.getLogger(MindMapPanel.class);
  private final MindMapPanelController controller;

  public class DraggedElement {
    private final AbstractElement element;
    private final Image prerenderedImage;
    private final Point mousePointerOffset;
    private final Point currentPosition;
    
    public DraggedElement(final AbstractElement element, final Point mousePointerOffset){
      this.element = element;
      this.prerenderedImage = Utils.renderWithTransparency(0.75f, element, config);
      this.mousePointerOffset = mousePointerOffset;
      this.currentPosition = new Point();
    }
    
    public boolean isPositionInside(){
      return this.element.getBounds().contains(this.currentPosition);
    }
    
    public AbstractElement getElement(){
      return this.element;
    }
    
    public void updatePosition(final Point point){
      this.currentPosition.setLocation(point);
    }
    
    public Point getPosition(){
      return this.currentPosition;
    }
    
    public Point getMousePointerOffset(){
      return this.mousePointerOffset;
    }
    
    public int getDrawPositionX(){
      return this.currentPosition.x - this.mousePointerOffset.x;
    }
    
    public int getDrawPositionY(){
      return this.currentPosition.y - this.mousePointerOffset.y;
    }
    
    public Image getImage(){
      return this.prerenderedImage;
    }
    
    public void draw(final Graphics2D gfx){
      final int x = getDrawPositionX();
      final int y = getDrawPositionY();
      gfx.drawImage(this.prerenderedImage, x, y, null);
    }
  }
  
  private static final ResourceBundle BUNDLE = java.util.ResourceBundle.getBundle("com/igormaznitsa/mindmap/swing/panel/Bundle");

  private volatile MindMap model;
  private volatile String errorText;

  private final List<MindMapListener> mindMapListeners = new CopyOnWriteArrayList<>();

  private static final double SCALE_STEP = 0.2d;

  private static final Color COLOR_MOUSE_DRAG_SELECTION = new Color(0x80000000, true);

  private final JTextArea textEditor = new JTextArea();
  private final JPanel textEditorPanel = new JPanel(new BorderLayout(0, 0));
  private transient AbstractElement elementUnderEdit = null;

  private final List<Topic> selectedTopics = new ArrayList<>();

  private transient MouseSelectedArea mouseDragSelection = null;
  private transient DraggedElement draggedElement = null;
  private transient AbstractElement destinationElement = null;

  private volatile boolean popupMenuActive = false;

  private final MindMapPanelConfig config;

  public MindMapPanel(final MindMapPanelController controller) {
    super(null);
    this.controller = controller;

    this.config = new MindMapPanelConfig(controller.provideConfigForMindMapPanel(this), false);

    this.textEditor.setMargin(new Insets(5, 5, 5, 5));
    this.textEditor.setBorder(BorderFactory.createEtchedBorder());
    this.textEditor.setTabSize(4);
    this.textEditor.addKeyListener(new KeyAdapter() {

      @Override
      public void keyPressed(final KeyEvent e) {
        switch (e.getKeyCode()) {
          case KeyEvent.VK_ENTER: {
            e.consume();
          }
          break;
          case KeyEvent.VK_TAB: {
            if ((e.getModifiersEx() & (KeyEvent.SHIFT_DOWN_MASK | KeyEvent.ALT_DOWN_MASK | KeyEvent.META_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK)) == 0) {
              e.consume();
              final Topic edited = elementUnderEdit.getModel();
              final int[] topicPosition = edited.getPositionPath();
              endEdit(true);
              SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                  final Topic theTopic = model.findForPositionPath(topicPosition);
                  if (theTopic != null) {
                    makeNewChildAndStartEdit(theTopic, null);
                  }
                }
              });
            }
          }
          break;
          default:
            break;
        }
      }

      @Override
      public void keyTyped(final KeyEvent e) {
        if (e.getKeyChar() == KeyEvent.VK_ENTER) {
          if (((KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK) & e.getModifiersEx()) == 0) {
            e.consume();
            endEdit(true);
          }
          else {
            e.consume();
            textEditor.insert("\n", textEditor.getCaretPosition()); //NOI18N
          }
        }
      }

      @Override
      public void keyReleased(final KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
          e.consume();
          final Topic edited = elementUnderEdit == null ? null : elementUnderEdit.getModel();
          endEdit(false);
          if (edited != null && edited.canBeLost()) {
            deleteTopics(edited);
          }
        }
      }
    });

    this.textEditor.getDocument().addDocumentListener(new DocumentListener() {

      private void updateEditorPanelSize(final Dimension newSize) {
        final Dimension editorPanelMinSize = textEditorPanel.getMinimumSize();
        final Dimension newDimension = new Dimension(Math.max(editorPanelMinSize.width, newSize.width), Math.max(editorPanelMinSize.height, newSize.height));
        textEditorPanel.setSize(newDimension);
        textEditorPanel.repaint();
      }

      @Override
      public void insertUpdate(DocumentEvent e) {
        updateEditorPanelSize(textEditor.getPreferredSize());
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        updateEditorPanelSize(textEditor.getPreferredSize());
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        updateEditorPanelSize(textEditor.getPreferredSize());
      }
    });
    this.textEditorPanel.add(this.textEditor, BorderLayout.CENTER);

    super.setOpaque(true);

    final KeyAdapter keyAdapter = new KeyAdapter() {

      @Override
      public void keyTyped(KeyEvent e) {
        switch (e.getKeyChar()) {
          case '\t': {
            if (hasOnlyTopicSelected()) {
              makeNewChildAndStartEdit(selectedTopics.get(0), null);
            }
          }
          break;
          case '\n': {
            if (!hasActiveEditor() && hasOnlyTopicSelected()) {
              final Topic baseTopic = selectedTopics.get(0);
              makeNewChildAndStartEdit(baseTopic.getParent() == null ? baseTopic : baseTopic.getParent(), baseTopic);
            }
          }
          break;
          case ' ': {
            if (!hasSelectedTopics()) {
              select(getModel().getRoot(), false);
            }
            else if (hasOnlyTopicSelected() & (e.getModifiersEx() & (KeyEvent.SHIFT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK | KeyEvent.META_DOWN_MASK)) != 0) {
              startEdit((AbstractElement) selectedTopics.get(0).getPayload());
            }
          }
          break;
          default:
            break;
        }
      }

      @Override
      public void keyReleased(final KeyEvent e) {
        switch (e.getKeyCode()) {
          case KeyEvent.VK_DELETE: {
            e.consume();
            deleteSelectedTopics();
          }
          break;
          case KeyEvent.VK_LEFT:
          case KeyEvent.VK_RIGHT:
          case KeyEvent.VK_UP:
          case KeyEvent.VK_DOWN: {
            e.consume();
            processMoveFocusByKey(e.getKeyCode());
          }
          break;
          default:
            break;
        }
      }
    };

    this.setFocusTraversalKeysEnabled(false);

    final MindMapPanel theInstance = this;

    final MouseAdapter adapter = new MouseAdapter() {

      @Override
      public void mouseEntered(final MouseEvent e) {
        setCursor(Cursor.getDefaultCursor());
      }

      @Override
      public void mouseMoved(final MouseEvent e) {
        if (!controller.isMouseMoveProcessingAllowed(theInstance)) {
          return;
        }
        final AbstractElement element = findTopicUnderPoint(e.getPoint());
        if (element == null) {
          setCursor(Cursor.getDefaultCursor());
          setToolTipText(null);
        }
        else {
          final ElementPart part = element.findPartForPoint(e.getPoint());
          setCursor(part == ElementPart.ICONS || part == ElementPart.COLLAPSATOR ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
          if (part == ElementPart.ICONS) {
            final Extra<?> extra = element.getIconBlock().findExtraForPoint(e.getPoint().getX() - element.getBounds().getX(), e.getPoint().getY() - element.getBounds().getY());
            if (extra != null) {
              setToolTipText(makeHtmlTooltipForExtra(extra));
            }
            else {
              setToolTipText(null);
            }
          }
          else {
            setToolTipText(null);
          }
        }
      }

      @Override
      public void mousePressed(final MouseEvent e) {
        if (!controller.isMouseClickProcessingAllowed(theInstance)) {
          return;
        }
        try {
          if (e.isPopupTrigger()) {
            mouseDragSelection = null;
            MindMap theMap = model;
            AbstractElement element = null;
            if (theMap != null) {
              element = findTopicUnderPoint(e.getPoint());
            }
            processPopUp(e.getPoint(), element);
            e.consume();
          }
          else {
            endEdit(false);
            mouseDragSelection = null;
          }
        }
        catch (Exception ex) {
          logger.error("Error during mousePressed()", ex);
        }
      }

      @Override
      public void mouseReleased(final MouseEvent e) {
        if (!controller.isMouseClickProcessingAllowed(theInstance)) {
          return;
        }
        try {
          if (draggedElement != null) {
            if (endDragOfElement(draggedElement.getPosition(), draggedElement.getElement(), destinationElement)) {
              updateView(true);
            }
          }
          else if (mouseDragSelection != null) {
            final List<Topic> covered = mouseDragSelection.getAllSelectedElements(model);
            if ((e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0) {
              for (final Topic m : covered) {
                select(m, false);
              }
            }
            else if ((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) != 0) {
              for (final Topic m : covered) {
                select(m, true);
              }
            }
            else {
              removeAllSelection();
              for (final Topic m : covered) {
                select(m, false);
              }
            }
          }
          else if (e.isPopupTrigger()) {
            mouseDragSelection = null;
            MindMap theMap = model;
            AbstractElement element = null;
            if (theMap != null) {
              element = findTopicUnderPoint(e.getPoint());
            }
            processPopUp(e.getPoint(), element);
            e.consume();
          }
        }
        catch (Exception ex) {
          logger.error("Error during mouseReleased()", ex);
        }
        finally {
          mouseDragSelection = null;
          draggedElement = null;
          destinationElement = null;
          repaint();
        }
      }

      @Override
      public void mouseDragged(final MouseEvent e) {
        if (!controller.isMouseMoveProcessingAllowed(theInstance)) {
          return;
        }
        scrollRectToVisible(new Rectangle(e.getX(), e.getY(), 1, 1));

        if (!popupMenuActive) {
          if (draggedElement == null && mouseDragSelection == null) {
            final AbstractElement elementUnderMouse = findTopicUnderPoint(e.getPoint());
            if (elementUnderMouse == null) {
              MindMap theMap = model;
              if (theMap != null) {
                final AbstractElement element = findTopicUnderPoint(e.getPoint());
                if (controller.isSelectionAllowed(theInstance) && element == null) {
                  mouseDragSelection = new MouseSelectedArea(e.getPoint());
                }
              }
            }
            else {
              if (controller.isElementDragAllowed(theInstance)) {
                if (elementUnderMouse!=null && elementUnderMouse.isMoveable()) {
                  selectedTopics.clear();
                  draggedElement = new DraggedElement(elementUnderMouse, new Point((int)Math.round(e.getPoint().getX() - elementUnderMouse.getBounds().getX()),(int) Math.round(e.getPoint().getY() - elementUnderMouse.getBounds().getY())));
                  draggedElement.updatePosition(e.getPoint());
                  findDestinationElementForDragged();
                }else{
                  draggedElement = null;
                }
                  repaint();
              }
            }
          }
          else if (mouseDragSelection != null) {
            if (controller.isSelectionAllowed(theInstance)) {
              mouseDragSelection.update(e);
            }
            else {
              mouseDragSelection = null;
            }
            repaint();
          }
          else if (draggedElement != null) {
            if (controller.isElementDragAllowed(theInstance)) {
              draggedElement.updatePosition(e.getPoint());
              findDestinationElementForDragged();
            }
            else {
              draggedElement = null;
            }
            repaint();
          }
        }
        else {
          mouseDragSelection = null;
        }
      }

      @Override
      public void mouseWheelMoved(final MouseWheelEvent e) {
        if (controller.isMouseWheelProcessingAllowed(theInstance)) {
          mouseDragSelection = null;
          draggedElement = null;

          if (!e.isConsumed() && e.isControlDown()) {
            endEdit(false);

            setScale(Math.max(0.3d, Math.min(getScale() + (SCALE_STEP * -e.getWheelRotation()), 10.0d)));

            updateView(false);
            e.consume();
          }
          else {
            sendToParent(e);
          }
        }
      }

      @Override
      public void mouseClicked(final MouseEvent e) {
        if (!controller.isMouseClickProcessingAllowed(theInstance)) {
          return;
        }
        mouseDragSelection = null;
        draggedElement = null;

        MindMap theMap = model;
        AbstractElement element = null;
        if (theMap != null) {
          element = findTopicUnderPoint(e.getPoint());
        }

        final ElementPart part = element == null ? ElementPart.NONE : element.findPartForPoint(e.getPoint());
        if (part == ElementPart.COLLAPSATOR) {
          removeAllSelection();
          ((AbstractCollapsableElement) element).setCollapse(!element.isCollapsed());
          invalidate();
          fireNotificationMindMapChanged();
          repaint();
        }
        else if (part != ElementPart.ICONS && e.getClickCount() > 1) {
          startEdit(element);
        }
        else if (part == ElementPart.ICONS) {
          final Extra<?> extra = element.getIconBlock().findExtraForPoint(e.getPoint().getX() - element.getBounds().getX(), e.getPoint().getY() - element.getBounds().getY());
          if (extra != null) {
            fireNotificationClickOnExtra(element.getModel(), e.getClickCount(), extra);
          }
        }
        else {
          if (element != null) {
            if ((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == 0) {
              // only
              removeAllSelection();
              select(element.getModel(), false);
            }
            else {
              // group
              if (selectedTopics.isEmpty()) {
                select(element.getModel(), false);
              }
              else {
                select(element.getModel(), true);
              }
            }
          }
        }
      }
    };

    addMouseWheelListener(adapter);
    addMouseListener(adapter);
    addMouseMotionListener(adapter);
    addKeyListener(keyAdapter);

    this.textEditorPanel.setVisible(false);
    this.add(this.textEditorPanel);
  }

  private String makeHtmlTooltipForExtra(final Extra<?> extra) {
    final StringBuilder builder = new StringBuilder();

    builder.append("<html>"); //NOI18N

    switch (extra.getType()) {
      case FILE: {
        builder.append(BUNDLE.getString("MindMapPanel.tooltipOpenFile")).append(StringEscapeUtils.escapeHtml(((ExtraFile) extra).getAsString()));
      }
      break;
      case TOPIC: {
        final Topic topic = this.getModel().findTopicForLink((ExtraTopic) extra);
        builder.append(BUNDLE.getString("MindMapPanel.tooltipJumpToTopic")).append(StringEscapeUtils.escapeHtml(ModelUtils.makeShortTextVersion(topic == null ? "----" : topic.getText(), 32)));
      }
      break;
      case LINK: {
        builder.append(BUNDLE.getString("MindMapPanel.tooltipOpenLink")).append(StringEscapeUtils.escapeHtml(ModelUtils.makeShortTextVersion(((ExtraLink) extra).getAsString(), 48)));
      }
      break;
      case NOTE: {
        builder.append(BUNDLE.getString("MindMapPanel.tooltipOpenText")).append(StringEscapeUtils.escapeHtml(ModelUtils.makeShortTextVersion(((ExtraNote) extra).getAsString(), 64)));
      }
      break;
      default: {
        builder.append("<b>Unknown</b>"); //NOI18N
      }
      break;
    }

    builder.append("</html>"); //NOI18N

    return builder.toString();
  }

  public void refreshConfiguration() {
    final MindMapPanel theInstance = this;
    final double scale = this.config.getScale();
    this.config.makeAtomicChange(new Runnable() {
      @Override
      public void run() {
        config.makeFullCopyOf(controller.provideConfigForMindMapPanel(theInstance), false, false);
        config.setScale(scale);
      }
    });
    invalidate();
    repaint();
  }

  private static final int DRAG_POSITION_UNKNOWN = -1;
  private static final int DRAG_POSITION_LEFT = 1;
  private static final int DRAG_POSITION_TOP = 2;
  private static final int DRAG_POSITION_BOTTOM = 3;
  private static final int DRAG_POSITION_RIGHT = 4;

  private int calcDropPosition(final AbstractElement destination, final Point dropPoint) {
    int result = DRAG_POSITION_UNKNOWN;
    if (destination.getClass() == ElementRoot.class) {
      result = dropPoint.getX() < destination.getBounds().getCenterX() ? DRAG_POSITION_LEFT : DRAG_POSITION_RIGHT;
    }
    else {
      final boolean destinationIsLeft = destination.isLeftDirection();
      final Rectangle2D bounds = destination.getBounds();

      if (bounds != null && dropPoint != null) {
        final double edgeOffset = bounds.getWidth() * 0.2d;
        if (dropPoint.getX() >= (bounds.getX() + edgeOffset) && dropPoint.getX() <= (bounds.getMaxX() - edgeOffset)) {
          result = dropPoint.getY() < bounds.getCenterY() ? DRAG_POSITION_TOP : DRAG_POSITION_BOTTOM;
        }
        else {
          if (destinationIsLeft) {
            result = dropPoint.getX() < bounds.getCenterX() ? DRAG_POSITION_LEFT : DRAG_POSITION_UNKNOWN;
          }
          else {
            result = dropPoint.getX() > bounds.getCenterX() ? DRAG_POSITION_RIGHT : DRAG_POSITION_UNKNOWN;
          }
        }
      }
    }
    return result;
  }

  private boolean endDragOfElement(final Point dropPoint, final AbstractElement dragged, final AbstractElement destination) {
    final boolean ignore = dragged.getModel() == destination.getModel() || dragged.getBounds().contains(dropPoint) || destination.getModel().hasAncestor(dragged.getModel());
    if (ignore) {
      return false;
    }

    boolean changed = true;

    final AbstractElement destParent = destination.getParent();
    final int pos = calcDropPosition(destination, dropPoint);
    switch (pos) {
      case DRAG_POSITION_TOP:
      case DRAG_POSITION_BOTTOM: {
        dragged.getModel().moveToNewParent(destParent.getModel());
        if (pos == DRAG_POSITION_TOP) {
          dragged.getModel().moveBefore(destination.getModel());
        }
        else {
          dragged.getModel().moveAfter(destination.getModel());
        }

        if (destination.getClass() == ElementLevelFirst.class) {
          AbstractCollapsableElement.makeTopicLeftSided(dragged.getModel(), destination.isLeftDirection());
        }
        else {
          AbstractCollapsableElement.makeTopicLeftSided(dragged.getModel(), false);
        }
      }
      break;
      case DRAG_POSITION_RIGHT:
      case DRAG_POSITION_LEFT: {
        if (dragged.getParent() == destination) {
          // the same parent
          if (destination.getClass() == ElementRoot.class) {
            // process only for the root, just update direction
            if (dragged instanceof AbstractCollapsableElement) {
              ((AbstractCollapsableElement) dragged).setLeftDirection(pos == DRAG_POSITION_LEFT);
            }
          }
        }
        else {
          dragged.getModel().moveToNewParent(destination.getModel());
          if (destination instanceof AbstractCollapsableElement && destination.isCollapsed() && (controller == null ? true : controller.isUnfoldCollapsedTopicDropTarget(this))) { //NOI18N
            ((AbstractCollapsableElement) destination).setCollapse(false);
          }
          if (dropPoint.getY() < destination.getBounds().getY()) {
            dragged.getModel().makeFirst();
          }
          else {
            dragged.getModel().makeLast();
          }
          if (destination.getClass() == ElementRoot.class) {
            AbstractCollapsableElement.makeTopicLeftSided(dragged.getModel(), pos == DRAG_POSITION_LEFT);
          }
          else {
            AbstractCollapsableElement.makeTopicLeftSided(dragged.getModel(), false);
          }
        }
      }
      break;
      default:
        break;
    }
    dragged.getModel().setPayload(null);

    return changed;
  }

  private void sendToParent(final AWTEvent evt) {
    final Container parent = this.getParent();
    if (parent != null) {
      parent.dispatchEvent(evt);
    }
  }

  private void processMoveFocusByKey(final int key) {
    if (hasOnlyTopicSelected()) {
      final AbstractElement current = (AbstractElement) this.selectedTopics.get(0).getPayload();
      if (current == null) {
        return;
      }

      AbstractElement nextFocused = null;

      boolean modelChanged = false;

      if (current.isMoveable()) {
        boolean processFirstChild = false;
        switch (key) {
          case KeyEvent.VK_LEFT: {
            if (current.isLeftDirection()) {
              processFirstChild = true;
            }
            else {
              nextFocused = (AbstractElement) current.getModel().getParent().getPayload();
            }
          }
          break;
          case KeyEvent.VK_RIGHT: {
            if (current.isLeftDirection()) {
              nextFocused = (AbstractElement) current.getModel().getParent().getPayload();
            }
            else {
              processFirstChild = true;
            }
          }
          break;
          case KeyEvent.VK_UP:
          case KeyEvent.VK_DOWN: {
            final boolean firstLevel = current.getClass() == ElementLevelFirst.class;
            final boolean currentLeft = AbstractCollapsableElement.isLeftSidedTopic(current.getModel());

            final TopicChecker checker = new TopicChecker() {
              @Override
              public boolean check(final Topic topic) {
                if (!firstLevel) {
                  return true;
                }
                else {
                  if (currentLeft) {
                    return AbstractCollapsableElement.isLeftSidedTopic(topic);
                  }
                  else {
                    return !AbstractCollapsableElement.isLeftSidedTopic(topic);
                  }
                }
              }
            };

            final Topic topic = key == KeyEvent.VK_UP ? current.getModel().findPrev(checker) : current.getModel().findNext(checker);
            nextFocused = topic == null ? null : (AbstractElement) topic.getPayload();
          }
          break;
          default:
            break;
        }

        if (processFirstChild) {
          if (current.hasChildren()) {
            if (current.isCollapsed()) {
              ((AbstractCollapsableElement) current).setCollapse(false);
              modelChanged = true;
            }

            nextFocused = (AbstractElement) (current.getModel().getChildren().get(0)).getPayload();
          }
        }
      }
      else {
        switch (key) {
          case KeyEvent.VK_LEFT: {
            for (final Topic t : current.getModel().getChildren()) {
              final AbstractElement e = (AbstractElement) t.getPayload();
              if (e != null && e.isLeftDirection()) {
                nextFocused = e;
                break;
              }
            }
          }
          break;
          case KeyEvent.VK_RIGHT: {
            for (final Topic t : current.getModel().getChildren()) {
              final AbstractElement e = (AbstractElement) t.getPayload();
              if (e != null && !e.isLeftDirection()) {
                nextFocused = e;
                break;
              }
            }
          }
          break;
        }
      }

      if (nextFocused != null) {
        removeAllSelection();
        select(nextFocused.getModel(), false);
      }

      if (modelChanged) {
        invalidate();
        fireNotificationMindMapChanged();
      }
    }
  }

  private void ensureVisibility(final AbstractElement e) {
    fireNotificationEnsureTopicVisibility(e.getModel());
  }

  private boolean hasActiveEditor() {
    return this.elementUnderEdit != null;
  }

  public boolean isShowJumps() {
    return Boolean.parseBoolean(this.model.getAttribute(ATTR_SHOW_JUMPS));
  }

  public void setShowJumps(final boolean flag) {
    this.model.setAttribute(ATTR_SHOW_JUMPS, flag ? "true" : null);
    repaint();
    fireNotificationMindMapChanged();
  }

  public void makeNewChildAndStartEdit(final Topic parent, final Topic baseTopic) {
    if (parent != null) {
      removeAllSelection();

      final Topic newTopic = parent.makeChild("", baseTopic); //NOI18N

      if (this.controller.isCopyColorInfoFromParentToNewChildAllowed(this) && !parent.isRoot()) {
        MindMapUtils.copyColorAttributes(parent, newTopic);
      }

      final AbstractElement parentElement = (AbstractElement) parent.getPayload();

      if (parent.getChildren().size() != 1 && parent.getParent() == null && baseTopic == null) {
        int numLeft = 0;
        int numRight = 0;
        for (final Topic t : parent.getChildren()) {
          if (AbstractCollapsableElement.isLeftSidedTopic(t)) {
            numLeft++;
          }
          else {
            numRight++;
          }
        }

        AbstractCollapsableElement.makeTopicLeftSided(newTopic, numLeft < numRight);
      }
      else {
        if (baseTopic != null && baseTopic.getPayload() != null) {
          final AbstractElement element = (AbstractElement) baseTopic.getPayload();
          AbstractCollapsableElement.makeTopicLeftSided(newTopic, element.isLeftDirection());
        }
      }

      if (parentElement instanceof AbstractCollapsableElement && parentElement.isCollapsed()) {
        ((AbstractCollapsableElement) parentElement).setCollapse(false);
      }

      select(newTopic, false);
      updateView(false);
      startEdit((AbstractElement) newTopic.getPayload());
    }
  }

  protected void fireNotificationSelectionChanged() {
    final Topic[] selected = this.selectedTopics.toArray(new Topic[this.selectedTopics.size()]);
    for (final MindMapListener l : this.mindMapListeners) {
      l.onChangedSelection(this, selected);
    }
  }

  protected void fireNotificationMindMapChanged() {
    for (final MindMapListener l : this.mindMapListeners) {
      l.onMindMapModelChanged(this);
    }
  }

  protected void fireNotificationClickOnExtra(final Topic topic, final int clicks, final Extra<?> extra) {
    for (final MindMapListener l : this.mindMapListeners) {
      l.onClickOnExtra(this, clicks, topic, extra);
    }
  }

  protected void fireNotificationEnsureTopicVisibility(final Topic topic) {
    for (final MindMapListener l : this.mindMapListeners) {
      l.onEnsureVisibilityOfTopic(this, topic);
    }
  }

  public void deleteTopics(final Topic... topics) {
    endEdit(false);
    removeAllSelection();
    boolean allowed = true;
    for (final MindMapListener l : this.mindMapListeners) {
      allowed &= l.allowedRemovingOfTopics(this, topics);
    }
    if (allowed) {
      for (final Topic t : topics) {
        this.model.removeTopic(t);
      }
      updateView(true);
    }
  }

  public void collapseOrExpandAll(final boolean collapse) {
    endEdit(false);
    removeAllSelection();

    if (this.model.getRoot() != null) {
      final AbstractElement root = (AbstractElement) this.model.getRoot().getPayload();
      if (root != null && root.collapseOrExpandAllChildren(collapse)) {
        updateView(true);
      }
    }
  }

  public void deleteSelectedTopics() {
    if (!this.selectedTopics.isEmpty()) {
      deleteTopics(this.selectedTopics.toArray(new Topic[this.selectedTopics.size()]));
    }
  }

  public boolean hasSelectedTopics() {
    return !this.selectedTopics.isEmpty();
  }

  public boolean hasOnlyTopicSelected() {
    return this.selectedTopics.size() == 1;
  }

  public void removeFromSelection(final Topic t) {
    if (this.selectedTopics.contains(t)) {
      if (this.selectedTopics.remove(t)) {
        fireNotificationSelectionChanged();
      }
      repaint();
    }
  }

  public void select(final Topic t, final boolean removeIfPresented) {
    if (this.controller.isSelectionAllowed(this) && t != null) {
      if (!this.selectedTopics.contains(t)) {
        if (this.selectedTopics.add(t)) {
          fireNotificationSelectionChanged();
        }
        fireNotificationEnsureTopicVisibility(t);
        repaint();
      }
      else if (removeIfPresented) {
        removeFromSelection(t);
      }
    }
  }

  public Topic[] getSelectedTopics() {
    return this.selectedTopics.toArray(new Topic[this.selectedTopics.size()]);
  }

  public void updateEditorAfterResizing() {
    if (this.elementUnderEdit != null) {
      final AbstractElement element = this.elementUnderEdit;
      final Dimension textBlockSize = new Dimension((int) element.getBounds().getWidth(), (int) element.getBounds().getHeight());
      this.textEditorPanel.setBounds((int) element.getBounds().getX(), (int) element.getBounds().getY(), textBlockSize.width, textBlockSize.height);
      this.textEditor.setMinimumSize(textBlockSize);
      this.textEditorPanel.setVisible(true);
      this.textEditor.requestFocus();
    }
  }

  public void hideEditor() {
    this.textEditorPanel.setVisible(false);
    this.elementUnderEdit = null;
  }

  public void endEdit(final boolean commit) {
    try {
      if (commit && this.elementUnderEdit != null) {
        final AbstractElement editedElement = this.elementUnderEdit;
        final Topic editedTopic = this.elementUnderEdit.getModel();
        editedElement.setText(this.textEditor.getText());
        this.textEditorPanel.setVisible(false);
        updateView(true);
        fireNotificationEnsureTopicVisibility(editedTopic);
      }
    }
    finally {
      this.elementUnderEdit = null;
      this.textEditorPanel.setVisible(false);
      this.requestFocus();
    }
  }

  public void startEdit(final AbstractElement element) {
    if (element == null) {
      this.elementUnderEdit = null;
      this.textEditorPanel.setVisible(false);
    }
    else {
      this.elementUnderEdit = element;
      element.fillByTextAndFont(this.textEditor);
      final Dimension textBlockSize = new Dimension((int) element.getBounds().getWidth(), (int) element.getBounds().getHeight());
      this.textEditorPanel.setBounds((int) element.getBounds().getX(), (int) element.getBounds().getY(), textBlockSize.width, textBlockSize.height);
      this.textEditor.setMinimumSize(textBlockSize);

      ensureVisibility(this.elementUnderEdit);

      this.textEditorPanel.setVisible(true);
      this.textEditor.requestFocus();
    }
  }

  private void findDestinationElementForDragged() {
    if (this.draggedElement != null) {
      final AbstractElement root = (AbstractElement) this.model.getRoot().getPayload();
      this.destinationElement = root.findNearestOpenedTopicToPoint(this.draggedElement.getElement(), this.draggedElement.getPosition());
    }
    else {
      this.destinationElement = null;
    }
  }

  protected void processPopUp(final Point point, final AbstractElement elementUnderMouse) {
    if (this.controller != null) {
      final ElementPart partUnderMouse = elementUnderMouse == null ? null : elementUnderMouse.findPartForPoint(point);

      if (elementUnderMouse != null && !this.selectedTopics.contains(elementUnderMouse.getModel())) {
        this.selectedTopics.clear();
        this.select(elementUnderMouse.getModel(), false);
      }

      final JPopupMenu menu = this.controller.makePopUpForMindMapPanel(this, point, elementUnderMouse, partUnderMouse);
      if (menu != null) {

        final MindMapPanel theInstance = this;
        menu.addPopupMenuListener(new PopupMenuListener() {

          @Override
          public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
            theInstance.mouseDragSelection = null;
            theInstance.popupMenuActive = true;
          }

          @Override
          public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
            theInstance.mouseDragSelection = null;
            theInstance.popupMenuActive = false;
          }

          @Override
          public void popupMenuCanceled(final PopupMenuEvent e) {
            theInstance.mouseDragSelection = null;
            theInstance.popupMenuActive = false;
          }
        });

        menu.show(this, point.x, point.y);
      }
    }
  }

  public void addMindMapListener(final MindMapListener l) {
    ModelUtils.assertNotNull("Listener must not be null", l); //NOI18N
    this.mindMapListeners.add(l);
  }

  public void removeMindMapListener(final MindMapListener l) {
    if (l != null) {
      this.mindMapListeners.remove(l);
    }
  }

  public void setModel(final MindMap model) {
    final List<int[]> selectedPaths = new ArrayList<>();
    for (final Topic t : this.selectedTopics) {
      selectedPaths.add(t.getPositionPath());
    }

    this.selectedTopics.clear();

    this.model = model;

    updateView(false);

    boolean selectionChanged = false;
    for (final int[] posPath : selectedPaths) {
      final Topic topic = this.model.findForPositionPath(posPath);
      if (topic == null) {
        selectionChanged = true;
      }
      else {
        if (!MindMapUtils.isHidden(topic)) {
          this.selectedTopics.add(topic);
        }
      }
    }
    if (selectionChanged) {
      fireNotificationSelectionChanged();
    }
    repaint();
  }

  @Override
  public boolean isFocusable() {
    return true;
  }

  public MindMap getModel() {
    return this.model;
  }

  public void setScale(final double zoom) {
    this.config.setScale(zoom);
  }

  public double getScale() {
    return this.config.getScale();
  }

  private static void drawBackground(final Graphics2D g, final MindMapPanelConfig cfg) {
    final Rectangle clipBounds = g.getClipBounds();

    if (cfg.isDrawBackground()) {
      g.setColor(cfg.getPaperColor());
      g.fillRect(clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height);

      if (cfg.isShowGrid()) {
        final double scaledGridStep = cfg.getGridStep() * cfg.getScale();

        final float minX = clipBounds.x;
        final float minY = clipBounds.y;
        final float maxX = clipBounds.x + clipBounds.width;
        final float maxY = clipBounds.y + clipBounds.height;

        g.setColor(cfg.getGridColor());

        for (float x = 0.0f; x < maxX; x += scaledGridStep) {
          if (x < minX) {
            continue;
          }
          final int intx = Math.round(x);
          g.drawLine(intx, (int) minY, intx, (int) maxY);
        }

        for (float y = 0.0f; y < maxY; y += scaledGridStep) {
          if (y < minY) {
            continue;
          }
          final int inty = Math.round(y);
          g.drawLine((int) minX, inty, (int) maxX, inty);
        }
      }
    }
  }

  private static boolean isModelValid(final MindMap map) {
    return map == null || map.getRoot() == null ? true : map.getRoot().getPayload() != null;
  }

  public static void drawOnGraphicsForConfiguration(final Graphics2D g, final MindMapPanelConfig config, final MindMap map, final boolean drawSelection, final List<Topic> selectedTopics) {
    drawBackground(g, config);
    drawTopics(g, config, map);
    if (drawSelection && selectedTopics != null && !selectedTopics.isEmpty()) {
      drawSelection(g, config, selectedTopics);
    }
  }

  private void drawDestinationElement(final Graphics2D g, final MindMapPanelConfig cfg) {
    if (this.destinationElement != null && this.draggedElement != null) {
      g.setColor(new Color((cfg.getSelectLineColor().getRGB() & 0xFFFFFF) | 0x80000000, true));
      g.setStroke(new BasicStroke(this.config.safeScaleFloatValue(3.0f, 0.1f)));

      final Rectangle2D rectToDraw = new Rectangle2D.Double();
      rectToDraw.setRect(this.destinationElement.getBounds());
      final double selectLineGap = cfg.getSelectLineGap() * 3.0d * cfg.getScale();
      rectToDraw.setRect(rectToDraw.getX() - selectLineGap, rectToDraw.getY() - selectLineGap, rectToDraw.getWidth() + selectLineGap * 2, rectToDraw.getHeight() + selectLineGap * 2);

      final int position = calcDropPosition(this.destinationElement, this.draggedElement.getPosition());

      boolean draw = !this.draggedElement.isPositionInside() && !this.destinationElement.getModel().hasAncestor(this.draggedElement.getElement().getModel());

      switch (position) {
        case DRAG_POSITION_TOP: {
          rectToDraw.setRect(rectToDraw.getX(), rectToDraw.getY(), rectToDraw.getWidth(), rectToDraw.getHeight() / 2);
        }
        break;
        case DRAG_POSITION_BOTTOM: {
          rectToDraw.setRect(rectToDraw.getX(), rectToDraw.getY() + rectToDraw.getHeight() / 2, rectToDraw.getWidth(), rectToDraw.getHeight() / 2);
        }
        break;
        case DRAG_POSITION_LEFT: {
          rectToDraw.setRect(rectToDraw.getX(), rectToDraw.getY(), rectToDraw.getWidth() / 2, rectToDraw.getHeight());
        }
        break;
        case DRAG_POSITION_RIGHT: {
          rectToDraw.setRect(rectToDraw.getX() + rectToDraw.getWidth() / 2, rectToDraw.getY(), rectToDraw.getWidth() / 2, rectToDraw.getHeight());
        }
        break;
        default:
          draw = false;
          break;
      }

      if (draw) {
        g.fill(rectToDraw);
      }
    }
  }

  private static void drawSelection(final Graphics2D g, final MindMapPanelConfig cfg, final List<Topic> selectedTopics) {
    if (selectedTopics != null && !selectedTopics.isEmpty()) {
      g.setColor(cfg.getSelectLineColor());
      final Stroke dashed = new BasicStroke(cfg.safeScaleFloatValue(cfg.getSelectLineWidth(), 0.1f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{cfg.safeScaleFloatValue(1.0f, 0.1f), cfg.safeScaleFloatValue(4.0f, 0.1f)}, 0);
      g.setStroke(dashed);
      final double selectLineGap = cfg.getSelectLineGap() * cfg.getScale();
      final double dblLineGap = selectLineGap * 2.0d;

      for (final Topic s : selectedTopics) {
        final AbstractElement e = (AbstractElement) s.getPayload();
        if (e != null) {
          g.drawRect((int) Math.round(e.getBounds().getX() - selectLineGap), (int) Math.round(e.getBounds().getY() - selectLineGap), (int) Math.round(e.getBounds().getWidth() + dblLineGap), (int) Math.round(e.getBounds().getHeight() + dblLineGap));
        }
      }
    }
  }

  private static void drawTopics(final Graphics2D g, final MindMapPanelConfig cfg, final MindMap map) {
    if (map != null) {
      if (Boolean.parseBoolean(map.getAttribute(ATTR_SHOW_JUMPS))) {
        drawJumps(g, map, cfg);
      }

      final Topic root = map.getRoot();
      if (root != null) {
        drawTopicTree(g, root, cfg);
      }
    }
  }

  private static double findLineAngle(final double sx, final double sy, final double ex, final double ey) {
    final double deltax = ex - sx;
    if (deltax == 0.0d) {
      return Math.PI / 2;
    }
    return Math.atan((ey - sy) / deltax) + (ex < sx ? Math.PI : 0);
  }

  private static void drawJumps(final Graphics2D gfx, final MindMap map, final MindMapPanelConfig cfg) {
    final List<Topic> allTopicsWithJumps = map.findAllTopicsForExtraType(Extra.ExtraType.TOPIC);

    final float scaledSize = cfg.safeScaleFloatValue(cfg.getJumpLinkWidth(), 0.1f);

    final Stroke lineStroke = new BasicStroke(scaledSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 0, new float[]{scaledSize, scaledSize * 3.0f}, 0);
    final Stroke arrowStroke = new BasicStroke(cfg.safeScaleFloatValue(cfg.getJumpLinkWidth() * 1.0f, 0.3f));

    gfx.setColor(cfg.getJumpLinkColor());

    final float arrowSize = cfg.safeScaleFloatValue(15.0f, 0.2f);

    for (Topic src : allTopicsWithJumps) {
      final ExtraTopic extra = (ExtraTopic) src.getExtras().get(Extra.ExtraType.TOPIC);

      src = MindMapUtils.isHidden(src) ? MindMapUtils.findFirstVisibleAncestor(src) : src;

      final AbstractElement srcElement = (AbstractElement) src.getPayload();
      if (extra != null) {
        Topic dst = map.findTopicForLink(extra);
        if (dst != null) {
          if (MindMapUtils.isHidden(dst)) {
            dst = MindMapUtils.findFirstVisibleAncestor(dst);
            if (dst == src) {
              dst = null;
            }
          }

          if (dst != null) {
            final AbstractElement dstElement = (AbstractElement) dst.getPayload();
            if (!MindMapUtils.isHidden(dst)) {
              final Rectangle2D srcRect = srcElement.getBounds();
              final Rectangle2D dstRect = dstElement.getBounds();
              drawArrowToDestination(gfx, srcRect, dstRect, lineStroke, arrowStroke, arrowSize);
            }
          }
        }
      }
    }
  }

  private static void drawArrowToDestination(final Graphics2D gfx, final Rectangle2D start, final Rectangle2D destination, final Stroke lineStroke, final Stroke arrowStroke, final float arrowSize) {

    final double startx = start.getCenterX();
    final double starty = start.getCenterY();
    final double endx = destination.getCenterX();
    final double endy = destination.getCenterY();

    final Point2D arrowPoint = Utils.findRectEdgeIntersection(destination, startx, starty);

    if (arrowPoint != null) {
      gfx.setStroke(lineStroke);
      gfx.drawLine((int) startx, (int) starty, (int) endx, (int) endy);
      gfx.setStroke(arrowStroke);

      double angle = findLineAngle(arrowPoint.getX(), arrowPoint.getY(), startx, starty);

      final double arrowAngle = Math.PI / 20.0d;

      final double x1 = arrowSize * Math.cos(angle - arrowAngle);
      final double y1 = arrowSize * Math.sin(angle - arrowAngle);
      final double x2 = arrowSize * Math.cos(angle + arrowAngle);
      final double y2 = arrowSize * Math.sin(angle + arrowAngle);

      final GeneralPath polygon = new GeneralPath();
      polygon.moveTo(arrowPoint.getX(), arrowPoint.getY());
      polygon.lineTo(arrowPoint.getX() + x1, arrowPoint.getY() + y1);
      polygon.lineTo(arrowPoint.getX() + x2, arrowPoint.getY() + y2);
      polygon.closePath();
      gfx.fill(polygon);
    }
  }

  private static void drawTopicTree(final Graphics2D gfx, final Topic topic, final MindMapPanelConfig cfg) {
    paintTopic(gfx, topic, cfg);
    final AbstractElement w = (AbstractElement) topic.getPayload();
    if (w.isCollapsed()) {
      return;
    }
    for (final Topic t : topic.getChildren()) {
      drawTopicTree(gfx, t, cfg);
    }
  }

  private static void paintTopic(final Graphics2D gfx, final Topic topic, final MindMapPanelConfig cfg) {
    ((AbstractElement) topic.getPayload()).doPaint(gfx, cfg);
  }

  private static void setElementSizesForElementAndChildren(final Graphics2D gfx, final MindMapPanelConfig cfg, final Topic topic, final int level) {
    AbstractElement widget = (AbstractElement) topic.getPayload();
    if (widget == null) {
      switch (level) {
        case 0:
          widget = new ElementRoot(topic);
          break;
        case 1:
          widget = new ElementLevelFirst(topic);
          break;
        default:
          widget = new ElementLevelOther(topic);
          break;
      }
      topic.setPayload(widget);
    }

    widget.updateElementBounds(gfx, cfg);
    for (final Topic t : topic.getChildren()) {
      setElementSizesForElementAndChildren(gfx, cfg, t, level + 1);
    }
    widget.updateBlockSize(cfg);
  }

  public static boolean calculateElementSizes(final Graphics2D gfx, final MindMap model, final MindMapPanelConfig cfg) {
    boolean result = false;

    final Topic root = model == null ? null : model.getRoot();
    if (root != null) {
      if (gfx.getFontMetrics() != null) {
        model.resetPayload();
        setElementSizesForElementAndChildren(gfx, cfg, root, 0);
        result = true;
      }
      else {
        root.setPayload(null);
      }
    }
    return result;
  }

  public static Dimension2D layoutModelElements(final MindMap model, final MindMapPanelConfig cfg) {
    Dimension2D result = null;
    final AbstractElement root = model == null ? null : model.getRoot() == null ? null : (AbstractElement) model.getRoot().getPayload();
    if (root != null) {
      root.alignElementAndChildren(cfg, true, 0, 0);
      result = root.getBlockSize();
    }
    return result;
  }

  protected static void moveDiagram(final MindMap model, final double deltaX, final double deltaY) {
    final AbstractElement root = model == null ? null : model.getRoot() == null ? null : (AbstractElement) model.getRoot().getPayload();
    if (root != null) {
      root.moveWholeTreeBranchCoordinates(deltaX, deltaY);
    }
  }

  private void changeSizeOfComponentWithNotification(final Dimension size) {
    if (size != null) {
      setMinimumSize(size);
      setPreferredSize(size);
      for (final MindMapListener l : this.mindMapListeners) {
        l.onMindMapModelRealigned(this, size);
      }
    }
  }

  public static Dimension layoutFullDiagramWithCenteringToPaper(final Graphics2D gfx, final MindMap map, final MindMapPanelConfig cfg, final Dimension2D paperSize) {
    Dimension resultSize = null;
    if (calculateElementSizes(gfx, map, cfg)) {
      Dimension2D rootBlockSize = layoutModelElements(map, cfg);
      final double paperMargin = cfg.getPaperMargins() * cfg.getScale();

      if (rootBlockSize != null) {
        if (paperSize != null) {
          final ElementRoot rootElement = (ElementRoot) map.getRoot().getPayload();

          double rootOffsetXInBlock = rootElement.getLeftBlockSize().getWidth();
          double rootOffsetYInBlock = (rootBlockSize.getHeight() - rootElement.getBounds().getHeight()) / 2;

          rootOffsetXInBlock += paperSize.getWidth() - rootBlockSize.getWidth() <= paperMargin ? paperMargin : (paperSize.getWidth() - rootBlockSize.getWidth()) / 2;
          rootOffsetYInBlock += paperSize.getHeight() - rootBlockSize.getHeight() <= paperMargin ? paperMargin : (paperSize.getHeight() - rootBlockSize.getHeight()) / 2;

          moveDiagram(map, rootOffsetXInBlock, rootOffsetYInBlock);
        }
        resultSize = new Dimension((int) Math.round(rootBlockSize.getWidth() + paperMargin * 2), (int) Math.round(rootBlockSize.getHeight() + paperMargin * 2));
      }
    }

    return resultSize;
  }

  public void updateView(final boolean structureWasChanged) {
    invalidate();
    revalidate();
    if (structureWasChanged) {
      fireNotificationMindMapChanged();
    }
    repaint();
  }

  @Override
  public void revalidate() {
    final Runnable runnable = new Runnable() {
      @Override
      public void run() {
        if (!isValid()) {
          final Graphics2D gfx = (Graphics2D) getGraphics();
          if (gfx != null && calculateElementSizes(gfx, model, config)) {
            changeSizeOfComponentWithNotification(layoutFullDiagramWithCenteringToPaper(gfx, model, config, getSize()));
          }
        }
      }
    };
    if (SwingUtilities.isEventDispatchThread()) {
      runnable.run();
    }
    else {
      SwingUtilities.invokeLater(runnable);
    }
  }

  public void setErrorText(final String text) {
    this.errorText = text;
    repaint();
  }

  public String getErrorText() {
    return this.errorText;
  }

  @Override
  public boolean isValid() {
    return isModelValid(this.model);
  }

  @Override
  public boolean isValidateRoot() {
    return true;
  }

  @Override
  public void invalidate() {
    super.invalidate();
    if (this.model != null && this.model.getRoot() != null) {
      this.model.resetPayload();
    }
  }

  private static void drawErrorText(final Graphics2D gfx, final Dimension fullSize, final String error) {
    final Font font = new Font(Font.DIALOG, Font.BOLD, 24);
    final FontMetrics metrics = gfx.getFontMetrics(font);
    final Rectangle2D textBounds = metrics.getStringBounds(error, gfx);
    gfx.setFont(font);
    gfx.setColor(Color.DARK_GRAY);
    gfx.fillRect(0, 0, fullSize.width, fullSize.height);
    final int x = (int) (fullSize.width - textBounds.getWidth()) / 2;
    final int y = (int) (fullSize.height - textBounds.getHeight()) / 2;
    gfx.setColor(Color.BLACK);
    gfx.drawString(error, x + 5, y + 5);
    gfx.setColor(Color.RED.brighter());
    gfx.drawString(error, x, y);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void paintComponent(final Graphics g) {
    final Graphics2D gfx = (Graphics2D) g.create();
    try {
      final String error = this.errorText;

      Utils.prepareGraphicsForQuality(gfx);
      if (error != null) {
        drawErrorText(gfx, this.getSize(), error);
      }
      else {
        revalidate();
        drawOnGraphicsForConfiguration(gfx, this.config, this.model, true, this.selectedTopics);
        drawDestinationElement(gfx, this.config);
      }

      paintChildren(g);

      if (this.draggedElement != null) {
        this.draggedElement.draw(gfx);
      }
      else if (this.mouseDragSelection != null) {
        gfx.setColor(COLOR_MOUSE_DRAG_SELECTION);
        gfx.fill(this.mouseDragSelection.asRectangle());
      }
    }
    finally {
      gfx.dispose();
    }
  }

  public AbstractElement findTopicUnderPoint(final Point point) {
    AbstractElement result = null;
    if (this.model != null) {
      final Topic root = this.model.getRoot();
      if (root != null) {
        final AbstractElement rootWidget = (AbstractElement) root.getPayload();
        if (rootWidget != null) {
          result = rootWidget.findForPoint(point);
        }
      }
    }

    return result;
  }

  public void removeAllSelection() {
    if (!this.selectedTopics.isEmpty()) {
      try {
        this.selectedTopics.clear();
        fireNotificationSelectionChanged();
      }
      finally {
        repaint();
      }
    }
  }

  public void focusTo(final Topic theTopic) {
    if (theTopic != null) {
      final AbstractElement element = (AbstractElement) theTopic.getPayload();
      if (element != null && element instanceof AbstractCollapsableElement) {
        final AbstractCollapsableElement cel = (AbstractCollapsableElement) element;
        if (MindMapUtils.ensureVisibility(cel.getModel())) {
          updateView(true);
        }
      }

      removeAllSelection();

      final int[] path = theTopic.getPositionPath();
      this.select(this.model.findForPositionPath(path), false);
    }
  }

  public boolean cloneTopic(final Topic topic) {
    if (topic == null || topic.getTopicLevel() == 0) {
      return false;
    }

    final Boolean cloneFullTree = this.controller == null ? Boolean.TRUE : this.controller.getDialogProvider(this).msgConfirmYesNoCancel(BUNDLE.getString("MindMapPanel.titleCloneTopicRequest"), BUNDLE.getString("MindMapPanel.cloneTopicSubtreeRequestMsg"));
    if (cloneFullTree == null) {
      return false;
    }

    final Topic cloned = this.model.cloneTopic(topic, cloneFullTree);

    if (cloned != null) {
      cloned.moveAfter(topic);
      updateView(true);
    }

    return true;
  }

  public MindMapPanelConfig getConfiguration() {
    return this.config;
  }

  public MindMapPanelController getController() {
    return this.controller;
  }

  public Topic getFirstSelected() {
    return this.selectedTopics.isEmpty() ? null : this.selectedTopics.get(0);
  }

  public static BufferedImage renderMindMapAsImage(final MindMap model, final MindMapPanelConfig cfg, final boolean expandAll) {
    final MindMap workMap = new MindMap(model, null);
    workMap.resetPayload();

    BufferedImage img = new BufferedImage(32, 32, cfg.isDrawBackground() ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB);
    Dimension2D blockSize = null;
    Graphics2D gfx = img.createGraphics();
    try {
      Utils.prepareGraphicsForQuality(gfx);
      if (calculateElementSizes(gfx, workMap, cfg)) {
        if (expandAll) {
          final AbstractElement root = (AbstractElement) workMap.getRoot().getPayload();
          root.collapseOrExpandAllChildren(false);
          calculateElementSizes(gfx, workMap, cfg);
        }
        blockSize = layoutModelElements(workMap, cfg);
      }
    }
    finally {
      gfx.dispose();
    }
    if (blockSize == null) {
      return null;
    }

    final double paperMargin = cfg.getPaperMargins() * cfg.getScale();
    blockSize.setSize(blockSize.getWidth() + paperMargin * 2, blockSize.getHeight() + paperMargin * 2);

    img = new BufferedImage((int) blockSize.getWidth(), (int) blockSize.getHeight(), BufferedImage.TYPE_INT_ARGB);
    gfx = img.createGraphics();
    try {
      Utils.prepareGraphicsForQuality(gfx);
      gfx.setClip(0, 0, img.getWidth(), img.getHeight());
      layoutFullDiagramWithCenteringToPaper(gfx, workMap, cfg, blockSize);
      drawOnGraphicsForConfiguration(gfx, cfg, workMap, false, null);
    }
    finally {
      gfx.dispose();
    }
    return img;
  }
}
