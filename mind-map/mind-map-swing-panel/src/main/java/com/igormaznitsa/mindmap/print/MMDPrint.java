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

import static com.igormaznitsa.mindmap.swing.panel.MindMapPanel.calculateSizeOfMapInPixels;
import static com.igormaznitsa.mindmap.swing.panel.MindMapPanel.drawOnGraphicsForConfiguration;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.geom.Dimension2D;

import java.awt.image.BufferedImage;
import javax.annotation.Nonnull;

import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.meta.common.utils.Assertions;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.swing.panel.ui.gfx.MMGraphics2DWrapper;

public class MMDPrint {

  private static final Logger LOGGER = LoggerFactory.getLogger(MMDPrint.class);

  private static final PrintPage[][] NO_PAGES = new PrintPage[0][0];

  private final PrintPage[][] pages;

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

  public MMDPrint(@Nonnull final MindMapPanel panel, final int paperWidthInPixels, final int paperHeightInPixels, @Nonnull final MMDPrintOptions options) {
    LOGGER.info(String.format("Request to prepare print pages for %dx%d", paperWidthInPixels, paperHeightInPixels));

    PrintPage[][] pgs = NO_PAGES;

    if (paperWidthInPixels > 0 && paperHeightInPixels > 0) {
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

      final MindMap theModel = new MindMap(panel.getModel(), null);

      double scale = 1.0d;
      cfg.setScale(scale);

      final Image scaledSinglePageAsImage;

      final double SCALE_STEP = 0.01d;

      final Point offsetOfImage;

      switch (options.getScaleType()) {
        case FIT_HEIGHT_TO_PAGES: {
          scaledSinglePageAsImage = null;

          Dimension2D calculatedSize = Assertions.assertNotNull("Must not be null", calculateSizeOfMapInPixels(theModel, null, cfg, false));
          scale = (double) (options.getPagesInColumn() * paperHeightInPixels) / (int) Math.round(calculatedSize.getHeight());
          cfg.setScale(scale);
          calculatedSize = Assertions.assertNotNull("Must not be null", calculateSizeOfMapInPixels(theModel, null, cfg, false));
          int pvert = 1 + (int) Math.round(calculatedSize.getHeight()) / (paperHeightInPixels + 1);

          while (pvert > options.getPagesInColumn() && scale > SCALE_STEP) {
            scale -= SCALE_STEP;
            cfg.setScale(scale);
            calculatedSize = Assertions.assertNotNull("Must not be null", calculateSizeOfMapInPixels(theModel, null, cfg, false));
            pvert = 1 + (int) Math.round(calculatedSize.getHeight()) / (paperHeightInPixels + 1);
          }

          final int phorz = 1 + (int) Math.round(calculatedSize.getWidth()) / (paperWidthInPixels + 1);
          offsetOfImage = calcOffsetImage(phorz, pvert, paperWidthInPixels, paperHeightInPixels, calculatedSize);
        }
        break;
        case FIT_WIDTH_TO_PAGES: {
          scaledSinglePageAsImage = null;

          Dimension2D calculatedSize = Assertions.assertNotNull("Must not be null", calculateSizeOfMapInPixels(theModel, null, cfg, false));
          scale = (double) (options.getPagesInRow() * paperWidthInPixels) / (int) Math.round(calculatedSize.getWidth());
          cfg.setScale(scale);
          calculatedSize = Assertions.assertNotNull("Must not be null", calculateSizeOfMapInPixels(theModel, null, cfg, false));
          int phorz = 1 + (int) Math.round(calculatedSize.getWidth()) / (paperWidthInPixels + 1);

          while (phorz > options.getPagesInRow() && scale > SCALE_STEP) {
            scale -= SCALE_STEP;
            cfg.setScale(scale);
            calculatedSize = Assertions.assertNotNull("Must not be null", calculateSizeOfMapInPixels(theModel, null, cfg, false));
            phorz = 1 + (int) Math.round(calculatedSize.getWidth()) / (paperWidthInPixels + 1);
          }

          final int pvert = 1 + (int) Math.round(calculatedSize.getHeight()) / (paperHeightInPixels + 1);
          offsetOfImage = calcOffsetImage(phorz, pvert, paperWidthInPixels, paperHeightInPixels, calculatedSize);
        }
        break;
        case FIT_TO_SINGLE_PAGE: {
          Dimension2D calculatedSize = Assertions.assertNotNull("Must not be null", calculateSizeOfMapInPixels(theModel, null, cfg, false));
          scale = (double) (options.getPagesInRow() * paperWidthInPixels) / (int) Math.round(calculatedSize.getWidth());
          scale = Math.min(scale, (double) (options.getPagesInColumn() * paperHeightInPixels) / (int) Math.round(calculatedSize.getHeight()));
          cfg.setScale(scale);
          calculatedSize = Assertions.assertNotNull("Must not be null", calculateSizeOfMapInPixels(theModel, null, cfg, false));
          int phorz = 1 + (int) Math.round(calculatedSize.getWidth()) / (paperWidthInPixels + 1);
          int pvert = 1 + (int) Math.round(calculatedSize.getHeight()) / (paperHeightInPixels + 1);

          while ((phorz > 1 || pvert > 1) && scale > SCALE_STEP) {
            scale -= SCALE_STEP;
            cfg.setScale(scale);
            calculatedSize = Assertions.assertNotNull("Must not be null", calculateSizeOfMapInPixels(theModel, null, cfg, false));
            phorz = 1 + (int) Math.round(calculatedSize.getWidth()) / (paperWidthInPixels + 1);
            pvert = 1 + (int) Math.round(calculatedSize.getHeight()) / (paperHeightInPixels + 1);
          }

          if (phorz > 1 || pvert > 1) {
            // we have to scale to fit only page
            final BufferedImage image = MindMapPanel.renderMindMapAsImage(theModel, cfg, false);
            if (image != null) {
              if (phorz > 1) {
                scaledSinglePageAsImage = image.getScaledInstance(paperWidthInPixels * image.getHeight() / image.getWidth(), paperHeightInPixels, Image.SCALE_SMOOTH);
              } else {
                scaledSinglePageAsImage = image.getScaledInstance(paperWidthInPixels, paperHeightInPixels * image.getWidth() / image.getHeight(), Image.SCALE_SMOOTH);
              }
              offsetOfImage = new Point(Math.max(0, (paperWidthInPixels - scaledSinglePageAsImage.getWidth(null)) / 2), Math.max(0, (paperHeightInPixels - scaledSinglePageAsImage.getHeight(null)) / 2));
            } else {
              scaledSinglePageAsImage = null;
              offsetOfImage = calcOffsetImage(phorz, pvert, paperWidthInPixels, paperHeightInPixels, calculatedSize);
            }
          } else {
            scaledSinglePageAsImage = null;
            offsetOfImage = calcOffsetImage(phorz, pvert, paperWidthInPixels, paperHeightInPixels, calculatedSize);
          }
        }
        break;
        case ZOOM: {
          scaledSinglePageAsImage = null;
          scale = options.getScale();
          cfg.setScale(scale);
          final Dimension2D calculatedSize = Assertions.assertNotNull("Must not be null", calculateSizeOfMapInPixels(theModel, null, cfg, false));
          final int phorz = 1 + (int) Math.round(calculatedSize.getWidth()) / (paperWidthInPixels + 1);
          final int pvert = 1 + (int) Math.round(calculatedSize.getHeight()) / (paperHeightInPixels + 1);
          offsetOfImage = calcOffsetImage(phorz, pvert, paperWidthInPixels, paperHeightInPixels, calculatedSize);
        }
        break;
        default:
          throw new Error("Unexpected value:" + options.getScaleType());
      }

      if (scaledSinglePageAsImage != null) {
        pgs = new PrintPage[][]{
          new PrintPage[]{
            new PrintPage() {
              @Override
              public void print(@Nonnull final Graphics g) {
                final Graphics2D gfx = (Graphics2D) g.create();
                Utils.prepareGraphicsForQuality(gfx);
                gfx.drawImage(scaledSinglePageAsImage, offsetOfImage.x, offsetOfImage.y, null);
              }
            }
          }
        };
      } else {

        cfg.setScale(scale);

        final Dimension2D modelImageSize = calculateSizeOfMapInPixels(theModel, null, cfg, false);

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
                  Utils.prepareGraphicsForQuality(gfx);
                  MindMapPanel.layoutFullDiagramWithCenteringToPaper(new MMGraphics2DWrapper(gfx), theModel, cfg, modelImageSize);

                  gfx.translate(offsetOfImage.x - pageX * paperWidthInPixels, offsetOfImage.y - pageY * paperHeightInPixels);
                  try {
                    drawOnGraphicsForConfiguration(new MMGraphics2DWrapper(gfx), cfg, theModel, false, null);
                  }
                  finally {
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
  @MustNotContainNull
  public PrintPage[][] getPages() {
    return this.pages.clone();
  }
}
