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
package com.igormaznitsa.nbmindmap.mmgui;

import com.igormaznitsa.nbmindmap.utils.Utils;
import com.igormaznitsa.nbmindmap.model.Extra;
import com.igormaznitsa.nbmindmap.model.ExtraFile;
import com.igormaznitsa.nbmindmap.model.ExtraLine;
import com.igormaznitsa.nbmindmap.model.ExtraLink;
import com.igormaznitsa.nbmindmap.model.ExtraNote;
import com.igormaznitsa.nbmindmap.model.ExtraTopic;
import com.igormaznitsa.nbmindmap.model.MindMap;
import com.igormaznitsa.nbmindmap.model.Topic;
import com.igormaznitsa.nbmindmap.utils.NbUtils;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.apache.commons.lang.StringEscapeUtils;

public final class MindMapPanel extends JPanel implements Configuration.ConfigurationListener {

  private static final long serialVersionUID = 7474413542713752159L;

  public interface PopUpProvider {

    JPopupMenu makePopUp(final Point point, final AbstractElement element, final ElementPart partUnderMouse);
  }

  private volatile MindMap model;
  private volatile String errorText;

  private final List<MindMapListener> mindMapListeners = new CopyOnWriteArrayList<MindMapListener>();

  private final float SCALE_STEP = 0.5f;

  private static final Color COLOR_MOUSE_DRAG_SELECTION = new Color(0x80000000, true);

  private final JTextArea textEditor = new JTextArea();
  private final JPanel textEditorPanel = new JPanel(new BorderLayout(0, 0));
  private transient AbstractElement elementUnderEdit = null;

  private final List<Topic> selectedTopics = new ArrayList<Topic>();

  private transient MouseSelectedArea mouseDragSelection = null;
  private transient AbstractElement draggedElement = null;
  private transient Point draggedElementPoint = null;
  private transient AbstractElement destinationElement = null;

  private static final Configuration COMMON_CONFIG = new Configuration();

  protected final Configuration config;
  private PopUpProvider popupProvider;

  static {
    loadCommonConfig();
  }

