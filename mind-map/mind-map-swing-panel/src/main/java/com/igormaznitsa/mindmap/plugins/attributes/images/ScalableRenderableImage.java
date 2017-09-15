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
package com.igormaznitsa.mindmap.plugins.attributes.images;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.igormaznitsa.mindmap.plugins.api.Renderable;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.mindmap.swing.panel.ui.gfx.MMGraphics;

final class ScalableRenderableImage implements Renderable {

  private final Image baseImage;
  private final int width;
  private final int height;
  private final Map<Double, SoftReference<Image>> cache = new HashMap<Double, SoftReference<Image>>();
  private double cachedScale = Double.MIN_VALUE;

  public ScalableRenderableImage(@Nonnull final Image baseImage) {
    this.baseImage = baseImage;
    this.width = baseImage.getWidth(null);
    this.height = baseImage.getHeight(null);
  }

  @Override
  public int getWidth(final double scale) {
    return (int) Math.round(this.width * scale);
  }

  @Override
  public int getHeight(final double scale) {
    return (int) Math.round(this.height * scale);
  }

  @Nullable
  private Image getCached(final double scale) {
    Image result;
    synchronized (this.cache) {
      final SoftReference<Image> cachedImage = this.cache.get(scale);
      result = cachedImage == null ? null : cachedImage.get();
      if (result == null || Double.compare(this.cachedScale, scale) != 0) {
        final int sw = getWidth(scale);
        final int sh = getHeight(scale);

        if (sw != 0 && sh != 0) {
          final BufferedImage scaled = new BufferedImage(sw, sh, BufferedImage.TYPE_INT_ARGB);
          final Graphics2D gfx = scaled.createGraphics();

          gfx.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
          gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
          gfx.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
          gfx.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
          gfx.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);

          gfx.drawImage(this.baseImage, AffineTransform.getScaleInstance(scale, scale), null);
          gfx.dispose();
          result = scaled;
          this.cache.put(scale, new SoftReference<Image>(result));
          this.cachedScale = scale;
        } else {
          result = null;
          this.cachedScale = scale;
        }
      }
    }
    return result;
  }

  @Override
  public void renderAt(@Nonnull final MMGraphics gfx, @Nonnull final MindMapPanelConfig config, final int x, final int y) {
    final Image image = getCached(config.getScale());
    gfx.drawImage(image, x, y);
  }

}
