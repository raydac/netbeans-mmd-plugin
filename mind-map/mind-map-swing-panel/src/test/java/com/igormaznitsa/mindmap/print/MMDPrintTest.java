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

import static com.igormaznitsa.mindmap.swing.panel.MindMapPanel.calculateSizeOfMapInPixels;
import static com.igormaznitsa.mindmap.swing.panel.MindMapPanel.layoutFullDiagramWithCenteringToPaper;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.mindmap.swing.panel.ui.AbstractElement;
import com.igormaznitsa.mindmap.swing.panel.ui.gfx.MMGraphics2DWrapper;
import com.igormaznitsa.mindmap.swing.panel.utils.RenderQuality;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import org.junit.Test;

public class MMDPrintTest {

  @Test
  public void testCountPagesAlong_exactFit() {
    assertEquals(1, MMDPrint.countPagesAlong(500.0d, 500));
  }

  @Test
  public void testCountPagesAlong_overflowUsesCeil() {
    assertEquals(3, MMDPrint.countPagesAlong(801.0d, 400));
    assertEquals(3, MMDPrint.countPagesAlong(1001.0d, 500));
  }

  @Test
  public void testImageZoomKeepsNativePageGrid() {
    final BufferedImage image = new BufferedImage(801, 400, BufferedImage.TYPE_INT_ARGB);
    final PrintPage[][] pages = new MMDPrint(
        PrintableObject.newBuild().image(image).build(),
        400,
        400,
        new MMDPrintOptions().setScaleType(MMDPrintOptions.ScaleType.ZOOM).setScale(1.0d)
    ).getPages();

    assertEquals(1, pages.length);
    assertEquals(3, pages[0].length);
  }

  @Test
  public void testImageFitToSinglePageStaysOnOnePage() {
    final BufferedImage image = new BufferedImage(1200, 800, BufferedImage.TYPE_INT_ARGB);
    final PrintPage[][] pages = new MMDPrint(
        PrintableObject.newBuild().image(image).build(),
        400,
        400,
        new MMDPrintOptions().setScaleType(MMDPrintOptions.ScaleType.FIT_TO_SINGLE_PAGE)
    ).getPages();

    assertEquals(1, pages.length);
    assertEquals(1, pages[0].length);
  }

  @Test
  public void testMindMapFitToSinglePageStaysOnOnePage() {
    final PrintPage[][] pages = new MMDPrint(
        PrintableObject.newBuild().mmdpanel(this.panelWithWideMap()).build(),
        200,
        200,
        new MMDPrintOptions().setScaleType(MMDPrintOptions.ScaleType.FIT_TO_SINGLE_PAGE)
    ).getPages();

    assertEquals(1, pages.length);
    assertEquals(1, pages[0].length);
  }

  @Test
  public void testMindMapZoomOneCanSpanMultiplePages() {
    final PrintPage[][] pages = new MMDPrint(
        PrintableObject.newBuild().mmdpanel(this.panelWithWideMap()).build(),
        120,
        120,
        new MMDPrintOptions().setScaleType(MMDPrintOptions.ScaleType.ZOOM).setScale(1.0d)
    ).getPages();

    assertTrue(pages.length >= 1);
    assertTrue(pages[0].length > 1);
  }

  @Test
  public void testImageDrawsSourcePixelsUnderPrinterScale() {
    final BufferedImage source = this.checkerboard(40);
    final PrintPage[][] pages = new MMDPrint(
        PrintableObject.newBuild().image(source).build(),
        20,
        20,
        new MMDPrintOptions().setScaleType(MMDPrintOptions.ScaleType.FIT_TO_SINGLE_PAGE)
    ).getPages();

    final BufferedImage printed = new BufferedImage(80, 80, BufferedImage.TYPE_INT_RGB);
    final Graphics2D graphics = printed.createGraphics();
    try {
      graphics.scale(4.0d, 4.0d);
      pages[0][0].print(graphics);
    } finally {
      graphics.dispose();
    }

    assertTrue(
        "Printer-scale blit must keep source contrast instead of a 72-DPI downscale",
        this.rowContrast(printed) > 40);
  }

