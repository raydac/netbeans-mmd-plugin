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
package com.igormaznitsa.mindmap.swing.panel.utils;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import org.apache.commons.io.IOUtils;
import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;

public class MiscIcons {

  private static final Logger LOGGER = LoggerFactory.getLogger(MiscIcons.class);

  private static final String[] ICON_NAMES;

  static {
    final InputStream iconListReadStream = MiscIcons.class.getResourceAsStream("/com/igormaznitsa/mindmap/swing/miscicons/icon.lst");
    try {
      final List<String> lines = IOUtils.readLines(iconListReadStream);
      ICON_NAMES = lines.toArray(new String[lines.size()]);
    } catch (Exception ex) {
      throw new Error("Can't read list of icons", ex);
    } finally {
      IOUtils.closeQuietly(iconListReadStream);
    }
  }

  private static final Map<String, Image> IMAGE_CACHE = new HashMap<String, Image>();

  @Nullable
  private static Image loadImage(@Nonnull final String name) {
    if ("empty".equals(name)) {
      final BufferedImage result = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
      return result;
    }
    final InputStream in = MiscIcons.class.getResourceAsStream("/com/igormaznitsa/mindmap/swing/miscicons/" + name + ".png");
    if (in == null) {
      return null;
    }
    try {
      return ImageIO.read(in);
    } catch (IOException ex) {
      LOGGER.error("IO exception for icon '" + name + '\'');
      return null;
    } finally {
      IOUtils.closeQuietly(in);
    }
  }

  @Nullable
  public static Image findForName(@Nonnull final String name) {
    synchronized (IMAGE_CACHE) {
      Image img = IMAGE_CACHE.get(name);
      if (img == null) {
        img = loadImage(name);
        if (img != null) {
          IMAGE_CACHE.put(name, img);
        }
      }
      return img;
    }
  }

  @Nonnull
  @MustNotContainNull
  public static String[] getNames() {
    return ICON_NAMES.clone();
  }
}
