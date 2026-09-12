/*
 * Copyright 2015-2018 Igor Maznitsa.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.igormaznitsa.mindmap.model.MindMap;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import javax.swing.SwingUtilities;
import org.junit.Test;

public class MindMapPanelTest {

  @Test
  public void testCreatePanel() {
    final MindMapPanelConfig config = new MindMapPanelConfig();

    final MindMapPanelController controller = mock(MindMapPanelController.class);
    when(controller.provideConfigForMindMapPanel(any(MindMapPanel.class))).thenReturn(config);

    final MindMapPanel panel = new MindMapPanel(controller);

    assertNotSame(config, panel.getConfiguration());
  }

  @Test
  public void testCalculateViewportPositionToCenter_centersTopic() {
    final Point position = MindMapPanel.calculateViewportPositionToCenter(
        new Rectangle2D.Double(400.0d, 300.0d, 100.0d, 50.0d),
        new Dimension(200, 100),
        new Dimension(800, 600));

    assertEquals(new Point(350, 275), position);
  }

  @Test
  public void testCalculateViewportPositionToCenter_clampsToView() {
    final Point position = MindMapPanel.calculateViewportPositionToCenter(
        new Rectangle2D.Double(10.0d, 10.0d, 20.0d, 20.0d),
        new Dimension(200, 100),
        new Dimension(200, 100));

    assertEquals(new Point(0, 0), position);
  }

  @Test
  public void testExpandToAtLeastPaper_growsToViewport() {
    assertEquals(new Dimension(400, 300),
        MindMapPanel.expandToAtLeastPaper(new Dimension(100, 50), new Dimension(400, 300)));
  }

  @Test
  public void testExpandToAtLeastPaper_keepsLargerDiagram() {
    assertEquals(new Dimension(800, 600),
        MindMapPanel.expandToAtLeastPaper(new Dimension(800, 600), new Dimension(400, 300)));
  }

  @Test
  public void testCenterRootInViewport_withoutViewportReturnsFalse() throws Exception {
    final MindMapPanelConfig config = new MindMapPanelConfig();
    final MindMapPanelController controller = mock(MindMapPanelController.class);
    when(controller.provideConfigForMindMapPanel(any(MindMapPanel.class))).thenReturn(config);

    SwingUtilities.invokeAndWait(() -> {
      final MindMapPanel panel = new MindMapPanel(controller);
      panel.setModel(new MindMap(true));
      assertFalse(panel.centerRootInViewport());
    });
  }

}
