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
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.plugins.MindMapPluginRegistry;
import com.igormaznitsa.mindmap.plugins.attributes.VisualAttributePlugin;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;

public class VisualAttributeImageBlock {

  private final Rectangle2D bounds = new Rectangle2D.Double();
  private final Topic model;
  private boolean contentPresented;

  private VisualItem[] items = null;

  private static class VisualItem {

    private static final VisualItem[] EMPTY = new VisualItem[0];

    private final VisualAttributeImageBlock parent;
    private final Image image;
    private final VisualAttributePlugin plugin;
    private final int relx;
    private int rely;
    private final int width;
    private final int height;

    VisualItem(@Nonnull final VisualAttributeImageBlock parent, final int x, final int y, @Nonnull final VisualAttributePlugin plugin, @Nullable final Image image) {
      this.parent = parent;
      this.image = image;
      this.relx = x;
      this.rely = y;
      this.width = image == null ? 0 : image.getWidth(null);
      this.height = image == null ? 0 : image.getHeight(null);
      this.plugin = plugin;
    }

    @Nonnull
    VisualAttributePlugin getPlugin() {
      return this.plugin;
    }

    void toHCenter(final int maxHeight) {
      this.rely = (maxHeight - this.height) / 2;
    }

    int getWidth() {
      return this.width;
    }

    int getHeight() {
      return this.height;
    }

    boolean isShown() {
      return this.image != null;
    }

    boolean containsPoint(final int relativeX, final int relativeY) {
      return relativeX >= this.relx && relativeY >= this.rely && relativeX < this.relx + this.width && relativeY < this.rely + this.height;
    }

    void draw(@Nonnull final Graphics2D gfx, final int basex, final int basey) {
      gfx.drawImage(this.image, basex + this.relx, basey + this.rely, null);
    }
  }

  public VisualAttributeImageBlock(@Nonnull final VisualAttributeImageBlock orig) {
    this.bounds.setRect(orig.bounds);
    this.model = orig.model;
    this.contentPresented = orig.contentPresented;
  }

  public VisualAttributeImageBlock(@Nonnull final Topic model) {
    this.model = model;
  }

  public void setCoordOffset(final double x, final double y) {
    this.bounds.setRect(x, y, this.bounds.getWidth(), this.bounds.getHeight());
  }

  public void updateSize(@Nonnull final Graphics2D gfx, @Nonnull final MindMapPanelConfig cfg) {
    final List<VisualAttributePlugin> plugins = MindMapPluginRegistry.getInstance().findFor(VisualAttributePlugin.class);
    int x = 0;
    int maxheight = 0;
    if (plugins.isEmpty()) {
      this.items = VisualItem.EMPTY;
    } else {
      final List<VisualAttributePlugin> detected = new ArrayList<VisualAttributePlugin>();
      final Map<String, String> attributes = this.model.getAttributes();
      for (final VisualAttributePlugin p : detected) {
        if (attributes.containsKey(p.getAttributeKey())) {
          detected.add(p);
        }
      }
      int index = 0;
      this.items = new VisualItem[detected.size()];
      for (final VisualAttributePlugin p : detected) {
        final VisualItem item = new VisualItem(this, x, 0, p, p.getScaledImage(cfg, this.model));
        this.items[index++] = item;
        x += item.getWidth();
        maxheight = Math.max(maxheight, item.getHeight());
      }
      for (final VisualItem i : this.items) {
        i.toHCenter(maxheight);
      }
    }

    this.bounds.setRect(0d, 0d, x, maxheight);
  }

  public boolean mayHaveContent() {
    return this.items == null || this.items.length > 0;
  }

  public void paint(@Nonnull final Graphics2D gfx, @Nonnull final MindMapPanelConfig cfg) {
    if (this.items == null) {
      updateSize(gfx, cfg);
    }

    int offsetX = (int) Math.round(this.bounds.getX());
    final int offsetY = (int) Math.round(this.bounds.getY());
    for (final VisualItem i : this.items) {
      i.draw(gfx, offsetX, offsetY);
    }
  }

  @Nullable
  public VisualAttributePlugin findPluginForPoint(final double x, final double y) {
    VisualAttributePlugin result = null;
    if (this.items != null && this.bounds.contains(x, y)) {
      final int px = (int) Math.round(x - this.bounds.getX());
      final int py = (int) Math.round(y - this.bounds.getY());
      for (final VisualItem i : this.items) {
        if (i.containsPoint(px, py)) {
          result = i.getPlugin();
          break;
        }
      }
    }
    return result;
  }

  @Nonnull
  public Rectangle2D getBounds() {
    return this.bounds;
  }

}
