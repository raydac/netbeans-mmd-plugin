/*
 * Copyright (C) 2015-2022 Igor A. Maznitsa
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

package com.igormaznitsa.mindmap.swing.panel.ui;

import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.mindmap.swing.panel.ui.gfx.MMGraphics;
import com.igormaznitsa.mindmap.swing.panel.ui.gfx.StrokeType;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

public class ElementLevelFirst extends AbstractCollapsableElement {

  public ElementLevelFirst(final Topic model) {
    super(model);
  }

  protected ElementLevelFirst(final ElementLevelFirst element) {
    super(element);
  }

  @Override
  public AbstractElement makeCopy() {
    return new ElementLevelFirst(this);
  }

  protected Shape makeShape(final MindMapPanelConfig config, final double x, final double y) {
    return new Rectangle2D.Double(x, y, this.bounds.getWidth(), this.bounds.getHeight());
  }

  @Override
  public void drawComponent(final MMGraphics g, final MindMapPanelConfig cfg,
                            final boolean drawCollapsator) {
    g.setStroke(cfg.safeScaleFloatValue(cfg.getElementBorderWidth(), 0.1f), StrokeType.SOLID);

    final Shape shape = makeShape(cfg, 0f, 0f);

    if (cfg.isDropShadow()) {
      final float offset = cfg.safeScaleFloatValue(cfg.getShadowOffset(), 0.0f);
      g.draw(makeShape(cfg, offset, offset), null, cfg.getShadowColor());
    }

    g.draw(shape, getBorderColor(cfg), getBackgroundColor(cfg));

    if (this.visualAttributeImageBlock.mayHaveContent()) {
      this.visualAttributeImageBlock.paint(g, cfg);
    }

    this.textBlock.paint(g, getTextColor(cfg));

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
    return this.fillColor == null ? config.getFirstLevelBackgroundColor() : this.fillColor;
  }

  @Override
  public Color getTextColor(final MindMapPanelConfig config) {
    return this.textColor == null ? config.getFirstLevelTextColor() : this.textColor;
  }
}
