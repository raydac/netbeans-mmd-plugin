/*
 * Copyright 2015 Igor Maznitsa.
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

import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;

import static com.igormaznitsa.mindmap.model.ModelUtils.breakToLines;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.text.JTextComponent;

import com.igormaznitsa.meta.common.utils.Assertions;

public final class TextBlock implements Cloneable {

  private static final class Line {

    private final Rectangle2D bounds;
    private final String line;

    private Line(@Nonnull final String line, @Nonnull final Rectangle2D bounds) {
      this.bounds = bounds;
      this.line = line;
    }
  }

  private String text;
  private Line[] lines;
  private Font font;
  private float maxLineAscent;
  private final Rectangle2D bounds = new Rectangle2D.Double();
  private TextAlign textAlign;
  
  private static final Rectangle2D ZERO = new Rectangle2D.Float();

  public TextBlock(@Nonnull final TextBlock orig){
    this.text = orig.text;
    this.lines = orig.lines.clone();
    this.font = orig.font;
    this.maxLineAscent = orig.maxLineAscent;
    this.bounds.setRect(orig.getBounds());
    this.textAlign = orig.textAlign;
  }
  
  public TextBlock(@Nonnull final String text, @Nonnull final TextAlign justify) {
    updateText(Assertions.assertNotNull(text));
    this.textAlign = Assertions.assertNotNull(justify);
  }

  public void updateText(@Nullable final String text) {
    this.text = text == null ? "" : text; //NOI18N
    invalidate();
  }

  public void fillByTextAndFont(@Nonnull final JTextComponent compo) {
    compo.setFont(this.font);
    compo.setText(this.text);
  }

  @Nonnull
  public Rectangle2D getBounds() {
    return this.bounds == null ? ZERO : this.bounds;
  }

  @Nonnull
  public TextAlign getTextAlign(){
    return this.textAlign;
  }
  
  public void setTextAlign(@Nullable final TextAlign textAlign){
    this.textAlign = textAlign == null ? TextAlign.CENTER : textAlign;
    invalidate();
  }
  
  public void invalidate() {
    this.lines = null;
  }

  public void setCoordOffset(final double x, final double y){
    this.bounds.setFrame(x, y, this.bounds.getWidth(), this.bounds.getHeight());
  }
  
  public void updateSize(@Nonnull final Graphics2D gfx, @Nonnull final MindMapPanelConfig cfg) {
      this.font = cfg.getFont().deriveFont(cfg.safeScaleFloatValue(cfg.getFont().getSize2D(),2f));
      final FontMetrics metrics = gfx.getFontMetrics(this.font);

      this.maxLineAscent = metrics.getMaxAscent();

      float maxWidth = 0.0f;
      float maxHeight = 0.0f;

      final String[] brokenText = breakToLines(this.text);

      this.lines = new Line[brokenText.length];

      int index = 0;
      for (final String s : brokenText) {
        final Rectangle2D lineBounds = metrics.getStringBounds(s, gfx);
        if (Float.compare((float) lineBounds.getWidth(), maxWidth) > 0) {
          maxWidth = (float) lineBounds.getWidth();
        }
        maxHeight += (float) lineBounds.getHeight();
        this.lines[index++] = new Line(s, lineBounds);
      }
      this.bounds.setRect(0f, 0f, maxWidth, maxHeight);
  }

  public void paint(@Nonnull final Graphics2D gfx) {
    if (this.font != null && this.lines != null) {
      double posy = this.bounds.getY() + this.maxLineAscent;
      gfx.setFont(this.font);
      for (final Line l : this.lines) {
        final double drawX;
        switch (this.textAlign) {
          case LEFT: {
            drawX = this.bounds.getX();
          }
          break;
          case CENTER: {
            drawX = this.bounds.getX() + (this.bounds.getWidth() - l.bounds.getWidth()) / 2;
          }
          break;
          case RIGHT: {
            drawX = this.bounds.getX() + (this.bounds.getWidth() - l.bounds.getWidth());
          }
          break;
          default:
            throw new Error("unexpected situation #283794"); //NOI18N
        }

        gfx.drawString(l.line, (int)Math.round(drawX), (int)Math.round(posy));
        posy += l.bounds.getHeight();
      }
    }
  }

}
