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
package com.igormaznitsa.mindmap.swing.panel.ui.gfx;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MMGraphics2DWrapper implements MMGraphics {

  private final Graphics2D wrapped;
  private StrokeType strokeType = StrokeType.SOLID;
  private float strokeWidth = 1.0f;

  public MMGraphics2DWrapper(@Nonnull final Graphics2D wrapped) {
    this.wrapped = wrapped;
    this.wrapped.setStroke(new BasicStroke(this.strokeWidth));
  }

  @Nonnull
  public Graphics2D getWrappedGraphics() {
    return this.wrapped;
  }

  @Override
  public void setClip(final int x, final int y, final int w, final int h) {
    this.wrapped.setClip(x, y, w, h);
  }

  @Override
  public void drawRect(final int x, final int y, final int width, final int height, @Nullable final Color border, @Nullable final Color fill) {
    if (fill != null) {
      this.wrapped.setColor(fill);
      this.wrapped.fillRect(x, y, width, height);
    }

    if (border != null) {
      this.wrapped.setColor(border);
      this.wrapped.drawRect(x, y, width, height);
    }
  }

  @Override
  @Nonnull
  public MMGraphics copy() {
    final MMGraphics2DWrapper result = new MMGraphics2DWrapper((Graphics2D) wrapped.create());
    result.strokeType = this.strokeType;
    result.strokeWidth = this.strokeWidth;
    return result;
  }

  @Override
  public void dispose() {
    this.wrapped.dispose();
  }

  @Override
  public void translate(final double x, final double y) {
    this.wrapped.translate(x, y);
  }

  @Override
  @Nullable
  public Rectangle getClipBounds() {
    return this.wrapped.getClipBounds();
  }

  @Override
  public void setStroke(@Nonnull final float width, @Nonnull final StrokeType type) {
    if (type != this.strokeType || Float.compare(this.strokeWidth, width) != 0) {
      this.strokeType = type;
      this.strokeWidth = width;

      final Stroke stroke;

      switch (type) {
        case SOLID:
          stroke = new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
          break;
        case DASHES:
          stroke = new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{width * 3.0f, width}, 0.0f);
          break;
        case DOTS:
          stroke = new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{width, width * 2.0f}, 0.0f);
          break;
        default:
          throw new Error("Unexpected stroke type : " + type);
      }
      this.wrapped.setStroke(stroke);
    }
  }

  @Override
  public void drawLine(final int startX, final int startY, final int endX, final int endY, @Nullable final Color color) {
    if (color != null) {
      this.wrapped.setColor(color);
      this.wrapped.drawLine(startX, startY, endX, endY);
    }
  }

  @Override
  public void draw(@Nonnull final Shape shape, @Nullable final Color border, @Nullable final Color fill) {
    if (fill != null) {
      this.wrapped.setColor(fill);
      this.wrapped.fill(shape);
    }

    if (border != null) {
      this.wrapped.setColor(border);
      this.wrapped.draw(shape);
    }
  }

  @Override
  public void drawCurve(final double startX, final double startY, final double endX, final double endY, @Nullable final Color color) {
    final Path2D path = new Path2D.Double();
    path.moveTo(startX, startY);
    path.curveTo(startX, endY, startX, endY, endX, endY);
    if (color != null) {
      this.wrapped.setColor(color);
    }
    this.wrapped.draw(path);
  }

  @Override
  public void drawOval(final int x, final int y, final int w, final int h, @Nullable final Color border, @Nullable final Color fill) {
    if (fill != null) {
      this.wrapped.setColor(fill);
      this.wrapped.fillOval(x, y, w, h);
    }

    if (border != null) {
      this.wrapped.setColor(border);
      this.wrapped.drawOval(x, y, w, h);
    }
  }

  @Override
  public void drawImage(@Nullable final Image image, final int x, final int y) {
    if (image != null) {
      this.wrapped.drawImage(image, x, y, null);
    }
  }

  @Override
  public float getFontMaxAscent() {
    return this.wrapped.getFontMetrics().getMaxAscent();
  }

  @Override
  @Nonnull
  public Rectangle2D getStringBounds(@Nonnull final String str) {
    return this.wrapped.getFontMetrics().getStringBounds(str, this.wrapped);
  }

  @Override
  public void setFont(@Nonnull final Font font) {
    this.wrapped.setFont(font);
  }

  @Override
  public void drawString(@Nonnull final String text, final int x, final int y, @Nullable Color color) {
    if (color != null && this.wrapped.getFont().getSize2D() > 1.0f) {
      this.wrapped.setColor(color);
      this.wrapped.drawString(text, x, y);
    }
  }

}
