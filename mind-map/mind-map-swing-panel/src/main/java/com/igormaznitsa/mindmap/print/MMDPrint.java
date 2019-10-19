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

import static com.igormaznitsa.meta.common.utils.Assertions.assertNotNull;
import static com.igormaznitsa.mindmap.swing.panel.MindMapPanel.calculateSizeOfMapInPixels;
import static com.igormaznitsa.mindmap.swing.panel.MindMapPanel.drawOnGraphicsForConfiguration;


import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.meta.common.utils.Assertions;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.mindmap.swing.panel.ui.gfx.MMGraphics2DWrapper;
import com.igormaznitsa.mindmap.swing.panel.utils.RenderQuality;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;

public class MMDPrint {

  private static final Logger LOGGER = LoggerFactory.getLogger(MMDPrint.class);

  private static final PrintPage[][] NO_PAGES = new PrintPage[0][0];

  private final PrintPage[][] pages;

  public MMDPrint(@Nonnull final PrintableObject printableObject, final int paperWidthInPixels, final int paperHeightInPixels, @Nonnull final MMDPrintOptions options) {
    LOGGER.info(String.format("Request to prepare print pages for %dx%d", paperWidthInPixels, paperHeightInPixels));

    PrintPage[][] pgs = NO_PAGES;

    final MindMapPanelConfig cfg;
    final MindMap theModel;
    double scale = 1.0d;

    final boolean drawAsImage = printableObject.isImage() || options.isDrawAsImage();

    if (paperWidthInPixels > 0 && paperHeightInPixels > 0) {

      if (printableObject.isMmdPanel()) {
        cfg = new MindMapPanelConfig(assertNotNull(printableObject.getPanel()).getConfiguration(), false);
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

        theModel = new MindMap(printableObject.getPanel().getModel(), null);

        cfg.setScale(scale);
      } else {
        theModel = null;
        cfg = null;
      }

      final Image imageToDraw;

      final double SCALE_STEP = 0.01d;

      final Point offsetOfImage;

      switch (options.getScaleType()) {
        case FIT_HEIGHT_TO_PAGES: {
          if (printableObject.isMmdPanel()) {
            Dimension2D calculatedSize = Assertions.assertNotNull("Must not be null", calculateSizeOfMapInPixels(theModel, null, cfg, false, RenderQuality.QUALITY));
            scale = (double) (options.getPagesInColumn() * paperHeightInPixels) / (int) Math.round(calculatedSize.getHeight());
            cfg.setScale(scale);
            calculatedSize = Assertions.assertNotNull("Must not be null", calculateSizeOfMapInPixels(theModel, null, cfg, false, RenderQuality.QUALITY));
            int pvert = 1 + (int) Math.round(calculatedSize.getHeight()) / (paperHeightInPixels + 1);

            while (pvert > options.getPagesInColumn() && scale > SCALE_STEP) {
              scale -= SCALE_STEP;
              cfg.setScale(scale);
              calculatedSize = Assertions.assertNotNull("Must not be null", calculateSizeOfMapInPixels(theModel, null, cfg, false, RenderQuality.QUALITY));
              pvert = 1 + (int) Math.round(calculatedSize.getHeight()) / (paperHeightInPixels + 1);
            }

            if (drawAsImage) {
              imageToDraw = Assertions.assertNotNull("Can't raster map as image", MindMapPanel.renderMindMapAsImage(theModel, cfg, false, RenderQuality.QUALITY));
              int phorz = 1 + imageToDraw.getWidth(null) / (paperWidthInPixels + 1);
              pvert = 1 + imageToDraw.getHeight(null) / (paperHeightInPixels + 1);
              offsetOfImage = calcOffsetImage(phorz, pvert, paperWidthInPixels, paperHeightInPixels, new Dimension(imageToDraw.getWidth(null), imageToDraw.getHeight(null)));
            } else {
              imageToDraw = null;
              final int phorz = 1 + (int) Math.round(calculatedSize.getWidth()) / (paperWidthInPixels + 1);
              offsetOfImage = calcOffsetImage(phorz, pvert, paperWidthInPixels, paperHeightInPixels, calculatedSize);
            }
          } else if (printableObject.isImage()) {
            final int neededHeight = options.getPagesInColumn() * paperHeightInPixels;
            imageToDraw = makeScaledInstance(printableObject.getImage(), (float) neededHeight / (float) printableObject.getImage().getHeight(null));
            final int phorz = 1 + imageToDraw.getWidth(null) / (paperWidthInPixels + 1);
            final int pvert = 1 + imageToDraw.getHeight(null) / (paperHeightInPixels + 1);
            offsetOfImage = calcOffsetImage(phorz, pvert, paperWidthInPixels, paperHeightInPixels, new Dimension(imageToDraw.getWidth(null), imageToDraw.getHeight(null)));
          } else {
            throw new Error("Unexpected type");
          }
        }
        break;
        case FIT_WIDTH_TO_PAGES: {
          if (printableObject.isMmdPanel()) {
            Dimension2D calculatedSize = Assertions.assertNotNull("Must not be null", calculateSizeOfMapInPixels(theModel, null, cfg, false, RenderQuality.QUALITY));
            scale = (double) (options.getPagesInRow() * paperWidthInPixels) / (int) Math.round(calculatedSize.getWidth());
            cfg.setScale(scale);
            calculatedSize = Assertions.assertNotNull("Must not be null", calculateSizeOfMapInPixels(theModel, null, cfg, false, RenderQuality.QUALITY));
            int phorz = 1 + (int) Math.round(calculatedSize.getWidth()) / (paperWidthInPixels + 1);

            while (phorz > options.getPagesInRow() && scale > SCALE_STEP) {
              scale -= SCALE_STEP;
              cfg.setScale(scale);
              calculatedSize = Assertions.assertNotNull("Must not be null", calculateSizeOfMapInPixels(theModel, null, cfg, false, RenderQuality.QUALITY));
              phorz = 1 + (int) Math.round(calculatedSize.getWidth()) / (paperWidthInPixels + 1);
            }

            if (drawAsImage) {
              imageToDraw = Assertions.assertNotNull("Can't raster map as image", MindMapPanel.renderMindMapAsImage(theModel, cfg, false, RenderQuality.QUALITY));
              int pvert = 1 + imageToDraw.getHeight(null) / (paperHeightInPixels + 1);
              phorz = 1 + imageToDraw.getWidth(null) / (paperWidthInPixels + 1);
              offsetOfImage = calcOffsetImage(phorz, pvert, paperWidthInPixels, paperHeightInPixels, new Dimension(imageToDraw.getWidth(null), imageToDraw.getHeight(null)));
            } else {
              imageToDraw = null;
              final int pvert = 1 + (int) Math.round(calculatedSize.getHeight()) / (paperHeightInPixels + 1);
              offsetOfImage = calcOffsetImage(phorz, pvert, paperWidthInPixels, paperHeightInPixels, calculatedSize);
            }
          } else if (printableObject.isImage()) {
            final int neededWidth = options.getPagesInRow() * paperWidthInPixels;
            imageToDraw = makeScaledInstance(printableObject.getImage(), (float) neededWidth / (float) printableObject.getImage().getWidth(null));
            final int phorz = 1 + imageToDraw.getWidth(null) / (paperWidthInPixels + 1);
            final int pvert = 1 + imageToDraw.getHeight(null) / (paperHeightInPixels + 1);
            offsetOfImage = calcOffsetImage(phorz, pvert, paperWidthInPixels, paperHeightInPixels, new Dimension(imageToDraw.getWidth(null), imageToDraw.getHeight(null)));
          } else {
            throw new Error("Unexpected type");
          }
        }
        break;
        case FIT_TO_SINGLE_PAGE: {
          if (printableObject.isMmdPanel()) {
            Dimension2D calculatedSize = Assertions.assertNotNull("Must not be null", calculateSizeOfMapInPixels(theModel, null, cfg, false, RenderQuality.QUALITY));
            scale = (double) (options.getPagesInRow() * paperWidthInPixels) / (int) Math.round(calculatedSize.getWidth());
            scale = Math.min(scale, (double) (options.getPagesInColumn() * paperHeightInPixels) / (int) Math.round(calculatedSize.getHeight()));
            cfg.setScale(scale);
            calculatedSize = Assertions.assertNotNull("Must not be null", calculateSizeOfMapInPixels(theModel, null, cfg, false, RenderQuality.QUALITY));
            int phorz = 1 + (int) Math.round(calculatedSize.getWidth()) / (paperWidthInPixels + 1);
            int pvert = 1 + (int) Math.round(calculatedSize.getHeight()) / (paperHeightInPixels + 1);

            while ((phorz > 1 || pvert > 1) && scale > SCALE_STEP) {
              scale -= SCALE_STEP;
              cfg.setScale(scale);
              calculatedSize = Assertions.assertNotNull("Must not be null", calculateSizeOfMapInPixels(theModel, null, cfg, false, RenderQuality.QUALITY));
              phorz = 1 + (int) Math.round(calculatedSize.getWidth()) / (paperWidthInPixels + 1);
              pvert = 1 + (int) Math.round(calculatedSize.getHeight()) / (paperHeightInPixels + 1);
            }

            if (phorz > 1 || pvert > 1) {
              // we have to scale to fit only page
              final BufferedImage image = MindMapPanel.renderMindMapAsImage(theModel, cfg, false, RenderQuality.QUALITY);
              if (image != null) {
                if (phorz > 1) {
                  imageToDraw = image.getScaledInstance(paperWidthInPixels * image.getHeight() / image.getWidth(), paperHeightInPixels, Image.SCALE_SMOOTH);
                } else {
                  imageToDraw = image.getScaledInstance(paperWidthInPixels, paperHeightInPixels * image.getWidth() / image.getHeight(), Image.SCALE_SMOOTH);
                }
                offsetOfImage = new Point(Math.max(0, (paperWidthInPixels - imageToDraw.getWidth(null)) / 2), Math.max(0, (paperHeightInPixels - imageToDraw.getHeight(null)) / 2));
              } else {
                imageToDraw = null;
                offsetOfImage = calcOffsetImage(phorz, pvert, paperWidthInPixels, paperHeightInPixels, calculatedSize);
              }
            } else {
              if (drawAsImage) {
                imageToDraw = Assertions.assertNotNull("Can't raster map as image", MindMapPanel.renderMindMapAsImage(theModel, cfg, false, RenderQuality.QUALITY));
                offsetOfImage = calcOffsetImage(phorz, pvert, paperWidthInPixels, paperHeightInPixels, new Dimension(imageToDraw.getWidth(null), imageToDraw.getHeight(null)));
              } else {
                imageToDraw = null;
                offsetOfImage = calcOffsetImage(phorz, pvert, paperWidthInPixels, paperHeightInPixels, calculatedSize);
              }
            }
          } else if (printableObject.isImage()) {
            final float hrzrScale = (float) paperWidthInPixels / (float) printableObject.getImage().getWidth(null);
            final float vertScale = (float) paperHeightInPixels / (float) printableObject.getImage().getHeight(null);

            if ((vertScale < 1.0f && hrzrScale < 1.0f) || (vertScale > 1.0f || hrzrScale > 1.0f)) {
              imageToDraw = makeScaledInstance(printableObject.getImage(), Math.min(vertScale, hrzrScale));
            } else {
              imageToDraw = printableObject.getImage();
            }

            final int phorz = 1 + imageToDraw.getWidth(null) / (paperWidthInPixels + 1);
            final int pvert = 1 + imageToDraw.getHeight(null) / (paperHeightInPixels + 1);
            offsetOfImage = calcOffsetImage(phorz, pvert, paperWidthInPixels, paperHeightInPixels, new Dimension(imageToDraw.getWidth(null), imageToDraw.getHeight(null)));
          } else {
            throw new Error("Unexpected type");
          }
        }
        break;
        case ZOOM: {
          scale = options.getScale();
          if (printableObject.isMmdPanel()) {
            cfg.setScale(scale);
            final Dimension2D calculatedSize = Assertions.assertNotNull("Must not be null", calculateSizeOfMapInPixels(theModel, null, cfg, false, RenderQuality.QUALITY));
            final int phorz = 1 + (int) Math.round(calculatedSize.getWidth()) / (paperWidthInPixels + 1);
            final int pvert = 1 + (int) Math.round(calculatedSize.getHeight()) / (paperHeightInPixels + 1);

            if (drawAsImage) {
              imageToDraw = Assertions.assertNotNull("Can't raster map as image", MindMapPanel.renderMindMapAsImage(theModel, cfg, false, RenderQuality.QUALITY));
              offsetOfImage = calcOffsetImage(phorz, pvert, paperWidthInPixels, paperHeightInPixels, new Dimension(imageToDraw.getWidth(null), imageToDraw.getHeight(null)));
            } else {
              imageToDraw = null;
              offsetOfImage = calcOffsetImage(phorz, pvert, paperWidthInPixels, paperHeightInPixels, calculatedSize);
            }
          } else if (printableObject.isImage()) {
            imageToDraw = makeScaledInstance(printableObject.getImage(), (float) scale);
            final int phorz = 1 + imageToDraw.getWidth(null) / (paperWidthInPixels + 1);
            final int pvert = 1 + imageToDraw.getHeight(null) / (paperHeightInPixels + 1);
            offsetOfImage = calcOffsetImage(phorz, pvert, paperWidthInPixels, paperHeightInPixels, new Dimension(imageToDraw.getWidth(null), imageToDraw.getHeight(null)));
          } else {
            throw new Error("Unexpected type");
          }
        }
        break;
        default:
          throw new Error("Unexpected value:" + options.getScaleType());
      }

      if (imageToDraw != null) {
        int pagesHorz = 1 + imageToDraw.getWidth(null) / (paperWidthInPixels + 1);
        int pagesVert = 1 + imageToDraw.getHeight(null) / (paperHeightInPixels + 1);

        pgs = new PrintPage[pagesVert][pagesHorz];
        for (int y = 0; y < pagesVert; y++) {
          for (int x = 0; x < pagesHorz; x++) {
            final int pageX = x;
            final int pageY = y;
            pgs[pageY][pageX] = new PrintPage() {
              @Override
              public void print(@Nonnull final Graphics g) {
                final Graphics2D gfx = (Graphics2D) g.create();
                RenderQuality.QUALITY.prepare(gfx);
                gfx.translate(offsetOfImage.x - pageX * paperWidthInPixels, offsetOfImage.y - pageY * paperHeightInPixels);
                gfx.drawImage(imageToDraw, 0, 0, null);
              }
            };
          }
        }
      } else {

        cfg.setScale(scale);

        final Dimension2D modelImageSize = calculateSizeOfMapInPixels(theModel, null, cfg, false, RenderQuality.QUALITY);

        if (theModel.getRoot() != null && modelImageSize != null) {
          final int modelWidth = (int) Math.round(modelImageSize.getWidth());
          final int modelHeight = (int) Math.round(modelImageSize.getHeight());
          int pagesHorz = 1 + modelWidth / (paperWidthInPixels + 1);
          int pagesVert = 1 + modelHeight / (paperHeightInPixels + 1);

          pgs = new PrintPage[pagesVert][pagesHorz];
          for (int y = 0; y < pagesVert; y++) {
            for (int x = 0; x < pagesHorz; x++) {
              final int pageX = x;
              final int pageY = y;

              pgs[pageY][pageX] = new PrintPage() {
                @Override
                public void print(@Nonnull final Graphics g) {
                  final Topic root = theModel.getRoot();
                  if (root == null) {
                    return;
                  }

                  final Graphics2D gfx = (Graphics2D) g.create();
                  RenderQuality.QUALITY.prepare(gfx);

                  MindMapPanel.layoutFullDiagramWithCenteringToPaper(new MMGraphics2DWrapper(gfx), theModel, cfg, modelImageSize);

                  gfx.translate(offsetOfImage.x - pageX * paperWidthInPixels, offsetOfImage.y - pageY * paperHeightInPixels);
                  try {
                    drawOnGraphicsForConfiguration(new MMGraphics2DWrapper(gfx), cfg, theModel, false, null);
                  } finally {
                    gfx.dispose();
                  }
                }
              };
            }
          }
        }
      }
    }
    this.pages = pgs;
  }

