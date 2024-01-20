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

package com.igormaznitsa.mindmap.swing.panel.utils;

import static java.util.Objects.requireNonNull;

import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import org.apache.commons.io.IOUtils;

public class MiscIcons {

  private static final Logger LOGGER = LoggerFactory.getLogger(MiscIcons.class);

  private static final Map<String, ImageContainer> IMAGE_CACHE;
  private static final String[] ICON_NAMES;

  static {
    final InputStream iconListReadStream =
        MiscIcons.class.getResourceAsStream("/com/igormaznitsa/mindmap/swing/miscicons/icon.lst");
    final Map<String, ImageContainer> imageContainers = new HashMap<>();

    try {
      imageContainers.put("empty", new ImageContainer("empty"));

      final List<String> lines =
          IOUtils.readLines(requireNonNull(iconListReadStream), StandardCharsets.UTF_8);
      ICON_NAMES = lines.toArray(new String[0]);
      for (final String icon : ICON_NAMES) {
        imageContainers.put(icon, new ImageContainer(icon));
      }
    } catch (Exception ex) {
      throw new Error("Can't read list of icons", ex);
    } finally {
      IOUtils.closeQuietly(iconListReadStream);
    }

    IMAGE_CACHE = Collections.unmodifiableMap(imageContainers);

    final Thread loadingDaemon = new Thread(() -> {
      LOGGER.info("Loading daemon started");
      for (final String s : ICON_NAMES) {
        findForName(s);
      }
      LOGGER.info("Loading daemon completed");
    }, "mindmap-emoticon-loading");
    loadingDaemon.setDaemon(true);
    loadingDaemon.setPriority(Thread.MIN_PRIORITY);
    loadingDaemon.start();
  }

  public static Image findForName(final String name) {
    final ImageContainer result = IMAGE_CACHE.get(name);
    return result == null ? null : result.getImage();
  }

  public static String[] getNames() {
    return ICON_NAMES.clone();
  }

  private static final class ImageContainer {

    private final String name;
    private volatile Image image;

    private ImageContainer(final String name) {
      this.name = name;
      if ("empty".equals(name)) {
        this.image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
      }
    }

    private synchronized Image getImage() {
      if (this.image == null) {
        this.image = loadImage(this.name);
      }
      return this.image;
    }

    private Image loadImage(final String name) {
      final InputStream in = MiscIcons.class.getResourceAsStream(
          "/com/igormaznitsa/mindmap/swing/miscicons/" + name + ".png");
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
  }
}
