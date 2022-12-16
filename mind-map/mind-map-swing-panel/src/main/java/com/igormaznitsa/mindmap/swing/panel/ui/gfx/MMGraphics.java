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

package com.igormaznitsa.mindmap.swing.panel.ui.gfx;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

/**
 * Decorator to provide graphic operations
 *
 * @see java.awt.Graphics2D
 */
public interface MMGraphics {
  /**
   * Make copy of graphics.
   *
   * @return copy, must not be null
   */
  MMGraphics copy();

  /**
   * Dispose object, it can't be used after operation.
   */
  void dispose();

  /**
   * Subsequent rendering is translated by the specified distance relative to the previous position.
   *
   * @param x the distance to translate along the x-axis
   * @param y the distance to translate along the y-axis
   */
  void translate(double x, double y);

  /**
   * Returns the bounding rectangle of the current clipping area. This method refers to the user clip,
   * which is independent of the clipping associated with device bounds and window visibility. If no clip has
   * previously been set, or if the clip has been cleared using setClip(null), this method returns null.
   * The coordinates in the rectangle are relative to the coordinate system origin of this graphics context.
   *
   * @return the bounding rectangle of the current clipping area, or null if no clip is set
   */
  Rectangle getClipBounds();

  /**
   * Sets the stroke.
   *
   * @param width width of stroke
   * @param type  type of stroke
   */
  void setStroke(float width, StrokeType type);

  /**
   * Draw line from start point to end point with specified color.
   *
   * @param startX start point X
   * @param startY start point Y
   * @param endX   end point X
   * @param endY   end point Y
   * @param color  color, can be null
   */
  void drawLine(int startX, int startY, int endX, int endY, Color color);

  /**
   * Draw filled rectangle in coordinates.
   *
   * @param x      letf top X
   * @param y      left top Y
   * @param width  rectangle width
   * @param height rectangle height
   * @param border color of border, can be null
   * @param fill   color of fill, can be null
   */
  void drawRect(int x, int y, int width, int height, Color border, Color fill);

  /**
   * Draw shape
   *
   * @param shape  shape to draw, can't be null
   * @param border border color, can be null
   * @param fill   fill color, can be null
   */
  void draw(Shape shape, Color border, Color fill);


  /**
   * Draw curve line
   *
   * @param startX start point X
   * @param startY start point Y
   * @param endX   end point X
   * @param endY   end point Y
   * @param color  color of line, can be null
   */
  void drawCurve(double startX, double startY, double endX, double endY, Color color);

  /**
   * Draw oval
   *
   * @param x      left top X
   * @param y      left top Y
   * @param w      width
   * @param h      height
   * @param border color of border, can be null
   * @param fill   color of fill, can be null
   */
  void drawOval(int x, int y, int w, int h, Color border, Color fill);

  /**
   * Draw image in coordinates
   *
   * @param image image to draw, can't be null
   * @param x     left top X
   * @param y     left top Y
   */
  void drawImage(Image image, int x, int y);

  /**
   * Set current font for draw operations
   *
   * @param font font, null will be ignored
   */
  void setFont(Font font);

  /**
   * Get current font max ascent
   *
   * @return current font max ascent as double
   */
  float getFontMaxAscent();

  /**
   * Set clip area
   *
   * @param x left top X
   * @param y left top Y
   * @param w width
   * @param h height
   */
  void setClip(int x, int y, int w, int h);

  /**
   * Calculate string bounds with current font and return as rectangle.
   *
   * @param text text string, can't be null
   * @return rectangle area bounded string
   */
  Rectangle2D getStringBounds(String text);

  /**
   * Draw string by current font in coordinates.
   *
   * @param text string to be drawn, must not be null
   * @param x    coordinate X
   * @param y    coordinate Y
   * @param fill fill color can be null
   */
  void drawString(String text, int x, int y, Color fill);

}
