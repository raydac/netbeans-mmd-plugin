/*
 * Copyright 2016 Igor Maznitsa.
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

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.plugins.AbstractPlugin;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.mindmap.swing.panel.utils.ScalableIcon;

public class PluginImageBlock {

  private final Rectangle2D bounds = new Rectangle2D.Double();
  private final Topic model;
  private double scale = 1.0d;
  private boolean contentPresented;

  public PluginImageBlock(@Nonnull final PluginImageBlock orig) {
    this.bounds.setRect(orig.bounds);
    this.model = orig.model;
    this.scale = orig.scale;
    this.contentPresented = orig.contentPresented;
  }

  public PluginImageBlock(@Nonnull final Topic model) {
    this.model = model;
  }

  public void setCoordOffset(final double x, final double y) {
    this.bounds.setRect(x, y, this.bounds.getWidth(), this.bounds.getHeight());
  }

  public void updateSize(@Nonnull final Graphics2D gfx, @Nonnull final MindMapPanelConfig cfg) {
    final int numberOfIcons = 0;
    this.scale = cfg.getScale();
    if (numberOfIcons == 0) {
      this.bounds.setRect(0d, 0d, 0d, 0d);
      this.contentPresented = false;
    } else {
      final double scaledIconWidth = ScalableIcon.BASE_WIDTH * this.scale;
      final double scaledIconHeight = ScalableIcon.BASE_HEIGHT * this.scale;
      this.bounds.setRect(0d, 0d, scaledIconWidth * numberOfIcons, scaledIconHeight);
      this.contentPresented = true;
    }
  }

  public boolean hasContent() {
    return this.contentPresented;
  }

  public void paint(@Nonnull final Graphics2D gfx) {
    final int numberOfIcons = this.model.getNumberOfExtras();
    if (numberOfIcons != 0) {
      double offsetX = this.bounds.getX();
      final int offsetY = (int) Math.round(this.bounds.getY());
      final double scaledIconWidth = ScalableIcon.BASE_WIDTH * this.scale;
    }
  }

  @Nullable
  public AbstractPlugin findPluginForPoint(final double x, final double y) {
    AbstractPlugin result = null;
    if (this.hasContent() && this.bounds.contains(x, y)) {
      final double iconWidth = this.scale * ScalableIcon.BASE_WIDTH;
      final int index = (int) ((x - this.bounds.getX()) / iconWidth);
    }
    return result;
  }

  @Nonnull
  public Rectangle2D getBounds() {
    return this.bounds;
  }

}
