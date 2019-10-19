/*
 * Copyright 2018 Igor Maznitsa.
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

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;

public enum RenderQuality {
  DEFAULT,
  SPEED,
  QUALITY;

  private static final Map<RenderingHints.Key, Object> RENDERING_HINTS_DEFAULT = new HashMap<RenderingHints.Key, Object>();
  private static final Map<RenderingHints.Key, Object> RENDERING_HINTS_SPEED = new HashMap<RenderingHints.Key, Object>();
  private static final Map<RenderingHints.Key, Object> RENDERING_HINTS_QUALTY = new HashMap<RenderingHints.Key, Object>();

  static {
    RENDERING_HINTS_QUALTY.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    RENDERING_HINTS_QUALTY.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    RENDERING_HINTS_QUALTY.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    RENDERING_HINTS_QUALTY.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    RENDERING_HINTS_QUALTY.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
    RENDERING_HINTS_QUALTY.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);

    RENDERING_HINTS_DEFAULT.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_DEFAULT);
    RENDERING_HINTS_DEFAULT.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
    RENDERING_HINTS_DEFAULT.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT);
    RENDERING_HINTS_DEFAULT.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    RENDERING_HINTS_DEFAULT.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_DEFAULT);
    RENDERING_HINTS_DEFAULT.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_DEFAULT);

    RENDERING_HINTS_SPEED.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
    RENDERING_HINTS_SPEED.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    RENDERING_HINTS_SPEED.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
    RENDERING_HINTS_SPEED.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
    RENDERING_HINTS_SPEED.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
    RENDERING_HINTS_SPEED.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
  }

  public void prepare(@Nonnull final Graphics2D g) {
    switch (this) {
      case QUALITY:
        g.setRenderingHints(RENDERING_HINTS_QUALTY);
        break;
      case DEFAULT:
        g.setRenderingHints(RENDERING_HINTS_DEFAULT);
        break;
      case SPEED:
        g.setRenderingHints(RENDERING_HINTS_SPEED);
        break;
      default:
        throw new Error("Unexpected state : " + this.name());
    }
  }
}
