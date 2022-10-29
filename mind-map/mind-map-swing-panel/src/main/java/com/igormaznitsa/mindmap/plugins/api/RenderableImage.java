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

package com.igormaznitsa.mindmap.plugins.api;

import static java.util.Objects.requireNonNull;

import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.mindmap.swing.panel.ui.gfx.MMGraphics;
import java.awt.Image;

public class RenderableImage implements Renderable {
  private final Image image;

  public RenderableImage(final Image image) {
    this.image = requireNonNull(image);
  }

  @Override
  public int getWidth(final double scale) {
    return this.image.getWidth(null);
  }

  @Override
  public int getHeight(final double scale) {
    return this.image.getHeight(null);
  }

  @Override
  public void renderAt(final MMGraphics gfx, final MindMapPanelConfig config, final int x,
                       final int y) {
    gfx.drawImage(this.image, x, y);
  }

}
