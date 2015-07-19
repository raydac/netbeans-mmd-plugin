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
package com.igormaznitsa.nbmindmap.gui.mmview;

import com.igormaznitsa.nbmindmap.model.MindMapTopic;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.geom.Dimension2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

public abstract class AbstractCollapsableElement extends AbstractElement {

  protected final Rectangle2D collapsatorZone = new Rectangle2D.Double();

  public AbstractCollapsableElement(final MindMapTopic model) {
    super(model);
  }

  protected void drawCollapsator(final Graphics2D g, final Configuration cfg, final boolean collapsed) {
    final int x = (int) Math.round(collapsatorZone.getX());
    final int y = (int) Math.round(collapsatorZone.getY());
    final int w = (int) Math.round(collapsatorZone.getWidth());
    final int h = (int) Math.round(collapsatorZone.getHeight());

    final int DELTA = (int) Math.round(cfg.getCollapsatorSize() * 0.3d * cfg.getScale());

    g.setStroke(new BasicStroke(cfg.getCollapsatorBorderWidth() * cfg.getScale()));
    g.setColor(cfg.getCollapsatorBackgroundColor());
    g.fillOval(x, y, w, h);
    g.setColor(cfg.getCollapsatorBorderColor());
    g.drawOval(x, y, w, h);
    g.drawLine(x + DELTA, y + h / 2, x + w - DELTA, y + h / 2);
    if (collapsed) {
      g.drawLine(x + w / 2, y + DELTA, x + w / 2, y + h - DELTA);
    }
  }

  @Override
  public ElementPart findPartForPoint(final Point point) {
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
    return "true".equals(this.model.getAttribute("collapsed"));
  }

  public void setCollapse(final boolean flag) {
    this.model.setAttribute("collapsed", flag ? "true" : null);
  }

  @Override
  public boolean isLeftDirection() {
    return "true".equals(this.model.getAttribute("leftSide"));
  }

  public void setLeftDirection(final boolean leftSide) {
    this.model.setAttribute("leftSide", leftSide ? "true" : null);
  }

  @Override
  public Dimension2D calcBlockSize(final Configuration cfg, final Dimension2D size) {
    final Dimension2D result = size == null ? new Dimension() : size;

    final float scaledVInset = cfg.getScale() * cfg.getOtherLevelVerticalInset();
    final float scaledHInset = cfg.getScale() * cfg.getOtherLevelHorizontalInset();

    double width = this.bounds.getWidth();
    double height = this.bounds.getHeight();

    if (this.hasChildren()) {
      if (!this.isCollapsed()) {
        width += scaledHInset;

        final double baseWidth = width;
        double childrenHeight = 0.0d;

        boolean notFirst = false;

        for (final MindMapTopic t : this.model.getChildren()) {
          if (notFirst) {
            childrenHeight += scaledVInset;
          }
          else {
            notFirst = true;
          }
          ((AbstractElement) t.getPayload()).calcBlockSize(cfg, result);
          width = Math.max(baseWidth + result.getWidth(), width);
          childrenHeight += result.getHeight();
        }

        height = Math.max(height, childrenHeight);
      }
      else {
        width += cfg.getCollapsatorSize() * cfg.getScale();
      }
    }
    result.setSize(width, height);

    return result;
  }

  @Override
  public void alignElementAndChildren(final Configuration cfg, final boolean leftSide, final double leftX, final double topY) {

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
    }
    else {
      childrenX = leftX;
      this.moveTo(childrenX, topY + (this.blockSize.getHeight() - this.bounds.getHeight()) / 2);
      childrenX += this.bounds.getWidth() + horzInset;
      collapsatorX = this.bounds.getWidth() + COLLAPSATORDISTANCE;
    }

    final int textMargin = Math.round(cfg.getScale() * cfg.getTextMargins());
    this.textBlock.setCoordOffset(textMargin, textMargin);

    this.collapsatorZone.setRect(collapsatorX, (this.bounds.getHeight() - COLLAPSATORSIZE) / 2, COLLAPSATORSIZE, COLLAPSATORSIZE);

