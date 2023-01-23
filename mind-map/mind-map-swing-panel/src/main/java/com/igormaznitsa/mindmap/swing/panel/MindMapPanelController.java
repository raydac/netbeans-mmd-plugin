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

import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.plugins.api.PluginContext;
import com.igormaznitsa.mindmap.swing.panel.ui.AbstractElement;
import com.igormaznitsa.mindmap.swing.panel.ui.ElementPart;
import java.awt.Point;
import javax.swing.JPopupMenu;

/**
 * Interface describes way to enable or disable some features of MindMapPanel and prepare some visual elements.
 *
 * @see MindMapPanel
 */
public interface MindMapPanelController {

  /**
   * Check that folded topic should be unfold after drop operation.
   *
   * @param source source mind map panel must not be null
   * @return true if should be unfold, false otherwise
   */
  boolean isUnfoldCollapsedTopicDropTarget(MindMapPanel source);

  /**
   * New created child topics should use color values of parent.
   *
   * @param source source mind map panel must not be null
   * @return true if new child topics should use parent color scheme, false otherwise
   */
  boolean isCopyColorInfoFromParentToNewChildAllowed(MindMapPanel source);

  /**
   * Entered topic text should be trimmed before set to its topic.
   *
   * @param source source mind map panel must not be null
   * @return true if texxt should be trimmed, false otherwise
   */
  boolean isTrimTopicTextBeforeSet(MindMapPanel source);

  /**
   * Enable select topics in the map.
   *
   * @param source source mind map panel must not be null
   * @return true if topic selection allowed, false otherwise
   */
  boolean isSelectionAllowed(MindMapPanel source);

  /**
   * Enable topic dragging.
   *
   * @param source source mind map panel must not be null
   * @return true if mind map panek should allow element dragging, false otherwise
   */
  boolean isElementDragAllowed(MindMapPanel source);

  /**
   * Enable observing of mouse move over mind map panel for instance to show tooltips and change cursor.
   *
   * @param source source mind map panel must not be null
   * @return true if mouse movements over panel should be observable or false otherwise
   */
  boolean isMouseMoveProcessingAllowed(MindMapPanel source);

  /**
   * Allow mouse wheel processing by mind map panel for instance for scale.
   *
   * @param source source mind map panel must not be null
   * @return true if mouse wheel processing allowed, false otherwise.
   */
  boolean isMouseWheelProcessingAllowed(MindMapPanel source);

  /**
   * Allows processing of mouse clicks by mind map panel.
   *
   * @param source source mind map panel must not be null
   * @return true if mouse click processing allowed, false otherwise
   */
  boolean isMouseClickProcessingAllowed(MindMapPanel source);

  /**
   * Check that topic allowed to be removed from mind map shown by mind map panel.
   *
   * @param source source mind map panel must not be null
   * @param topic  topic to be prepared for removing, must not be null.
   * @return true if topic allowed for remove, false otherwise
   */
  boolean canTopicBeDeleted(MindMapPanel source, Topic topic);

  /**
   * Prepare plugin context for mind map panel.
   *
   * @param source source mind map panel must not be null
   * @return prepared plugin context, must not be null
   * @see PluginContext
   * @see com.igormaznitsa.mindmap.plugins.api.MindMapPlugin
   */
  PluginContext makePluginContext(MindMapPanel source);

  /**
   * Provide configuration for mind map panel.
   *
   * @param source source mind map panel must not be null
   * @return prepared configuration, must not be null
   */
  MindMapPanelConfig provideConfigForMindMapPanel(MindMapPanel source);

  /**
   * Prepare popup ,emu component to be shown on mind map panel in specified point.
   *
   * @param source      source mind map panel must not be null
   * @param point       point on mind map panel, must not be null
   * @param element     element in the point, can be null
   * @param elementPart element part in the point, can be null
   * @return prepared popup menu component, can be null
   */
  JPopupMenu makePopUpForMindMapPanel(
      MindMapPanel source,
      Point point,
      AbstractElement element,
      ElementPart elementPart
  );

  /**
   * Dialog provider to be used by dialogs of mind map panel.
   *
   * @param source source mind map panel must not be null
   * @return dialog provider object, must not be null
   */
  DialogProvider getDialogProvider(MindMapPanel source);

  /**
   * Process drop operation of one topic to another topic.
   *
   * @param source           source mind map panel must not be null
   * @param dropPoint        drop point of topic, must not be null
   * @param draggedTopic     topic to be dropped, must not be null
   * @param destinationTopic destination topic, must not be null
   * @return true if drop successfully completed, false otherwise
   */
  boolean processDropTopicToAnotherTopic(MindMapPanel source, Point dropPoint, Topic draggedTopic,
                                         Topic destinationTopic);

  /**
   * Is Bird's eye view allowed for mind map view.
   *
   * @return true if it is allowed, false otherwise
   * @since 1.6.2
   */
  default boolean isBirdsEyeAllowed() {
    return false;
  }
}
