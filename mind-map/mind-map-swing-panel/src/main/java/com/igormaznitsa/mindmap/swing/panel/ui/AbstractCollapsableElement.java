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

package com.igormaznitsa.mindmap.swing.panel.ui;

import static com.igormaznitsa.meta.common.utils.Assertions.assertNotNull;


import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.mindmap.swing.panel.StandardTopicAttribute;
import com.igormaznitsa.mindmap.swing.panel.ui.gfx.MMGraphics;
import com.igormaznitsa.mindmap.swing.panel.ui.gfx.StrokeType;
import com.igormaznitsa.mindmap.swing.panel.utils.MindMapUtils;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Dimension2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class AbstractCollapsableElement extends AbstractElement {

  protected final Rectangle2D collapsatorZone = new Rectangle2D.Double();

  protected AbstractCollapsableElement(@Nonnull final AbstractCollapsableElement element) {
    super(element);
    this.collapsatorZone.setRect(element.collapsatorZone);
  }

  public AbstractCollapsableElement(@Nonnull final Topic model) {
    super(model);
  }

  public static boolean isLeftSidedTopic(@Nonnull final Topic t) {
    return "true".equals(t.getAttribute(StandardTopicAttribute.ATTR_LEFTSIDE.getText()));
  }

  public static void makeTopicLeftSided(@Nonnull final Topic topic, final boolean left) {
    if (left) {
      topic.setAttribute(StandardTopicAttribute.ATTR_LEFTSIDE.getText(), "true");//NOI18N
    } else {
      topic.setAttribute(StandardTopicAttribute.ATTR_LEFTSIDE.getText(), null);
    }
  }

  protected void drawCollapsator(@Nonnull final MMGraphics g, @Nonnull final MindMapPanelConfig cfg, final boolean collapsed) {
    final int x = (int) Math.round(collapsatorZone.getX());
    final int y = (int) Math.round(collapsatorZone.getY());
    final int w = (int) Math.round(collapsatorZone.getWidth());
    final int h = (int) Math.round(collapsatorZone.getHeight());

    final int DELTA = (int) Math.round(cfg.getCollapsatorSize() * 0.3d * cfg.getScale());

    g.setStroke(cfg.safeScaleFloatValue(cfg.getCollapsatorBorderWidth(), 0.1f), StrokeType.SOLID);

    final Color linecolor = cfg.getCollapsatorBorderColor();

    g.drawOval(x, y, w, h, linecolor, cfg.getCollapsatorBackgroundColor());

    g.drawLine(x + DELTA, y + h / 2, x + w - DELTA, y + h / 2, linecolor);
    if (collapsed) {
      g.drawLine(x + w / 2, y + DELTA, x + w / 2, y + h - DELTA, linecolor);
    }
  }

  @Override
  @Nullable
  public ElementPart findPartForPoint(@Nonnull final Point point) {
    ElementPart result = super.findPartForPoint(point);
    if (result == ElementPart.NONE) {
      if (this.hasChildren()) {
        if (this.collapsatorZone.contains(point.getX() - this.bounds.getX(), point.getY() - this.bounds.getY())) {
          result = ElementPart.COLLAPSATOR;
        }
      }
    }
    return result;
  }

  @Override
  public boolean isCollapsed() {
    return MindMapUtils.isCollapsed(this.model);
  }

  public void setCollapse(final boolean collapseElementFlag) {
    MindMapUtils.setCollapsed(this.model, collapseElementFlag);
  }

  public void foldAllChildren() {

  }

  public void collapseAllFirstLevelChildren() {
    for (final Topic t : this.model.getChildren()) {
      MindMapUtils.setCollapsed(t, true);
    }
  }

  @Override
  public boolean isLeftDirection() {
    return isLeftSidedTopic(this.model);
  }

  public void setLeftDirection(final boolean leftSide) {
    makeTopicLeftSided(this.model, leftSide);
  }

  @Override
  @Nonnull
  public Dimension2D calcBlockSize(@Nonnull final MindMapPanelConfig cfg, @Nullable final Dimension2D size, final boolean childrenOnly) {
    final Dimension2D result = size == null ? new Dimension() : size;

    final double scaledVInset = cfg.getScale() * cfg.getOtherLevelVerticalInset();
    final double scaledHInset = cfg.getScale() * cfg.getOtherLevelHorizontalInset();

    double width = childrenOnly ? 0.0d : this.bounds.getWidth();
    double height = childrenOnly ? 0.0d : this.bounds.getHeight();

    if (this.hasChildren()) {
      if (!this.isCollapsed()) {
        width += childrenOnly ? 0.0d : scaledHInset;

        final double baseWidth = childrenOnly ? 0.0d : width;
        double childrenHeight = 0.0d;

        boolean notFirstChiild = false;

        for (final Topic t : this.model.getChildren()) {
          if (notFirstChiild) {
            childrenHeight += scaledVInset;
          } else {
            notFirstChiild = true;
          }
          ((AbstractElement) assertNotNull(t.getPayload())).calcBlockSize(cfg, result, false);
          width = Math.max(baseWidth + result.getWidth(), width);
          childrenHeight += result.getHeight();
        }

        height = Math.max(height, childrenHeight);
      } else if (!childrenOnly) {
        width += cfg.getCollapsatorSize() * cfg.getScale();
      }
    }
    result.setSize(width, height);

    return result;
  }

  @Override
  public void alignElementAndChildren(@Nonnull final MindMapPanelConfig cfg, final boolean leftSide, final double leftX, final double topY) {
    super.alignElementAndChildren(cfg, leftSide, leftX, topY);

    final double horzInset = cfg.getOtherLevelHorizontalInset() * cfg.getScale();

    double childrenX;

    final double COLLAPSATORSIZE = cfg.getCollapsatorSize() * cfg.getScale();
    final double COLLAPSATORDISTANCE = cfg.getCollapsatorSize() * 0.1d * cfg.getScale();

    final double collapsatorX;

    if (leftSide) {
      childrenX = leftX + this.blockSize.getWidth() - this.bounds.getWidth();
      this.moveTo(childrenX, topY + (this.blockSize.getHeight() - this.bounds.getHeight()) / 2);
      childrenX -= horzInset;
      collapsatorX = -COLLAPSATORSIZE - COLLAPSATORDISTANCE;
    } else {
      childrenX = leftX;
      this.moveTo(childrenX, topY + (this.blockSize.getHeight() - this.bounds.getHeight()) / 2);
      childrenX += this.bounds.getWidth() + horzInset;
      collapsatorX = this.bounds.getWidth() + COLLAPSATORDISTANCE;
    }

    this.collapsatorZone.setRect(collapsatorX, (this.bounds.getHeight() - COLLAPSATORSIZE) / 2, COLLAPSATORSIZE, COLLAPSATORSIZE);

    if (!this.isCollapsed()) {
      final double vertInset = cfg.getOtherLevelVerticalInset() * cfg.getScale();

      final Dimension2D childBlockSize = calcBlockSize(cfg, null, true);
      double currentY = topY + (this.blockSize.getHeight() - childBlockSize.getHeight()) / 2.0d;

      boolean notFirstChild = false;

      for (final Topic t : this.model.getChildren()) {
        if (notFirstChild) {
          currentY += vertInset;
        } else {
          notFirstChild = true;
        }
        final AbstractElement w = (AbstractElement) assertNotNull(t.getPayload());
        w.alignElementAndChildren(cfg, leftSide, leftSide ? childrenX - w.getBlockSize().getWidth() : childrenX, currentY);
        currentY += w.getBlockSize().getHeight();
      }
    }
  }

  @Override
  public void doPaintConnectors(@Nonnull final MMGraphics g, final boolean leftDirection, @Nonnull final MindMapPanelConfig cfg) {
    final Rectangle2D source = new Rectangle2D.Double(this.bounds.getX() + this.collapsatorZone.getX(), this.bounds.getY() + this.collapsatorZone.getY(), this.collapsatorZone.getWidth(), this.collapsatorZone.getHeight());
    final boolean lefDir = isLeftDirection();
    for (final Topic t : this.model.getChildren()) {
      this.drawConnector(g, source, (assertNotNull((AbstractElement) t.getPayload())).getBounds(), lefDir, cfg);
    }
  }

  @Override
  public void drawConnector(@Nonnull final MMGraphics g, @Nonnull final Rectangle2D source, @Nonnull final Rectangle2D destination, final boolean leftDirection, @Nonnull final MindMapPanelConfig cfg) {
    g.setStroke(cfg.safeScaleFloatValue(cfg.getConnectorWidth(), 0.1f), StrokeType.SOLID);

    final double dy = Math.abs(destination.getCenterY() - source.getCenterY());
    if (dy < (16.0d * cfg.getScale())) {
      g.drawLine((int) source.getCenterX(), (int) source.getCenterY(), (int) destination.getCenterX(), (int) source.getCenterY(), cfg.getConnectorColor());
    } else {
      final Path2D path = new Path2D.Double();
      path.moveTo(source.getCenterX(), source.getCenterY());

      if (leftDirection) {
        final double dx = source.getCenterX() - destination.getMaxX();
        path.lineTo((source.getCenterX() - dx / 2), source.getCenterY());
        path.lineTo((source.getCenterX() - dx / 2), destination.getCenterY());
      } else {
        final double dx = destination.getX() - source.getCenterX();
        path.lineTo((source.getCenterX() + dx / 2), source.getCenterY());
        path.lineTo((source.getCenterX() + dx / 2), destination.getCenterY());
      }
      path.lineTo(destination.getCenterX(), destination.getCenterY());

      g.draw(path, cfg.getConnectorColor(), null);
    }
  }

  @Override
  @Nullable
  public AbstractElement findForPoint(@Nullable final Point point) {
    AbstractElement result = null;
    if (point != null) {
      if (this.bounds.contains(point.getX(), point.getY()) || this.collapsatorZone.contains(point.getX() - this.bounds.getX(), point.getY() - this.bounds.getY())) {
        result = this;
      } else if (!isCollapsed()) {
        final double topZoneY = this.bounds.getY() - (this.blockSize.getHeight() - this.bounds.getHeight()) / 2;
        final double topZoneX = isLeftDirection() ? this.bounds.getMaxX() - this.blockSize.getWidth() : this.bounds.getX();

        if (point.getX() >= topZoneX && point.getY() >= topZoneY && point.getX() < (this.blockSize.getWidth() + topZoneX) && point.getY() < (this.blockSize.getHeight() + topZoneY)) {
          for (final Topic t : this.model.getChildren()) {
            final AbstractElement w = (AbstractElement) t.getPayload();
            result = w == null ? null : w.findForPoint(point);
            if (result != null) {
              break;
            }
          }
        }
      }
    }
    return result;
  }

  @Override
  public void updateElementBounds(@Nonnull final MMGraphics gfx, @Nonnull final MindMapPanelConfig cfg) {
    super.updateElementBounds(gfx, cfg);
    final double marginOffset = ((cfg.getTextMargins() + cfg.getElementBorderWidth()) * 2.0d) * cfg.getScale();
    this.bounds.setRect(this.bounds.getX(), this.bounds.getY(), this.bounds.getWidth() + marginOffset, this.bounds.getHeight() + marginOffset);
  }

  @Nonnull
  public Rectangle2D getCollapsatorArea() {
    return this.collapsatorZone;
  }

  @Override
  public boolean hasDirection() {
    return true;
  }

}
