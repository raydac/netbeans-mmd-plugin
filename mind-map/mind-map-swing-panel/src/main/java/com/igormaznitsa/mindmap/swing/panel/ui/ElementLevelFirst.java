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
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

public class ElementLevelFirst extends AbstractCollapsableElement {

  public ElementLevelFirst(final Topic model) {
    super(model);
  }

  protected ElementLevelFirst(final  ElementLevelFirst element) {
    super(element);
  }

  @Override
  public AbstractElement makeCopy() {
    return new ElementLevelFirst(this);
  }

  protected Shape makeShape(final MindMapPanelConfig cfg, final float x, final float y) {
    return new Rectangle2D.Float(x, y, (float) this.bounds.getWidth(), (float) this.bounds.getHeight());
  }

  @Override
  public void drawComponent(final Graphics2D g, final MindMapPanelConfig cfg, final boolean drawCollapsator) {
    g.setStroke(new BasicStroke(cfg.safeScaleFloatValue(cfg.getElementBorderWidth(),0.1f)));

    final Shape shape = makeShape(cfg, 0f, 0f);

    if (cfg.isDropShadow()) {
      g.setColor(cfg.getShadowColor());
      final float offset = cfg.safeScaleFloatValue(cfg.getShadowOffset(), 0.0f);
      g.fill(makeShape(cfg, offset, offset));
    }

    g.setColor(getBackgroundColor(cfg));
    g.fill(shape);

    g.setColor(getBorderColor(cfg));
    g.draw(shape);

    g.setColor(getTextColor(cfg));
    this.textBlock.paint(g);

    if (this.extrasIconBlock.hasContent()) {
      this.extrasIconBlock.paint(g);
    }

    if (drawCollapsator && this.hasChildren()) {
      drawCollapsator(g, cfg, this.isCollapsed());
    }
  }

  @Override
  public boolean isMoveable() {
    return true;
  }

  @Override
  public Color getBackgroundColor(final MindMapPanelConfig config) {
    final Color dflt = this.fillColor == null ? config.getFirstLevelBackgroundColor() : this.fillColor;
    return dflt;
  }

  @Override
  public Color getTextColor(final MindMapPanelConfig config) {
    final Color dflt = this.textColor == null ? config.getFirstLevelTextColor() : this.textColor;
    return dflt;
  }
}