  public static synchronized void loadCommonConfig() {
    COMMON_CONFIG.makeAtomicChange(new Runnable() {
      @Override
      public void run() {
        COMMON_CONFIG.setShowGrid(NbUtils.getPreferences().getBoolean("showGrid", COMMON_CONFIG.isShowGrid()));
        COMMON_CONFIG.setDropShadow(NbUtils.getPreferences().getBoolean("dropShadow", COMMON_CONFIG.isDropShadow()));
        COMMON_CONFIG.setPaperColor(new Color(NbUtils.getPreferences().getInt("paperColor", COMMON_CONFIG.getPaperColor().getRGB())));
        COMMON_CONFIG.setGridColor(new Color(NbUtils.getPreferences().getInt("gridColor", COMMON_CONFIG.getGridColor().getRGB())));
        COMMON_CONFIG.setConnectorColor(new Color(NbUtils.getPreferences().getInt("connectorColor", COMMON_CONFIG.getConnectorColor().getRGB())));
        COMMON_CONFIG.setGridStep(NbUtils.getPreferences().getInt("gridStep", COMMON_CONFIG.getGridStep()));
        COMMON_CONFIG.setConnectorWidth(NbUtils.getPreferences().getFloat("connectorWidth", COMMON_CONFIG.getConnectorWidth()));

        COMMON_CONFIG.setRootBackgroundColor(new Color(NbUtils.getPreferences().getInt("rootBackColor", COMMON_CONFIG.getRootBackgroundColor().getRGB())));
        COMMON_CONFIG.setRootTextColor(new Color(NbUtils.getPreferences().getInt("rootTextColor", COMMON_CONFIG.getRootTextColor().getRGB())));

        COMMON_CONFIG.setFirstLevelBackgroundColor(new Color(NbUtils.getPreferences().getInt("1stBackColor", COMMON_CONFIG.getFirstLevelBackgroundColor().getRGB())));
        COMMON_CONFIG.setFirstLevelTextColor(new Color(NbUtils.getPreferences().getInt("1stTextColor", COMMON_CONFIG.getFirstLevelTextColor().getRGB())));

        COMMON_CONFIG.setOtherLevelBackgroundColor(new Color(NbUtils.getPreferences().getInt("2stBackColor", COMMON_CONFIG.getOtherLevelBackgroundColor().getRGB())));
        COMMON_CONFIG.setOtherLevelTextColor(new Color(NbUtils.getPreferences().getInt("2stTextColor", COMMON_CONFIG.getOtherLevelTextColor().getRGB())));

        COMMON_CONFIG.setSelectLineColor(new Color(NbUtils.getPreferences().getInt("selectLineColor", COMMON_CONFIG.getSelectLineColor().getRGB())));
        COMMON_CONFIG.setSelectLineWidth(NbUtils.getPreferences().getFloat("selectLineWidth", COMMON_CONFIG.getSelectLineWidth()));
        COMMON_CONFIG.setSelectLineGap(NbUtils.getPreferences().getInt("selectLineGap", COMMON_CONFIG.getSelectLineGap()));

        COMMON_CONFIG.setCollapsatorBackgroundColor(new Color(NbUtils.getPreferences().getInt("collapsatorBackColor", COMMON_CONFIG.getCollapsatorBackgroundColor().getRGB())));
        COMMON_CONFIG.setCollapsatorBorderColor(new Color(NbUtils.getPreferences().getInt("collapsatorBorderColor", COMMON_CONFIG.getCollapsatorBorderColor().getRGB())));
        COMMON_CONFIG.setCollapsatorSize(NbUtils.getPreferences().getInt("collapsatorSize", COMMON_CONFIG.getCollapsatorSize()));
        COMMON_CONFIG.setCollapsatorBorderWidth(NbUtils.getPreferences().getFloat("collapsatorBorderWidth", COMMON_CONFIG.getCollapsatorBorderWidth()));

        COMMON_CONFIG.setFirstLevelHorizontalInset(NbUtils.getPreferences().getInt("firstLevelHInset", COMMON_CONFIG.getFirstLevelHorizontalInset()));
        COMMON_CONFIG.setFirstLevelVerticalInset(NbUtils.getPreferences().getInt("firstLevelVInset", COMMON_CONFIG.getFirstLevelVerticalInset()));

        COMMON_CONFIG.setOtherLevelHorizontalInset(NbUtils.getPreferences().getInt("otherLevelHInset", COMMON_CONFIG.getOtherLevelHorizontalInset()));
        COMMON_CONFIG.setOtherLevelVerticalInset(NbUtils.getPreferences().getInt("otherLevelVInset", COMMON_CONFIG.getOtherLevelVerticalInset()));

        final String fontName = NbUtils.getPreferences().get("font.name", COMMON_CONFIG.getFont().getName());
        final int fontSize = NbUtils.getPreferences().getInt("font.size", COMMON_CONFIG.getFont().getSize());
        final int fontStyle = NbUtils.getPreferences().getInt("font.style", COMMON_CONFIG.getFont().getStyle());
        COMMON_CONFIG.setFont(new Font(fontName, fontStyle, fontSize));
      }
    });
  }

  public Configuration getConfiguration() {
    return this.config;
  }

  public void setPopUpProvider(final PopUpProvider provider) {
    this.popupProvider = provider;
  }

  public PopUpProvider getPopUpProvider() {
    return this.popupProvider;
  }

