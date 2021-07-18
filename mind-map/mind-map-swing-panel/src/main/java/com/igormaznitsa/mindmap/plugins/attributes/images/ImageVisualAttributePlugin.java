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

package com.igormaznitsa.mindmap.plugins.attributes.images;

import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.MMapURI;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.plugins.api.MindMapPlugin;
import com.igormaznitsa.mindmap.plugins.api.PluginContext;
import com.igormaznitsa.mindmap.plugins.api.Renderable;
import com.igormaznitsa.mindmap.plugins.api.VisualAttributePlugin;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;

public class ImageVisualAttributePlugin implements VisualAttributePlugin {

  public static final String ATTR_KEY = "mmd.image";
  public static final String ATTR_IMAGE_NAME = "mmd.image.name";
  public static final String ATTR_IMAGE_URI_KEY = "mmd.image.uri";

  private static final Logger LOGGER = LoggerFactory.getLogger(ImageVisualAttributePlugin.class);
  private static final Map<Topic, Renderable> CACHED_IMAGES = new WeakHashMap<>();

  public static void clearCachedImages() {
    CACHED_IMAGES.clear();
  }

  @Override
  public boolean doesTopicContentMatches(
      @Nonnull Topic topic,
      @Nullable File baseFolder,
      @Nonnull Pattern pattern,
      @Nullable Set<Extra.ExtraType> extraTypes
  ) {
    boolean result = false;
    if (extraTypes != null && topic.getAttribute(ATTR_KEY) != null) {
      if (extraTypes.contains(Extra.ExtraType.NOTE)) {
        final String text = topic.getAttribute(ATTR_IMAGE_NAME);
        if (text != null) {
          result = pattern.matcher(text).find();
        }
      }
      if (!result &&
          (extraTypes.contains(Extra.ExtraType.LINK) ||
              extraTypes.contains(Extra.ExtraType.FILE))) {
        final String text = topic.getAttribute(ATTR_IMAGE_URI_KEY);
        if (text != null) {
          result =
              pattern.matcher(MMapURI.makeFromFilePath(baseFolder, text, null).toString()).find();
        }
      }
    }
    return result;
  }

  @Override
  @Nullable
  public Renderable getScaledImage(@Nonnull final MindMapPanelConfig config,
                                   @Nonnull final Topic activeTopic) {
    Renderable result = CACHED_IMAGES.get(activeTopic);
    if (result == null) {
      result = new ScalableRenderableImage(extractImage(activeTopic));
      CACHED_IMAGES.put(activeTopic, result);
    }
    return result;
  }

  @Nullable
  private Image extractImage(@Nonnull final Topic topic) {
    Image result = null;
    final String encoded = topic.getAttribute(ATTR_KEY);
    if (encoded != null) {
      try {
        result = ImageIO.read(new ByteArrayInputStream(Utils.base64decode(encoded)));
      } catch (Exception ex) {
        LOGGER.error("Can't extract image", ex);
      }
    }
    return result;
  }

  @Override
  public boolean onClick(@Nonnull final PluginContext context, @Nonnull final Topic topic,
                         final boolean activeGroupModifier, final int clickCount) {
    if (clickCount > 1) {
      final String imageFilePathUri = topic.getAttribute(ATTR_IMAGE_URI_KEY);
      if (imageFilePathUri != null) {
        try {
          context.openFile(new MMapURI(imageFilePathUri).asFile(context.getProjectFolder()), false);
        } catch (URISyntaxException ex) {
          context.getDialogProvider()
              .msgWarn(context.getPanel(), "URI syntax exception: " + imageFilePathUri);
        }
      }
    } else {
      if (!activeGroupModifier) {
        context.getPanel().removeAllSelection();
      }
      context.getPanel().select(topic, false);
    }
    return false;
  }

  @Override
  @Nullable
  public String getToolTip(@Nonnull final PluginContext context, @Nonnull final Topic activeTopic) {
    String result = activeTopic.getAttribute(ATTR_IMAGE_URI_KEY);
    if (result == null) {
      result = activeTopic.getAttribute(ATTR_IMAGE_NAME);
    }
    return result;
  }

  @Override
  public boolean isClickable(@Nonnull final PluginContext context,
                             @Nonnull final Topic activeTopic) {
    final String imageFilePath = activeTopic.getAttribute(ATTR_IMAGE_URI_KEY);
    return imageFilePath != null;
  }

  @Override
  @Nonnull
  public String getAttributeKey() {
    return ATTR_KEY;
  }

  @Override
  public int getOrder() {
    return Integer.MIN_VALUE + 100;
  }

  @Override
  public int compareTo(@Nonnull final MindMapPlugin o) {
    return Integer.compare(this.getOrder(), o.getOrder());
  }

}
