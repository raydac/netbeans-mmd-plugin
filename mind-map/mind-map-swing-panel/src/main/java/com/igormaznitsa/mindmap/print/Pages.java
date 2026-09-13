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

package com.igormaznitsa.mindmap.print;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import javax.swing.JPanel;

class Pages extends JPanel {

  private static final long serialVersionUID = -6728277837828116266L;
  private static final int INTERVAL_X = 25;
  private static final int INTERVAL_Y = 25;
  private static final int SHADOW_X = 10;
  private static final int SHADOW_Y = 10;
  private final MMDPrintPanel parent;

  public Pages(final MMDPrintPanel parent) {
    this.parent = parent;
  }

  @Override
  public Dimension getPreferredSize() {
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

    final int width =
        (int) Math.round(INTERVAL_X + ((paperWidth + INTERVAL_X) * pagesAtHorizontal));
    final int height =
        (int) Math.round(INTERVAL_Y + ((paperHeight + INTERVAL_Y) * pagesAtVertical));

    return new Dimension((int) Math.round(width * scale), (int) Math.round(height * scale));
  }

  @Override
  public Dimension getMinimumSize() {
    return this.getPreferredSize();
  }

  @Override
  public Dimension getMaximumSize() {
    return this.getPreferredSize();
  }

  @Override
  protected void paintComponent(final Graphics g) {
    super.paintComponent(g);

    final Graphics2D gfx = (Graphics2D) g.create();
    try {
      gfx.setColor(this.parent.isDarkTheme() ? Color.DARK_GRAY : Color.LIGHT_GRAY);
      final Dimension size = this.getSize();
      gfx.fillRect(0, 0, size.width, size.height);

      final double scale = this.parent.getScale();
      final PageFormat thePageFormat = this.parent.getPageFormat();

      final PrintPage[][] allPages = this.parent.getPages();

      final double pageWidth = thePageFormat.getWidth();
      final double pageHeight = thePageFormat.getHeight();

      final double areaWidth = thePageFormat.getImageableWidth();
      final double areaHeight = thePageFormat.getImageableHeight();

      final Rectangle2D pageBack = new Rectangle2D.Double(0.0d, 0.0d, pageWidth, pageHeight);
      final Rectangle2D pageArea = new Rectangle2D.Double(0.0d, 0.0d, areaWidth, areaHeight);

      final Color shadow = new Color(0, 0, 0, 0x50);

      int y = INTERVAL_Y;

      final double areaX = thePageFormat.getImageableX();
      final double areaY = thePageFormat.getImageableY();

      final boolean drawBorder = this.parent.isDrawBorder();

      gfx.scale(scale, scale);
      final AffineTransform pageOrigin = gfx.getTransform();
      for (final PrintPage[] pages : allPages) {
        int x = INTERVAL_X;
        for (final PrintPage page : pages) {
          gfx.setTransform(pageOrigin);
          gfx.translate(x, y);

          gfx.setColor(shadow);
          pageBack.setRect(SHADOW_X, SHADOW_Y, pageWidth, pageHeight);
          gfx.fill(pageBack);
          gfx.setColor(Color.WHITE);
          pageBack.setRect(0.0d, 0.0d, pageWidth, pageHeight);
          gfx.fill(pageBack);

          final Graphics2D sheetGfx = (Graphics2D) gfx.create();
          try {
            sheetGfx.clip(new Rectangle2D.Double(0.0d, 0.0d, pageWidth, pageHeight));
            sheetGfx.translate(areaX, areaY);

            final Graphics2D gfxCopy = (Graphics2D) sheetGfx.create();
            try {
              gfxCopy.clip(pageArea);
              page.print(gfxCopy);
            } finally {
              gfxCopy.dispose();
            }

            if (drawBorder) {
              final Stroke oldStroke = sheetGfx.getStroke();
              sheetGfx.setColor(MMDPrintPanel.BORDER_COLOR);
              sheetGfx.setStroke(MMDPrintPanel.BORDER_STYLE);
              sheetGfx.draw(pageArea);
              sheetGfx.setStroke(oldStroke);
            }
          } finally {
            sheetGfx.dispose();
          }

          x += INTERVAL_X + (int) Math.round(pageWidth);
        }
        y += INTERVAL_Y + (int) Math.round(pageHeight);
      }
    } finally {
      gfx.dispose();
    }
  }
}
