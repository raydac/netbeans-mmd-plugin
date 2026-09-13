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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.igormaznitsa.mindmap.model.ExtraNote;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.plugins.api.Renderable;
import com.igormaznitsa.mindmap.plugins.attributes.emoticon.EmoticonVisualAttributePlugin;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.mindmap.swing.panel.ui.AbstractCollapsableElement;
import com.igormaznitsa.mindmap.swing.panel.ui.AbstractElement;
import com.igormaznitsa.mindmap.swing.panel.ui.IconBlock;
import com.igormaznitsa.mindmap.swing.panel.ui.gfx.MMGraphics2DWrapper;
import com.igormaznitsa.mindmap.swing.panel.utils.MiscIcons;
import com.igormaznitsa.mindmap.swing.panel.utils.RenderQuality;
import com.igormaznitsa.mindmap.swing.panel.utils.ScalableIcon;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;
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
  public void testLaterPagesKeepInkWhenGraphicsAreScaled() {
    final PrintPage[][] pages = new MMDPrint(
        PrintableObject.newBuild().mmdpanel(this.panelWithWideMap()).build(),
        120,
        120,
        new MMDPrintOptions().setScaleType(MMDPrintOptions.ScaleType.ZOOM).setScale(1.0d)
    ).getPages();

    assertTrue(pages[0].length > 1);

    final int inkFirst = this.countInk(this.renderPage(pages[0][0], 120, 2.0d));
    final int inkSecond = this.countInk(this.renderPage(pages[0][1], 120, 2.0d));

    assertTrue("First page must contain the map at 200% preview scale, ink=" + inkFirst,
        inkFirst > 0);
    assertTrue("Second page must contain the map at 200% preview scale, ink=" + inkSecond,
        inkSecond > 0);
  }

  @Test
  public void testFitWidthToTwoPagesKeepsBothColumns() {
    final PrintPage[][] pages = new MMDPrint(
        PrintableObject.newBuild().mmdpanel(this.panelWithWideMap()).build(),
        200,
        200,
        new MMDPrintOptions()
            .setScaleType(MMDPrintOptions.ScaleType.FIT_WIDTH_TO_PAGES)
            .setPagesInRow(2)
    ).getPages();

    assertEquals(2, pages[0].length);

    int rightColumnInk = 0;
    for (int rowIndex = 0; rowIndex < pages.length; rowIndex++) {
      rightColumnInk += this.countInk(this.renderPage(pages[rowIndex][1], 200, 2.0d));
    }

    assertTrue(
        "Right-hand pages of a 2-page-wide print must contain map ink, ink=" + rightColumnInk,
        rightColumnInk > 0);
  }

  @Test
  public void testFitToTwoByTwoPagesKeepsMapOnGrid() {
    final PrintPage[][] pages = new MMDPrint(
        PrintableObject.newBuild().mmdpanel(this.panelWithLargeMap()).build(),
        120,
        120,
        new MMDPrintOptions()
            .setScaleType(MMDPrintOptions.ScaleType.FIT_TO_PAGES)
            .setPagesInRow(2)
            .setPagesInColumn(2)
    ).getPages();

    assertTrue(pages.length <= 2);
    assertTrue(pages[0].length <= 2);

    int ink = 0;
    for (int rowIndex = 0; rowIndex < pages.length; rowIndex++) {
      for (int columnIndex = 0; columnIndex < pages[rowIndex].length; columnIndex++) {
        ink += this.countInk(this.renderPage(pages[rowIndex][columnIndex], 120, 2.0d));
      }
    }

    assertTrue("A 2x2 fit must still print the map at 200% preview scale, ink=" + ink, ink > 0);
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

  @Test
  public void testTopicExtraIconPrintUsesSourcePixels() {
    final MindMap map = new MindMap(true);
    map.getRoot().setExtra(new ExtraNote("note"));

    final MindMapPanelConfig cfg = new MindMapPanelConfig();
    cfg.setScale(1.0d);

    final BufferedImage printed = this.renderAtPrinterScale(64, gfx -> {
      final IconBlock icons = new IconBlock(map.getRoot());
      icons.updateSize(gfx, cfg);
      icons.setCoordOffset(0.0d, 0.0d);
      icons.paint(gfx);
    });

    final BufferedImage fromSource = this.renderAtPrinterScale(64, gfx -> gfx.drawImage(
        ScalableIcon.TEXT.getBaseImage(),
        0.0d,
        0.0d,
        ScalableIcon.BASE_WIDTH,
        ScalableIcon.BASE_HEIGHT));

    final BufferedImage fromDownscaled = this.renderAtPrinterScale(64, gfx -> gfx.drawImage(
        ScalableIcon.TEXT.getImage(1.0d),
        0,
        0));

    assertEquals(
        "Print must sample the original 32px extra icon, not a 16px screen bitmap",
        0,
        this.pixelDiff(printed, fromSource));
    assertTrue(
        "A pre-downscaled 16px blit must differ from the source-pixel print",
        this.pixelDiff(fromDownscaled, fromSource) > 0);
  }

  @Test
  public void testEmoticonPrintUsesSourcePixels() {
    final MindMap map = new MindMap(true);
    map.getRoot().putAttribute("mmd.emoticon", "abacus");

    final MindMapPanelConfig cfg = new MindMapPanelConfig();
    cfg.setScale(0.5d);

    final Renderable emoticon =
        new EmoticonVisualAttributePlugin().getScaledImage(cfg, map.getRoot());
    final Image source = MiscIcons.findForName("abacus");
    assertNotNull(emoticon);
    assertNotNull(source);
    final double visualSize = 32.0d * cfg.getScale();

    final BufferedImage printed = this.renderAtPrinterScale(64,
        gfx -> emoticon.renderAt(gfx, cfg, 0, 0));
    final BufferedImage fromSource = this.renderAtPrinterScale(64,
        gfx -> gfx.drawImage(source, 0.0d, 0.0d, visualSize, visualSize));

    assertEquals(
        "Print must sample the original emoticon pixels instead of a pre-downscaled copy",
        0,
        this.pixelDiff(printed, fromSource));
  }

  private BufferedImage renderAtPrinterScale(
      final int size,
      final Consumer<MMGraphics2DWrapper> painter
  ) {
    final BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
    final Graphics2D graphics = image.createGraphics();
    try {
      graphics.scale(4.0d, 4.0d);
      RenderQuality.QUALITY.prepare(graphics);
      painter.accept(new MMGraphics2DWrapper(graphics));
    } finally {
      graphics.dispose();
    }
    return image;
  }

  private int pixelDiff(final BufferedImage left, final BufferedImage right) {
    int diff = 0;
    for (int y = 0; y < left.getHeight(); y++) {
      for (int x = 0; x < left.getWidth(); x++) {
        if (left.getRGB(x, y) != right.getRGB(x, y)) {
          diff++;
        }
      }
    }
    return diff;
  }

  private MindMapPanel panelWithWideMap() {
    final MindMap map = new MindMap(true);
    map.getRoot().setText(this.repeat("Wide topic title ", 40));
    AbstractCollapsableElement.makeTopicLeftSided(
        new Topic(map, map.getRoot(), this.repeat("Left child with a long label ", 20)), true);
    new Topic(map, map.getRoot(), this.repeat("Right child with a long label ", 20));

    return this.panelWithMap(map);
  }

  private MindMapPanel panelWithLargeMap() {
    final MindMap map = new MindMap(true);
    map.getRoot().setText("Root of a large print map");

    for (int index = 0; index < 8; index++) {
      AbstractCollapsableElement.makeTopicLeftSided(
          new Topic(map, map.getRoot(), "Left branch " + index + " " + this.repeat("topic ", 6)),
          true);
      new Topic(map, map.getRoot(), "Right branch " + index + " " + this.repeat("topic ", 6));
    }

    return this.panelWithMap(map);
  }

  private MindMapPanel panelWithMap(final MindMap map) {
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

  private BufferedImage renderPage(
      final PrintPage page,
      final int paperSize,
      final double deviceScale
  ) {
    final int pixels = Math.max(1, (int) Math.round(paperSize * deviceScale));
    final BufferedImage printed = new BufferedImage(pixels, pixels, BufferedImage.TYPE_INT_RGB);
    final Graphics2D graphics = printed.createGraphics();
    try {
      graphics.setColor(Color.WHITE);
      graphics.fillRect(0, 0, pixels, pixels);
      graphics.scale(deviceScale, deviceScale);
      page.print(graphics);
    } finally {
      graphics.dispose();
    }
    return printed;
  }

  private int countInk(final BufferedImage image) {
    int ink = 0;
    for (int y = 0; y < image.getHeight(); y++) {
      for (int x = 0; x < image.getWidth(); x++) {
        if ((image.getRGB(x, y) & 0xFFFFFF) != 0xFFFFFF) {
          ink++;
        }
      }
    }
    return ink;
  }

  private boolean hasInk(final BufferedImage image) {
    return this.countInk(image) > 0;
  }

  private String repeat(final String text, final int times) {
    final StringBuilder buffer = new StringBuilder(text.length() * times);
    for (int i = 0; i < times; i++) {
      buffer.append(text);
    }
    return buffer.toString();
  }
}