    if (!this.isCollapsed()) {
      final double vertInset = cfg.getOtherLevelVerticalInset() * cfg.getScale();

      double currentY = topY;

      boolean notFirst = false;

      for (final MindMapTopic t : this.model.getChildren()) {
        if (notFirst) {
          currentY += vertInset;
        }
        else {
          notFirst = true;
        }
        final AbstractElement w = (AbstractElement) t.getPayload();
        w.alignElementAndChildren(cfg, leftSide, leftSide ? childrenX - w.getBlockSize().getWidth() : childrenX, currentY);
        currentY += w.getBlockSize().getHeight();
      }
    }
  }

  @Override
  public void doPaintConnectors(final Graphics2D g, final boolean leftDirection, final Configuration cfg) {
    final Rectangle2D source = new Rectangle2D.Double(this.bounds.getX() + this.collapsatorZone.getX(), this.bounds.getY() + this.collapsatorZone.getY(), this.collapsatorZone.getWidth(), this.collapsatorZone.getHeight());
    final boolean lefDir = isLeftDirection();
    for (final MindMapTopic t : this.model.getChildren()) {
      this.drawConnector(g, source, ((AbstractElement) t.getPayload()).getBounds(), lefDir, cfg);
    }
  }

  @Override
  public void drawConnector(final Graphics2D g, final Rectangle2D source, final Rectangle2D destination, final boolean leftDirection, final Configuration cfg) {
    g.setStroke(new BasicStroke(cfg.getConnectorWidth() * cfg.getScale()));
    g.setColor(cfg.getConnectorColor());

    final double dy = Math.abs(destination.getCenterY() - source.getCenterY());
    if (dy < (16d * cfg.getScale())) {
      g.drawLine((int) source.getCenterX(), (int) source.getCenterY(), (int) destination.getCenterX(), (int) destination.getCenterY());
    }
    else {
      final Path2D path = new Path2D.Double();
      path.moveTo(source.getCenterX(), source.getCenterY());

      if (leftDirection) {
        final double dx = source.getCenterX() - destination.getMaxX();
        path.lineTo((source.getCenterX() - dx / 2), source.getCenterY());
        path.lineTo((source.getCenterX() - dx / 2), destination.getCenterY());
        path.lineTo(destination.getCenterX(), destination.getCenterY());
      }
      else {
        final double dx = destination.getX() - source.getCenterX();
        path.lineTo((source.getCenterX() + dx / 2), source.getCenterY());
        path.lineTo((source.getCenterX() + dx / 2), destination.getCenterY());
        path.lineTo(destination.getCenterX(), destination.getCenterY());
      }

      g.draw(path);
    }
  }

  @Override
  public AbstractElement findForPoint(final Point point) {
    AbstractElement result = null;
    if (point != null) {
      if (this.bounds.contains(point.getX(), point.getY()) || this.collapsatorZone.contains(point.getX() - this.bounds.getX(), point.getY() - this.bounds.getY())) {
        result = this;
      }
      else {
        if (!isCollapsed()) {
          final double topZoneY = this.bounds.getY() - (this.blockSize.getHeight() - this.bounds.getHeight()) / 2;
          final double topZoneX = isLeftDirection() ? this.bounds.getMaxX() - this.blockSize.getWidth() : this.bounds.getX();

          if (point.getX() >= topZoneX && point.getY() >= topZoneY && point.getX() < (this.blockSize.getWidth() + topZoneX) && point.getY() < (this.blockSize.getHeight() + topZoneY)) {
            for (final MindMapTopic t : this.model.getChildren()) {
              final AbstractElement w = (AbstractElement) t.getPayload();
              result = w == null ? null : w.findForPoint(point);
              if (result != null) {
                break;
              }
            }
          }
        }
      }
    }
    return result;
  }

  @Override
  public void updateElementBounds(final Graphics2D gfx, final Configuration cfg) {
    super.updateElementBounds(gfx, cfg);
    final float marginOffset = (cfg.getTextMargins() << 1) * cfg.getScale();
    this.bounds.setRect(this.bounds.getX(), this.bounds.getY(), this.bounds.getWidth() + marginOffset, this.bounds.getHeight() + marginOffset);
  }

  public Rectangle2D getCollapsatorArea() {
    return this.collapsatorZone;
  }

  @Override
  public boolean hasDirection() {
    return true;
  }

}
