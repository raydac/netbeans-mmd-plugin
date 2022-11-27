/*
 * Copyright (C) 2015-2022 Igor A. Maznitsa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.igormaznitsa.mindmap.swing.panel;

import static com.igormaznitsa.mindmap.swing.panel.utils.Utils.assertSwingDispatchThread;
import static com.igormaznitsa.mindmap.swing.panel.utils.Utils.isPopupEvent;
import static java.util.Objects.requireNonNull;

import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.ExtraFile;
import com.igormaznitsa.mindmap.model.ExtraNote;
import com.igormaznitsa.mindmap.model.ExtraTopic;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.ModelUtils;
import com.igormaznitsa.mindmap.model.StandardMmdAttributes;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.plugins.MindMapPluginRegistry;
import com.igormaznitsa.mindmap.plugins.api.ModelAwarePlugin;
import com.igormaznitsa.mindmap.plugins.api.PanelAwarePlugin;
import com.igormaznitsa.mindmap.plugins.api.PluginContext;
import com.igormaznitsa.mindmap.plugins.api.VisualAttributePlugin;
import com.igormaznitsa.mindmap.swing.i18n.MmdI18n;
import com.igormaznitsa.mindmap.swing.panel.ui.AbstractCollapsableElement;
import com.igormaznitsa.mindmap.swing.panel.ui.AbstractElement;
import com.igormaznitsa.mindmap.swing.panel.ui.ElementLevelFirst;
import com.igormaznitsa.mindmap.swing.panel.ui.ElementLevelOther;
import com.igormaznitsa.mindmap.swing.panel.ui.ElementPart;
import com.igormaznitsa.mindmap.swing.panel.ui.ElementRoot;
import com.igormaznitsa.mindmap.swing.panel.ui.MouseSelectedArea;
import com.igormaznitsa.mindmap.swing.panel.ui.gfx.MMGraphics;
import com.igormaznitsa.mindmap.swing.panel.ui.gfx.MMGraphics2DWrapper;
import com.igormaznitsa.mindmap.swing.panel.ui.gfx.StrokeType;
import com.igormaznitsa.mindmap.swing.panel.utils.KeyEventType;
import com.igormaznitsa.mindmap.swing.panel.utils.MindMapUtils;
import com.igormaznitsa.mindmap.swing.panel.utils.Pair;
import com.igormaznitsa.mindmap.swing.panel.utils.RenderQuality;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactory;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactoryProvider;
import java.awt.AWTEvent;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.apache.commons.lang3.StringEscapeUtils;

public class MindMapPanel extends JComponent implements ClipboardOwner {

  public static final long serialVersionUID = 2783412123454232L;
  private static final int MIN_DISTANCE_FOR_TOPIC_DRAGGING_START = 8;
  private static final Logger LOGGER = LoggerFactory.getLogger(MindMapPanel.class);
  private static final UIComponentFactory UI_COMPO_FACTORY = UIComponentFactoryProvider.findInstance();
  private static final int ALL_SUPPORTED_MODIFIERS = KeyEvent.SHIFT_MASK | KeyEvent.ALT_MASK | KeyEvent.META_MASK | KeyEvent.CTRL_MASK;
  private static final double SCALE_STEP = 0.1d;
  private static final double SCALE_MINIMUM = 0.3d;
  private static final double SCALE_MAXIMUM = 8.0d;
  private static final Color COLOR_MOUSE_DRAG_SELECTION = new Color(0x80000000, true);
  private static final int DRAG_POSITION_UNKNOWN = -1;
  private static final int DRAG_POSITION_LEFT = 1;
  private static final int DRAG_POSITION_TOP = 2;
  private static final int DRAG_POSITION_BOTTOM = 3;
  private static final int DRAG_POSITION_RIGHT = 4;
  private final MindMapPanelController controller;
  private final AtomicBoolean disposed = new AtomicBoolean();
  private final ReentrantLock panelLocker = new ReentrantLock();
  private final Map<String, Object> sessionObjects = new HashMap<>();
  private final List<MindMapListener> mindMapListeners = new CopyOnWriteArrayList<>();
  private final JTextArea textEditor = UI_COMPO_FACTORY.makeTextArea();
  private final JPanel textEditorPanel = UI_COMPO_FACTORY.makePanel();
  private final List<Topic> selectedTopics = new ArrayList<>();
  private final MindMapPanelConfig config;
  private final AtomicBoolean popupMenuActive = new AtomicBoolean();
  private final AtomicBoolean removeEditedTopicForRollback = new AtomicBoolean();
  private final AtomicReference<Dimension> mindMapImageSize = new AtomicReference<>(new Dimension());
  private final UUID uuid = UUID.randomUUID();
  private final MmdI18n mmdI18n = MmdI18n.getInstance();
  private volatile MindMap model;
  private volatile String errorText;
  private transient AbstractElement elementUnderEdit = null;
  private transient int[] pathToPrevTopicBeforeEdit = null;
  private transient MouseSelectedArea mouseDragSelection = null;
  private transient DraggedElement draggedElement = null;
  private transient AbstractElement destinationElement = null;
  private Point lastMousePressed = null;
  public MindMapPanel(final MindMapPanelController controller) {
    super();

    final MindMapPanelConfig panelConfig = controller.provideConfigForMindMapPanel(this);

    this.textEditorPanel.setLayout(new BorderLayout(0, 0));
    this.controller = controller;

    this.config = new MindMapPanelConfig(panelConfig, false);

    this.textEditor.setMargin(new Insets(5, 5, 5, 5));
    this.textEditor.setBorder(BorderFactory.createEtchedBorder());
    this.textEditor.setTabSize(4);
    this.textEditor.addKeyListener(new KeyAdapter() {

      @Override
      public void keyPressed(final KeyEvent e) {
        if (lockIfNotDisposed()) {
          try {
            switch (e.getKeyCode()) {
              case KeyEvent.VK_ENTER: {
                e.consume();
              }
              break;
              case KeyEvent.VK_TAB: {
                if ((e.getModifiers() & ALL_SUPPORTED_MODIFIERS) == 0) {
                  e.consume();
                  final Topic edited = elementUnderEdit.getModel();
                  final int[] topicPosition = edited.getPositionPath();
                  endEdit(true);
                  final Topic theTopic = model.findAtPosition(topicPosition);
                  if (theTopic != null) {
                    makeNewChildAndStartEdit(theTopic, null);
                  }
                }
              }
              break;
              default:
                break;
            }
          } finally {
            unlock();
          }
        }
      }

      @Override
      public void keyTyped(final KeyEvent e) {
        if (lockIfNotDisposed()) {
          try {
            if (config.isKeyEvent(MindMapPanelConfig.KEY_TOPIC_TEXT_NEXT_LINE, e)) {
              e.consume();
              textEditor.insert("\n", textEditor.getCaretPosition());
            } else if (e.getKeyChar() == KeyEvent.VK_ENTER &&
                (e.getModifiers() & ALL_SUPPORTED_MODIFIERS) == 0) {
              e.consume();
              endEdit(true);
            }
          } finally {
            unlock();
          }
        }
      }

      @Override
      public void keyReleased(final KeyEvent e) {
        if (lockIfNotDisposed()) {
          try {
            if (config.isKeyEvent(MindMapPanelConfig.KEY_CANCEL_EDIT, e)) {
              e.consume();
              final Topic edited = elementUnderEdit == null ? null : elementUnderEdit.getModel();
              endEdit(false);
              if (edited != null && controller.canTopicBeDeleted(MindMapPanel.this, edited)) {
                deleteTopics(false, edited);
                if (pathToPrevTopicBeforeEdit != null) {
                  final int[] path = pathToPrevTopicBeforeEdit;
                  pathToPrevTopicBeforeEdit = null;
                  final Topic topic = model.findAtPosition(path);
                  if (topic != null) {
                    select(topic, false);
                  }
                }
              }
            }
          } finally {
            unlock();
          }
        }
      }
    });

    this.textEditor.getDocument().addDocumentListener(new DocumentListener() {

      private void updateEditorPanelSize(final Dimension newSize) {
        if (lockIfNotDisposed()) {
          try {
            final Dimension editorPanelMinSize = textEditorPanel.getMinimumSize();
            final Dimension newDimension =
                new Dimension(Math.max(editorPanelMinSize.width, newSize.width),
                    Math.max(editorPanelMinSize.height, newSize.height));

            final Rectangle editorBounds = textEditorPanel.getBounds();
            editorBounds.setSize(newDimension);

            final Rectangle mainPanelBounds = MindMapPanel.this.getBounds();

            if (!mainPanelBounds.contains(editorBounds)) {
              double ex = editorBounds.getX();
              double ey = editorBounds.getY();
              double ew = editorBounds.getWidth();
              double eh = editorBounds.getHeight();

              if (ex < 0.0d) {
                ew -= ex;
                ex = 0.0d;
              }
              if (ey < 0.0d) {
                eh -= ey;
                ey = 0.0d;
              }

              if (ex + ew > mainPanelBounds.getWidth()) {
                ex = mainPanelBounds.getWidth() - ew;
              }
              if (ey + eh > mainPanelBounds.getHeight()) {
                ey = mainPanelBounds.getHeight() - eh;
              }

              editorBounds.setRect(ex, ey, ew, eh);
            }

            textEditorPanel.setBounds(editorBounds);
            textEditorPanel.repaint();
          } finally {
            unlock();
          }
        }
      }

      private void callUpdateEditorPanelSize() {
        SwingUtilities.invokeLater(() -> updateEditorPanelSize(textEditor.getPreferredSize()));
      }

      @Override
      public void insertUpdate(final DocumentEvent e) {
        callUpdateEditorPanelSize();
      }

      @Override
      public void removeUpdate(final DocumentEvent e) {
        callUpdateEditorPanelSize();
      }

      @Override
      public void changedUpdate(final DocumentEvent e) {
        callUpdateEditorPanelSize();
      }
    });

    super.setOpaque(true);

    final KeyAdapter keyAdapter = new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        if (lockIfNotDisposed()) {
          try {
            if (!e.isConsumed()) {
              fireNotificationNonConsumedKeyEvent(e, KeyEventType.PRESSED);
            }
          } finally {
            unlock();
          }
        }
      }

      private void processTypedKeyInternal(final KeyEvent e) {
        if (config.isKeyEvent(MindMapPanelConfig.KEY_ADD_CHILD_AND_START_EDIT, e)) {
          e.consume();
          if (!selectedTopics.isEmpty()) {
            makeNewChildAndStartEdit(selectedTopics.get(0), null);
          }
        } else if (config.isKeyEvent(MindMapPanelConfig.KEY_ADD_SIBLING_AND_START_EDIT, e)) {
          e.consume();
          if (!hasActiveEditor() && hasOnlyTopicSelected()) {
            final Topic baseTopic = selectedTopics.get(0);
            makeNewChildAndStartEdit(
                baseTopic.getParent() == null ? baseTopic : baseTopic.getParent(), baseTopic);
          }
        } else if (config.isKeyEvent(MindMapPanelConfig.KEY_FOCUS_ROOT_OR_START_EDIT, e)) {
          e.consume();
          if (!hasSelectedTopics()) {
            select(getModel().getRoot(), false);
          } else if (hasOnlyTopicSelected()) {
            startEdit((AbstractElement) selectedTopics.get(0).getPayload());
          }
        }
      }

      @Override
      public void keyTyped(final KeyEvent e) {
        if (lockIfNotDisposed()) {
          try {
            this.processTypedKeyInternal(e);
            if (!e.isConsumed()) {
              fireNotificationNonConsumedKeyEvent(e, KeyEventType.TYPED);
            }
          } finally {
            unlock();
          }
        }
      }

      @Override
      public void keyReleased(final KeyEvent e) {
        if (lockIfNotDisposed()) {
          try {
            if ((e.getKeyCode() >= KeyEvent.VK_F1 && e.getKeyCode() <= KeyEvent.VK_F12)
                || e.getKeyCode() == KeyEvent.VK_INSERT) {
              this.processTypedKeyInternal(e);
            }

            if (!e.isConsumed()) {
              if (e.getKeyCode() == KeyEvent.VK_CONTEXT_MENU) {
                e.consume();
                processContextMenuKey();
              } else if (config.isKeyEvent(MindMapPanelConfig.KEY_SHOW_POPUP, e)) {
                e.consume();
                processPopUpForShortcut();
              } else if (config.isKeyEvent(MindMapPanelConfig.KEY_DELETE_TOPIC, e)) {
                e.consume();
                focusTo(deleteSelectedTopics(false));
              } else if (config.isKeyEventDetected(e,
                      MindMapPanelConfig.KEY_FOCUS_MOVE_LEFT,
                      MindMapPanelConfig.KEY_FOCUS_MOVE_RIGHT,
                      MindMapPanelConfig.KEY_FOCUS_MOVE_UP,
                      MindMapPanelConfig.KEY_FOCUS_MOVE_DOWN,
                      MindMapPanelConfig.KEY_FOCUS_MOVE_LEFT_ADD_FOCUSED,
                      MindMapPanelConfig.KEY_FOCUS_MOVE_RIGHT_ADD_FOCUSED,
                      MindMapPanelConfig.KEY_FOCUS_MOVE_UP_ADD_FOCUSED,
                      MindMapPanelConfig.KEY_FOCUS_MOVE_DOWN_ADD_FOCUSED)) {
                e.consume();
                processMoveFocusByKey(e);
              } else if (config.isKeyEvent(MindMapPanelConfig.KEY_ZOOM_IN, e)) {
                e.consume();
                setScale(Math.max(SCALE_MINIMUM, Math.min(getScale() + SCALE_STEP, SCALE_MAXIMUM)), false);
                doLayout();
                revalidate();
                repaint();
              } else if (config.isKeyEvent(MindMapPanelConfig.KEY_ZOOM_OUT, e)) {
                e.consume();
                setScale(Math.max(SCALE_MINIMUM, Math.min(getScale() - SCALE_STEP, SCALE_MAXIMUM)), false);
                doLayout();
                revalidate();
                repaint();
              } else if (config.isKeyEvent(MindMapPanelConfig.KEY_ZOOM_RESET, e)) {
                e.consume();
                setScale(1.0, false);
                doLayout();
                revalidate();
                repaint();
              } else if (config.isKeyEvent(MindMapPanelConfig.KEY_TOPIC_FOLD, e)
                      || config.isKeyEvent(MindMapPanelConfig.KEY_TOPIC_UNFOLD, e)
                      || config.isKeyEvent(MindMapPanelConfig.KEY_TOPIC_FOLD_ALL, e)
                      || config.isKeyEvent(MindMapPanelConfig.KEY_TOPIC_UNFOLD_ALL, e)) {
                e.consume();
                final List<AbstractElement> elements = new ArrayList<>();
                for (final Topic t : getSelectedTopics()) {
                  final AbstractElement element = (AbstractElement) t.getPayload();
                  if (element != null) {
                    elements.add(element);
                  }
                }
                if (!elements.isEmpty()) {
                  endEdit(false);
                  doFoldOrUnfoldTopic(elements,
                          config.isKeyEvent(MindMapPanelConfig.KEY_TOPIC_FOLD, e) || config.isKeyEvent(MindMapPanelConfig.KEY_TOPIC_FOLD_ALL, e),
                          config.isKeyEvent(MindMapPanelConfig.KEY_TOPIC_FOLD, e) || config.isKeyEvent(MindMapPanelConfig.KEY_TOPIC_UNFOLD, e)
                  );
                }
              }

              if (!e.isConsumed()) {
                fireNotificationNonConsumedKeyEvent(e, KeyEventType.RELEASED);
              }
            }
          } finally {
            unlock();
          }
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
        if (!e.isConsumed() && lockIfNotDisposed()) {
          try {
            if (!controller.isMouseMoveProcessingAllowed(theInstance)) {
              return;
            }
            final AbstractElement element = findTopicUnderPoint(e.getPoint());
            if (element == null) {
              setCursor(Cursor.getDefaultCursor());
              setToolTipText(null);
            } else {
              final ElementPart part = element.findPartForPoint(e.getPoint());
              switch (part) {
                case ICONS: {
                  final Extra<?> extra = element.getIconBlock().findExtraForPoint(e.getPoint().getX() - element.getBounds().getX(), e.getPoint().getY() - element.getBounds().getY());
                  if (extra != null) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    setToolTipText(makeHtmlTooltipForExtra(extra));
                  } else {
                    setCursor(null);
                    setToolTipText(null);
                  }
                }
                break;
                case VISUAL_ATTRIBUTES: {
                  final VisualAttributePlugin plugin = element.getVisualAttributeImageBlock().findPluginForPoint(e.getPoint().getX() - element.getBounds().getX(), e.getPoint().getY() - element.getBounds().getY());
                  final PluginContext context = controller.makePluginContext(theInstance);
                  if (plugin != null) {
                    final Topic theTopic = element.getModel();
                    if (plugin.isClickable(context, theTopic)) {
                      setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    } else {
                      setCursor(null);
                    }
                    setToolTipText(plugin.getToolTip(context, theTopic));
                  } else {
                    setCursor(null);
                    setToolTipText(null);
                  }
                }
                break;
                case COLLAPSATOR: {
                  setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                  setToolTipText(null);
                }
                break;
                default: {
                  setCursor(Cursor.getDefaultCursor());
                  setToolTipText(null);
                }
                break;
              }
            }
          } finally {
            unlock();
          }
        }
      }

      @Override
      public void mousePressed(final MouseEvent e) {
        if (!e.isConsumed() && lockIfNotDisposed()) {
          lastMousePressed = e.getPoint();
          try {
            if (!controller.isMouseClickProcessingAllowed(theInstance)) {
              return;
            }
            try {
              if (isPopupEvent(e)) {
                mouseDragSelection = null;
                MindMap theMap = model;
                AbstractElement element = null;
                if (theMap != null) {
                  element = findTopicUnderPoint(e.getPoint());
                }
                processPopUp(e.getPoint(), element);
                e.consume();
              } else {
                if (elementUnderEdit != null) {
                  endEdit(!(textEditor.getText().isEmpty() && removeEditedTopicForRollback.get()));
                }
                mouseDragSelection = null;
              }
            } catch (Exception ex) {
              LOGGER.error("Error during mousePressed()", ex);
            }
          } finally {
            unlock();
          }
        }
      }

      @Override
      public void mouseReleased(final MouseEvent e) {
        if (!e.isConsumed() && lockIfNotDisposed()) {
          try {
            if (!controller.isMouseClickProcessingAllowed(theInstance)) {
              return;
            }
            try {
              if (draggedElement != null) {
                draggedElement.updatePosition(e.getPoint());
                if (endDragOfElement(draggedElement, destinationElement)) {
                  doLayout();
                  revalidate();
                  repaint();
                  fireNotificationMindMapChanged(true);
                }
              } else if (mouseDragSelection != null) {
                final List<Topic> covered = mouseDragSelection.getAllSelectedElements(model);
                if (e.isShiftDown()) {
                  for (final Topic m : covered) {
                    select(m, false);
                  }
                } else if (e.isControlDown()) {
                  for (final Topic m : covered) {
                    select(m, true);
                  }
                } else {
                  removeAllSelection();
                  for (final Topic m : covered) {
                    select(m, false);
                  }
                }
              } else if (isPopupEvent(e)) {
                mouseDragSelection = null;
                MindMap theMap = model;
                AbstractElement element = null;
                if (theMap != null) {
                  element = findTopicUnderPoint(e.getPoint());
                }
                processPopUp(e.getPoint(), element);
                e.consume();
              }
            } catch (Exception ex) {
              LOGGER.error("Error during mouseReleased()", ex);
            } finally {
              mouseDragSelection = null;
              draggedElement = null;
              destinationElement = null;
              repaint();
            }
          } finally {
            unlock();
          }
        }
      }

      private boolean isNonOverCollapsator(final MouseEvent e, final AbstractElement element) {
        final ElementPart part = element.findPartForPoint(e.getPoint());
        return part != ElementPart.COLLAPSATOR;
      }

      private boolean isDraggedDistanceReached(final MouseEvent dragEvent) {
        boolean result = false;
        if (lastMousePressed != null) {
          final Point dragPoint = dragEvent.getPoint();
          final int dx = lastMousePressed.x - dragEvent.getX();
          final int dy = lastMousePressed.y - dragEvent.getY();
          final double distance = Math.sqrt(dx * dx + dy * dy);
          result = distance >= MIN_DISTANCE_FOR_TOPIC_DRAGGING_START;
        }
        return result;
      }

      @Override
      public void mouseDragged(final MouseEvent e) {
        if (!e.isConsumed() && lockIfNotDisposed()) {
          try {
            if (!controller.isMouseMoveProcessingAllowed(theInstance)) {
              return;
            }
            scrollRectToVisible(new Rectangle(e.getX(), e.getY(), 1, 1));

            if (!popupMenuActive.get()) {
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
                } else if (controller.isElementDragAllowed(theInstance)) {
                  if (elementUnderMouse.isMoveable() && isNonOverCollapsator(e, elementUnderMouse) && isDraggedDistanceReached(e)) {
                    selectedTopics.clear();

                    final Point mouseOffset = new Point((int) Math.round(e.getPoint().getX() - elementUnderMouse.getBounds().getX()), (int) Math.round(e.getPoint().getY() - elementUnderMouse.getBounds().getY()));
                    draggedElement = new DraggedElement(elementUnderMouse, config, mouseOffset, e.isControlDown() || e.isMetaDown() ? DraggedElement.Modifier.MAKE_JUMP : DraggedElement.Modifier.NONE, getConfiguration().getRenderQuality());
                    draggedElement.updatePosition(e.getPoint());
                    findDestinationElementForDragged();
                  } else {
                    draggedElement = null;
                  }
                  repaint();
                }
              } else if (mouseDragSelection != null) {
                if (controller.isSelectionAllowed(theInstance)) {
                  mouseDragSelection.update(e);
                } else {
                  mouseDragSelection = null;
                }
                repaint();
              } else if (draggedElement != null) {
                if (controller.isElementDragAllowed(theInstance)) {
                  draggedElement.updatePosition(e.getPoint());
                  findDestinationElementForDragged();
                } else {
                  draggedElement = null;
                }
                repaint();
              }
            } else {
              mouseDragSelection = null;
            }
          } finally {
            unlock();
          }
        }
      }

      @Override
      public void mouseWheelMoved(final MouseWheelEvent e) {
        if (!e.isConsumed() && lockIfNotDisposed()) {
          try {
            if (controller.isMouseWheelProcessingAllowed(theInstance)) {
              mouseDragSelection = null;
              draggedElement = null;

              final MindMapPanelConfig theConfig = config;

              if (!e.isConsumed() && (theConfig != null &&
                  ((e.getModifiers() & theConfig.getScaleModifiers()) ==
                      theConfig.getScaleModifiers()))) {
                endEdit(elementUnderEdit != null);

                final Dimension oldSize = mindMapImageSize.get();

                final double oldScale = getScale();
                final double curScale = ((long) (10.0d * (oldScale + (SCALE_STEP * -e.getWheelRotation()) + (SCALE_STEP / 2.0d)))) / 10.0d;
                final double newScale = Math.max(SCALE_MINIMUM, Math.min(curScale, SCALE_MAXIMUM));

                setScale(newScale, false);
                MindMapPanel.this.doLayout();
                MindMapPanel.this.revalidate();
                MindMapPanel.this.repaint();

                final Dimension newSize = mindMapImageSize.get().getSize();

                fireNotificationScaledByMouse(e.getPoint(), oldScale, newScale, oldSize, newSize);
                e.consume();
              } else {
                sendToParent(e);
              }
            }
          } finally {
            unlock();
          }
        }
      }

      @Override
      public void mouseClicked(final MouseEvent e) {
        if (!e.isConsumed() && lockIfNotDisposed() && !popupMenuActive.get()) {
          requestFocus();
          try {
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

            final boolean isCtrlDown = e.isControlDown();
            final boolean isShiftDown = e.isShiftDown();

            if (element != null) {
              final ElementPart part = element.findPartForPoint(e.getPoint());
              if (part == ElementPart.COLLAPSATOR) {
                fireNotificationTopicCollapsatorClick(element.getModel(), true);
                doFoldOrUnfoldTopic(Collections.singletonList(element), !element.isCollapsed(), isCtrlDown);
                if (selectedTopics.isEmpty() || selectedTopics.size() == 1) {
                  removeAllSelection();
                  select(element.getModel(), false);
                }
                fireNotificationTopicCollapsatorClick(element.getModel(), false);
              } else if (!isCtrlDown) {
                switch (part) {
                  case VISUAL_ATTRIBUTES:
                    final VisualAttributePlugin plugin = element.getVisualAttributeImageBlock().findPluginForPoint(e.getPoint().getX() - element.getBounds().getX(), e.getPoint().getY() - element.getBounds().getY());
                    boolean processedByPlugin = false;
                    if (plugin != null) {
                      final PluginContext context = controller.makePluginContext(theInstance);
                      if (plugin.isClickable(context, element.getModel())) {
                        processedByPlugin = true;
                        try {
                          if (plugin.onClick(context, element.getModel(), e.isShiftDown() || e.isControlDown(), e.getClickCount())) {
                            doLayout();
                            revalidate();
                            repaint();
                            fireNotificationMindMapChanged(true);
                          }
                        } catch (Exception ex) {
                          LOGGER.error("Error during visual attribute processing", ex);
                          controller.getDialogProvider(theInstance).msgError(null, "Detectd critical error! See log!");
                        }
                      }
                    }
                    if (!processedByPlugin) {
                      removeAllSelection();
                      select(element.getModel(), false);
                    }
                    break;
                  case ICONS:
                    final Extra<?> extra = element.getIconBlock().findExtraForPoint(e.getPoint().getX() - element.getBounds().getX(), e.getPoint().getY() - element.getBounds().getY());
                    if (extra != null) {
                      fireNotificationClickOnExtra(element.getModel(), e.getModifiers(), e.getClickCount(), extra);
                    }
                    break;
                  default:
                    if (isShiftDown) {
                      // diapason
                      selectSiblingDiapasone(element);
                    } else {
                      // only
                      removeAllSelection();
                      select(element.getModel(), false);
                      if (e.getClickCount() > 1) {
                        startEdit(element);
                      }
                    }
                    break;
                }
              } else {
                // group
                select(element.getModel(), !selectedTopics.isEmpty());
              }
            }
          } finally {
            unlock();
          }
        }
      }
    };

    SwingUtilities.invokeLater(() -> {
      addComponentListener(new ComponentAdapter() {
        @Override
        public void componentResized(final ComponentEvent e) {
          doLayout();
          updateEditorAfterResizing();
        }
      });

      addMouseWheelListener(adapter);
      addMouseListener(adapter);
      addMouseMotionListener(adapter);
      addKeyListener(keyAdapter);

      if (lockIfNotDisposed()) {
        try {
          textEditorPanel.add(textEditor, BorderLayout.CENTER);
          textEditorPanel.setVisible(false);
          add(textEditorPanel);

          for (final PanelAwarePlugin p : MindMapPluginRegistry.getInstance()
              .findFor(PanelAwarePlugin.class)) {
            p.onPanelCreate(theInstance);
          }
        } finally {
          unlock();
        }
      }
    });
  }

  private static void drawBackground(final MMGraphics g, final MindMapPanelConfig cfg) {
    final Rectangle clipBounds = g.getClipBounds();

    if (cfg.isDrawBackground()) {
      if (clipBounds == null) {
        LOGGER.warn("Can't draw background because clip bounds is not provided!");
      } else {
        g.drawRect(clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height, null,
            cfg.getPaperColor());

        if (cfg.isShowGrid()) {
          final double scaledGridStep = cfg.getGridStep() * cfg.getScale();

          final float minX = clipBounds.x;
          final float minY = clipBounds.y;
          final float maxX = clipBounds.x + clipBounds.width;
          final float maxY = clipBounds.y + clipBounds.height;

          final Color gridColor = cfg.getGridColor();

          for (float x = 0.0f; x < maxX; x += scaledGridStep) {
            if (x < minX) {
              continue;
            }
            final int intx = Math.round(x);
            g.drawLine(intx, (int) minY, intx, (int) maxY, gridColor);
          }

          for (float y = 0.0f; y < maxY; y += scaledGridStep) {
            if (y < minY) {
              continue;
            }
            final int inty = Math.round(y);
            g.drawLine((int) minX, inty, (int) maxX, inty, gridColor);
          }
        }
      }
    }
  }

  public static void drawOnGraphicsForConfiguration(final MMGraphics g,
                                                    final MindMapPanelConfig config,
                                                    final MindMap map, final boolean drawSelection,
                                                    final List<Topic> selectedTopics) {
    drawBackground(g, config);
    drawTopics(g, config, map);
    if (drawSelection && selectedTopics != null && !selectedTopics.isEmpty()) {
      drawSelection(g, config, selectedTopics);
    }
  }

  private static void drawSelection(final MMGraphics g, final MindMapPanelConfig cfg,
                                    final List<Topic> selectedTopics) {
    if (selectedTopics != null && !selectedTopics.isEmpty()) {
      final Color selectLineColor = cfg.getSelectLineColor();
      g.setStroke(cfg.safeScaleFloatValue(cfg.getSelectLineWidth(), 0.1f), StrokeType.DASHES);
      final double selectLineGap = cfg.safeScaleFloatValue(cfg.getSelectLineGap(), 0.05f);
      final double selectLineGapX2 = selectLineGap + selectLineGap;

      for (final Topic s : selectedTopics) {
        final AbstractElement e = (AbstractElement) s.getPayload();
        if (e != null) {
          final int x = (int) Math.round(e.getBounds().getX() - selectLineGap);
          final int y = (int) Math.round(e.getBounds().getY() - selectLineGap);
          final int w = (int) Math.round(e.getBounds().getWidth() + selectLineGapX2);
          final int h = (int) Math.round(e.getBounds().getHeight() + selectLineGapX2);
          g.drawRect(x, y, w, h, selectLineColor, null);
        }
      }
    }
  }

  private static void drawTopics(final MMGraphics g, final MindMapPanelConfig cfg,
                                 final MindMap map) {
    if (map != null) {
      if (Boolean.parseBoolean(map.findAttribute(StandardMmdAttributes.MMD_ATTRIBUTE_SHOW_JUMPS))) {
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

  private static void drawJumps(final MMGraphics gfx, final MindMap map,
                                final MindMapPanelConfig cfg) {
    final List<Topic> allTopicsWithJumps = map.findAllTopicsForExtraType(Extra.ExtraType.TOPIC);

    final float scaledSize = cfg.safeScaleFloatValue(cfg.getJumpLinkWidth(), 0.1f);

    final float lineWidth = scaledSize;
    final float arrowWidth = cfg.safeScaleFloatValue(cfg.getJumpLinkWidth(), 0.3f);

    final Color jumpLinkColor = cfg.getJumpLinkColor();

    final float arrowSize = cfg.safeScaleFloatValue(10.0f * cfg.getJumpLinkWidth(), 0.2f);

    for (Topic src : allTopicsWithJumps) {
      final ExtraTopic extra =
          (ExtraTopic) requireNonNull(requireNonNull(src).getExtras()).get(Extra.ExtraType.TOPIC);

      src = MindMapUtils.isHidden(src) ? MindMapUtils.findFirstVisibleAncestor(src) : src;

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
            if (!MindMapUtils.isHidden(dst) && dstElement != null) {
              final AbstractElement srcElement =
                  requireNonNull((AbstractElement) requireNonNull(src).getPayload());
              final Rectangle2D srcRect = srcElement.getBounds();
              final Rectangle2D dstRect = dstElement.getBounds();
              drawArrowToDestination(gfx, srcRect, dstRect, lineWidth, arrowWidth, arrowSize, jumpLinkColor);
            }
          }
        }
      }
    }
  }

  private static void drawArrowToDestination(final MMGraphics gfx, final Rectangle2D start,
                                             final Rectangle2D destination, final float lineWidth,
                                             final float arrowWidth, final float arrowSize,
                                             final Color color) {

    final double startx = start.getCenterX();
    final double starty = start.getCenterY();

    final Point2D arrowPoint = Utils.findRectEdgeIntersection(destination, startx, starty);

    if (arrowPoint != null) {
      gfx.setStroke(lineWidth, StrokeType.SOLID);

      double angle = findLineAngle(arrowPoint.getX(), arrowPoint.getY(), startx, starty);

      final double arrowAngle = Math.PI / 12.0d;

      final double x1 = arrowSize * Math.cos(angle - arrowAngle);
      final double y1 = arrowSize * Math.sin(angle - arrowAngle);
      final double x2 = arrowSize * Math.cos(angle + arrowAngle);
      final double y2 = arrowSize * Math.sin(angle + arrowAngle);

      final double cx = (arrowSize / 2.0f) * Math.cos(angle);
      final double cy = (arrowSize / 2.0f) * Math.sin(angle);

      final GeneralPath polygon = new GeneralPath();
      polygon.moveTo(arrowPoint.getX(), arrowPoint.getY());
      polygon.lineTo(arrowPoint.getX() + x1, arrowPoint.getY() + y1);
      polygon.lineTo(arrowPoint.getX() + x2, arrowPoint.getY() + y2);
      polygon.closePath();
      gfx.draw(polygon, null, color);

      gfx.setStroke(lineWidth, StrokeType.DOTS);
      gfx.drawLine((int) startx, (int) starty, (int) (arrowPoint.getX() + cx), (int) (arrowPoint.getY() + cy), color);
    }
  }

  private static void drawTopicTree(final MMGraphics gfx, final Topic topic,
                                    final MindMapPanelConfig cfg) {
    paintTopic(gfx, topic, cfg);
    final AbstractElement w = (AbstractElement) topic.getPayload();
    if (w != null) {
      if (w.isCollapsed()) {
        return;
      }
      for (final Topic t : topic.getChildren()) {
        drawTopicTree(gfx, t, cfg);
      }
    }
  }

  private static void paintTopic(final MMGraphics gfx, final Topic topic,
                                 final MindMapPanelConfig cfg) {
    final AbstractElement element = (AbstractElement) topic.getPayload();
    if (element != null) {
      element.doPaint(gfx, cfg, true);

// -------------- DRAW BORDERS ABOUND COMPONENT AREAS ---------------------------
//
//      final Dimension2D elementAreaSize = element.getBlockSize();
//      final Rectangle2D elementPosition = element.getBounds();
//
//      if (element instanceof ElementRoot) {
//        final ElementRoot root = (ElementRoot) element;
//        final Dimension2D leftSubblock = root.getLeftBlockSize();
//        final Dimension2D rightSubblock = root.getRightBlockSize();
//
//        if (leftSubblock.getWidth()>0) {
//          gfx.draw(new Rectangle2D.Double(elementPosition.getX() - leftSubblock.getWidth(), elementPosition.getY() - (leftSubblock.getHeight() - elementPosition.getHeight()) / 2d, leftSubblock.getWidth(), leftSubblock.getHeight()), Color.RED, null);
//        }
//
//        if (rightSubblock.getWidth()>0) {
//          gfx.draw(new Rectangle2D.Double(elementPosition.getX() + elementPosition.getWidth(), elementPosition.getY() - (rightSubblock.getHeight() - elementPosition.getHeight()) / 2d, rightSubblock.getWidth(), rightSubblock.getHeight()), Color.RED, null);
//        }
//
//        gfx.draw(new Rectangle2D.Double(elementPosition.getX() - leftSubblock.getWidth(), elementPosition.getY() - (elementAreaSize.getHeight() - elementPosition.getHeight()) / 2d, elementAreaSize.getWidth(), elementAreaSize.getHeight()), Color.CYAN, null);
//
//      }else
//      if (element.isLeftDirection()){
//        gfx.draw(new Rectangle2D.Double(elementPosition.getX()-(elementAreaSize.getWidth()-elementPosition.getWidth()),elementPosition.getY()-(elementAreaSize.getHeight()-elementPosition.getHeight())/2d,elementAreaSize.getWidth(),elementAreaSize.getHeight()), Color.GREEN, null);
//      } else {
//        gfx.draw(new Rectangle2D.Double(elementPosition.getX(), elementPosition.getY() - (elementAreaSize.getHeight() - elementPosition.getHeight()) / 2d, elementAreaSize.getWidth(), elementAreaSize.getHeight()), Color.YELLOW, null);
//      }
// ------------------------------------------------------------------------------
    }
  }

  private static void setElementSizesForElementAndChildren(final MMGraphics gfx,
                                                           final MindMapPanelConfig cfg,
                                                           final Topic topic, final int level) {
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

  public static boolean calculateElementSizes(final MMGraphics gfx, final MindMap model,
                                              final MindMapPanelConfig cfg) {
    boolean result = false;

    final Topic root = model == null ? null : model.getRoot();
    if (root != null && model != null) {
      model.clearAllPayloads();
      setElementSizesForElementAndChildren(gfx, cfg, root, 0);
      result = true;
    }
    return result;
  }

  public static Dimension2D layoutModelElements(final MindMap model, final MindMapPanelConfig cfg) {
    Dimension2D result = null;
    if (model != null) {
      final Topic rootTopic = model.getRoot();
      if (rootTopic != null) {
        final AbstractElement root = (AbstractElement) rootTopic.getPayload();
        if (root != null) {
          root.alignElementAndChildren(cfg, true, 0, 0);
          result = root.getBlockSize();
        }
      }
    }
    return result;
  }

  protected static void moveDiagram(final MindMap model, final double deltaX, final double deltaY) {
    if (model != null) {
      final Topic root = model.getRoot();
      if (root != null) {
        final AbstractElement element = (AbstractElement) root.getPayload();
        if (element != null) {
          element.moveWholeTreeBranchCoordinates(deltaX, deltaY);
        }
      }
    }
  }

  public static Dimension layoutFullDiagramWithCenteringToPaper(final MMGraphics gfx,
                                                                final MindMap map,
                                                                final MindMapPanelConfig cfg,
                                                                final Dimension2D paperSize) {
    Dimension resultSize = null;
    if (calculateElementSizes(gfx, map, cfg)) {
      Dimension2D rootBlockSize = layoutModelElements(map, cfg);
      final double paperMargin = cfg.getPaperMargins() * cfg.getScale();

      if (rootBlockSize != null) {
        final ElementRoot rootElement =
            requireNonNull((ElementRoot) requireNonNull(map.getRoot()).getPayload());

        double rootOffsetXInBlock = rootElement.getLeftBlockSize().getWidth();
        double rootOffsetYInBlock =
            (rootBlockSize.getHeight() - rootElement.getBounds().getHeight()) / 2;

        rootOffsetXInBlock += (paperSize.getWidth() - rootBlockSize.getWidth()) <= paperMargin ? paperMargin : (paperSize.getWidth() - rootBlockSize.getWidth()) / 2;
        rootOffsetYInBlock += (paperSize.getHeight() - rootBlockSize.getHeight()) <= paperMargin ? paperMargin : (paperSize.getHeight() - rootBlockSize.getHeight()) / 2;

        moveDiagram(map, rootOffsetXInBlock, rootOffsetYInBlock);
        resultSize = new Dimension((int) Math.round(rootBlockSize.getWidth() + paperMargin * 2), (int) Math.round(rootBlockSize.getHeight() + paperMargin * 2));
      }
    }

    return resultSize;
  }

  private static void drawErrorText(final Graphics2D gfx, final Dimension fullSize,
                                    final String error) {
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

  public static Dimension2D calculateSizeOfMapInPixels(final MindMap model,
                                                       final Graphics2D graphicsContext,
                                                       final MindMapPanelConfig cfg,
                                                       final boolean expandAll,
                                                       final RenderQuality quality) {
    final MindMap workMap = model.makeCopy();
    workMap.clearAllPayloads();

    Graphics2D g = graphicsContext;

    if (g == null) {
      BufferedImage img = new BufferedImage(32, 32,
          cfg.isDrawBackground() ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB);
      g = img.createGraphics();
    }
    final MMGraphics gfx = new MMGraphics2DWrapper(g);

    quality.prepare(g);

    Dimension2D blockSize = null;
    try {

      if (calculateElementSizes(gfx, workMap, cfg)) {
        if (expandAll) {
          final AbstractElement root =
              requireNonNull((AbstractElement) requireNonNull(workMap.getRoot()).getPayload());
          root.collapseOrExpandAllChildren(false);
          calculateElementSizes(gfx, workMap, cfg);
        }
        blockSize = requireNonNull(layoutModelElements(workMap, cfg));
        final double paperMargin = cfg.getPaperMargins() * cfg.getScale();
        blockSize.setSize(blockSize.getWidth() + paperMargin * 2, blockSize.getHeight() + paperMargin * 2);
      }
    } finally {
      gfx.dispose();
    }
    return blockSize;
  }

  public static BufferedImage renderMindMapAsImage(final MindMap model,
                                                   final MindMapPanelConfig cfg,
                                                   final boolean expandAll,
                                                   final RenderQuality quality) {
    final MindMap workMap = model.makeCopy();
    workMap.clearAllPayloads();

    if (expandAll) {
      MindMapUtils.removeCollapseAttr(workMap);
    }

    final Dimension2D blockSize =
        calculateSizeOfMapInPixels(workMap, null, cfg, expandAll, quality);
    if (blockSize == null) {
      return null;
    }

    final BufferedImage img = new BufferedImage((int) blockSize.getWidth(), (int) blockSize.getHeight(), BufferedImage.TYPE_INT_ARGB);
    final Graphics2D g = img.createGraphics();
    final MMGraphics gfx = new MMGraphics2DWrapper(g);
    try {
      quality.prepare(g);
      gfx.setClip(0, 0, img.getWidth(), img.getHeight());
      layoutFullDiagramWithCenteringToPaper(gfx, workMap, cfg, blockSize);
      drawOnGraphicsForConfiguration(gfx, cfg, workMap, false, null);
    } finally {
      gfx.dispose();
    }
    return img;
  }

  private static Topic[] ensureNoRootInArray(final Topic... topics) {
    final List<Topic> buffer = new ArrayList<>(topics.length);
    for (final Topic t : topics) {
      if (!t.isRoot()) {
        buffer.add(t);
      }
    }
    return buffer.toArray(new Topic[0]);
  }

  public UUID getUuid() {
    return this.uuid;
  }

  private void selectSiblingDiapasone(AbstractElement element) {
    final AbstractElement parent = element.getParent();
    Topic selectedSibling = null;

    if (parent != null) {
      for (final Topic e : this.selectedTopics) {
        if (e != element.getModel() && parent.getModel() == e.getParent() &&
            element.isLeftDirection() == AbstractCollapsableElement.isLeftSidedTopic(e)) {
          selectedSibling = e;
          break;
        }
      }
    }

    if (selectedSibling != null) {
      boolean select = false;
      for (final Topic t : parent.getModel().getChildren()) {
        if (select && element.isLeftDirection() == AbstractCollapsableElement.isLeftSidedTopic(t)) {
          select(t, false);
        }

        if (t == element.getModel() || t == selectedSibling) {
          select = !select;
          if (!select) {
            break;
          }
        }
      }
    }
    select(element.getModel(), false);
  }

  private void doFoldOrUnfoldTopic(final List<AbstractElement> elements, final boolean fold,
                                   final boolean onlyFirstLevel) {
    boolean changed = false;

    for (final AbstractElement e : elements) {
      if (fold) {
        changed |= MindMapUtils.foldOrUnfoldChildren(e.getModel(), true,
            onlyFirstLevel ? 1 : Integer.MAX_VALUE);
        for (final Topic t : getSelectedTopics()) {
          if (!MindMapUtils.isTopicVisible(t)) {
            this.selectedTopics.remove(t);
            changed = true;
          }
        }
      } else {
        changed |= MindMapUtils.foldOrUnfoldChildren(e.getModel(), false, onlyFirstLevel ? 1 : Integer.MAX_VALUE);
      }
    }

    if (changed) {
      this.doLayout();
      this.revalidate();
      this.repaint();
      this.fireNotificationMindMapChanged(true);
    }
  }

  /**
   * Get saved session object. Object is presented and saved only for the
   * current panel and only in memory.
   *
   * @param <T>   type of object
   * @param key   key of object, must not be null
   * @param klazz object type, must not be null
   * @param def   default value will be returned as result if object not
   *              presented, can be null
   * @return null if object is not found, the found object otherwise
   * @throws ClassCastException if object type is wrong for saved object
   * @since 1.4.2
   */
  public <T> T getSessionObject(final String key, final Class<T> klazz, final T def) {
    this.lock();
    try {
      T result = klazz.cast(this.sessionObjects.get(key));
      return result == null ? def : result;
    } finally {
      this.unlock();
    }
  }

  /**
   * Put session object for key.
   *
   * @param key key of he object, must not be null
   * @param obj object to be placed, if null then object will be removed
   * @since 1.4.2
   */
  public void putSessionObject(final String key, final Object obj) {
    this.lock();
    try {
      if (obj == null) {
        this.sessionObjects.remove(key);
      } else {
        this.sessionObjects.put(key, obj);
      }
    } finally {
      this.unlock();
    }
  }

  public Optional<Pair<AbstractElement, Point>> findBestPointForContextMenu(final boolean ensureComponentVisibility) {
    final AbstractElement element = findTopicForContextMenu();
    if (element == null) {
      return Optional.empty();
    } else {
      if (ensureComponentVisibility) {
        this.ensureVisibility(element);
      }
      return Optional.of(new Pair<>(element, element.getCenter()));
    }
  }

  private void processContextMenuKey() {
    final Pair<AbstractElement, Point> elementPoint = this.findBestPointForContextMenu(true).orElse(null);
    if (elementPoint == null) {
      final Rectangle rect = this.getBounds();
      this.processPopUp(new Point((int) rect.getCenterX(), (int) rect.getCenterY()), null);
    } else {
      this.processPopUp(elementPoint.getRight(), elementPoint.getLeft());
    }
  }

  private String makeHtmlTooltipForExtra(final Extra<?> extra) {
    final StringBuilder builder = new StringBuilder();

    builder.append("<html>");

    switch (extra.getType()) {
      case FILE: {
        final ExtraFile efile = (ExtraFile) extra;
        final String line = efile.getAsURI().getParameters().getProperty("line", null);
        if (line != null && !line.equals("0")) {
          builder.append(String.format(this.mmdI18n.findBundle().getString("MindMapPanel.tooltipOpenFileWithLine"),
              StringEscapeUtils.escapeHtml3(efile.getAsString()),
              StringEscapeUtils.escapeHtml3(line)));
        } else {
          builder.append(this.mmdI18n.findBundle().getString("MindMapPanel.tooltipOpenFile")).append(StringEscapeUtils.escapeHtml3(efile.getAsString()));
        }
      }
      break;
      case TOPIC: {
        final Topic topic = this.getModel().findTopicForLink((ExtraTopic) extra);
        builder.append(this.mmdI18n.findBundle().getString("MindMapPanel.tooltipJumpToTopic")).append(StringEscapeUtils.escapeHtml3(ModelUtils.makeEllipsis(topic == null ? "----" : topic.getText(), 32)));
      }
      break;
      case LINK: {
        builder.append(this.mmdI18n.findBundle().getString("MindMapPanel.tooltipOpenLink")).append(StringEscapeUtils.escapeHtml3(ModelUtils.makeEllipsis(extra.getAsString(), 48)));
      }
      break;
      case NOTE: {
        final ExtraNote extraNote = (ExtraNote) extra;
        if (extraNote.isEncrypted()) {
          builder.append(this.mmdI18n.findBundle().getString("MindMapPanel.tooltipOpenText")).append("#######");
        } else {
          builder.append(this.mmdI18n.findBundle().getString("MindMapPanel.tooltipOpenText")).append(StringEscapeUtils
                  .escapeHtml3(ModelUtils.makeEllipsis(extra.getAsString(), 64)));
        }
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

  public void refreshConfiguration() {
    if (this.lockIfNotDisposed()) {
      try {
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
      } finally {
        this.unlock();
      }
    }
  }

  private int calcDropPosition(final AbstractElement destination, final Point dropPoint) {
    final int result;
    if (destination.getClass() == ElementRoot.class) {
      result = dropPoint.getX() < destination.getBounds().getCenterX() ? DRAG_POSITION_LEFT :
          DRAG_POSITION_RIGHT;
    } else {
      final boolean destinationIsLeft = destination.isLeftDirection();
      final Rectangle2D bounds = destination.getBounds();

      final double edgeOffset = bounds.getWidth() * 0.2d;
      if (dropPoint.getX() >= (bounds.getX() + edgeOffset) &&
          dropPoint.getX() <= (bounds.getMaxX() - edgeOffset)) {
        result = dropPoint.getY() < bounds.getCenterY() ? DRAG_POSITION_TOP : DRAG_POSITION_BOTTOM;
      } else if (destinationIsLeft) {
        result = dropPoint.getX() < bounds.getCenterX() ? DRAG_POSITION_LEFT : DRAG_POSITION_UNKNOWN;
      } else {
        result = dropPoint.getX() > bounds.getCenterX() ? DRAG_POSITION_RIGHT : DRAG_POSITION_UNKNOWN;
      }
    }
    return result;
  }

  private boolean endDragOfElement(final DraggedElement draggedElement,
                                   final AbstractElement destination) {
    final AbstractElement dragged = draggedElement.getElement();
    final Point dropPoint = draggedElement.getPosition();

    final boolean ignore =
        dragged.getModel() == destination.getModel() || dragged.getBounds().contains(dropPoint) ||
            destination.getModel().hasAncestor(dragged.getModel());
    if (ignore) {
      return false;
    }

    boolean changed = true;

    if (draggedElement.getModifier() == DraggedElement.Modifier.MAKE_JUMP) {
      // make link
      return this.controller.processDropTopicToAnotherTopic(this, dropPoint, dragged.getModel(), destination.getModel());
    }

    final int pos = calcDropPosition(destination, dropPoint);
    switch (pos) {
      case DRAG_POSITION_TOP:
      case DRAG_POSITION_BOTTOM: {
        dragged.getModel().moveToNewParent(requireNonNull(destination.getParent()).getModel());
        if (pos == DRAG_POSITION_TOP) {
          dragged.getModel().moveBefore(destination.getModel());
        } else {
          dragged.getModel().moveAfter(destination.getModel());
        }

        if (destination.getClass() == ElementLevelFirst.class) {
          AbstractCollapsableElement.makeTopicLeftSided(dragged.getModel(), destination.isLeftDirection());
        } else {
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
        } else {
          dragged.getModel().moveToNewParent(destination.getModel());
          if (destination instanceof AbstractCollapsableElement && destination.isCollapsed() &&
              (controller == null || controller.isUnfoldCollapsedTopicDropTarget(this))) {
            ((AbstractCollapsableElement) destination).setCollapse(false);
          }
          if (dropPoint.getY() < destination.getBounds().getY()) {
            dragged.getModel().makeFirst();
          } else {
            dragged.getModel().makeLast();
          }
          if (destination.getClass() == ElementRoot.class) {
            AbstractCollapsableElement.makeTopicLeftSided(dragged.getModel(), pos == DRAG_POSITION_LEFT);
          } else {
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

  private void processMoveFocusByKey(final KeyEvent key) {
    final AbstractElement lastSelectedTopic = this.selectedTopics.isEmpty() ? null :
        (AbstractElement) this.selectedTopics.get(this.selectedTopics.size() - 1).getPayload();
    if (lastSelectedTopic == null) {
      return;
    }

    AbstractElement nextFocused = null;

    boolean modelChanged = false;

    if (lastSelectedTopic.isMoveable()) {
      boolean processFirstChild = false;
      if (config.isKeyEventDetected(key, MindMapPanelConfig.KEY_FOCUS_MOVE_LEFT, MindMapPanelConfig.KEY_FOCUS_MOVE_LEFT_ADD_FOCUSED)) {
        if (lastSelectedTopic.isLeftDirection()) {
          processFirstChild = true;
        } else {
          nextFocused = (AbstractElement) requireNonNull(
              lastSelectedTopic.getModel().getParent()).getPayload();
        }
      } else if (config.isKeyEventDetected(key, MindMapPanelConfig.KEY_FOCUS_MOVE_RIGHT, MindMapPanelConfig.KEY_FOCUS_MOVE_RIGHT_ADD_FOCUSED)) {
        if (lastSelectedTopic.isLeftDirection()) {
          nextFocused = (AbstractElement) requireNonNull(
              lastSelectedTopic.getModel().getParent()).getPayload();
        } else {
          processFirstChild = true;
        }
      } else {
        final boolean pressedButtonMoveUp = config.isKeyEventDetected(key, MindMapPanelConfig.KEY_FOCUS_MOVE_UP, MindMapPanelConfig.KEY_FOCUS_MOVE_UP_ADD_FOCUSED);
        final boolean firstLevel = lastSelectedTopic.getClass() == ElementLevelFirst.class;
        final boolean currentLeft = AbstractCollapsableElement.isLeftSidedTopic(lastSelectedTopic.getModel());

        final Predicate<Topic> checker = topic -> {
          if (!firstLevel) {
            return true;
          } else if (currentLeft) {
            return AbstractCollapsableElement.isLeftSidedTopic(topic);
          } else {
            return !AbstractCollapsableElement.isLeftSidedTopic(topic);
          }
        };
        final Topic topic = pressedButtonMoveUp ? lastSelectedTopic.getModel().findPrev(checker) : lastSelectedTopic.getModel().findNext(checker);
        nextFocused = topic == null ? null : (AbstractElement) topic.getPayload();
      }

      if (processFirstChild) {
        if (lastSelectedTopic.hasChildren()) {
          if (lastSelectedTopic.isCollapsed()) {
            ((AbstractCollapsableElement) lastSelectedTopic).setCollapse(false);
            modelChanged = true;
          }

          nextFocused = (AbstractElement) (lastSelectedTopic.getModel().getChildren().get(0)).getPayload();
        }
      }
    } else if (config.isKeyEventDetected(key, MindMapPanelConfig.KEY_FOCUS_MOVE_LEFT, MindMapPanelConfig.KEY_FOCUS_MOVE_LEFT_ADD_FOCUSED)) {
      for (final Topic t : lastSelectedTopic.getModel().getChildren()) {
        final AbstractElement e = (AbstractElement) t.getPayload();
        if (e != null && e.isLeftDirection()) {
          nextFocused = e;
          break;
        }
      }
    } else if (config.isKeyEventDetected(key, MindMapPanelConfig.KEY_FOCUS_MOVE_RIGHT, MindMapPanelConfig.KEY_FOCUS_MOVE_RIGHT_ADD_FOCUSED)) {
      for (final Topic t : lastSelectedTopic.getModel().getChildren()) {
        final AbstractElement e = (AbstractElement) t.getPayload();
        if (e != null && !e.isLeftDirection()) {
          nextFocused = e;
          break;
        }
      }
    }

    if (nextFocused != null) {
      final boolean addFocused = config.isKeyEventDetected(key,
              MindMapPanelConfig.KEY_FOCUS_MOVE_UP_ADD_FOCUSED,
              MindMapPanelConfig.KEY_FOCUS_MOVE_DOWN_ADD_FOCUSED,
              MindMapPanelConfig.KEY_FOCUS_MOVE_LEFT_ADD_FOCUSED,
              MindMapPanelConfig.KEY_FOCUS_MOVE_RIGHT_ADD_FOCUSED);
      if (!addFocused || this.selectedTopics.contains(nextFocused.getModel())) {
        removeAllSelection();
      }
      select(nextFocused.getModel(), false);
    }

    if (modelChanged) {
      fireNotificationMindMapChanged(true);
    }
  }

  /**
   * Safe Swing thread execution sequence of some jobs over model with model
   * changed notification in the end
   *
   * @param jobs sequence of jobs to be executed
   * @since 1.3.1
   */
  public void executeModelJobs(final ModelJob... jobs) {
    Utils.safeSwingCall(new Runnable() {
      @Override
      public void run() {
        for (final ModelJob j : jobs) {
          try {
            if (!j.doChangeModel(model)) {
              break;
            }
          } catch (Exception ex) {
            LOGGER.error("Errot during job execution", ex);
          }
        }
        fireNotificationMindMapChanged(true);
      }
    });
  }

  private void ensureVisibility(final AbstractElement e) {
    fireNotificationEnsureTopicVisibility(e.getModel());
  }

  private boolean hasActiveEditor() {
    if (lockIfNotDisposed()) {
      try {
        return this.elementUnderEdit != null;
      } finally {
        unlock();
      }
    }
    return false;
  }

  public boolean isShowJumps() {
    return Boolean.parseBoolean(this.model.findAttribute(StandardMmdAttributes.MMD_ATTRIBUTE_SHOW_JUMPS));
  }

  public void setShowJumps(final boolean flag) {
    if (lockIfNotDisposed()) {
      try {
        this.model.putAttribute(StandardMmdAttributes.MMD_ATTRIBUTE_SHOW_JUMPS, flag ? "true" : null);
        repaint();
        fireNotificationMindMapChanged(true);
      } finally {
        this.unlock();
      }
    }
  }

  private Topic makeNewTopic(final Topic parent, final Topic afterTopic, final String text) {
    final Topic result = parent.makeChild(text, afterTopic);
    for (final ModelAwarePlugin p : MindMapPluginRegistry.getInstance()
        .findFor(ModelAwarePlugin.class)) {
      p.onCreateTopic(this, parent, result);
    }
    invalidate();
    return result;
  }

  public void makeNewChildAndStartEdit(final Topic parent, final Topic baseTopic) {
    if (this.lockIfNotDisposed()) {
      try {
        if (parent != null) {
          final Topic currentSelected = getFirstSelected();
          this.pathToPrevTopicBeforeEdit =
              currentSelected == null ? null : currentSelected.getPositionPath();

          removeAllSelection();

          final Topic newTopic = makeNewTopic(parent, baseTopic, "");

          if (this.controller.isCopyColorInfoFromParentToNewChildAllowed(this) && !parent.isRoot()) {
            MindMapUtils.copyColorAttributes(parent, newTopic);
          }

          AbstractElement parentElement = (AbstractElement) parent.getPayload();
          if (parentElement == null) {
            doLayout();
            parentElement = (AbstractElement) parent.getPayload();
          }

          if (parent.getChildren().size() != 1 && parent.getParent() == null && baseTopic == null) {
            int numLeft = 0;
            int numRight = 0;
            for (final Topic t : parent.getChildren()) {
              if (AbstractCollapsableElement.isLeftSidedTopic(t)) {
                numLeft++;
              } else {
                numRight++;
              }
            }

            AbstractCollapsableElement.makeTopicLeftSided(newTopic, Utils.LTR_LANGUAGE ? numLeft < numRight : numLeft > numRight);
          } else if (baseTopic != null && baseTopic.getPayload() != null) {
            final AbstractElement element =
                requireNonNull((AbstractElement) baseTopic.getPayload());
            AbstractCollapsableElement.makeTopicLeftSided(newTopic, element.isLeftDirection());
          }

          if (parentElement instanceof AbstractCollapsableElement && parentElement.isCollapsed()) {
            ((AbstractCollapsableElement) parentElement).setCollapse(false);
          }

          doLayout();
          fireNotificationMindMapChanged(false);
          removeEditedTopicForRollback.set(true);

          fireNotificationEnsureTopicVisibility(newTopic);
          select(newTopic, false);
          startEdit((AbstractElement) newTopic.getPayload());
          repaint();
        }
      } finally {
        this.unlock();
      }
    }
  }

  protected void fireNotificationSelectionChanged() {
    final Topic[] selected = this.selectedTopics.toArray(new Topic[0]);
    for (final MindMapListener l : MindMapPanel.this.mindMapListeners) {
      l.onChangedSelection(MindMapPanel.this, selected);
    }
    this.repaint();
  }

  protected void fireNotificationMindMapChanged(final boolean saveToHistory) {
    for (final MindMapListener l : MindMapPanel.this.mindMapListeners) {
      l.onMindMapModelChanged(MindMapPanel.this, saveToHistory);
    }
    this.repaint();
  }

  protected void fireNotificationComponentElementsLayouted(final Graphics2D graphics) {
    for (final MindMapListener l : MindMapPanel.this.mindMapListeners) {
      l.onComponentElementsLayout(MindMapPanel.this, graphics);
    }
    this.repaint();
  }

  protected void fireNotificationClickOnExtra(final Topic topic, final int modifiers,
                                              final int clicks, final Extra<?> extra) {
    for (final MindMapListener l : MindMapPanel.this.mindMapListeners) {
      l.onClickOnExtra(MindMapPanel.this, modifiers, clicks, topic, extra);
    }
  }

  protected void fireNotificationEnsureTopicVisibility(final Topic topic) {
    for (final MindMapListener l : MindMapPanel.this.mindMapListeners) {
      l.onEnsureVisibilityOfTopic(MindMapPanel.this, topic);
    }
  }

  protected void fireNotificationTopicCollapsatorClick(final Topic topic,
                                                       final boolean beforeAction) {
    for (final MindMapListener l : MindMapPanel.this.mindMapListeners) {
      l.onTopicCollapsatorClick(MindMapPanel.this, topic, beforeAction);
    }
    this.repaint();
  }

  protected void fireNotificationScaledByMouse(
      final Point mousePoint,
      final double oldScale,
      final double newScale,
      final Dimension oldSize,
      final Dimension newSize
  ) {
    for (final MindMapListener l : MindMapPanel.this.mindMapListeners) {
      l.onScaledByMouse(MindMapPanel.this, mousePoint, oldScale, newScale, oldSize, newSize);
    }
    this.repaint();
  }

  protected void fireNotificationNonConsumedKeyEvent(final KeyEvent keyEvent,
                                                     final KeyEventType type) {
    for (final MindMapListener l : MindMapPanel.this.mindMapListeners) {
      if (keyEvent.isConsumed()) {
        break;
      }
      l.onNonConsumedKeyEvent(MindMapPanel.this, keyEvent, type);
    }
  }

  public void deleteTopics(final boolean force, final Topic... topics) {
    if (lockIfNotDisposed()) {
      try {
        endEdit(false);

        final List<ModelAwarePlugin> plugins =
            MindMapPluginRegistry.getInstance().findFor(ModelAwarePlugin.class);

        boolean allowed = true;
        if (!force) {
          for (final MindMapListener l : this.mindMapListeners) {
            allowed &= l.allowedRemovingOfTopics(this, topics);
          }
        }

        if (allowed) {
          removeAllSelection();
          for (final Topic t : topics) {
            for (final ModelAwarePlugin p : plugins) {
              p.onDeleteTopic(this, t);
            }
            this.model.removeTopic(t);
          }
          doLayout();
          revalidate();
          repaint();
          fireNotificationMindMapChanged(true);
        }
      } finally {
        unlock();
      }
    }
  }

  public void collapseOrExpandAll(final boolean collapse) {
    if (this.lockIfNotDisposed()) {
      try {
        endEdit(false);
        removeAllSelection();

        final Topic topic = this.model.getRoot();

        if (topic != null && MindMapUtils.foldOrUnfoldChildren(topic, collapse, Integer.MAX_VALUE)) {
          doLayout();
          revalidate();
          repaint();
          fireNotificationMindMapChanged(true);
        }
      } finally {
        this.unlock();
      }
    }
  }

  public Topic deleteSelectedTopics(final boolean force) {
    Topic nextToFocus = null;
    if (this.lockIfNotDisposed()) {
      try {
        if (!this.selectedTopics.isEmpty()) {
          if (this.selectedTopics.size() == 1) {
            nextToFocus = this.selectedTopics.get(0).getParent();
          }

          deleteTopics(force, this.selectedTopics.toArray(new Topic[0]));
        }
      } finally {
        this.unlock();
      }
    }
    return nextToFocus;
  }

  public boolean hasSelectedTopics() {
    if (this.lockIfNotDisposed()) {
      try {
        return !this.selectedTopics.isEmpty();
      } finally {
        this.unlock();
      }
    } else {
      return false;
    }
  }

  public boolean hasOnlyTopicSelected() {
    if (this.lockIfNotDisposed()) {
      try {
        return this.selectedTopics.size() == 1;
      } finally {
        this.unlock();
      }
    } else {
      return false;
    }
  }

  public void removeFromSelection(final Topic t) {
    if (this.lockIfNotDisposed()) {
      try {
        if (this.selectedTopics.contains(t)) {
          if (this.selectedTopics.remove(t)) {
            fireNotificationSelectionChanged();
          }
          repaint();
        }
      } finally {
        this.unlock();
      }
    }
  }

  public void select(final Topic t, final boolean removeIfPresented) {
    if (this.lockIfNotDisposed()) {
      try {
        if (this.controller.isSelectionAllowed(this) && t != null) {
          if (!this.selectedTopics.contains(t)) {
            if (this.selectedTopics.add(t)) {
              fireNotificationSelectionChanged();
            }
            fireNotificationEnsureTopicVisibility(t);
            repaint();
          } else if (removeIfPresented) {
            removeFromSelection(t);
          }
        }
      } finally {
        this.unlock();
      }
    }
  }

  public Topic[] getSelectedTopics() {
    this.lock();
    try {
      return this.selectedTopics.toArray(new Topic[0]);
    } finally {
      this.unlock();
    }
  }

  public void updateEditorAfterResizing() {
    if (this.lockIfNotDisposed()) {
      try {
        if (this.elementUnderEdit != null) {
          final AbstractElement element = this.elementUnderEdit;
          final Dimension textBlockSize = new Dimension((int) element.getBounds().getWidth(), (int) element.getBounds().getHeight());
          this.textEditorPanel.setBounds((int) element.getBounds().getX(), (int) element.getBounds().getY(), textBlockSize.width, textBlockSize.height);
          this.textEditor.setMinimumSize(textBlockSize);
          this.textEditorPanel.setVisible(true);
          this.textEditor.requestFocus();
        }
      } finally {
        this.unlock();
      }
    }
  }

  public void hideEditor() {
    if (this.lockIfNotDisposed()) {
      try {
        this.textEditorPanel.setVisible(false);
        this.elementUnderEdit = null;
      } finally {
        this.unlock();
      }
    }
  }

  public boolean endEdit(final boolean commit) {
    boolean result = false;
    if (this.lockIfNotDisposed()) {
      result = this.elementUnderEdit != null;
      try {
        if (this.elementUnderEdit != null) {
          final AbstractElement editedElement = this.elementUnderEdit;
          Topic editedTopic = this.elementUnderEdit.getModel();

          final int[] pathToEditedTopic = editedTopic.getPositionPath();

          if (commit) {
            this.pathToPrevTopicBeforeEdit = null;

            final String oldText = editedElement.getText();
            String newText = this.textEditor.getText();

            if (this.controller.isTrimTopicTextBeforeSet(this)) {
              newText = newText.trim();
            }

            boolean contentChanged = false;
            if (!oldText.equals(newText)) {
              editedElement.setText(newText);
              contentChanged = true;
            }
            this.textEditorPanel.setVisible(false);

            doLayout();
            revalidate();
            repaint();
            fireNotificationEnsureTopicVisibility(editedTopic);

            if (contentChanged) {
              fireNotificationMindMapChanged(true);
            }

            editedTopic = this.model.findAtPosition(pathToEditedTopic);
            this.focusTo(editedTopic);
          } else {
            if (this.removeEditedTopicForRollback.get()) {
              this.selectedTopics.remove(editedTopic);
              this.model.removeTopic(editedTopic);
              doLayout();
              revalidate();
              repaint();
            }
          }
        }
      } finally {
        try {
          this.removeEditedTopicForRollback.set(false);
          this.elementUnderEdit = null;
          this.textEditorPanel.setVisible(false);
          this.requestFocus();
        } finally {
          this.unlock();
        }
      }
    }
    return result;
  }

  public void startEdit(final AbstractElement element) {
    if (this.lockIfNotDisposed()) {
      try {
        if (element == null) {
          this.elementUnderEdit = null;
          this.textEditorPanel.setVisible(false);
        } else {
          this.elementUnderEdit = element;
          element.fillByTextAndFont(this.textEditor);
          ensureVisibility(this.elementUnderEdit);

          final Dimension textBlockSize = new Dimension((int) element.getBounds().getWidth(), (int) element.getBounds().getHeight());
          textEditorPanel.setBounds((int) element.getBounds().getX(), (int) element.getBounds().getY(), textBlockSize.width, textBlockSize.height);
          textEditor.setMinimumSize(textBlockSize);

          textEditorPanel.setVisible(true);
          textEditor.requestFocus();
        }
      } finally {
        this.unlock();
      }
    }
  }

  private void findDestinationElementForDragged() {
    final Topic theroot = this.model.getRoot();
    if (this.draggedElement != null && theroot != null) {
      final AbstractElement root = (AbstractElement) requireNonNull(theroot.getPayload());
      this.destinationElement = root.findNearestOpenedTopicToPoint(this.draggedElement.getElement(), this.draggedElement.getPosition());
    } else {
      this.destinationElement = null;
    }
  }

  protected void processPopUpForShortcut() {
    if (this.lockIfNotDisposed()) {
      try {
        final Topic topic = this.selectedTopics.isEmpty() ? null : this.selectedTopics.get(0);

        if (topic != null) {
          fireNotificationEnsureTopicVisibility(topic);
        }

        if (topic == null) {
          select(getModel().getRoot(), false);
        } else {
          final AbstractElement element = (AbstractElement) topic.getPayload();
          if (element != null) {
            final Rectangle2D bounds = element.getBounds();
            processPopUp(new Point((int) Math.round(bounds.getCenterX()), (int) Math.round(bounds.getCenterY())), element);
          }
        }
      } finally {
        unlock();
      }
    }
  }

  protected void processPopUp(final Point point, final AbstractElement elementUnderMouse) {
    if (this.lockIfNotDisposed()) {
      try {
        if (this.controller != null) {
          final ElementPart partUnderMouse =
              elementUnderMouse == null ? null : elementUnderMouse.findPartForPoint(point);

          if (elementUnderMouse != null &&
              !this.selectedTopics.contains(elementUnderMouse.getModel())) {
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
                theInstance.popupMenuActive.set(true);
              }

              @Override
              public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
                theInstance.mouseDragSelection = null;
                theInstance.popupMenuActive.set(false);
              }

              @Override
              public void popupMenuCanceled(final PopupMenuEvent e) {
                theInstance.mouseDragSelection = null;
                theInstance.popupMenuActive.set(false);
              }
            });

            menu.show(this, point.x, point.y);
          }
        }
      } finally {
        unlock();
      }
    }
  }

  public void addMindMapListener(final MindMapListener l) {
    if (this.lockIfNotDisposed()) {
      try {
        this.mindMapListeners.add(requireNonNull(l));
      } finally {
        this.unlock();
      }
    }
  }

  public void removeMindMapListener(final MindMapListener l) {
    if (this.lockIfNotDisposed()) {
      try {
        this.mindMapListeners.remove(requireNonNull(l));
      } finally {
        this.unlock();
      }
    }
  }

  /**
   * Set model for the panel, allows to notify listeners optionally.
   *
   * @param model                      model to be set
   * @param notifyModelChangeListeners true if to notify model change listeners,
   *                                   false otherwise
   * @since 1.3.0
   */
  public void setModel(final MindMap model, final boolean notifyModelChangeListeners) {
    this.lock();
    try {
      if (this.elementUnderEdit != null) {
        Utils.safeSwingBlockingCall(new Runnable() {
          @Override
          public void run() {
            endEdit(false);
          }
        });
      }

      final List<int[]> selectedPaths = new ArrayList<>();
      for (final Topic t : this.selectedTopics) {
        selectedPaths.add(t.getPositionPath());
      }

      this.selectedTopics.clear();

      final MindMap oldModel = this.model;
      this.model = requireNonNull(model, "Model must not be null");

      for (final PanelAwarePlugin p : MindMapPluginRegistry.getInstance().findFor(PanelAwarePlugin.class)) {
        p.onPanelModelChange(this, oldModel, this.model);
      }

      doLayout();
      revalidate();

      boolean selectionChanged = false;
      for (final int[] posPath : selectedPaths) {
        final Topic topic = this.model.findAtPosition(posPath);
        if (topic == null) {
          selectionChanged = true;
        } else if (!MindMapUtils.isHidden(topic)) {
          this.selectedTopics.add(topic);
        }
      }
      if (selectionChanged) {
        fireNotificationSelectionChanged();
      }
      repaint();
    } finally {
      this.unlock();
      if (notifyModelChangeListeners) {
        fireNotificationMindMapChanged(true);
      }
    }
  }

  @Override
  public boolean isFocusable() {
    return true;
  }

  public MindMap getModel() {
    this.lock();
    try {
      return requireNonNull(this.model, "Model is not provided, it must not be null!");
    } finally {
      this.unlock();
    }
  }

  public void setModel(final MindMap model) {
    this.setModel(model, false);
  }

  public double getScale() {
    this.lock();
    try {
      return this.config.getScale();
    } finally {
      this.unlock();
    }
  }

  public void setScale(final double zoom, final boolean notifyListeners) {
    if (this.lockIfNotDisposed()) {
      try {
        if (notifyListeners) {
          this.config.setScale(zoom);
        } else {
          this.config.setScaleWithoutListenerNotification(zoom);
        }
      } finally {
        this.unlock();
      }
    }
  }

  private void drawDestinationElement(final Graphics2D g, final MindMapPanelConfig cfg) {
    if (this.destinationElement != null && this.draggedElement != null) {
      g.setColor(new Color((cfg.getSelectLineColor().getRGB() & 0xFFFFFF) | 0x80000000, true));
      g.setStroke(new BasicStroke(this.config.safeScaleFloatValue(3.0f, 0.1f)));

      final Rectangle2D rectToDraw = new Rectangle2D.Double();
      rectToDraw.setRect(this.destinationElement.getBounds());
      final double selectLineGap = cfg.getSelectLineGap() * 3.0d * cfg.getScale();
      rectToDraw.setRect(rectToDraw.getX() - selectLineGap, rectToDraw.getY() - selectLineGap,
          rectToDraw.getWidth() + selectLineGap * 2, rectToDraw.getHeight() + selectLineGap * 2);

      final int position =
          calcDropPosition(this.destinationElement, this.draggedElement.getPosition());

      boolean draw = !this.draggedElement.isPositionInside() && !this.destinationElement.getModel().hasAncestor(this.draggedElement.getElement().getModel());

      switch (this.draggedElement.getModifier()) {
        case NONE: {
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
        }
        break;
        case MAKE_JUMP: {
        }
        break;
        default:
          throw new Error("Unexpected state " + this.draggedElement.getModifier());
      }

      if (draw) {
        g.fill(rectToDraw);
      }
    }
  }

  @Override
  public Dimension getPreferredSize() {
    return this.mindMapImageSize.get().getSize();
  }

  @Override
  public Dimension getMinimumSize() {
    return this.getPreferredSize();
  }

  private void changeSizeOfComponent(final Dimension size,
                                     final boolean doNotificationThatRealigned) {
    if (size != null) {
      final Dimension oldSize = this.mindMapImageSize.getAndSet(size);

      if (doNotificationThatRealigned) {
        this.firePropertyChange("preferredSize", oldSize, size);
        this.firePropertyChange("minimumSize", oldSize, size);

        for (final MindMapListener l : this.mindMapListeners) {
          l.onMindMapModelRealigned(this, size);
        }
      }
    }
  }

  @Override
  public void doLayout() {
    final Runnable run = new Runnable() {
      @Override
      public void run() {
        if (lockIfNotDisposed()) {
          try {
            invalidate();
            updateElementsAndSizeForCurrentGraphics(true, false);
            repaint();
          } finally {
            unlock();
          }
        }
        MindMapPanel.super.doLayout();
      }
    };

    if (SwingUtilities.isEventDispatchThread()) {
      run.run();
    } else {
      try {
        SwingUtilities.invokeAndWait(run);
      } catch (InvocationTargetException ex) {
        throw new RuntimeException(ex);
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
      }
    }
  }

  public boolean updateElementsAndSizeForGraphics(final Graphics2D graph, final boolean forceLayout,
                                                  final boolean doListenerNotification) {
    boolean result = true;
    if (forceLayout || !isValid()) {
      if (lockIfNotDisposed()) {
        try {
          if (graph != null) {
            final MMGraphics gfx = new MMGraphics2DWrapper(graph);
            if (calculateElementSizes(gfx, this.model, this.config)) {

              Dimension pageSize = getSize();

              final Container parent = this.getParent();
              if (parent != null) {
                if (parent instanceof JViewport) {
                  pageSize = ((JViewport) parent).getExtentSize();
                }
              }

              changeSizeOfComponent(layoutFullDiagramWithCenteringToPaper(gfx, this.model, this.config, pageSize), doListenerNotification);
              result = true;

              if (doListenerNotification) {
                fireNotificationComponentElementsLayouted(graph);
              }
            }
          }
        } finally {
          unlock();
        }
      }
    }
    return result;
  }

  public boolean updateElementsAndSizeForCurrentGraphics(final boolean enforce, final boolean doListenerNotification) {
    assertSwingDispatchThread();
    Graphics2D gfx = (Graphics2D) this.getGraphics();
    try {
      if (gfx == null) {
        gfx = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB).createGraphics();
      }
      return updateElementsAndSizeForGraphics((Graphics2D) getGraphics(), enforce, doListenerNotification);
    } finally {
      gfx.dispose();
    }
  }

  public String getErrorText() {
    return this.errorText;
  }

  public void setErrorText(final String text) {
    if (this.lockIfNotDisposed()) {
      try {
        this.errorText = text;
        repaint();
      } finally {
        this.unlock();
      }
    }
  }

  @Override
  public boolean isValid() {
    if (this.lockIfNotDisposed()) {
      try {
        if (this.model != null) {
          final Topic root = this.model.getRoot();
          AbstractElement rootElement = null;
          if (root != null) {
            rootElement = (AbstractElement) root.getPayload();
          }
          return rootElement != null;
        }
      } finally {
        this.unlock();
      }
    }
    return true;
  }

  @Override
  public boolean isValidateRoot() {
    return true;
  }

  @Override
  public void invalidate() {
    if (lockIfNotDisposed()) {
      try {
        super.invalidate();
        if (this.model != null && this.model.getRoot() != null) {
          this.model.clearAllPayloads();
        }
      } finally {
        this.unlock();
      }
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public void paintComponent(final Graphics g) {
    if (this.lockIfNotDisposed()) {
      try {
        final Graphics2D gfx = (Graphics2D) g.create();
        try {
          final String error = this.errorText;

          this.getConfiguration().getRenderQuality().prepare(gfx);

          if (error != null) {
            drawErrorText(gfx, this.getSize(), error);
          } else {
            if (this.model.getRoot().getPayload() == null) {
              updateElementsAndSizeForGraphics(gfx, true, false);
            }
            drawOnGraphicsForConfiguration(new MMGraphics2DWrapper(gfx), this.config, this.model, true, this.selectedTopics);
            drawDestinationElement(gfx, this.config);
          }

          paintChildren(g);

          if (this.draggedElement != null) {
            this.draggedElement.draw(gfx);
          } else if (this.mouseDragSelection != null) {
            gfx.setColor(COLOR_MOUSE_DRAG_SELECTION);
            gfx.fill(this.mouseDragSelection.asRectangle());
          }
        } finally {
          gfx.dispose();
        }
      } finally {
        this.unlock();
      }
    }
  }

  public AbstractElement findTopicForContextMenu() {
    if (this.lockIfNotDisposed()) {
      try {
        AbstractElement result = null;
        if (this.selectedTopics.isEmpty()) {
          if (this.model != null) {
            final Topic root = this.model.getRoot();
            if (root != null) {
              result = (AbstractElement) root.getPayload();
            }
          }
        } else {
          result = (AbstractElement) this.selectedTopics.get(0).getPayload();
        }
        return result;
      } finally {
        this.unlock();
      }
    }
    return null;
  }

  public AbstractElement findTopicUnderPoint(final Point point) {
    if (this.lockIfNotDisposed()) {
      try {
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
      } finally {
        this.unlock();
      }
    }
    return null;
  }

  public void removeAllSelection() {
    if (this.lockIfNotDisposed()) {
      try {
        if (!this.selectedTopics.isEmpty()) {
          try {
            this.selectedTopics.clear();
            fireNotificationSelectionChanged();
          } finally {
            repaint();
          }
        }
      } finally {
        this.unlock();
      }
    }
  }

  public void focusTo(final Topic theTopic) {
    if (this.lockIfNotDisposed()) {
      try {
        if (theTopic != null) {
          final AbstractElement element = (AbstractElement) theTopic.getPayload();
          if (element instanceof AbstractCollapsableElement) {
            final AbstractCollapsableElement cel = (AbstractCollapsableElement) element;
            if (MindMapUtils.ensureVisibility(cel.getModel())) {
              doLayout();
              revalidate();
              repaint();
              fireNotificationMindMapChanged(false);
            }
          }

          removeAllSelection();

          final int[] path = theTopic.getPositionPath();
          this.select(this.model.findAtPosition(path), false);
        }
      } finally {
        this.unlock();
      }
    }
  }

  public boolean cloneTopic(final Topic topic) {
    return this.cloneTopic(topic, true);
  }

  public boolean cloneTopic(final Topic topic, final boolean cloneSubtree) {
    this.lock();
    try {
      if (topic == null || topic.getTopicLevel() == 0) {
        return false;
      }

      final Topic cloned = this.model.cloneTopicInMap(topic, cloneSubtree);

      if (cloned != null) {
        cloned.moveAfter(topic);
        doLayout();
        revalidate();
        repaint();
        fireNotificationMindMapChanged(true);
      }

      return true;
    } finally {
      this.unlock();
    }
  }

  public MindMapPanelConfig getConfiguration() {
    this.lock();
    try {
      return this.config;
    } finally {
      this.unlock();
    }
  }

  public MindMapPanelController getController() {
    this.lock();
    try {
      return this.controller;
    } finally {
      this.unlock();
    }
  }

  public Topic getFirstSelected() {
    if (this.lockIfNotDisposed()) {
      try {
        return this.selectedTopics.isEmpty() ? null : this.selectedTopics.get(0);
      } finally {
        this.unlock();
      }
    } else {
      return null;
    }
  }

  public boolean isLocked() {
    return this.panelLocker != null && this.panelLocker.isLocked();
  }

  /**
   * Try lock the panel if it is not disposed.
   *
   * @return true if the panel is locked successfully, false if the panel has
   * been disposed.
   */
  public boolean lockIfNotDisposed() {
    boolean result = false;
    if (this.panelLocker != null) {
      this.panelLocker.lock();
      if (this.disposed.get()) {
        this.panelLocker.unlock();
      } else {
        result = true;
      }
    }
    return result;
  }

  /**
   * Lock the panel.
   *
   * @return the panel
   * @throws IllegalStateException it will be thrown if the panel is disposed
   */
  public MindMapPanel lock() {
    if (this.panelLocker != null) {
      this.panelLocker.lock();
      if (this.isDisposed()) {
        this.panelLocker.unlock();
        throw new IllegalStateException("Mind map has been already disposed!");
      }
    }
    return this;
  }

  /**
   * Unlock the panel. it will not throw any exception if the panel is disposed.
   *
   * @throws AssertionError if the panel is locked by another thread
   */
  public void unlock() {
    if (this.panelLocker != null) {
      if (!this.panelLocker.isHeldByCurrentThread()) {
        throw new IllegalStateException("Panel must be held by the current thread");
      }
      this.panelLocker.unlock();
    }
  }

  @Override
  public void lostOwnership(final Clipboard clipboard, final Transferable contents) {

  }

  /**
   * Create transferable topic list in system clipboard.
   *
   * @param cut    true shows that remove topics after placing into clipboard
   * @param topics topics to be placed into clipboard, if there are successors
   *               and ancestors then successors will be removed
   * @return true if topic array is not empty and operation completed
   * successfully, false otherwise
   * @since 1.3.1
   */
  public boolean copyTopicsToClipboard(final boolean cut, final Topic... topics) {
    boolean result = false;

    if (this.lockIfNotDisposed()) {
      try {
        this.endEdit(true);
        if (topics.length > 0) {
          final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
          clipboard.setContents(new MMDTopicsTransferable(topics), this);

          if (cut) {
            deleteTopics(true, ensureNoRootInArray(topics));
          }

          result = true;
        }
      } finally {
        this.unlock();
      }
    }

    return result;
  }

  /**
   * Paste topics from clipboard to currently selected ones.
   *
   * @return true if there detected topic list in clipboard and these topics
   * added to selected ones, false otherwise
   * @since 1.3.1
   */
  public boolean pasteTopicsFromClipboard() {
    boolean result = false;

    if (this.lockIfNotDisposed()) {
      try {
        final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        if (Utils.isDataFlavorAvailable(clipboard, MMDTopicsTransferable.MMD_DATA_FLAVOR)) {
          try {
            final NBMindMapTopicsContainer container = (NBMindMapTopicsContainer) clipboard.getData(MMDTopicsTransferable.MMD_DATA_FLAVOR);
            if (container != null && !container.isEmpty()) {
              this.endEdit(true);

              final Topic[] selected = this.getSelectedTopics();

              for (final Topic s : selected) {
                for (final Topic t : container.getTopics()) {
                  final Topic newTopic = new Topic(this.model, t, true);
                  newTopic.removeExtra(Extra.ExtraType.TOPIC);
                  newTopic.moveToNewParent(s);
                  MindMapUtils.ensureVisibility(newTopic);
                }
              }

              doLayout();
              revalidate();
              repaint();

              fireNotificationMindMapChanged(true);
              result = true;
            }
          } catch (final Exception ex) {
            LOGGER.error("Can't get clipboard data", ex);
          }
        } else if (Utils.isDataFlavorAvailable(clipboard, DataFlavor.stringFlavor)) {
          try {
            final int MAX_TEXT_LEN = 96;
            String clipboardText = (String) clipboard.getContents(null).getTransferData(DataFlavor.stringFlavor);

            if (clipboardText != null) {
              if (this.textEditor != null) {
                this.textEditor.insert(clipboardText, this.textEditor.getCaretPosition());
              } else {
                if (this.getConfiguration().isSmartTextPaste()) {
                  for (final Topic t : this.getSelectedTopics()) {
                    MindMapUtils.makeSubTreeFromText(t, clipboardText);
                  }
                } else {
                  clipboardText = clipboardText.trim();

                  final String topicText;
                  final String extraNoteText;

                  if (clipboardText.length() > MAX_TEXT_LEN) {
                    topicText = clipboardText.substring(0, MAX_TEXT_LEN) + "...";
                    extraNoteText = clipboardText;
                  } else {
                    topicText = clipboardText;
                    extraNoteText = null;
                  }

                  final Topic[] selectedTopics = this.getSelectedTopics();

                  for (final Topic s : selectedTopics) {
                    final Topic newTopic;
                    if (extraNoteText == null) {
                      newTopic = new Topic(this.model, s, topicText);
                    } else {
                      newTopic =
                          new Topic(this.model, s, topicText, new ExtraNote(extraNoteText));
                    }
                    MindMapUtils.ensureVisibility(newTopic);
                  }
                }
              }
              doLayout();
              revalidate();
              repaint();

              fireNotificationMindMapChanged(true);
              result = true;
            }
          } catch (Exception ex) {
            LOGGER.error("Can't get clipboard text", ex);
          }
        }
      } finally {
        this.unlock();
      }
    }
    return result;
  }

  public boolean isDisposed() {
    return this.disposed.get();
  }

  public void dispose() {
    if (this.lockIfNotDisposed()) {
      try {
        if (this.disposed.compareAndSet(false, true)) {
          this.selectedTopics.clear();
          this.mindMapListeners.clear();

          for (final PanelAwarePlugin p : MindMapPluginRegistry.getInstance().findFor(PanelAwarePlugin.class)) {
            p.onPanelDispose(this);
          }
        }
      } finally {
        this.unlock();
      }
    }
  }

  public void doNotifyModelChanged(final boolean addToHistory) {
    this.revalidate();
    this.fireNotificationMindMapChanged(addToHistory);
  }

  /**
   * Some Job over mind map model.
   *
   * @see MindMapPanel#executeModelJobs(com.igormaznitsa.mindmap.swing.panel.MindMapPanel.ModelJob...)
   * @since 1.3.1
   */
  public interface ModelJob {

    /**
     * Execute the job.
     *
     * @param model model to be processed
     * @return true if to continue job sequence, false if to interrupt
     */
    boolean doChangeModel(MindMap model);
  }

  public static class DraggedElement {

    private final AbstractElement element;
    private final Image prerenderedImage;
    private final Point mousePointerOffset;
    private final Point currentPosition;
    private final DraggedElement.Modifier modifier;

    public DraggedElement(final AbstractElement element, final MindMapPanelConfig cfg,
                          final Point mousePointerOffset, final DraggedElement.Modifier modifier,
                          final RenderQuality quality) {
      this.element = element;
      this.prerenderedImage = Utils.renderWithTransparency(0.55f, element, cfg, quality);
      this.mousePointerOffset = mousePointerOffset;
      this.currentPosition = new Point();
      this.modifier = modifier;
    }

    public DraggedElement.Modifier getModifier() {
      return this.modifier;
    }

    public boolean isPositionInside() {
      return this.element.getBounds().contains(this.currentPosition);
    }

    public AbstractElement getElement() {
      return this.element;
    }

    public void updatePosition(final Point point) {
      this.currentPosition.setLocation(point);
    }

    public Point getPosition() {
      return this.currentPosition;
    }

    public Point getMousePointerOffset() {
      return this.mousePointerOffset;
    }

    public int getDrawPositionX() {
      return this.currentPosition.x - this.mousePointerOffset.x;
    }

    public int getDrawPositionY() {
      return this.currentPosition.y - this.mousePointerOffset.y;
    }

    public Image getImage() {
      return this.prerenderedImage;
    }

    public void draw(final Graphics2D gfx) {
      final int x = getDrawPositionX();
      final int y = getDrawPositionY();
      gfx.drawImage(this.prerenderedImage, x, y, null);
    }

    public enum Modifier {

      NONE,
      MAKE_JUMP
    }
  }

}
