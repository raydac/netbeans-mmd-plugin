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
import java.awt.Font;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface MMGraphics {
  @Nonnull
  MMGraphics copy();

  void dispose();

  void translate(double x, double y);

  @Nullable
  Rectangle getClipBounds();

  void setStroke(float width, @Nonnull StrokeType type);

  void drawLine(int startX, int startY, int endX, int endY, @Nullable Color color);

  void drawRect(int x, int y, int width, int height, @Nullable Color border, @Nullable Color fill);

  void draw(@Nonnull Shape shape, @Nullable Color border, @Nullable Color fill);

  void drawCurve(double startX, double startY, double endX, double endY, @Nullable Color color);

  void drawOval(int x, int y, int w, int h, @Nullable Color border, @Nullable Color fill);

  public void drawImage(@Nullable Image image, int x, int y);

  void setFont(@Nonnull Font font);

  float getFontMaxAscent();

  void setClip(int x, int y, int w, int h);

  @Nonnull
  Rectangle2D getStringBounds(@Nonnull String s);

  void drawString(@Nonnull String text, int x, int y, @Nullable Color fill);

}
