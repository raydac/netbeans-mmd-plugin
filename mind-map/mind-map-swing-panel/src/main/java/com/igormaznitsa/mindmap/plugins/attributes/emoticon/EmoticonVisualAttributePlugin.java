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

package com.igormaznitsa.mindmap.plugins.attributes.emoticon;

import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.plugins.api.MindMapPlugin;
import com.igormaznitsa.mindmap.plugins.api.Renderable;
import com.igormaznitsa.mindmap.plugins.api.RenderableImage;
import com.igormaznitsa.mindmap.plugins.api.VisualAttributePlugin;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.mindmap.swing.panel.utils.MiscIcons;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import java.awt.Image;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EmoticonVisualAttributePlugin implements VisualAttributePlugin {

  static final String ATTR_KEY = "mmd.emoticon";

  private final Map<String, ScaledImage> SCALED_IMAGE_CACHE = new HashMap<String, ScaledImage>();

  @Override
  @Nullable
  public Renderable getScaledImage(@Nonnull final MindMapPanelConfig config, @Nonnull final Topic topic) {
    final String name = topic.getAttribute(ATTR_KEY);
    if (name == null) {
      return null;
    } else {
      ScaledImage scaled = SCALED_IMAGE_CACHE.get(name);
      if (scaled == null) {
        scaled = new ScaledImage(name, config.getScale());
        SCALED_IMAGE_CACHE.put(name, scaled);
      }
      return scaled.getImage(config.getScale());
    }
  }

  @Override
  public boolean onClick(@Nonnull MindMapPanel panel, @Nonnull final Topic topic, int clickCount) {
    return false;
  }

  @Override
  @Nullable
  public String getToolTip(@Nonnull final MindMapPanel panel, @Nonnull final Topic topic) {
    return topic.getAttribute(ATTR_KEY);
  }

  @Override
  public boolean isClickable(@Nonnull final MindMapPanel panel, @Nonnull final Topic topic) {
    return false;
  }

  @Override
  @Nonnull
  public String getAttributeKey() {
    return ATTR_KEY;
  }

  @Override
  public int getOrder() {
    return Integer.MIN_VALUE;
  }

  @Override
  public int compareTo(@Nonnull final MindMapPlugin o) {
    if (this.getOrder() == o.getOrder()) {
      return 0;
    } else if (this.getOrder() < o.getOrder()) {
      return -1;
    } else {
      return 1;
    }
  }

  private static final class ScaledImage {

    private static final int ICON_SIZE = 32;

    private final double BASE_SCALE_X;
    private final double BASE_SCALE_Y;
    private final Image baseImage;
    private double scale = -1.0d;
    private Renderable scaledImage;

    public ScaledImage(@Nonnull final String imageName, final double scale) {
      this.baseImage = MiscIcons.findForName(imageName);
      if (this.baseImage != null) {
        this.BASE_SCALE_X = (double) ICON_SIZE / (double) this.baseImage.getWidth(null);
        this.BASE_SCALE_Y = (double) ICON_SIZE / (double) this.baseImage.getHeight(null);
      } else {
        this.BASE_SCALE_X = 1.0d;
        this.BASE_SCALE_Y = 1.0d;
      }
      getImage(scale);
    }

    @Nullable
    public Renderable getImage(final double scale) {
      final Renderable result;
      if (this.baseImage == null || Double.compare(this.scale, scale) == 0) {
        result = this.scaledImage;
      } else {
        this.scale = scale;
        final Image scaled = Utils.scaleImage(this.baseImage, BASE_SCALE_X, BASE_SCALE_Y, scale);
        if (scaled == null) {
          result = null;
          this.scaledImage = null;
        } else {
          this.scaledImage = new RenderableImage(scaled);
          result = this.scaledImage;
        }
      }
      return result;
    }

  }

}
