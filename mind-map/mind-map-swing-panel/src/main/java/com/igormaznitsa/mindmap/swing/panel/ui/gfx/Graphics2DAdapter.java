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
package com.igormaznitsa.mindmap.swing.panel.ui.gfx;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Graphics2DAdapter extends Graphics2D {

  @Override
  public void draw(@Nonnull final Shape s) {
  }

  @Override
  public boolean drawImage(@Nonnull final Image img, @Nonnull final AffineTransform xform, @Nullable final ImageObserver obs) {
    return false;
  }

  @Override
  public void drawImage(@Nonnull final BufferedImage img, @Nonnull final BufferedImageOp op, final int x, final int y) {
  }

  @Override
  public void drawRenderedImage(@Nonnull final RenderedImage img, @Nonnull final AffineTransform xform) {
  }

  @Override
  public void drawRenderableImage(@Nonnull final RenderableImage img, @Nonnull final AffineTransform xform) {
  }

  @Override
  public void drawString(@Nonnull final String str, final int x, final int y) {
  }

  @Override
  public void drawString(@Nonnull final String str, final float x, final float y) {
  }

  @Override
  public void drawString(@Nonnull final AttributedCharacterIterator iterator, final int x, final int y) {
  }

  @Override
  public void drawString(@Nonnull final AttributedCharacterIterator iterator, final float x, final float y) {
  }

  @Override
  public void drawGlyphVector(@Nonnull final GlyphVector g, final float x, final float y) {
  }

  @Override
  public void fill(@Nonnull final Shape s) {
  }

  @Override
  public boolean hit(@Nonnull final Rectangle rect, @Nonnull final Shape s, final boolean onStroke) {
    return false;
  }

  @Override
  @Nullable
  public GraphicsConfiguration getDeviceConfiguration() {
    return null;
  }

  @Override
  public void setComposite(@Nullable final Composite comp) {
  }

  @Override
  public void setPaint(@Nullable final Paint paint) {
  }

  @Override
  public void setStroke(@Nullable final Stroke s) {
  }

  @Override
  public void setRenderingHint(@Nonnull final RenderingHints.Key hintKey, @Nonnull final Object hintValue) {
  }

  @Override
  @Nullable
  public Object getRenderingHint(@Nonnull final RenderingHints.Key hintKey) {
    return null;
  }

  @Override
  public void setRenderingHints(@Nonnull final Map<?, ?> hints) {
  }

  @Override
  public void addRenderingHints(@Nonnull final Map<?, ?> hints) {
  }

  @Override
  @Nullable
  public RenderingHints getRenderingHints() {
    return null;
  }

  @Override
  public void translate(final int x, final int y) {
  }

  @Override
  public void translate(final double tx, final double ty) {
  }

  @Override
  public void rotate(final double theta) {
  }

  @Override
  public void rotate(final double theta, final double x, final double y) {
  }

  @Override
  public void scale(final double sx, final double sy) {
  }

  @Override
  public void shear(final double shx, final double shy) {
  }

  @Override
  public void transform(@Nullable final AffineTransform Tx) {
  }

  @Override
  public void setTransform(@Nullable final AffineTransform Tx) {
  }

  @Override
  @Nullable
  public AffineTransform getTransform() {
    return null;
  }

  @Override
  @Nullable
  public Paint getPaint() {
    return null;
  }

  @Override
  @Nullable
  public Composite getComposite() {
    return null;
  }

  @Override
  public void setBackground(@Nonnull final Color color) {
  }

  @Override
  @Nullable
  public Color getBackground() {
    return null;
  }

  @Override
  @Nullable
  public Stroke getStroke() {
    return null;
  }

  @Override
  public void clip(@Nullable final Shape s) {
  }

  @Override
  @Nullable
  public FontRenderContext getFontRenderContext() {
    return null;
  }

  @Override
  @Nullable
  public Graphics create() {
    return null;
  }

  @Override
  @Nullable
  public Color getColor() {
    return null;
  }

  @Override
  public void setColor(@Nullable final Color c) {
  }

  @Override
  public void setPaintMode() {
  }

  @Override
  public void setXORMode(@Nullable final Color c1) {
  }

  @Override
  @Nullable
  public Font getFont() {
    return null;
  }

  @Override
  public void setFont(@Nullable final Font font) {
  }

  @Override
  @Nullable
  public FontMetrics getFontMetrics(@Nullable final Font f) {
    return null;
  }

  @Override
  @Nullable
  public Rectangle getClipBounds() {
    return null;
  }

  @Override
  public void clipRect(final int x, final int y, final int width, final int height) {
  }

  @Override
  public void setClip(final int x, final int y, final int width, final int height) {
  }

  @Override
  @Nullable
  public Shape getClip() {
    return null;
  }

  @Override
  public void setClip(@Nullable final Shape clip) {
  }

  @Override
  public void copyArea(final int x, final int y, final int width, final int height, final int dx, final int dy) {
  }

  @Override
  public void drawLine(final int x1, final int y1, final int x2, final int y2) {
  }

  @Override
  public void fillRect(final int x, final int y, final int width, final int height) {
  }

  @Override
  public void clearRect(final int x, final int y, final int width, final int height) {
  }

  @Override
  public void drawRoundRect(final int x, final int y, final int width, final int height, final int arcWidth, final int arcHeight) {
  }

  @Override
  public void fillRoundRect(final int x, final int y, final int width, final int height, final int arcWidth, final int arcHeight) {
  }

  @Override
  public void drawOval(final int x, final int y, final int width, final int height) {
  }

  @Override
  public void fillOval(final int x, final int y, final int width, final int height) {
    
  }

  @Override
  public void drawArc(final int x, final int y, final int width, final int height, final int startAngle, final int arcAngle) {
  }

  @Override
  public void fillArc(final int x, final int y, final int width, final int height, final int startAngle, final int arcAngle) {
  }

  @Override
  public void drawPolyline(@Nullable final int[] xPoints, @Nullable final int[] yPoints, final int nPoints) {
  }

  @Override
  public void drawPolygon(@Nullable final int[] xPoints, @Nullable final int[] yPoints, final int nPoints) {
  }

  @Override
  public void fillPolygon(@Nullable final int[] xPoints, @Nullable final int[] yPoints, final int nPoints) {
  }

  @Override
  public boolean drawImage(@Nullable final Image img, final int x, final int y, @Nullable final ImageObserver observer) {
    return false;
  }

  @Override
  public boolean drawImage(@Nullable final Image img, final int x, final int y, final int width, final int height, @Nullable final ImageObserver observer) {
    return false;
  }

  @Override
  public boolean drawImage(@Nullable final Image img, final int x, final int y, @Nullable final Color bgcolor, @Nullable final ImageObserver observer) {
    return false;
  }

  @Override
  public boolean drawImage(@Nullable final Image img, final int x, final int y, final int width, final int height, @Nullable final Color bgcolor, @Nullable final ImageObserver observer) {
    return false;
  }

  @Override
  public boolean drawImage(@Nullable final Image img, final int dx1, final int dy1, final int dx2, final int dy2, final int sx1, final int sy1, final int sx2, final int sy2, @Nullable final ImageObserver observer) {
    return false;
  }

  @Override
  public boolean drawImage(@Nullable final Image img, final int dx1, final int dy1, final int dx2, final int dy2, final int sx1, final int sy1, final int sx2, final int sy2, @Nullable final Color bgcolor, @Nullable final ImageObserver observer) {
    return false;
  }

  @Override
  public void dispose() {
  }
  
}
