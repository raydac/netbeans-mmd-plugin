package com.igormaznitsa.ideamindmap.print;

import com.igormaznitsa.ideamindmap.utils.IdeaUtils;
import com.intellij.ui.components.JBPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;

class Pages extends JBPanel {

  private final MMDPrintPanel parent;

  private static final int INTERVAL_X = 45;
  private static final int INTERVAL_Y = 45;

  public Pages(final MMDPrintPanel parent) {
    this.parent = parent;
  }

  public Dimension getPreferredSize(){
    final PrintPage [][] pages = this.parent.getPages();
    final PageFormat pageFormat = this.parent.getPageFormat();
    final double scale = this.parent.getScale();
    final int scaledIntervalX = (int) Math.round(INTERVAL_X * scale);
    final int scaledIntervalY = (int) Math.round(INTERVAL_Y * scale);

    final double pageScaledWidth = pageFormat.getWidth() * scale;
    final double pageScaledHeight = pageFormat.getHeight() * scale;

    int horzpages = 0;
    int vertpages = pages.length;

    for(final PrintPage [] row : pages){
      horzpages = Math.max(horzpages, row.length);
    }

    final int width = (int)Math.round(scaledIntervalX+((pageScaledWidth+scaledIntervalX)*horzpages));
    final int height = (int)Math.round(scaledIntervalY+((pageScaledHeight+scaledIntervalY)*vertpages));

    return new Dimension(width,height);
  }

  public Dimension getMinimumSize(){
    return this.getPreferredSize();
  }

  public Dimension getMaximumSize(){
    return this.getPreferredSize();
  }

  @Override public void paint(final Graphics g) {
    final Graphics2D gfx = (Graphics2D) g;
    gfx.setColor(IdeaUtils.isDarkTheme() ? Color.DARK_GRAY : Color.LIGHT_GRAY);
    final Dimension size = getSize();
    gfx.fillRect(0,0,size.width,size.height);

    final double scale = this.parent.getScale();
    final PageFormat pageFormat = this.parent.getPageFormat();
    final int scaledIntervalX = (int) Math.round(INTERVAL_X * scale);
    final int scaledIntervalY = (int) Math.round(INTERVAL_Y * scale);

    final PrintPage[][] allPages = this.parent.getPages();

    int y = scaledIntervalX;

    final double pageScaledWidth = pageFormat.getWidth() * scale;
    final double pageScaledHeight = pageFormat.getHeight() * scale;

    final double shadowOffsetX = scale * 10.0d;
    final double shadowOffsetY = scale * 10.0d;

    final Rectangle2D pageBack = new Rectangle2D.Double(0.0d, 0.0d, pageScaledWidth, pageScaledHeight);

    final Color SHADOW = new Color(0,0,0,0x50);

    for (final PrintPage[] pages : allPages) {
      int x = scaledIntervalX;
      for (final PrintPage p : pages) {
        gfx.setColor(SHADOW);
        pageBack.setRect(x+shadowOffsetX,y+shadowOffsetY,pageBack.getWidth(),pageBack.getHeight());
        gfx.fill(pageBack);
        gfx.setColor(Color.WHITE);
        pageBack.setRect(x,y,pageBack.getWidth(),pageBack.getHeight());
        gfx.fill(pageBack);
        x += scaledIntervalX+pageScaledWidth;
      }
      y += scaledIntervalY+pageScaledHeight;
    }

    paintBorder(g);
  }
}
