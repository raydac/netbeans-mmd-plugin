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

import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.mindmap.model.Topic;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import javax.annotation.Nonnull;
import com.igormaznitsa.mindmap.swing.panel.ui.gfx.StrokeType;
import com.igormaznitsa.mindmap.swing.panel.ui.gfx.MMGraphics;

public class ElementLevelFirst extends AbstractCollapsableElement {

  public ElementLevelFirst(@Nonnull final Topic model) {
    super(model);
  }

  protected ElementLevelFirst(@Nonnull final  ElementLevelFirst element) {
    super(element);
  }

  @Override
  @Nonnull
  public AbstractElement makeCopy() {
    return new ElementLevelFirst(this);
  }

  @Nonnull
  protected Shape makeShape(@Nonnull final MindMapPanelConfig cfg, final float x, final float y) {
    return new Rectangle2D.Float(x, y, (float) this.bounds.getWidth(), (float) this.bounds.getHeight());
  }

  @Override
  public void drawComponent(@Nonnull final MMGraphics g, @Nonnull final MindMapPanelConfig cfg, final boolean drawCollapsator) {
    g.setStroke(cfg.safeScaleFloatValue(cfg.getElementBorderWidth(),0.1f),StrokeType.SOLID);

    final Shape shape = makeShape(cfg, 0f, 0f);

    if (cfg.isDropShadow()) {
      final float offset = cfg.safeScaleFloatValue(cfg.getShadowOffset(), 0.0f);
      g.draw(makeShape(cfg, offset, offset),null,cfg.getShadowColor());
    }

    g.draw(shape,getBorderColor(cfg),getBackgroundColor(cfg));
    
    if (this.visualAttributeImageBlock.mayHaveContent()) {
      this.visualAttributeImageBlock.paint(g, cfg);
    }
    
    this.textBlock.paint(g,getTextColor(cfg));

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
  @Nonnull
  public Color getBackgroundColor(@Nonnull final MindMapPanelConfig config) {
    final Color dflt = this.fillColor == null ? config.getFirstLevelBackgroundColor() : this.fillColor;
    return dflt;
  }

  @Override
  @Nonnull
  public Color getTextColor(@Nonnull final MindMapPanelConfig config) {
    final Color dflt = this.textColor == null ? config.getFirstLevelTextColor() : this.textColor;
    return dflt;
  }
}
