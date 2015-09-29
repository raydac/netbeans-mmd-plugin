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
package com.igormaznitsa.mindmap.swing.panel.ui;

import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.mindmap.model.Topic;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Dimension2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public final class ElementRoot extends AbstractElement {

  private final Dimension2D leftBlockSize = new Dimension();
  private final Dimension2D rightBlockSize = new Dimension();

  public ElementRoot(final Topic topic) {
    super(topic);
  }

  protected ElementRoot(final ElementRoot element) {
    super(element);
    this.leftBlockSize.setSize(element.leftBlockSize);
    this.rightBlockSize.setSize(element.rightBlockSize);
  }

  @Override
  public AbstractElement makeCopy() {
    return new ElementRoot(this);
  }
  
  
  @Override
  public boolean isMoveable() {
    return false;
  }

  @Override
  public boolean isCollapsed() {
    return false;
  }

  private Shape makeShape(final MindMapPanelConfig cfg, final float x, final float y) {
    final float round = cfg.safeScaleFloatValue(10.0f, 0.1f);
    final float border = cfg.safeScaleFloatValue(cfg.getElementBorderWidth(), 0.5f);
    return new RoundRectangle2D.Float(x, y, (float) this.bounds.getWidth()-border, (float) this.bounds.getHeight()-border, round, round);
  }

  @Override
  public void drawComponent(final Graphics2D g, final MindMapPanelConfig cfg) {
    g.setStroke(new BasicStroke(cfg.safeScaleFloatValue(cfg.getElementBorderWidth(),0.1f)));

    final Shape shape = makeShape(cfg, 0f, 0f);

    if (cfg.isDropShadow()) {
      g.setColor(cfg.getShadowColor());
      final float offset = cfg.safeScaleFloatValue(5.0f, 0.1f);
      g.fill(makeShape(cfg, offset, offset));
    }

    g.setColor(this.getBackgroundColor(cfg));
    g.fill(shape);

    g.setColor(this.getBorderColor(cfg));
    g.draw(shape);

    g.setColor(this.getTextColor(cfg));
    this.textBlock.paint(g);

    if (this.extrasIconBlock.hasContent()) {
      this.extrasIconBlock.paint(g);
    }
  }

  @Override
  public void drawConnector(final Graphics2D g, final Rectangle2D source, final Rectangle2D destination, final boolean leftDirection, final MindMapPanelConfig cfg) {
    g.setStroke(new BasicStroke(cfg.safeScaleFloatValue(cfg.getConnectorWidth(),0.1f)));
    g.setColor(cfg.getConnectorColor());

    final Path2D path = new Path2D.Double();

    final double startX;
    if (destination.getCenterX() < source.getCenterX()) {
      // left
      startX = source.getCenterX() - source.getWidth() / 4;
    }
    else {
      // right
      startX = source.getCenterX() + source.getWidth() / 4;
    }

    path.moveTo(startX, source.getCenterY());
    path.curveTo(startX, destination.getCenterY(), startX, destination.getCenterY(), destination.getCenterX(), destination.getCenterY());

    g.draw(path);
  }

  private double calcTotalChildrenHeight(final double vertInset, final boolean left) {
    double result = 0.0d;
    boolean nonfirst = false;
    for (final Topic t : this.model.getChildren()) {
      final AbstractCollapsableElement w = (AbstractCollapsableElement) t.getPayload();
      final boolean lft = w.isLeftDirection();
      if ((left && lft) || (!left && !lft)) {
        if (nonfirst) {
          result += vertInset;
        }
        else {
          nonfirst = true;
        }
        result += w.getBlockSize().getHeight();
      }
    }
    return result;
  }

  @Override
  public void alignElementAndChildren(final MindMapPanelConfig cfg, final boolean leftSide, final double cx, final double cy) {
    final double dx = cx;
    final double dy = cy;
    this.moveTo(dx, dy);

    final double textMargin = cfg.getScale() * cfg.getTextMargins();
    final double centralLineY = textMargin + Math.max(this.textBlock.getBounds().getHeight(), this.extrasIconBlock.getBounds().getHeight()) / 2;

    this.textBlock.setCoordOffset(textMargin, centralLineY - this.textBlock.getBounds().getHeight() / 2);
    if (this.extrasIconBlock.hasContent()) {
      this.extrasIconBlock.setCoordOffset(textMargin + this.textBlock.getBounds().getWidth() + cfg.getScale() * cfg.getHorizontalBlockGap(), centralLineY - this.extrasIconBlock.getBounds().getHeight() / 2);
    }

    final double insetVert = cfg.getFirstLevelVerticalInset() * cfg.getScale();
    final double insetHorz = cfg.getFirstLevelHorizontalInset() * cfg.getScale();

    final double leftHeight = calcTotalChildrenHeight(insetVert, true);
    final double rightHeight = calcTotalChildrenHeight(insetVert, false);

    if (leftHeight > 0.0d) {
      final double ddx = dx - insetHorz;
      double ddy = dy - (leftHeight - this.bounds.getHeight()) / 2;
      for (final Topic t : this.model.getChildren()) {
        final AbstractCollapsableElement c = (AbstractCollapsableElement) t.getPayload();
        if (c.isLeftDirection()) {
          c.alignElementAndChildren(cfg, true, ddx - c.getBlockSize().getWidth(), ddy);
          ddy += c.getBlockSize().getHeight() + insetVert;
        }
      }
    }

    if (rightHeight > 0.0d) {
      final double ddx = dx + this.bounds.getWidth() + insetHorz;
      double ddy = dy - (rightHeight - this.bounds.getHeight()) / 2;
      for (final Topic t : this.model.getChildren()) {
        final AbstractCollapsableElement c = (AbstractCollapsableElement) t.getPayload();
        if (!c.isLeftDirection()) {
          c.alignElementAndChildren(cfg, false, ddx, ddy);
          ddy += c.getBlockSize().getHeight() + insetVert;
        }
      }
    }
  }

  @Override
  public void updateElementBounds(final Graphics2D gfx, final MindMapPanelConfig cfg) {
    super.updateElementBounds(gfx, cfg);
    final double marginOffset = ((cfg.getTextMargins()+cfg.getElementBorderWidth()) * 2.0d) * cfg.getScale();
    this.bounds.setRect(this.bounds.getX(), this.bounds.getY(), this.bounds.getWidth() + marginOffset, this.bounds.getHeight() + marginOffset);
  }

  public Dimension2D getLeftBlockSize() {
    return this.leftBlockSize;
  }

  public Dimension2D getRightBlockSize() {
    return this.rightBlockSize;
  }

  @Override
  public Dimension2D calcBlockSize(final MindMapPanelConfig cfg, final Dimension2D size, final boolean childrenOnly) {
    final double insetV = cfg.getScale() * cfg.getFirstLevelVerticalInset();
    final double insetH = cfg.getScale() * cfg.getFirstLevelHorizontalInset();

    final Dimension2D result = size == null ? new Dimension() : size;

    double leftWidth = 0.0d;
    double leftHeight = 0.0d;
    double rightWidth = 0.0d;
    double rightHeight = 0.0d;

    boolean nonfirstOnLeft = false;
    boolean nonfirstOnRight = false;

    for (final Topic t : this.model.getChildren()) {
      final ElementLevelFirst w = (ElementLevelFirst) t.getPayload();

      w.calcBlockSize(cfg, result, false);

      if (w.isLeftDirection()) {
        leftWidth = Math.max(leftWidth, result.getWidth());
        leftHeight += result.getHeight();
        if (nonfirstOnLeft) {
          leftHeight += insetV;
        }
        else {
          nonfirstOnLeft = true;
        }
      }
      else {
        rightWidth = Math.max(rightWidth, result.getWidth());
        rightHeight += result.getHeight();
        if (nonfirstOnRight) {
          rightHeight += insetV;
        }
        else {
          nonfirstOnRight = true;
        }
      }
    }

    if (!childrenOnly) {
      leftWidth += nonfirstOnLeft ? insetH : 0.0d;
      rightWidth += nonfirstOnRight ? insetH : 0.0d;
    }

    this.leftBlockSize.setSize(leftWidth, leftHeight);
    this.rightBlockSize.setSize(rightWidth, rightHeight);

    if (childrenOnly) {
      result.setSize(leftWidth + rightWidth, Math.max(leftHeight, rightHeight));
    }
    else {
      result.setSize(leftWidth + rightWidth + this.bounds.getWidth(), Math.max(this.bounds.getHeight(), Math.max(leftHeight, rightHeight)));
    }

    return result;
  }

  @Override
  public boolean hasDirection() {
    return true;
  }

  @Override
  public Topic findTopicBeforePoint(final MindMapPanelConfig cfg, final Point point) {

    Topic result = null;
    if (this.hasChildren()) {
      if (this.isCollapsed()) {
        return this.getModel().getLast();
      }
      else {
        double py = point.getY();
        final double vertInset = cfg.getOtherLevelVerticalInset() * cfg.getScale();

        Topic prev = null;

        final List<Topic> childForDirection = new ArrayList<>();
        if (point.getX() < this.bounds.getCenterX()) {
          for (final Topic t : this.model.getChildren()) {
            if (((AbstractElement) t.getPayload()).isLeftDirection()) {
              childForDirection.add(t);
            }
          }
        }
        else {
          for (final Topic t : this.model.getChildren()) {
            if (!((AbstractElement) t.getPayload()).isLeftDirection()) {
              childForDirection.add(t);
            }
          }
        }

        final Topic lastOne = childForDirection.isEmpty() ? null : childForDirection.get(childForDirection.size() - 1);

        for (final Topic t : childForDirection) {
          final AbstractElement el = (AbstractElement) t.getPayload();

          final double childStartBlockY = el.calcBlockY();
          final double childEndBlockY = childStartBlockY + el.getBlockSize().getHeight() + vertInset;

          if (py < childEndBlockY) {
            result = py < el.getBounds().getCenterY() ? prev : t;
            break;
          }
          else {
            if (t == lastOne) {
              result = t;
              break;
            }
          }

          prev = t;
        }
      }
    }
    return result;
  }

  @Override
  public Color getBackgroundColor(final MindMapPanelConfig config) {
    final Color dflt = this.fillColor == null ? config.getRootBackgroundColor() : this.fillColor;
    return dflt;
  }

  @Override
  public Color getTextColor(final MindMapPanelConfig config) {
    final Color dflt = this.textColor == null ? config.getRootTextColor() : this.textColor;
    return dflt;
  }
  
}
