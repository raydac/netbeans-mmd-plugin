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

import com.igormaznitsa.nbmindmap.model.Topic;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import javax.swing.text.JTextComponent;

public abstract class AbstractElement {

  protected final Topic model;

  protected final TextBlock textBlock;
  protected final IconBlock extrasIconBlock;

  protected final Rectangle2D bounds = new Rectangle2D.Float();
  protected final Dimension2D blockSize = new Dimension();

  public String getText() {
    return this.model.getText();
  }

  public void setText(final String text) {
    this.model.setText(text);
    this.textBlock.updateText(text);
  }

  public AbstractElement(final Topic model) {
    this.model = model;
    this.textBlock = new TextBlock(this.model.getText(), TextAlign.CENTER);
    this.textBlock.setTextAlign(TextAlign.findForName(model.getAttribute("align"))); //NOI18N
    this.extrasIconBlock = new IconBlock(model);
  }

  public AbstractElement getParent() {
    final Topic parent = this.model.getParent();
    return parent == null ? null : (AbstractElement) parent.getPayload();
  }

  public Topic getModel() {
    return this.model;
  }

  public TextAlign getTextAlign() {
    return this.textBlock.getTextAlign();
  }

  public void setTextAlign(final TextAlign textAlign) {
    this.textBlock.setTextAlign(textAlign);
    this.model.setAttribute("align", this.textBlock.getTextAlign().name()); //NOI18N
  }

  public void updateElementBounds(final Graphics2D gfx, final Configuration cfg) {
    this.textBlock.updateSize(gfx, cfg);
    this.extrasIconBlock.updateSize(gfx, cfg);

    final double width;
    if (this.extrasIconBlock.hasContent()) {
      width = this.textBlock.getBounds().getWidth() + cfg.getScale() * cfg.getHorizontalBlockGap() + this.extrasIconBlock.getBounds().getWidth();
    }
    else {
      width = this.textBlock.getBounds().getWidth();
    }

    this.bounds.setRect(0d, 0d, width, Math.max(this.textBlock.getBounds().getHeight(), this.extrasIconBlock.getBounds().getHeight()));
  }

  public void updateBlockSize(final Configuration cfg) {
    this.calcBlockSize(cfg, this.blockSize, false);
  }

  public Dimension2D getBlockSize() {
    return this.blockSize;
  }

  public void moveTo(final double x, final double y) {
    this.bounds.setFrame(x, y, this.bounds.getWidth(), this.bounds.getHeight());
  }

  public void moveWholeTreeBranchCoordinates(final double deltaX, final double deltaY) {
    moveTo(this.bounds.getX() + deltaX, this.bounds.getY() + deltaY);
    for (final Topic t : this.model.getChildren()) {
      final AbstractElement el = (AbstractElement) t.getPayload();
      if (el != null) {
        el.moveWholeTreeBranchCoordinates(deltaX, deltaY);
      }
    }
  }

  public Rectangle2D getBounds() {
    return this.bounds;
  }

  public final void doPaint(final Graphics2D g, final Configuration cfg) {
    if (this.hasChildren() && !isCollapsed()) {
      doPaintConnectors(g, isLeftDirection(), cfg);
    }

    if (g.getClipBounds().intersects(this.bounds)) {
      g.translate(this.bounds.getX(), this.bounds.getY());
      try {
        drawComponent(g, cfg);
      }
      finally {
        g.translate(-this.bounds.getX(), -this.bounds.getY());
      }
    }
  }

  public void doPaintConnectors(final Graphics2D g, final boolean leftDirection, final Configuration cfg) {
    final Rectangle2D source = this.bounds;
    for (final Topic t : this.model.getChildren()) {
      drawConnector(g, source, ((AbstractElement) t.getPayload()).getBounds(), leftDirection, cfg);
    }
  }

  public boolean hasChildren() {
    return this.model.hasChildren();
  }

  public JTextComponent fillByTextAndFont(final JTextComponent compo) {
    this.textBlock.fillByTextAndFont(compo);
    return compo;
  }

  public abstract void drawComponent(Graphics2D g, Configuration cfg);

  public abstract void drawConnector(Graphics2D g, Rectangle2D source, Rectangle2D destination, boolean leftDirection, Configuration cfg);

  public abstract boolean isMoveable();

  public abstract boolean isCollapsed();

  public abstract void alignElementAndChildren(Configuration cfg, boolean leftSide, double centerX, double centerY);

  public abstract Dimension2D calcBlockSize(Configuration cfg, Dimension2D size, boolean childrenOnly);

  public abstract boolean hasDirection();

  public ElementPart findPartForPoint(final Point point) {
    ElementPart result = ElementPart.NONE;
    if (this.bounds.contains(point)) {
      final double offX = point.getX() - this.bounds.getX();
      final double offY = point.getY() - this.bounds.getY();

      result = ElementPart.AREA;
      if (this.textBlock.getBounds().contains(offX, offY)) {
        result = ElementPart.TEXT;
      }
      else if (this.extrasIconBlock.getBounds().contains(offX, offY)) {
        result = ElementPart.ICONS;
      }
    }
    return result;
  }

