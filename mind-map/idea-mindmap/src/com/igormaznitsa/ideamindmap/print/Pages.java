package com.igormaznitsa.ideamindmap.print;

import com.igormaznitsa.ideamindmap.utils.IdeaUtils;
import com.igormaznitsa.mindmap.print.PrintPage;
import com.intellij.ui.components.JBPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;

class Pages extends JBPanel {

  private final MMDPrintPanel parent;

  private static final int INTERVAL_X = 25;
  private static final int INTERVAL_Y = 25;

  private static final int SHADOW_X = 10;
  private static final int SHADOW_Y = 10;

  public Pages(final MMDPrintPanel parent) {
    this.parent = parent;
  }

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

    final int width = (int) Math.round(INTERVAL_X + ((paperWidth + INTERVAL_X) * pagesAtHorizontal));
    final int height = (int) Math.round(INTERVAL_Y + ((paperHeight + INTERVAL_Y) * pagesAtVertical));

    return new Dimension((int) Math.round(width * scale), (int) Math.round(height * scale));
  }

  public Dimension getMinimumSize() {
    return this.getPreferredSize();
  }

  public Dimension getMaximumSize() {
    return this.getPreferredSize();
  }

  @Override public void paint(final Graphics g) {
    final Graphics2D gfx = (Graphics2D) g;
    gfx.setColor(IdeaUtils.isDarkTheme() ? Color.DARK_GRAY : Color.LIGHT_GRAY);
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
