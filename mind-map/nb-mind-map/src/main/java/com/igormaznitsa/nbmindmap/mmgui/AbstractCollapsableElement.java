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

import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.Topic;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Dimension2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

public abstract class AbstractCollapsableElement extends AbstractElement {

  protected final Rectangle2D collapsatorZone = new Rectangle2D.Double();
  public static final String ATTR_COLLAPSED = "collapsed";
  
  public AbstractCollapsableElement(final Topic model) {
    super(model);
  }

  protected void drawCollapsator(final Graphics2D g, final Configuration cfg, final boolean collapsed) {
    final int x = (int) Math.round(collapsatorZone.getX());
    final int y = (int) Math.round(collapsatorZone.getY());
    final int w = (int) Math.round(collapsatorZone.getWidth());
    final int h = (int) Math.round(collapsatorZone.getHeight());

    final int DELTA = (int) Math.round(cfg.getCollapsatorSize() * 0.3d * cfg.getScale());

    g.setStroke(new BasicStroke(cfg.safeScaleFloatValue(cfg.getCollapsatorBorderWidth(),0.1f)));
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
    return "true".equalsIgnoreCase(this.model.getAttribute(ATTR_COLLAPSED));//NOI18N
  }

  public void setCollapse(final boolean flag) {
    this.model.setAttribute(ATTR_COLLAPSED, flag ? "true" : null);//NOI18N
  }

  @Override
  public boolean isLeftDirection() {
    return isLeftSidedTopic(this.model);
  }

  public void setLeftDirection(final boolean leftSide) {
    makeTopicLeftSided(this.model, leftSide);
  }

  public static boolean isLeftSidedTopic(final Topic t) {
    return "true".equals(t.getAttribute("leftSide"));//NOI18N
  }

  public static void makeTopicLeftSided(final Topic topic, final boolean left) {
    if (left) {
      topic.setAttribute("leftSide", "true");//NOI18N
    }
    else {
      topic.setAttribute("leftSide", null);//NOI18N
    }
  }

  @Override
  public Dimension2D calcBlockSize(final Configuration cfg, final Dimension2D size, final boolean childrenOnly) {
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
          }
          else {
            notFirstChiild = true;
          }
          ((AbstractElement) t.getPayload()).calcBlockSize(cfg, result, false);
          width = Math.max(baseWidth + result.getWidth(), width);
          childrenHeight += result.getHeight();
        }

        height = Math.max(height, childrenHeight);
      }
      else {
        if (!childrenOnly) {
          width += cfg.getCollapsatorSize() * cfg.getScale();
        }
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

    final double textMargin = cfg.getScale() * cfg.getTextMargins();
    final double centralBlockLineY = textMargin + Math.max(this.textBlock.getBounds().getHeight(), this.extrasIconBlock.getBounds().getHeight()) / 2;

    this.textBlock.setCoordOffset(textMargin, centralBlockLineY - this.textBlock.getBounds().getHeight() / 2);
    if (this.extrasIconBlock.hasContent()) {
      this.extrasIconBlock.setCoordOffset(textMargin + this.textBlock.getBounds().getWidth() + cfg.getScale() * cfg.getHorizontalBlockGap(), centralBlockLineY - this.extrasIconBlock.getBounds().getHeight() / 2);
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
        }
        else {
          notFirstChild = true;
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
    for (final Topic t : this.model.getChildren()) {
      this.drawConnector(g, source, ((AbstractElement) t.getPayload()).getBounds(), lefDir, cfg);
    }
  }

  @Override
  public void drawConnector(final Graphics2D g, final Rectangle2D source, final Rectangle2D destination, final boolean leftDirection, final Configuration cfg) {
    g.setStroke(new BasicStroke(cfg.safeScaleFloatValue(cfg.getConnectorWidth(),0.1f)));
    g.setColor(cfg.getConnectorColor());

    final double dy = Math.abs(destination.getCenterY() - source.getCenterY());
    if (dy < (16.0d * cfg.getScale())) {
      g.drawLine((int) source.getCenterX(), (int) source.getCenterY(), (int) destination.getCenterX(), (int) source.getCenterY());
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
    }
    return result;
  }

  @Override
  public void updateElementBounds(final Graphics2D gfx, final Configuration cfg) {
    super.updateElementBounds(gfx, cfg);
    final double marginOffset = (cfg.getTextMargins() << 1) * cfg.getScale();
    this.bounds.setRect(this.bounds.getX(), this.bounds.getY(), this.bounds.getWidth() + marginOffset, this.bounds.getHeight() + marginOffset);
  }

  public Rectangle2D getCollapsatorArea() {
    return this.collapsatorZone;
  }

  @Override
  public boolean hasDirection() {
    return true;
  }

  public boolean ensureUncollapsed() {
    boolean result = false;

    Topic parent = this.model.getParent();
    while (parent != null) {
      final AbstractElement payload = (AbstractElement) parent.getPayload();
      if (payload == null) {
        break;
      }
      if (payload.isCollapsed() && payload instanceof AbstractCollapsableElement) {
        ((AbstractCollapsableElement) payload).setCollapse(false);
        result = true;
      }
      parent = payload.model.getParent();
    }
    return result;
  }

  public static void removeCollapseAttributeFromTopicsWithoutChildren(final MindMap map){
    removeCollapseAttrIfNoChildren(map == null ? null : map.getRoot());
  }
  
  private static void removeCollapseAttrIfNoChildren(final Topic topic){
    if (topic!=null){
    if (!topic.hasChildren()){
      topic.setAttribute(ATTR_COLLAPSED, null);
    }else{
      for(final Topic t : topic.getChildren()){
        removeCollapseAttrIfNoChildren(t);
      }
    }
    }
  }
  
}
