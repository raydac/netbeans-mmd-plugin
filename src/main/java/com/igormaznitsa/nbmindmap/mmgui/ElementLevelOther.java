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
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

public class ElementLevelOther extends ElementLevelFirst {

  public ElementLevelOther(final Topic model) {
    super(model);
  }

  @Override
  protected Shape makeShape(final float x, final float y) {
    return new Rectangle2D.Float(x, y, (float) this.bounds.getWidth(), (float) this.bounds.getHeight());
  }

  @Override
  public void drawComponent(final Graphics2D g, final Configuration cfg) {
    g.setStroke(new BasicStroke(cfg.getScale() * cfg.getElementBorderWidth()));

    final Shape shape = makeShape(0f, 0f);

    if (cfg.isDropShadow()) {
      g.setColor(cfg.getShadowColor());
      g.fill(makeShape(5.0f * cfg.getScale(), 5.0f * cfg.getScale()));
    }

    g.setColor(cfg.getOtherLevelBackgroundColor());
    g.fill(shape);

    g.setColor(cfg.getElementBorderColor());
    g.draw(shape);

    g.setColor(cfg.getOtherLevelTextColor());
    this.textBlock.paint(g);
    
    if (this.extrasIconBlock.hasContent()) {
      this.extrasIconBlock.paint(g);
    }

    if (this.hasChildren()){
      drawCollapsator(g,  cfg,this.isCollapsed());
    }
  }
  
  @Override
  public void doPaintConnectors(final Graphics2D g, final boolean leftDirection, final Configuration cfg) {
    final Rectangle2D source = new Rectangle2D.Double(this.bounds.getX() + this.collapsatorZone.getX(), this.bounds.getY() + this.collapsatorZone.getY(), this.collapsatorZone.getWidth(), this.collapsatorZone.getHeight());
    for (final Topic t : this.model.getChildren()) {
      this.drawConnector(g, source, ((AbstractElement) t.getPayload()).getBounds(), leftDirection, cfg);
    }
  }
  
  @Override
  public void setLeftDirection(boolean leftSide) {
  }

  @Override
  public boolean isLeftDirection() {
    Topic topic = this.model.getParent();

    boolean result = false;

    while (topic != null) {
      final AbstractElement w = (AbstractElement) topic.getPayload();
      if (w.getClass() == ElementLevelFirst.class) {
        result = ((ElementLevelFirst) w).isLeftDirection();
        break;
      }
      else {
        topic = topic.getParent();
      }
    }

    return result;
  }

  
}
