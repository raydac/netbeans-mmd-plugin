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
import static com.igormaznitsa.mindmap.swing.panel.MindMapPanel.drawOnGraphicsForConfiguration;
import static java.util.Objects.requireNonNull;

import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.mindmap.swing.panel.ui.gfx.MMGraphics2DWrapper;
import com.igormaznitsa.mindmap.swing.panel.utils.RenderQuality;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class MMDPrint {

  static final double PAGE_POINTS_PER_INCH = 72.0d;
  static final double PRINT_RASTER_DPI = 300.0d;
  static final double MIN_SCALE = 0.01d;
  static final double SCALE_STEP = 0.01d;

  private static final Logger LOGGER = LoggerFactory.getLogger(MMDPrint.class);
  private static final PrintPage[][] NO_PAGES = new PrintPage[0][0];
  private static final int UNBOUNDED_PAGES = Integer.MAX_VALUE;

  private final PrintPage[][] pages;

  public MMDPrint(
      final PrintableObject printableObject,
      final int paperWidthInPixels,
      final int paperHeightInPixels,
      final MMDPrintOptions options
  ) {
    requireNonNull(printableObject, "printableObject must not be null");
    requireNonNull(options, "options must not be null");

    LOGGER.info(String.format("Request to prepare print pages for %dx%d", paperWidthInPixels,
        paperHeightInPixels));

    this.pages = this.buildPages(printableObject, paperWidthInPixels, paperHeightInPixels, options);
  }

  static int countPagesAlong(final double size, final int paperSize) {
    if (paperSize <= 0 || size <= 0.0d) {
      return 1;
    }
    return Math.max(1, (int) Math.ceil(size / (double) paperSize));
  }

  public PrintPage[][] getPages() {
    return this.pages.clone();
  }

  private PrintPage[][] buildPages(
      final PrintableObject printableObject,
      final int paperWidth,
      final int paperHeight,
      final MMDPrintOptions options
  ) {
    if (paperWidth <= 0 || paperHeight <= 0) {
      return NO_PAGES;
    }

    if (printableObject.isImage()) {
      return this.buildImagePages(requireNonNull(printableObject.getImage()), paperWidth,
          paperHeight, options);
    }

    if (printableObject.isMmdPanel()) {
      return this.buildMindMapPages(requireNonNull(printableObject.getPanel()), paperWidth,
          paperHeight, options);
    }

    throw new Error("Unexpected printable object type");
  }

  private PrintPage[][] buildImagePages(
      final Image image,
      final int paperWidth,
      final int paperHeight,
      final MMDPrintOptions options
  ) {
    final double scale = this.resolveImageScale(image, paperWidth, paperHeight, options);
    final double scaledWidth = image.getWidth(null) * scale;
    final double scaledHeight = image.getHeight(null) * scale;
    final int pagesHorz = countPagesAlong(scaledWidth, paperWidth);
    final int pagesVert = countPagesAlong(scaledHeight, paperHeight);
    final Point offset = this.calcCenteredOffset(pagesHorz, pagesVert, paperWidth, paperHeight,
        scaledWidth, scaledHeight);

    return this.createImageTiles(image, scale, offset, pagesHorz, pagesVert, paperWidth,
        paperHeight);
  }

  private PrintPage[][] buildMindMapPages(
      final MindMapPanel panel,
      final int paperWidth,
      final int paperHeight,
      final MMDPrintOptions options
  ) {
    final MindMap model = panel.getModel().makeCopy();
    if (model.getRoot() == null) {
      return NO_PAGES;
    }

    final MindMapPanelConfig cfg = this.createPrintConfiguration(panel);
    this.applyMapScale(model, cfg, paperWidth, paperHeight, options);

    final Dimension2D mapSize = this.measureMap(model, cfg);
    if (mapSize == null) {
      return NO_PAGES;
    }

    this.layoutMap(model, cfg, mapSize);

    double paintScale = 1.0d;
    int pagesHorz = countPagesAlong(mapSize.getWidth(), paperWidth);
    int pagesVert = countPagesAlong(mapSize.getHeight(), paperHeight);

    if (options.getScaleType() == MMDPrintOptions.ScaleType.FIT_TO_SINGLE_PAGE
        && (pagesHorz > 1 || pagesVert > 1)) {
      paintScale =
          this.scaleToFit(mapSize.getWidth(), mapSize.getHeight(), paperWidth, paperHeight);
      pagesHorz = 1;
      pagesVert = 1;
    }

    final Point offset = this.calcCenteredOffset(pagesHorz, pagesVert, paperWidth, paperHeight,
        mapSize.getWidth() * paintScale, mapSize.getHeight() * paintScale);

    return this.createMindMapTiles(model, cfg, mapSize, offset, pagesHorz, pagesVert, paperWidth,
        paperHeight, paintScale, options.isDrawAsImage());
  }

  private MindMapPanelConfig createPrintConfiguration(final MindMapPanel panel) {
    final MindMapPanelConfig cfg = new MindMapPanelConfig(panel.getConfiguration(), false);
    cfg.setDrawBackground(false);
    cfg.setDropShadow(false);

    cfg.setConnectorColor(Color.black);
    cfg.setRootBackgroundColor(Color.black);
    cfg.setRootTextColor(Color.white);
    cfg.setFirstLevelBackgroundColor(Color.lightGray);
    cfg.setFirstLevelTextColor(Color.black);
    cfg.setOtherLevelBackgroundColor(Color.white);
    cfg.setOtherLevelTextColor(Color.black);
    cfg.setCollapsatorBorderColor(Color.black);
    cfg.setCollapsatorBackgroundColor(Color.white);
    cfg.setJumpLinkColor(Color.DARK_GRAY);

    cfg.setElementBorderWidth(1.5f);
    cfg.setCollapsatorBorderWidth(1.0f);
    cfg.setConnectorWidth(2.0f);
    cfg.setPaperMargins(2);
    cfg.setScale(1.0d);
    return cfg;
  }

  private void applyMapScale(
      final MindMap model,
      final MindMapPanelConfig cfg,
      final int paperWidth,
      final int paperHeight,
      final MMDPrintOptions options
  ) {
    switch (options.getScaleType()) {
      case ZOOM:
        cfg.setScale(options.getScale());
        break;
      case FIT_WIDTH_TO_PAGES:
        this.fitMapToTarget(model, cfg, paperWidth, paperHeight,
            options.getPagesInRow() * paperWidth, Double.POSITIVE_INFINITY,
            options.getPagesInRow(), UNBOUNDED_PAGES);
        break;
      case FIT_HEIGHT_TO_PAGES:
        this.fitMapToTarget(model, cfg, paperWidth, paperHeight,
            Double.POSITIVE_INFINITY, options.getPagesInColumn() * paperHeight,
            UNBOUNDED_PAGES, options.getPagesInColumn());
        break;
      case FIT_TO_SINGLE_PAGE:
        this.fitMapToTarget(model, cfg, paperWidth, paperHeight, paperWidth, paperHeight, 1, 1);
        break;
      default:
        throw new Error("Unexpected value:" + options.getScaleType());
    }
  }

  private void fitMapToTarget(
      final MindMap model,
      final MindMapPanelConfig cfg,
      final int paperWidth,
      final int paperHeight,
      final double targetWidth,
      final double targetHeight,
      final int maxPagesHorz,
      final int maxPagesVert
  ) {
    cfg.setScale(1.0d);
    final Dimension2D unitSize = requireNonNull(this.measureMap(model, cfg));
    cfg.setScale(this.scaleToFit(unitSize.getWidth(), unitSize.getHeight(), targetWidth,
        targetHeight));
    this.shrinkScaleToPageLimit(model, cfg, paperWidth, paperHeight, maxPagesHorz, maxPagesVert);
  }

  private double scaleToFit(
      final double contentWidth,
      final double contentHeight,
      final double targetWidth,
      final double targetHeight
  ) {
    double scale = Double.POSITIVE_INFINITY;

    if (contentWidth > 0.0d && Double.isFinite(targetWidth)) {
      scale = targetWidth / contentWidth;
    }

    if (contentHeight > 0.0d && Double.isFinite(targetHeight)) {
      scale = Math.min(scale, targetHeight / contentHeight);
    }

    if (!Double.isFinite(scale)) {
      return 1.0d;
    }

    return Math.max(MIN_SCALE, scale);
  }

  private void shrinkScaleToPageLimit(
      final MindMap model,
      final MindMapPanelConfig cfg,
      final int paperWidth,
      final int paperHeight,
      final int maxPagesHorz,
      final int maxPagesVert
  ) {
    double scale = cfg.getScale();
    Dimension2D size = requireNonNull(this.measureMap(model, cfg));

    while (this.exceedsPageLimit(size, paperWidth, paperHeight, maxPagesHorz, maxPagesVert)
        && scale > MIN_SCALE) {
      scale = Math.max(MIN_SCALE, scale - SCALE_STEP);
      cfg.setScale(scale);
      size = requireNonNull(this.measureMap(model, cfg));
    }
  }

  private boolean exceedsPageLimit(
      final Dimension2D size,
      final int paperWidth,
      final int paperHeight,
      final int maxPagesHorz,
      final int maxPagesVert
  ) {
    return countPagesAlong(size.getWidth(), paperWidth) > maxPagesHorz
        || countPagesAlong(size.getHeight(), paperHeight) > maxPagesVert;
  }

  private double resolveImageScale(
      final Image image,
      final int paperWidth,
      final int paperHeight,
      final MMDPrintOptions options
  ) {
    final double imageWidth = image.getWidth(null);
    final double imageHeight = image.getHeight(null);

    switch (options.getScaleType()) {
      case ZOOM:
        return options.getScale();
      case FIT_WIDTH_TO_PAGES:
        return this.scaleToFit(imageWidth, imageHeight, options.getPagesInRow() * paperWidth,
            Double.POSITIVE_INFINITY);
      case FIT_HEIGHT_TO_PAGES:
        return this.scaleToFit(imageWidth, imageHeight, Double.POSITIVE_INFINITY,
            options.getPagesInColumn() * paperHeight);
      case FIT_TO_SINGLE_PAGE:
        return this.scaleToFit(imageWidth, imageHeight, paperWidth, paperHeight);
      default:
        throw new Error("Unexpected value:" + options.getScaleType());
    }
  }

  private Dimension2D measureMap(final MindMap model, final MindMapPanelConfig cfg) {
    return calculateSizeOfMapInPixels(model, null, cfg, false, RenderQuality.QUALITY);
  }

  private void layoutMap(
      final MindMap model,
      final MindMapPanelConfig cfg,
      final Dimension2D mapSize
  ) {
    final BufferedImage probe = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
    final Graphics2D graphics = probe.createGraphics();
    try {
      this.preparePrintGraphics(graphics);
      this.layoutMapOn(graphics, model, cfg, mapSize);
    } finally {
      graphics.dispose();
    }
  }

  private void layoutMapOn(
      final Graphics2D gfx,
      final MindMap model,
      final MindMapPanelConfig cfg,
      final Dimension2D mapSize
  ) {
    MindMapPanel.layoutFullDiagramWithCenteringToPaper(new MMGraphics2DWrapper(gfx), model, cfg,
        mapSize);
  }

  private void preparePrintGraphics(final Graphics2D gfx) {
    RenderQuality.QUALITY.prepare(gfx);
    gfx.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    gfx.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
        RenderingHints.VALUE_FRACTIONALMETRICS_ON);
  }

  private PrintPage[][] createMindMapTiles(
      final MindMap model,
      final MindMapPanelConfig cfg,
      final Dimension2D mapSize,
      final Point offset,
      final int pagesHorz,
      final int pagesVert,
      final int paperWidth,
      final int paperHeight,
      final double paintScale,
      final boolean rasterize
  ) {
    final PrintPage[][] result = new PrintPage[pagesVert][pagesHorz];
    for (int pageY = 0; pageY < pagesVert; pageY++) {
      for (int pageX = 0; pageX < pagesHorz; pageX++) {
        final int tileX = pageX;
        final int tileY = pageY;
        result[pageY][pageX] = g -> this.paintMindMapPage((Graphics2D) g, model, cfg, mapSize,
            offset, tileX, tileY, paperWidth, paperHeight, paintScale, rasterize);
      }
    }
    return result;
  }

  private PrintPage[][] createImageTiles(
      final Image image,
      final double scale,
      final Point offset,
      final int pagesHorz,
      final int pagesVert,
      final int paperWidth,
      final int paperHeight
  ) {
    final PrintPage[][] result = new PrintPage[pagesVert][pagesHorz];
    for (int pageY = 0; pageY < pagesVert; pageY++) {
      for (int pageX = 0; pageX < pagesHorz; pageX++) {
        final int tileX = pageX;
        final int tileY = pageY;
        result[pageY][pageX] = g -> this.paintImagePage((Graphics2D) g, image, scale, offset, tileX,
            tileY, paperWidth, paperHeight);
      }
    }
    return result;
  }

  private void paintMindMapPage(
      final Graphics2D pageGraphics,
      final MindMap model,
      final MindMapPanelConfig cfg,
      final Dimension2D mapSize,
      final Point offset,
      final int pageX,
      final int pageY,
      final int paperWidth,
      final int paperHeight,
      final double paintScale,
      final boolean rasterize
  ) {
    if (model.getRoot() == null) {
      return;
    }

    final Graphics2D gfx = (Graphics2D) pageGraphics.create();
    try {
      this.preparePrintGraphics(gfx);
      this.clipToPaper(gfx, paperWidth, paperHeight);

      if (rasterize) {
        this.paintMindMapAsDeviceRaster(gfx, model, cfg, mapSize, offset, pageX, pageY, paperWidth,
            paperHeight, paintScale);
      } else {
        this.layoutMapOn(gfx, model, cfg, mapSize);
        this.translateToPage(gfx, offset, pageX, pageY, paperWidth, paperHeight);
        gfx.scale(paintScale, paintScale);
        this.drawMindMap(gfx, model, cfg);
      }
    } finally {
      gfx.dispose();
    }
  }

  private void paintMindMapAsDeviceRaster(
      final Graphics2D gfx,
      final MindMap model,
      final MindMapPanelConfig cfg,
      final Dimension2D mapSize,
      final Point offset,
      final int pageX,
      final int pageY,
      final int paperWidth,
      final int paperHeight,
      final double paintScale
  ) {
    final double rasterScale = this.resolveRasterScale(gfx);
    if (rasterScale <= 1.0d) {
      this.layoutMapOn(gfx, model, cfg, mapSize);
      this.translateToPage(gfx, offset, pageX, pageY, paperWidth, paperHeight);
      gfx.scale(paintScale, paintScale);
      this.drawMindMap(gfx, model, cfg);
      return;
    }

    final int tileWidth = Math.max(1, (int) Math.round(paperWidth * rasterScale));
    final int tileHeight = Math.max(1, (int) Math.round(paperHeight * rasterScale));

    try {
      final BufferedImage tile = new BufferedImage(tileWidth, tileHeight,
          BufferedImage.TYPE_INT_ARGB);
      final Graphics2D tileGraphics = tile.createGraphics();
      try {
        this.preparePrintGraphics(tileGraphics);
        tileGraphics.scale(rasterScale, rasterScale);
        this.layoutMapOn(tileGraphics, model, cfg, mapSize);
        this.translateToPage(tileGraphics, offset, pageX, pageY, paperWidth, paperHeight);
        tileGraphics.scale(paintScale, paintScale);
        this.drawMindMap(tileGraphics, model, cfg);
      } finally {
        tileGraphics.dispose();
      }
      gfx.drawImage(tile, 0, 0, paperWidth, paperHeight, null);
    } catch (final OutOfMemoryError error) {
      LOGGER.error("Not enough memory for high-resolution print raster, drawing vector", error);
      this.layoutMapOn(gfx, model, cfg, mapSize);
      this.translateToPage(gfx, offset, pageX, pageY, paperWidth, paperHeight);
      gfx.scale(paintScale, paintScale);
      this.drawMindMap(gfx, model, cfg);
    }
  }

  private void paintImagePage(
      final Graphics2D pageGraphics,
      final Image image,
      final double scale,
      final Point offset,
      final int pageX,
      final int pageY,
      final int paperWidth,
      final int paperHeight
  ) {
    final Graphics2D gfx = (Graphics2D) pageGraphics.create();
    try {
      this.preparePrintGraphics(gfx);
      this.clipToPaper(gfx, paperWidth, paperHeight);
      this.translateToPage(gfx, offset, pageX, pageY, paperWidth, paperHeight);

      final AffineTransform transform = AffineTransform.getScaleInstance(scale, scale);
      gfx.drawImage(image, transform, null);
    } finally {
      gfx.dispose();
    }
  }

  private void drawMindMap(
      final Graphics2D gfx,
      final MindMap model,
      final MindMapPanelConfig cfg
  ) {
    drawOnGraphicsForConfiguration(new MMGraphics2DWrapper(gfx), cfg, model, false, null);
  }

  private void translateToPage(
      final Graphics2D gfx,
      final Point offset,
      final int pageX,
      final int pageY,
      final int paperWidth,
      final int paperHeight
  ) {
    gfx.translate(offset.x - (double) pageX * paperWidth,
        offset.y - (double) pageY * paperHeight);
  }

  private void clipToPaper(final Graphics2D gfx, final int paperWidth, final int paperHeight) {
    gfx.clip(new Rectangle2D.Double(0.0d, 0.0d, paperWidth, paperHeight));
  }

  private double resolveRasterScale(final Graphics2D gfx) {
    final AffineTransform transform = gfx.getTransform();
    final double deviceScale = Math.max(Math.abs(transform.getScaleX()),
        Math.abs(transform.getScaleY()));
    if (deviceScale <= 1.0d) {
      return 1.0d;
    }
    return Math.min(deviceScale, PRINT_RASTER_DPI / PAGE_POINTS_PER_INCH);
  }

  private Point calcCenteredOffset(
      final int pagesHorz,
      final int pagesVert,
      final int paperWidth,
      final int paperHeight,
      final double contentWidth,
      final double contentHeight
  ) {
    int x = 0;
    int y = 0;

    if (pagesHorz == 1) {
      x = Math.max(0, (paperWidth - (int) Math.round(contentWidth)) / 2);
    }

    if (pagesVert == 1) {
      y = Math.max(0, (paperHeight - (int) Math.round(contentHeight)) / 2);
    }

    return new Point(x, y);
  }
}
