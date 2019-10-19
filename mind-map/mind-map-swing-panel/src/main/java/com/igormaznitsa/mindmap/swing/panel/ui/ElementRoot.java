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
import com.igormaznitsa.mindmap.swing.panel.ui.gfx.MMGraphics;
import com.igormaznitsa.mindmap.swing.panel.ui.gfx.StrokeType;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class ElementRoot extends AbstractElement {

  private final Dimension2D leftBlockSize = new Dimension();
  private final Dimension2D rightBlockSize = new Dimension();

  public ElementRoot(@Nonnull final Topic topic) {
    super(topic);
  }

  protected ElementRoot(@Nonnull final ElementRoot element) {
    super(element);
    this.leftBlockSize.setSize(element.leftBlockSize);
    this.rightBlockSize.setSize(element.rightBlockSize);
  }

  @Override
  @Nonnull
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

  @Nonnull
  private Shape makeShape(@Nonnull final MindMapPanelConfig cfg, final double x, final double y) {
    final float round = cfg.safeScaleFloatValue(10.0f, 0.1f);
    return new RoundRectangle2D.Double(x, y, this.bounds.getWidth(), this.bounds.getHeight(), round, round);
  }

  @Override
  public void drawComponent(@Nonnull final MMGraphics g, @Nonnull final MindMapPanelConfig cfg, final boolean drawCollapsator) {
    g.setStroke(cfg.safeScaleFloatValue(cfg.getElementBorderWidth(), 0.1f), StrokeType.SOLID);

    final Shape shape = makeShape(cfg, 0f, 0f);

    if (cfg.isDropShadow()) {
      final float offset = cfg.safeScaleFloatValue(cfg.getShadowOffset(), 0.0f);
      g.draw(makeShape(cfg, offset, offset), null, cfg.getShadowColor());
    }

    g.draw(shape, this.getBorderColor(cfg), this.getBackgroundColor(cfg));

    if (this.visualAttributeImageBlock.mayHaveContent()) {
      this.visualAttributeImageBlock.paint(g, cfg);
    }

    this.textBlock.paint(g, this.getTextColor(cfg));

    if (this.extrasIconBlock.hasContent()) {
      this.extrasIconBlock.paint(g);
    }
  }

  @Override
  public void drawConnector(@Nonnull final MMGraphics g, @Nonnull final Rectangle2D source, @Nonnull final Rectangle2D destination, final boolean leftDirection, @Nonnull final MindMapPanelConfig cfg) {
    g.setStroke(cfg.safeScaleFloatValue(cfg.getConnectorWidth(), 0.1f), StrokeType.SOLID);

    final double startX;
    if (destination.getCenterX() < source.getCenterX()) {
      // left
      startX = source.getCenterX() - source.getWidth() / 4;
    } else {
      // right
      startX = source.getCenterX() + source.getWidth() / 4;
    }

    g.drawCurve(startX, source.getCenterY(), destination.getCenterX(), destination.getCenterY(), cfg.getConnectorColor());
  }

  private double calcTotalChildrenHeight(final double vertInset, final boolean left) {
    double result = 0.0d;
    boolean nonfirst = false;
    for (final Topic t : this.model.getChildren()) {
      final AbstractCollapsableElement w = assertNotNull((AbstractCollapsableElement) t.getPayload());
      final boolean lft = w.isLeftDirection();
      if ((left && lft) || (!left && !lft)) {
        if (nonfirst) {
          result += vertInset;
        } else {
          nonfirst = true;
        }
        result += w.getBlockSize().getHeight();
      }
    }
    return result;
  }

  @Override
  public void alignElementAndChildren(@Nonnull final MindMapPanelConfig cfg, final boolean leftSide, final double cx, final double cy) {
    super.alignElementAndChildren(cfg, leftSide, cx, cy);

    final double dx = cx;
    final double dy = cy;
    this.moveTo(dx, dy);

    final double insetVert = cfg.getFirstLevelVerticalInset() * cfg.getScale();
    final double insetHorz = cfg.getFirstLevelHorizontalInset() * cfg.getScale();

    final double leftHeight = calcTotalChildrenHeight(insetVert, true);
    final double rightHeight = calcTotalChildrenHeight(insetVert, false);

    if (leftHeight > 0.0d) {
      final double ddx = dx - insetHorz;
      double ddy = dy - (leftHeight - this.bounds.getHeight()) / 2;
      for (final Topic t : this.model.getChildren()) {
        final AbstractCollapsableElement c = assertNotNull((AbstractCollapsableElement) t.getPayload());
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
        final AbstractCollapsableElement c = assertNotNull((AbstractCollapsableElement) t.getPayload());
        if (!c.isLeftDirection()) {
          c.alignElementAndChildren(cfg, false, ddx, ddy);
          ddy += c.getBlockSize().getHeight() + insetVert;
        }
      }
    }
  }

  @Override
  public void updateElementBounds(@Nonnull final MMGraphics gfx, @Nonnull final MindMapPanelConfig cfg) {
    super.updateElementBounds(gfx, cfg);
    final double marginOffset = ((cfg.getTextMargins() + cfg.getElementBorderWidth()) * 2.0d) * cfg.getScale();
    this.bounds.setRect(this.bounds.getX(), this.bounds.getY(), this.bounds.getWidth() + marginOffset, this.bounds.getHeight() + marginOffset);
  }

  @Nonnull
  public Dimension2D getLeftBlockSize() {
    return this.leftBlockSize;
  }

  @Nonnull
  public Dimension2D getRightBlockSize() {
    return this.rightBlockSize;
  }

  @Override
  @Nonnull
  public Dimension2D calcBlockSize(@Nonnull final MindMapPanelConfig cfg, @Nonnull final Dimension2D size, final boolean childrenOnly) {
    final double insetV = cfg.getScale() * cfg.getFirstLevelVerticalInset();
    final double insetH = cfg.getScale() * cfg.getFirstLevelHorizontalInset();

    final Dimension2D result = size;

    double leftWidth = 0.0d;
    double leftHeight = 0.0d;
    double rightWidth = 0.0d;
    double rightHeight = 0.0d;

    boolean nonfirstOnLeft = false;
    boolean nonfirstOnRight = false;

    for (final Topic t : this.model.getChildren()) {
      final ElementLevelFirst w = assertNotNull((ElementLevelFirst) t.getPayload());

      w.calcBlockSize(cfg, result, false);

      if (w.isLeftDirection()) {
        leftWidth = Math.max(leftWidth, result.getWidth());
        leftHeight += result.getHeight();
        if (nonfirstOnLeft) {
          leftHeight += insetV;
        } else {
          nonfirstOnLeft = true;
        }
      } else {
        rightWidth = Math.max(rightWidth, result.getWidth());
        rightHeight += result.getHeight();
        if (nonfirstOnRight) {
          rightHeight += insetV;
        } else {
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
    } else {
      result.setSize(leftWidth + rightWidth + this.bounds.getWidth(), Math.max(this.bounds.getHeight(), Math.max(leftHeight, rightHeight)));
    }

    return result;
  }

  @Override
  public boolean hasDirection() {
    return true;
  }

  @Override
  @Nullable
  public Topic findTopicBeforePoint(@Nonnull final MindMapPanelConfig cfg, @Nonnull final Point point) {

    Topic result = null;
    if (this.hasChildren()) {
      if (this.isCollapsed()) {
        return this.getModel().getLast();
      } else {
        double py = point.getY();
        final double vertInset = cfg.getOtherLevelVerticalInset() * cfg.getScale();

        Topic prev = null;

        final List<Topic> childForDirection = new ArrayList<Topic>();
        if (point.getX() < this.bounds.getCenterX()) {
          for (final Topic t : this.model.getChildren()) {
            if ((assertNotNull((AbstractElement) t.getPayload())).isLeftDirection()) {
              childForDirection.add(t);
            }
          }
        } else {
          for (final Topic t : this.model.getChildren()) {
            if (!(assertNotNull((AbstractElement) t.getPayload())).isLeftDirection()) {
              childForDirection.add(t);
            }
          }
        }

        final Topic lastOne = childForDirection.isEmpty() ? null : childForDirection.get(childForDirection.size() - 1);

        for (final Topic t : childForDirection) {
          final AbstractElement el = assertNotNull((AbstractElement) t.getPayload());

          final double childStartBlockY = el.calcBlockY();
          final double childEndBlockY = childStartBlockY + el.getBlockSize().getHeight() + vertInset;

          if (py < childEndBlockY) {
            result = py < el.getBounds().getCenterY() ? prev : t;
            break;
          } else {
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
  @Nonnull
  public Color getBackgroundColor(@Nonnull final MindMapPanelConfig config) {
    final Color dflt = this.fillColor == null ? config.getRootBackgroundColor() : this.fillColor;
    return dflt;
  }

  @Override
  @Nonnull
  public Color getTextColor(@Nonnull final MindMapPanelConfig config) {
    final Color dflt = this.textColor == null ? config.getRootTextColor() : this.textColor;
    return dflt;
  }

}