  @Nonnull
  private static Point calcOffsetImage(final int pagesHorz, final int pagesVert, final int paperWidthInPixels, final int paperHeighInPixels, @Nonnull final Dimension2D calculatedSize) {
    int x = 0;
    int y = 0;

    if (pagesHorz == 1) {
      x = Math.max(0, (paperWidthInPixels - (int) Math.round(calculatedSize.getWidth())) / 2);
    }

    if (pagesVert == 1) {
      y = Math.max(0, (paperHeighInPixels - (int) Math.round(calculatedSize.getHeight())) / 2);
    }

    return new Point(x, y);
  }

  @Nonnull
  private Image makeScaledInstance(@Nonnull final Image image, final float scale) {
    if (Float.compare(scale, 1.0f) == 0) {
      return image;
    }

    final int scaledWidth = Math.round(image.getWidth(null) * scale);
    final int scaledHeight = Math.round(image.getHeight(null) * scale);

    final BufferedImage result = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
    final Graphics2D graphics = (Graphics2D) result.getGraphics();
    try {
      final Map rhints = new HashMap();
      rhints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      rhints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      rhints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      rhints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
      rhints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
      rhints.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
      final AffineTransform transform = new AffineTransform();
      transform.setToScale(scale, scale);
      graphics.setRenderingHints(rhints);
      graphics.drawImage(image, transform, null);
    } catch (Exception ex) {
      LOGGER.error("Can't scale image", ex);
      return image;
    } finally {
      graphics.dispose();
    }
    return result;
  }

  @Nonnull
  @MustNotContainNull
  public PrintPage[][] getPages() {
    return this.pages.clone();
  }
}
