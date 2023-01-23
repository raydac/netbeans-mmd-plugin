/*
 * Copyright (C) 2015-2023 Igor A. Maznitsa
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

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.function.Consumer;

/**
 * Interface describes bird's eye visualizer for {@link MindMapPanel}.
 *
 * @see MindMapPanel#findBirdEyeVisualizer()
 * @since 1.6.2
 */
public interface BirdsEyeVisualizer {
  /**
   * Called in the end of draw chain of mind map panel.
   *
   * @param panel         source panel, must not be null
   * @param panelGraphics panel graphics, must not be null
   */
  void draw(MindMapPanel panel, Graphics2D panelGraphics);

  /**
   * Called during panel mouse dragging in bird eye mode on
   *
   * @param panel                       source panel, must not be null
   * @param mouseEvent                  mouse event, must not be null
   * @param calculatedRectangleConsumer consumer getting rectangle to scroll panel, can't be null
   */
  void onPanelMouseDragging(MindMapPanel panel, MouseEvent mouseEvent,
                            Consumer<Rectangle2D> calculatedRectangleConsumer);
}