  @Test
  public void testMindMapVectorPrintOntoPrinterScale() {
    final PrintPage[][] pages = new MMDPrint(
        PrintableObject.newBuild().mmdpanel(this.panelWithWideMap()).build(),
        400,
        400,
        new MMDPrintOptions().setScaleType(MMDPrintOptions.ScaleType.FIT_TO_SINGLE_PAGE)
    ).getPages();

    final BufferedImage printed = new BufferedImage(800, 800, BufferedImage.TYPE_INT_RGB);
    final Graphics2D graphics = printed.createGraphics();
    try {
      graphics.scale(4.0d, 4.0d);
      graphics.setColor(Color.WHITE);
      graphics.fillRect(0, 0, 200, 200);
      pages[0][0].print(graphics);
    } finally {
      graphics.dispose();
    }

    assertTrue(this.hasInk(printed));
  }

  @Test
  public void testPrinterTransformLayoutKeepsTextInsideTopic() {
    final MindMap map = new MindMap(true);
    map.getRoot().setText("Printer font metric mismatch");

    final MindMapPanelConfig cfg = new MindMapPanelConfig();
    cfg.setScale(1.0d);
    cfg.setDrawBackground(false);

    final Dimension2D size =
        calculateSizeOfMapInPixels(map, null, cfg, false, RenderQuality.QUALITY);

    final BufferedImage image = new BufferedImage(800, 400, BufferedImage.TYPE_INT_ARGB);
    final Graphics2D graphics = image.createGraphics();
    try {
      graphics.scale(300.0d / 72.0d, 300.0d / 72.0d);
      RenderQuality.QUALITY.prepare(graphics);
      graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
          RenderingHints.VALUE_STROKE_PURE);
      graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
          RenderingHints.VALUE_FRACTIONALMETRICS_ON);

      layoutFullDiagramWithCenteringToPaper(new MMGraphics2DWrapper(graphics), map, cfg, size);

      final AbstractElement root = (AbstractElement) map.getRoot().getPayload();
      final Font font = cfg.getFont();
      graphics.setFont(font);
      final Rectangle2D textBounds =
          font.getStringBounds(map.getRoot().getText(), graphics.getFontRenderContext());

      assertTrue(
          "Topic frame must be at least as wide as the printer-measured text",
          root.getBounds().getWidth() + 0.5d >= textBounds.getWidth());
    } finally {
      graphics.dispose();
    }
  }

  private MindMapPanel panelWithWideMap() {
    final MindMap map = new MindMap(true);
    map.getRoot().setText(this.repeat("Wide topic title ", 40));
    new Topic(map, map.getRoot(), this.repeat("Left child with a long label ", 20));
    new Topic(map, map.getRoot(), this.repeat("Right child with a long label ", 20));

    final MindMapPanel panel = mock(MindMapPanel.class);
    when(panel.getModel()).thenReturn(map);
    when(panel.getConfiguration()).thenReturn(new MindMapPanelConfig());
    return panel;
  }

  private BufferedImage checkerboard(final int size) {
    final BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
    for (int y = 0; y < size; y++) {
      for (int x = 0; x < size; x++) {
        image.setRGB(x, y, ((x + y) & 1) == 0 ? 0x000000 : 0xFFFFFF);
      }
    }
    return image;
  }

  private int rowContrast(final BufferedImage image) {
    int min = 255;
    int max = 0;
    for (int x = 0; x < image.getWidth(); x++) {
      final int gray = image.getRGB(x, image.getHeight() / 2) & 0xFF;
      min = Math.min(min, gray);
      max = Math.max(max, gray);
    }
    return max - min;
  }

  private boolean hasInk(final BufferedImage image) {
    for (int y = 0; y < image.getHeight(); y++) {
      for (int x = 0; x < image.getWidth(); x++) {
        if ((image.getRGB(x, y) & 0xFFFFFF) != 0xFFFFFF) {
          return true;
        }
      }
    }
    return false;
  }

  private String repeat(final String text, final int times) {
    final StringBuilder buffer = new StringBuilder(text.length() * times);
    for (int i = 0; i < times; i++) {
      buffer.append(text);
    }
    return buffer.toString();
  }
}
