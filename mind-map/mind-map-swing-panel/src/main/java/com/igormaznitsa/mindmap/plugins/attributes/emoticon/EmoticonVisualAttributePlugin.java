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

package com.igormaznitsa.mindmap.plugins.attributes.emoticon;

import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.plugins.api.MindMapPlugin;
import com.igormaznitsa.mindmap.plugins.api.PluginContext;
import com.igormaznitsa.mindmap.plugins.api.Renderable;
import com.igormaznitsa.mindmap.plugins.api.VisualAttributePlugin;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.mindmap.swing.panel.ui.gfx.MMGraphics;
import com.igormaznitsa.mindmap.swing.panel.utils.MiscIcons;
import java.awt.Image;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class EmoticonVisualAttributePlugin implements VisualAttributePlugin {

  static final String ATTR_KEY = "mmd.emoticon";

  private final Map<String, ScaledImage> IMAGE_CACHE = new HashMap<>();

  @Override
  public Renderable getScaledImage(final MindMapPanelConfig config, final Topic topic) {
    final String name = topic.getAttribute(ATTR_KEY);
    if (name == null) {
      return null;
    }

    ScaledImage cached = this.IMAGE_CACHE.get(name);
    if (cached == null) {
      cached = new ScaledImage(name);
      this.IMAGE_CACHE.put(name, cached);
    }
    return cached.hasImage() ? cached : null;
  }

  @Override
  public boolean doesTopicContentMatches(
      final Topic topic,
      final File baseFolder,
      final Pattern pattern,
      final Set<Extra.ExtraType> extraTypes
  ) {
    boolean result = false;
    if (extraTypes != null && extraTypes.contains(Extra.ExtraType.NOTE)) {
      final String name = topic.getAttribute(ATTR_KEY);
      if (name != null) {
        result = pattern.matcher(name).find();
      }
    }
    return result;
  }

  @Override
  public boolean onClick(
      final PluginContext context,
      final Topic topic,
      final boolean activeGroupModifier,
      final int clickCount
  ) {
    return false;
  }

  @Override
  public String getToolTip(final PluginContext context, final Topic topic) {
    return topic.getAttribute(ATTR_KEY);
  }

  @Override
  public boolean isClickable(final PluginContext context, final Topic topic) {
    return false;
  }

  @Override
  public String getAttributeKey() {
    return ATTR_KEY;
  }

  @Override
  public int getOrder() {
    return Integer.MIN_VALUE;
  }

  @Override
  public int compareTo(final MindMapPlugin plugin) {
    return Integer.compare(this.getOrder(), plugin.getOrder());
  }

  private static final class ScaledImage implements Renderable {

    private static final int ICON_SIZE = 32;

    private final Image baseImage;

    private ScaledImage(final String imageName) {
      this.baseImage = MiscIcons.findForName(imageName);
    }

    private boolean hasImage() {
      return this.baseImage != null;
    }

    @Override
    public int getWidth(final double scale) {
      return (int) Math.round(ICON_SIZE * scale);
    }

    @Override
    public int getHeight(final double scale) {
      return (int) Math.round(ICON_SIZE * scale);
    }

    @Override
    public void renderAt(
        final MMGraphics gfx,
        final MindMapPanelConfig config,
        final int x,
        final int y
    ) {
      if (this.baseImage == null) {
        return;
      }

      gfx.drawImage(
          this.baseImage,
          x,
          y,
          ICON_SIZE * config.getScale(),
          ICON_SIZE * config.getScale());
    }
  }
}
