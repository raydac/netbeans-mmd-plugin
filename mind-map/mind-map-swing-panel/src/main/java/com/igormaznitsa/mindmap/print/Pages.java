/*
 * Copyright 2015-2016 Igor Maznitsa.
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
package com.igormaznitsa.mindmap.print;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;

import javax.annotation.Nonnull;
import javax.swing.JPanel;

class Pages extends JPanel {

  private static final long serialVersionUID = -6728277837828116266L;

  private final MMDPrintPanel parent;

  private static final int INTERVAL_X = 25;
  private static final int INTERVAL_Y = 25;

  private static final int SHADOW_X = 10;
  private static final int SHADOW_Y = 10;

  public Pages (@Nonnull final MMDPrintPanel parent) {
    this.parent = parent;
  }

  @Override
  @Nonnull
  public Dimension getPreferredSize () {
    final PrintPage[][] pages = this.parent.getPages();
    final PageFormat thePageFormat = this.parent.getPageFormat();
    final double scale = this.parent.getScale();

    int pagesAtHorizontal = 0;
    int pagesAtVertical = pages.length;

    final double paperWidth = thePageFormat.getWidth();
    final double paperHeight = thePageFormat.getHeight();

    for (final PrintPage[] row : pages) {
      pagesAtHorizontal = Math.max(pagesAtHorizontal, row.length);
    }

    final int width = (int) Math.round(INTERVAL_X + ((paperWidth + INTERVAL_X) * pagesAtHorizontal));
    final int height = (int) Math.round(INTERVAL_Y + ((paperHeight + INTERVAL_Y) * pagesAtVertical));

    return new Dimension((int) Math.round(width * scale), (int) Math.round(height * scale));
  }

  @Override
  @Nonnull
  public Dimension getMinimumSize () {
    return this.getPreferredSize();
  }

  @Override
  @Nonnull
  public Dimension getMaximumSize () {
    return this.getPreferredSize();
  }

  @SuppressWarnings("unchecked")
  @Override
  public void paint (@Nonnull final Graphics g) {
    final Graphics2D gfx = (Graphics2D) g;
    gfx.setColor(parent.isDarkTheme() ? Color.DARK_GRAY : Color.LIGHT_GRAY);
    final Dimension size = getSize();
    gfx.fillRect(0, 0, size.width, size.height);

    final double scale = this.parent.getScale();
    final PageFormat thePageFormat = this.parent.getPageFormat();

    final PrintPage[][] allPages = this.parent.getPages();

    final double PAGE_WIDTH = thePageFormat.getWidth();
    final double PAGE_HEIGHT = thePageFormat.getHeight();

    final double AREA_WIDTH = thePageFormat.getImageableWidth();
    final double AREA_HEIGHT = thePageFormat.getImageableHeight();

    final Rectangle2D pageBack = new Rectangle2D.Double(0.0d, 0.0d, PAGE_WIDTH, PAGE_HEIGHT);
    final Rectangle2D pageArea = new Rectangle2D.Double(0.0d, 0.0d, AREA_WIDTH, AREA_HEIGHT);

    final Color SHADOW = new Color(0, 0, 0, 0x50);

    int y = INTERVAL_Y;

    final double AREA_X = thePageFormat.getImageableX();
    final double AREA_Y = thePageFormat.getImageableY();

    final boolean drawBorder = this.parent.isDrawBorder();
    
    gfx.scale(scale, scale);
    for (final PrintPage[] pages : allPages) {
      int x = INTERVAL_X;
      for (final PrintPage p : pages) {
        gfx.translate(x, y);

        gfx.setColor(SHADOW);
        pageBack.setRect(SHADOW_X, SHADOW_Y, pageBack.getWidth(), pageBack.getHeight());
        gfx.fill(pageBack);
        gfx.setColor(Color.WHITE);
        pageBack.setRect(0.0d, 0.0d, pageBack.getWidth(), pageBack.getHeight());
        gfx.fill(pageBack);

        gfx.translate(AREA_X, AREA_Y);

        final Graphics2D gfxCopy = (Graphics2D) gfx.create();
        gfxCopy.clip(pageArea);
        p.print(gfxCopy);
        gfxCopy.dispose();

        if (drawBorder) {
          final Stroke oldStroke = gfx.getStroke();
          gfx.setColor(MMDPrintPanel.BORDER_COLOR);
          gfx.setStroke(MMDPrintPanel.BORDER_STYLE);
          gfx.draw(pageArea);
          gfx.setStroke(oldStroke);
        }

        gfx.translate(-AREA_X, -AREA_Y);

        gfx.translate(-x, -y);
        x += INTERVAL_X + PAGE_WIDTH;
      }
      y += INTERVAL_Y + PAGE_HEIGHT;
    }
    gfx.scale(1.0d, 1.0d);

    paintBorder(g);
  }
}