  public Topic findTopicBeforePoint(final Configuration cfg, final Point point) {

    Topic result = null;
    if (this.hasChildren()) {
      if (this.isCollapsed()) {
        return this.getModel().getLast();
      }
      else {
        double py = point.getY();
        final double vertInset = cfg.getOtherLevelVerticalInset() * cfg.getScale();

        Topic prev = null;

        for (final Topic t : this.model.getChildren()) {
          final AbstractElement el = (AbstractElement) t.getPayload();

          final double childStartBlockY = el.calcBlockY();
          final double childEndBlockY = childStartBlockY + el.getBlockSize().getHeight() + vertInset;

          if (py < childEndBlockY) {
            result = py < el.getBounds().getCenterY() ? prev : t;
            break;
          }
          else {
            if (this.model.isLastChild(t)) {
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

  protected double calcBlockY() {
    return this.bounds.getY() - (this.blockSize.getHeight() - this.bounds.getHeight()) / 2;
  }

  protected double calcBlockX() {
    return this.bounds.getX() - (this.isLeftDirection() ? this.blockSize.getWidth() - this.bounds.getWidth() : 0.0d);
  }

  public AbstractElement findNearestTopic(final AbstractElement elementToIgnore, final Point point) {
    return findNearestTopic(elementToIgnore, calcDistanceToPoint(point), point);
  }

  private AbstractElement findNearestTopic(final AbstractElement elementToIgnore, final double lessThanDistance, final Point point) {
    double curDistance = this == elementToIgnore ? Double.MAX_VALUE : calcDistanceToPoint(point);
    AbstractElement result = curDistance <= lessThanDistance ? this : null;
    if (!this.isCollapsed()) {
      for (final Topic t : this.model.getChildren()) {
        final AbstractElement element = t.getPayload() == null ? null : (AbstractElement) t.getPayload();
        if (element != null) {
          final AbstractElement nearestChild = element.findNearestTopic(elementToIgnore, curDistance, point);
          if (nearestChild != null) {
            final double dist = nearestChild.calcDistanceToPoint(point);
            if (dist < curDistance) {
              curDistance = dist;
              result = nearestChild;
            }
          }
        }
      }
    }
    return result;
  }

  public double calcDistanceToPoint(final Point point) {
    return Math.sqrt(Math.pow(this.bounds.getCenterX() - point.getX(), 2.0d) + Math.pow(this.bounds.getCenterY() - point.getY(), 2.0d));
  }

  public AbstractElement findTopicBlockForPoint(final Point point) {
    AbstractElement result = null;
    if (point != null) {
      final double px = point.getX();
      final double py = point.getY();

      if (px >= calcBlockX() && py >= calcBlockY() && px < this.bounds.getX() + this.blockSize.getWidth() && py < this.bounds.getY() + this.blockSize.getHeight()) {
        if (this.isCollapsed()) {
          result = this;
        }
        else {
          AbstractElement foundChild = null;
          for (final Topic t : this.model.getChildren()) {
            foundChild = t.getPayload() == null ? null : ((AbstractElement) t.getPayload()).findTopicBlockForPoint(point);
            if (foundChild != null) {
              break;
            }
          }
          result = foundChild == null ? this : foundChild;
        }
      }
    }
    return result;
  }

  public AbstractElement findForPoint(final Point point) {
    AbstractElement result = null;
    if (point != null) {
      if (this.bounds.contains(point)) {
        result = this;
      }
      else {
        for (final Topic t : this.model.getChildren()) {
          final AbstractElement w = (AbstractElement) t.getPayload();
          result = w == null ? null : w.findForPoint(point);
          if (result != null) {
            break;
          }
        }
      }
    }
    return result;
  }

  public boolean isLeftDirection() {
    return false;
  }

  public TextBlock getTextBlock() {
    return this.textBlock;
  }

  public IconBlock getIconBlock() {
    return this.extrasIconBlock;
  }

  public boolean collapseOrExpandAllChildren(final boolean collapse) {
    boolean result = false;

    if (this instanceof AbstractCollapsableElement) {
      final AbstractCollapsableElement el = (AbstractCollapsableElement) this;

      if (collapse) {
        if (!el.isCollapsed()) {
          el.setCollapse(true);
          result = true;
        }
      }
      else {
        if (el.isCollapsed()) {
          el.setCollapse(false);
          result = true;
        }
      }
    }

    if (this.hasChildren()) {
      for (final Topic t : this.model.getChildren()) {
        final AbstractElement e = (AbstractElement) t.getPayload();
        if (e != null) {
          result |= e.collapseOrExpandAllChildren(collapse);
        }
      }
    }

    return result;
  }

}
