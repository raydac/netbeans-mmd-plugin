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

import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.swing.panel.ui.AbstractElement;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.function.Consumer;
import javax.swing.JViewport;

public class InMapBirdsEye implements BirdsEyeVisualizer {
  private final Rectangle2D page;
  private final Rectangle2D view;
  private final double scale;
  private final MindMapPanel panel;

  public InMapBirdsEye(final MindMapPanel panel) {
    this.panel = panel;

    final Rectangle viewRectangle;

    if (panel.getParent() instanceof JViewport) {
      viewRectangle = ((JViewport) panel.getParent()).getViewRect();
    } else {
      viewRectangle = new Rectangle(0, 0, panel.getWidth(), panel.getHeight());
    }

    final Dimension docSize = panel.getSize();

    final Dimension eyeBirdAreaSize =
        new Dimension(viewRectangle.width / 3, viewRectangle.height / 3);

    final double scaleW = (double) eyeBirdAreaSize.width / (double) docSize.width;
    final double scaleH = (double) eyeBirdAreaSize.height / (double) docSize.height;
    this.scale = Math.min(scaleW, scaleH);

    docSize.setSize(docSize.width * this.scale, docSize.height * this.scale);

    final int x = viewRectangle.x + (viewRectangle.width - docSize.width) / 2;
    final int y = viewRectangle.y + (viewRectangle.height - docSize.height) / 2;

    this.page = new Rectangle2D.Double(x, y, docSize.width, docSize.height);
    this.view =
        new Rectangle2D.Double(x + viewRectangle.x * scale, y + viewRectangle.y * this.scale,
            viewRectangle.width * this.scale, viewRectangle.height * this.scale);
  }

  @Override
  public void draw(final MindMapPanel panel, final Graphics2D panelGraphics) {
    panelGraphics.setColor(Color.WHITE);

    final Color back = this.panel.getConfiguration().getBirdseyeBackground();

    final Color front = this.panel.getConfiguration().getBirdseyeFront();

    panelGraphics.setStroke(new BasicStroke(1.0f));

    if (this.panel.getConfiguration().isDropShadow()) {
      panelGraphics.setColor(this.panel.getConfiguration().getShadowColor());
      panelGraphics.fill(
          new Rectangle2D.Double(this.page.getX() + 16, this.page.getY() + 16, this.page.getWidth(),
              this.page.getHeight()));
    }

    panelGraphics.setColor(back);
    panelGraphics.fill(page);

    panelGraphics.setColor(front);

    this.drawTopicsTree(this.panel.getModel().getRoot(), panelGraphics);

    panelGraphics.setColor(back);
    panelGraphics.fill(view);
    panelGraphics.setColor(front);
    panelGraphics.draw(view);
  }

  private boolean isMouseOverPageThumbnail(final MouseEvent mouseEvent) {
    return this.page.contains(mouseEvent.getX(), mouseEvent.getY());
  }

  @Override
  public void onPanelMouseDragging(final MindMapPanel panel, final MouseEvent mouseEvent,
                                   final Consumer<Rectangle2D> calculatedRectangleConsumer) {
    if (calculatedRectangleConsumer != null && this.isMouseOverPageThumbnail(mouseEvent)) {
      double dx = Math.max(0.0d, (mouseEvent.getX() - this.page.getX()) - this.view.getWidth() / 2);
      double dy =
          Math.max(0.0d, (mouseEvent.getY() - this.page.getY()) - this.view.getHeight() / 2);

      if (dx + this.view.getWidth() > this.page.getWidth()) {
        dx = this.page.getWidth() - this.view.getWidth();
      }

      if (dy + this.view.getHeight() > this.page.getHeight()) {
        dy = this.page.getHeight() - this.view.getHeight();
      }

      calculatedRectangleConsumer.accept(new Rectangle2D.Double(dx / this.scale, dy / this.scale,
          this.view.getWidth() / this.scale, this.view.getHeight() / this.scale));
    }
  }


  private void drawTopicsTree(final Topic topic, final Graphics2D graphics2D) {
    if (topic != null) {
      final AbstractElement abstractElement = (AbstractElement) topic.getPayload();
      final Rectangle2D rectangle = findScaledBoundsOnPage(abstractElement);
      if (rectangle != null) {
        graphics2D.fill(rectangle);
        if (abstractElement.isCollapsed()) {
          return;
        }
        topic.getChildren().forEach(t -> this.drawTopicsTree(t, graphics2D));
      }
    }
  }

  private Rectangle2D findScaledBoundsOnPage(final AbstractElement abstractElement) {
    Rectangle2D result = null;
    if (abstractElement != null) {
      final Rectangle2D bounds = abstractElement.getBounds();
      result = new Rectangle2D.Double(this.page.getX() + bounds.getX() * this.scale,
          this.page.getY() + bounds.getY() * this.scale, bounds.getWidth() * this.scale,
          bounds.getHeight() * this.scale);
    }
    return result;
  }

}