  public MindMapPanel() {
    super(null);

    this.config = new Configuration(COMMON_CONFIG, false);

    COMMON_CONFIG.addConfigurationListener(this);

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
            textEditor.insert("\n", textEditor.getCaretPosition());
          }
        }
      }

      @Override
      public void keyReleased(final KeyEvent e) {
        switch (e.getKeyCode()) {
          case KeyEvent.VK_ESCAPE: {
            e.consume();
            endEdit(false);
          }
          break;
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
            if (hasOnlyTopicSelected() && (e.getModifiersEx() & (KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK | KeyEvent.META_DOWN_MASK)) != 0) {
              startEdit((AbstractElement) selectedTopics.get(0).getPayload());
            }
          }
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
        }
      }
    };

    this.setFocusTraversalKeysEnabled(false);

    final MouseAdapter adapter = new MouseAdapter() {

      @Override
      public void mouseEntered(final MouseEvent e) {
        setCursor(Cursor.getDefaultCursor());
      }

      @Override
      public void mouseMoved(final MouseEvent e) {
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
        if (e.isPopupTrigger()) {
          e.consume();
          MindMap theMap = model;
          AbstractElement element = null;
          if (theMap != null) {
            element = findTopicUnderPoint(e.getPoint());
          }
          processPopUp(e.getPoint(), element);
        }
        else {
          endEdit(false);
          mouseDragSelection = null;
          MindMap theMap = model;
          if (theMap != null) {
            final AbstractElement element = findTopicUnderPoint(e.getPoint());
            if (element == null) {
              mouseDragSelection = new MouseSelectedArea(e.getPoint());
            }
          }
        }
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        try {
          if (draggedElementPoint != null) {
            if (endDragOfElement(draggedElementPoint, draggedElement, destinationElement)) {
              invalidate();
              revalidate();
              fireNotificationMindMapChanged();
              repaint();
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
            e.consume();
            MindMap theMap = model;
            AbstractElement element = null;
            if (theMap != null) {
              element = findTopicUnderPoint(e.getPoint());
            }
            processPopUp(e.getPoint(), element);
          }
        }
        finally {
          mouseDragSelection = null;
          draggedElement = null;
          draggedElementPoint = null;
          destinationElement = null;
          repaint();
        }
      }

      @Override
      public void mouseDragged(final MouseEvent e) {
        if (mouseDragSelection != null) {
          mouseDragSelection.update(e);
          repaint();
        }
        else if (draggedElementPoint == null) {
          draggedElement = findTopicUnderPoint(e.getPoint());
          if (draggedElement != null && draggedElement.isMoveable()) {
            draggedElementPoint = e.getPoint();
            findDestinationElementForDragged();
            repaint();
          }
        }
        else {
          draggedElementPoint.setLocation(e.getPoint());
          findDestinationElementForDragged();
          repaint();
        }
      }

      @Override
      public void mouseWheelMoved(final MouseWheelEvent e) {
        mouseDragSelection = null;
        draggedElement = null;
        draggedElementPoint = null;

        if (e.isControlDown()) {
          endEdit(false);
          setScale(getScale() + (SCALE_STEP * -e.getWheelRotation()));
          invalidate();
          revalidate();
          repaint();
          e.consume();
        }
        else {
          sendToParent(e);
        }
      }

      @Override
      public void mouseClicked(final MouseEvent e) {
        mouseDragSelection = null;
        draggedElement = null;
        draggedElementPoint = null;

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

    builder.append("<html>");

    switch (extra.getType()) {
      case FILE: {
        builder.append("<b>Open File</b><br>").append(StringEscapeUtils.escapeHtml(((ExtraFile) extra).getAsString()));
      }
      break;
      case TOPIC: {
        final Topic topic = this.getModel().findTopicForLink((ExtraTopic) extra);
        builder.append("<b>Jump to topic</b><br>").append(StringEscapeUtils.escapeHtml(Utils.makeShortTextVersion(topic.getText(), 32)));
      }
      break;
      case LINK: {
        builder.append("<b>Open link</b><br>").append(StringEscapeUtils.escapeHtml(Utils.makeShortTextVersion(((ExtraLink) extra).getAsString(), 48)));
      }
      break;
      case NOTE: {
        builder.append("<b>Open text content</b><br>").append(StringEscapeUtils.escapeHtml(Utils.makeShortTextVersion(((ExtraNote) extra).getAsString(), 64)));
      }
      break;
      case LINE: {
        builder.append("<b>Open line</b><br>").append(StringEscapeUtils.escapeHtml(((ExtraLine) extra).getValue().toString()));
      }
      break;
      default: {
        builder.append("<b>Unknown</b>");
      }
      break;
    }

    builder.append("</html>");

    return builder.toString();
  }

  @Override
  public void onConfigurationPropertyChanged(final Configuration source) {
    if (source == COMMON_CONFIG) {
      final float scale = this.config.getScale();
      this.config.makeAtomicChange(new Runnable() {
        @Override
        public void run() {
          config.makeFullCopyOf(source, false, false);
          config.setScale(scale);
        }
      });
    }
    invalidate();
    repaint();
  }

  private static final int DRAG_POSITION_LEFT = 1;
  private static final int DRAG_POSITION_TOP = 2;
  private static final int DRAG_POSITION_BOTTOM = 3;
  private static final int DRAG_POSITION_RIGHT = 4;

  private int calcDropPosition(final AbstractElement destination, final Point dropPoint) {
    final int result;
    if (destination.getClass() == ElementRoot.class) {
      result = dropPoint.getX() < destination.getBounds().getCenterX() ? DRAG_POSITION_LEFT : DRAG_POSITION_RIGHT;
    }
    else {
      final Rectangle2D bounds = destination.getBounds();
      if (dropPoint.getX() >= destination.getBounds().getX() && dropPoint.getX() <= destination.getBounds().getMaxX()) {
        result = dropPoint.getY() < destination.getBounds().getCenterY() ? DRAG_POSITION_TOP : DRAG_POSITION_BOTTOM;
      }
      else {
        result = dropPoint.getX() < destination.getBounds().getCenterX() ? DRAG_POSITION_LEFT : DRAG_POSITION_RIGHT;
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
          if (destination instanceof AbstractCollapsableElement && destination.isCollapsed()) {
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

  public void makeNewChildAndStartEdit(final Topic parent, final Topic baseTopic) {
    if (parent != null) {
      removeAllSelection();

      final Topic newTopic = parent.makeChild("", baseTopic);

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
      invalidate();
      revalidate();
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
      invalidate();
      revalidate();
      fireNotificationMindMapChanged();
      repaint();
    }
  }

  public void collapseOrExpandAll(final boolean collapse) {
    endEdit(false);
    removeAllSelection();

    if (this.model.getRoot() != null) {
      final AbstractElement root = (AbstractElement) this.model.getRoot().getPayload();
      if (root != null && root.collapseOrExpandAllChildren(collapse)) {
        invalidate();
        revalidate();
        fireNotificationMindMapChanged();
        repaint();
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

  public void endEdit(final boolean commit) {
    try {
      if (commit && this.elementUnderEdit != null) {
        this.elementUnderEdit.setText(this.textEditor.getText());
        fireNotificationMindMapChanged();
        fireNotificationEnsureTopicVisibility(this.elementUnderEdit.model);
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
    if (this.draggedElementPoint != null && this.draggedElement != null) {
      final AbstractElement root = (AbstractElement) this.model.getRoot().getPayload();
      this.destinationElement = root.findNearestTopic(this.draggedElement, this.draggedElementPoint);
    }
    else {
      this.destinationElement = null;
    }
  }

  protected void processPopUp(final Point point, final AbstractElement element) {
    final PopUpProvider provider = this.popupProvider;
    if (provider != null) {
      final ElementPart part = element == null ? null : element.findPartForPoint(point);

      final JPopupMenu menu = provider.makePopUp(point, element, part);
      if (menu != null) {
        menu.show(this, point.x, point.y);
      }
    }
  }

  public void addMindMapListener(final MindMapListener l) {
    Utils.assertNotNull("Listener must not be null", l);
    this.mindMapListeners.add(l);
  }

  public void removeMindMapListener(final MindMapListener l) {
    if (l != null) {
      this.mindMapListeners.remove(l);
    }
  }

  public void setModel(final MindMap model) {
    final List<int[]> selectedPaths = new ArrayList<int[]>();
    for (final Topic t : this.selectedTopics) {
      selectedPaths.add(t.getPositionPath());
    }

    this.selectedTopics.clear();

    this.model = model;
    invalidate();
    revalidate();

    boolean selectionChanged = false;
    for (final int[] posPath : selectedPaths) {
      final Topic topic = this.model.findForPositionPath(posPath);
      if (topic == null) {
        selectionChanged = true;
      }
      else {
        this.selectedTopics.add(topic);
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

  public void setScale(final float zoom) {
    this.config.setScale(zoom);
  }

  public float getScale() {
    return this.config.getScale();
  }

  private static void drawBackground(final Graphics2D g, final Configuration cfg) {
    final Rectangle clipBounds = g.getClipBounds();

    if (cfg.isDrawBackground()) {
      g.setColor(cfg.getPaperColor());
      g.fillRect(clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height);

      if (cfg.isShowGrid()) {
        final float scaledGridStep = cfg.getGridStep() * cfg.getScale();

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

  public static void drawOnGraphicsForConfiguration(final Graphics2D g, final Configuration config, final MindMap map, final boolean drawSelection, final List<Topic> selectedTopics) {
    drawBackground(g, config);
    drawTopics(g, config, map);
    if (drawSelection && selectedTopics != null && !selectedTopics.isEmpty()) {
      drawSelection(g, config, selectedTopics);
    }
  }

  private void drawDestinationElement(final Graphics2D g, final Configuration cfg) {
    if (this.destinationElement != null) {
      g.setColor(cfg.getSelectLineColor());
      g.setStroke(new BasicStroke(3.0f * this.config.getScale()));

      final Rectangle2D rectToDraw = new Rectangle2D.Double();
      rectToDraw.setRect(this.destinationElement.getBounds());
      final double selectLineGap = cfg.getSelectLineGap() * cfg.getScale();
      rectToDraw.setRect(rectToDraw.getX() - selectLineGap, rectToDraw.getY() - selectLineGap, rectToDraw.getWidth() + selectLineGap * 2, rectToDraw.getHeight() + selectLineGap * 2);

      final int position = calcDropPosition(this.destinationElement, this.draggedElementPoint);

      boolean draw = !this.draggedElement.getBounds().contains(this.draggedElementPoint) && !this.destinationElement.getModel().hasAncestor(this.draggedElement.getModel());

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
        g.draw(rectToDraw);
      }
    }
  }

  private static void drawSelection(final Graphics2D g, final Configuration cfg, final List<Topic> selectedTopics) {
    if (selectedTopics != null && !selectedTopics.isEmpty()) {
      g.setColor(cfg.getSelectLineColor());
      final Stroke dashed = new BasicStroke(cfg.getSelectLineWidth() * cfg.getScale(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{1 * cfg.getScale(), 4 * cfg.getScale()}, 0);
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

  private static void drawTopics(final Graphics2D g, final Configuration cfg, final MindMap map) {
    if (map != null) {
      final Topic root = map.getRoot();
      if (root != null) {
        drawTopicTree(g, root, cfg);
      }
    }
  }

  private static void drawTopicTree(final Graphics2D gfx, final Topic topic, final Configuration cfg) {
    paintTopic(gfx, topic, cfg);
    final AbstractElement w = (AbstractElement) topic.getPayload();
    if (w.isCollapsed()) {
      return;
    }
    for (final Topic t : topic.getChildren()) {
      drawTopicTree(gfx, t, cfg);
    }
  }

  private static void paintTopic(final Graphics2D gfx, final Topic topic, final Configuration cfg) {
    ((AbstractElement) topic.getPayload()).doPaint(gfx, cfg);
  }

  private static void setElementSizesForElementAndChildren(final Graphics2D gfx, final Configuration cfg, final Topic topic, final int level) {
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

  protected static boolean calculateElementSizes(final Graphics2D gfx, final MindMap model, final Configuration cfg) {
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

  protected static Dimension2D layoutModelElements(final MindMap model, final Configuration cfg) {
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

  private static Dimension layoutFullDiagramWithCenteringToPaper(final Graphics2D gfx, final MindMap map, final Configuration cfg, final Dimension2D paperSize) {
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

  private static void prepareGraphicsForQuality(final Graphics2D gfx) {
    gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    gfx.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    gfx.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    gfx.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);
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
  public void paintComponent(final Graphics g) {
    final Graphics2D gfx = (Graphics2D) g;

    final String error = this.errorText;

    prepareGraphicsForQuality(gfx);
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
      final int px = this.draggedElementPoint.x - ((int) this.draggedElement.getBounds().getWidth()) / 2;
      final int py = this.draggedElementPoint.y - ((int) this.draggedElement.getBounds().getHeight()) / 2;
      gfx.translate(px, py);
      try {
        this.draggedElement.drawComponent(gfx, this.config);
      }
      finally {
        gfx.translate(-px, -py);
      }
    }
    else if (this.mouseDragSelection != null) {
      gfx.setColor(COLOR_MOUSE_DRAG_SELECTION);
      gfx.fill(this.mouseDragSelection.asRectangle());
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
        if (cel.ensureUncollapsed()) {
          invalidate();
          revalidate();
          fireNotificationMindMapChanged();
        }
      }

      removeAllSelection();
      this.select(theTopic, false);
    }
  }

  public boolean cloneTopic(final Topic topic) {
    if (topic == null || topic.getTopicLevel() == 0) {
      return false;
    }

    final Boolean cloneFullTree = NbUtils.msgConfirmYesNoCancel("Clone topic", "Do you want clone whole topic subtree?");
    if (cloneFullTree == null) {
      return false;
    }

    final Topic cloned = this.model.cloneTopic(topic, cloneFullTree);

    if (cloned != null) {
      cloned.moveAfter(topic);

      invalidate();
      revalidate();
      fireNotificationMindMapChanged();
      repaint();
    }

    return true;
  }

  public Topic getFirstSelected() {
    return this.selectedTopics.isEmpty() ? null : this.selectedTopics.get(0);
  }

  public static RenderedImage renderMindMapAsImage(final MindMap model, final Configuration cfg, final boolean expandAll) {
    final MindMap workMap = new MindMap(model);
    workMap.resetPayload();

    BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
    Dimension2D blockSize = null;
    Graphics2D gfx = img.createGraphics();
    try {
      prepareGraphicsForQuality(gfx);
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
      prepareGraphicsForQuality(gfx);
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
