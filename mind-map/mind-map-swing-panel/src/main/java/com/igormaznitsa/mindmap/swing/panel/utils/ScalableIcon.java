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

import java.awt.Image;
import java.io.InputStream;
import javax.imageio.ImageIO;

public final class ScalableIcon {

  public static final ScalableIcon FILE = new ScalableIcon("extra_file.png");
  public static final ScalableIcon FILE_WARN = new ScalableIcon("extra_file_warn.png");
  public static final ScalableIcon FILE_MMD = new ScalableIcon("extra_mmd.png");
  public static final ScalableIcon FILE_MMD_WARN = new ScalableIcon("extra_mmd_warn.png");
  public static final ScalableIcon FILE_PLANTUML = new ScalableIcon("extra_plantuml.png");
  public static final ScalableIcon FILE_PLANTUML_WARN = new ScalableIcon("extra_plantuml_warn.png");
  public static final ScalableIcon TOPIC = new ScalableIcon("extra_topic.png");
  public static final ScalableIcon TEXT = new ScalableIcon("extra_note.png");
  public static final ScalableIcon LINK = new ScalableIcon("extra_uri.png");
  public static final ScalableIcon LINK_EMAIL = new ScalableIcon("extra_email.png");

  public static final int BASE_WIDTH = 16;
  public static final int BASE_HEIGHT = 16;
  private final Image baseImage;
  private final float baseScaleX;
  private final float baseScaleY;
  private volatile double scale = -1.0d;
  private Image scaledCachedImage;

  public ScalableIcon(final Image image) {
    this.baseImage = requireNonNull(image, "Image must not be null");
    this.baseScaleX = (float) BASE_WIDTH / (float) this.baseImage.getWidth(null);
    this.baseScaleY = (float) BASE_HEIGHT / (float) this.baseImage.getHeight(null);
  }

  private ScalableIcon(final String name) {
    this(loadStandardImage(name));
  }

  public static Image loadStandardImage(final String name) {
    try (final InputStream in = ScalableIcon.class.getClassLoader()
        .getResourceAsStream("com/igormaznitsa/mindmap/swing/panel/icons/" + name)) {
      return ImageIO.read(requireNonNull(in));
    } catch (Exception ex) {
      throw new IllegalStateException("Can't load resource image " + name, ex);
    }
  }

  public double getScale() {
    return this.scale;
  }

  public Image getImage(final double scale) {
    if (Double.compare(this.scale, scale) != 0) {
      this.scaledCachedImage = null;
    }

    if (this.scaledCachedImage == null) {
      this.scaledCachedImage =
          Utils.scaleImage(this.baseImage, this.baseScaleX, this.baseScaleY, scale);
      this.scale = scale;
    }
    return this.scaledCachedImage;
  }
}
