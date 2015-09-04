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
package com.igormaznitsa.nbmindmap.mmgui;

import com.igormaznitsa.nbmindmap.utils.Utils;
import static com.igormaznitsa.nbmindmap.utils.Utils.assertNotNull;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import javax.swing.text.JTextComponent;

public final class TextBlock {

  private static final class Line {

    private final Rectangle2D bounds;
    private final String line;

    private Line(final String line, final Rectangle2D bounds) {
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

  public TextBlock(final String text, final TextAlign justify) {
    assertNotNull("Text must not be null", text); //NOI18N
    updateText(text);
    this.textAlign = justify;
  }

  public void updateText(final String text) {
    this.text = text == null ? "" : text; //NOI18N
    invalidate();
  }

  public void fillByTextAndFont(final JTextComponent compo) {
    compo.setFont(this.font);
    compo.setText(this.text);
  }

  public Rectangle2D getBounds() {
    return this.bounds == null ? ZERO : this.bounds;
  }

  public TextAlign getTextAlign(){
    return this.textAlign;
  }
  
  public void setTextAlign(final TextAlign textAlign){
    this.textAlign = textAlign == null ? TextAlign.CENTER : textAlign;
    invalidate();
  }
  
  public void invalidate() {
    this.lines = null;
  }

  public void setCoordOffset(final double x, final double y){
    this.bounds.setFrame(x, y, this.bounds.getWidth(), this.bounds.getHeight());
  }
  
  public void updateSize(final Graphics2D gfx, final Configuration cfg) {
      this.font = cfg.getFont().deriveFont(cfg.safeScaleFloatValue(cfg.getFont().getSize2D(),2f));
      final FontMetrics metrics = gfx.getFontMetrics(this.font);

      this.maxLineAscent = metrics.getMaxAscent();

      float maxWidth = 0.0f;
      float maxHeight = 0.0f;

      final String[] brokenText = Utils.breakToLines(this.text);

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

  public void paint(final Graphics2D gfx) {
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
