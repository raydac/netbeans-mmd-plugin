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
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import javax.annotation.Nonnull;

public class ElementLevelOther extends ElementLevelFirst {

  public ElementLevelOther(@Nonnull final Topic model) {
    super(model);
  }

  protected ElementLevelOther(@Nonnull final ElementLevelOther element) {
    super(element);
  }

  @Override
  @Nonnull
  public AbstractElement makeCopy() {
    return new ElementLevelOther(this);
  }

  @Override
  public void drawComponent(@Nonnull final MMGraphics g, @Nonnull final MindMapPanelConfig cfg, final boolean drawCollapsator) {
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
  public void doPaintConnectors(@Nonnull final MMGraphics g, final boolean leftDirection, @Nonnull final MindMapPanelConfig cfg) {
    final Rectangle2D source = new Rectangle2D.Double(this.bounds.getX() + this.collapsatorZone.getX(), this.bounds.getY() + this.collapsatorZone.getY(), this.collapsatorZone.getWidth(), this.collapsatorZone.getHeight());
    for (final Topic t : this.model.getChildren()) {
      this.drawConnector(g, source, assertNotNull(((AbstractElement) t.getPayload())).getBounds(), leftDirection, cfg);
    }
  }

  @Override
  public boolean isLeftDirection() {
    Topic topic = this.model.getParent();

    boolean result = false;

    while (topic != null) {
      final AbstractElement w = assertNotNull((AbstractElement) topic.getPayload());
      if (w.getClass() == ElementLevelFirst.class) {
        result = ((ElementLevelFirst) w).isLeftDirection();
        break;
      } else {
        topic = topic.getParent();
      }
    }

    return result;
  }

  @Override
  public void setLeftDirection(boolean leftSide) {
  }

  @Override
  @Nonnull
  public Color getBackgroundColor(@Nonnull final MindMapPanelConfig config) {
    final Color dflt = this.fillColor == null ? config.getOtherLevelBackgroundColor() : this.fillColor;
    return dflt;
  }

  @Override
  @Nonnull
  public Color getTextColor(@Nonnull final MindMapPanelConfig config) {
    final Color dflt = this.textColor == null ? config.getOtherLevelTextColor() : this.textColor;
    return dflt;
  }

}
