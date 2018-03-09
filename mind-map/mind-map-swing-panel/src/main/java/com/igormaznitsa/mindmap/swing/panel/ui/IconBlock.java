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

package com.igormaznitsa.mindmap.swing.panel.ui;

import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.ExtraFile;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.mindmap.swing.panel.ui.gfx.MMGraphics;
import com.igormaznitsa.mindmap.swing.panel.utils.ScalableIcon;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.geom.Rectangle2D;

public class IconBlock {

  private final Rectangle2D bounds = new Rectangle2D.Double();
  private final Topic model;
  private double scale = 1.0d;
  private boolean contentPresented;

  private Extra<?>[] currentExtras = null;

  public IconBlock(@Nonnull final IconBlock orig) {
    this.bounds.setRect(orig.bounds);
    this.model = orig.model;
    this.scale = orig.scale;
    this.contentPresented = orig.contentPresented;
    this.currentExtras = orig.currentExtras == null ? null : orig.currentExtras.clone();
  }

  public IconBlock(@Nonnull final Topic model) {
    this.model = model;
  }

  public void setCoordOffset(final double x, final double y) {
    this.bounds.setRect(x, y, this.bounds.getWidth(), this.bounds.getHeight());
  }

  public void updateSize(@Nonnull final MMGraphics gfx, @Nonnull final MindMapPanelConfig cfg) {
    final int numberOfIcons = this.model.getNumberOfExtras();
    this.scale = cfg.getScale();
    if (numberOfIcons == 0) {
      this.bounds.setRect(0d, 0d, 0d, 0d);
      this.contentPresented = false;
    } else {
      final double scaledIconWidth = ScalableIcon.BASE_WIDTH * this.scale;
      final double scaledIconHeight = ScalableIcon.BASE_HEIGHT * this.scale;
      this.bounds.setRect(0d, 0d, scaledIconWidth * numberOfIcons, scaledIconHeight);
      this.contentPresented = true;
      this.currentExtras = new Extra<?>[numberOfIcons];
      int index = 0;
      for (final Extra<?> e : this.model.getExtras().values()) {
        this.currentExtras[index++] = e;
      }
    }
  }

  public boolean hasContent() {
    return this.currentExtras != null && this.contentPresented;
  }

  public void paint(@Nonnull final MMGraphics gfx) {
    final int numberOfIcons = this.model.getNumberOfExtras();
    if (numberOfIcons != 0) {
      double offsetX = this.bounds.getX();
      final int offsetY = (int) Math.round(this.bounds.getY());
      final double scaledIconWidth = ScalableIcon.BASE_WIDTH * this.scale;
      for (final Extra<?> e : this.currentExtras) {
        final ScalableIcon ico;
        switch (e.getType()) {
          case FILE:
            ico = findIconForFileType((ExtraFile) e);
            break;
          case LINK:
            ico = ScalableIcon.LINK;
            break;
          case NOTE:
            ico = ScalableIcon.TEXT;
            break;
          case TOPIC:
            ico = ScalableIcon.TOPIC;
            break;
          default:
            throw new Error("Unexpected extras"); //NOI18N
        }
        if (scaledIconWidth >= 1.0d) {
          gfx.drawImage(ico.getImage(this.scale), (int) Math.round(offsetX), offsetY);
          offsetX += scaledIconWidth;
        }
      }
    }
  }

  @Nonnull
  public ScalableIcon findIconForFileType(@Nonnull final ExtraFile theFileLink) {
    final ScalableIcon result;
    if (theFileLink.isMMDFile()) {
      result = theFileLink.isAbsolute() ? ScalableIcon.FILE_MMD_WARN : ScalableIcon.FILE_MMD;
    } else if ("plantuml".equals(theFileLink.getLCFileExtension())) {
      result = theFileLink.isAbsolute() ? ScalableIcon.FILE_PLANTUML_WARN : ScalableIcon.FILE_PLANTUML;
    } else {
      result = theFileLink.isAbsolute() ? ScalableIcon.FILE_WARN : ScalableIcon.FILE;
    }
    return result;
  }

  @Nullable
  public Extra<?> findExtraForPoint(final double x, final double y) {
    Extra<?> result = null;
    if (this.hasContent() && this.bounds.contains(x, y)) {
      final double iconWidth = this.scale * ScalableIcon.BASE_WIDTH;
      final int index = (int) ((x - this.bounds.getX()) / iconWidth);
      result = index >= 0 && index < this.currentExtras.length ? this.currentExtras[index] : null;
    }
    return result;
  }

  @Nonnull
  public Rectangle2D getBounds() {
    return this.bounds;
  }

}
