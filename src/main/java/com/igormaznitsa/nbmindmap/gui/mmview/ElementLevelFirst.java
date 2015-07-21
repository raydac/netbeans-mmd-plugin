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
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

public class ElementLevelFirst extends AbstractCollapsableElement {

  public ElementLevelFirst(final MindMapTopic model) {
    super(model);
  }

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

    g.setColor(cfg.getFirstLevelBacgroundColor());
    g.fill(shape);

    g.setColor(cfg.getElementBorderColor());
    g.draw(shape);

    final int margin = Math.round(cfg.getScale() * cfg.getTextMargins());

    g.setColor(cfg.getFirstLevelTextColor());
    this.textBlock.paint(g);

    if (this.iconBlock.hasContent()) {
      this.iconBlock.paint(g);
    }

    if (this.hasChildren()) {
      drawCollapsator(g, cfg, this.isCollapsed());
    }
  }

  @Override
  public boolean isMoveable() {
    return true;
  }

}
