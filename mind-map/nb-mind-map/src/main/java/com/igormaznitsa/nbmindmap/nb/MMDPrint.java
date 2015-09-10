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
package com.igormaznitsa.nbmindmap.nb;

import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import org.netbeans.spi.print.PrintPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MMDPrint {

  private static final Logger logger = LoggerFactory.getLogger(MMDPrint.class);

  private final PrintPage[][] pages;

  private static final double SMALL_SCALING_THRESHOLD = 0.20d;
  private static final double SCALE_RATIO_THRESHOLD = 0.20d;

  public MMDPrint(final MindMapPanel panel, final int paperWidthInPixels, final int paperHeightInPixels, final double pageZoomFactor) {
    logger.info(String.format("Request to prepare print pages for %dx%d with scale %f", paperWidthInPixels, paperHeightInPixels, pageZoomFactor));

    if (panel == null || paperWidthInPixels <= 0 || paperHeightInPixels <= 0) {
      this.pages = new PrintPage[0][0];
    }
    else {
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

      cfg.setElementBorderWidth(1.5f);
      cfg.setCollapsatorBorderWidth(1.0f);
      cfg.setConnectorWidth(2.0f);

      cfg.setScale(1.0d);

      final MindMap theModel = new MindMap(panel.getModel());

      BufferedImage renderedMap = MindMapPanel.renderMindMapAsImage(theModel, cfg, false);

      if (renderedMap.getWidth() > paperWidthInPixels || renderedMap.getHeight() > paperHeightInPixels) {
        // split to pages
        int pagesHorz = Math.max(1, renderedMap.getWidth() / paperWidthInPixels + (renderedMap.getWidth() % paperWidthInPixels != 0 ? 1 : 0));
        int pagesVert = Math.max(1, renderedMap.getHeight() / paperHeightInPixels + (renderedMap.getHeight() % paperHeightInPixels != 0 ? 1 : 0));

        final boolean couldBeScaledForX = (renderedMap.getWidth() % paperWidthInPixels) <= Math.round(paperWidthInPixels * SMALL_SCALING_THRESHOLD);
        final boolean couldBeScaledForY = (renderedMap.getHeight() % paperHeightInPixels) <= Math.round(paperHeightInPixels * SMALL_SCALING_THRESHOLD);

        if (couldBeScaledForX || couldBeScaledForY) {
          final double currentRatio = (double) Math.min(renderedMap.getWidth(), renderedMap.getHeight()) / (double) Math.max(renderedMap.getWidth(), renderedMap.getHeight());

          final int potentialPageWidth = Math.min(renderedMap.getWidth(), Math.max(1, pagesHorz - (couldBeScaledForX ? 1 : 0)) * paperWidthInPixels);
          final int potentialPageHeight = Math.min(renderedMap.getHeight(), Math.max(1, pagesVert - (couldBeScaledForY ? 1 : 0)) * paperHeightInPixels);

          final double possibleRatio = (double) Math.min(potentialPageWidth, potentialPageHeight) / (double) Math.max(potentialPageWidth, potentialPageHeight);

          if (Math.abs(currentRatio - possibleRatio) <= SCALE_RATIO_THRESHOLD) {
            final BufferedImage scaledImage = new BufferedImage(potentialPageWidth, potentialPageHeight, BufferedImage.TYPE_INT_ARGB);
            final Graphics2D sigfx = scaledImage.createGraphics();
            MindMapPanel.prepareGraphicsForQuality(sigfx);
            try {
              sigfx.drawImage(renderedMap, 0, 0, scaledImage.getWidth(), scaledImage.getHeight(), null);
            }
            finally {
              sigfx.dispose();
            }
            renderedMap = scaledImage;
            pagesHorz = Math.max(1, renderedMap.getWidth() / paperWidthInPixels + (renderedMap.getWidth() % paperWidthInPixels == 0 ? 0 : 1));
            pagesVert = Math.max(1, renderedMap.getHeight() / paperHeightInPixels + (renderedMap.getHeight() % paperHeightInPixels == 0 ? 0 : 1));
          }
        }

        final boolean centerX = pagesHorz == 1;
        final boolean centerY = pagesVert == 1;

        this.pages = new PrintPage[pagesVert][pagesHorz];
        for (int y = 0; y < pagesVert; y++) {
          for (int x = 0; x < pagesHorz; x++) {
            final int pageX = x;
            final int pageY = y;

            final BufferedImage theImage = renderedMap;

            this.pages[pageY][pageX] = new PrintPage() {
              @Override
              public void print(final Graphics g) {
                final Graphics2D gfx = (Graphics2D) g.create();
                MindMapPanel.prepareGraphicsForQuality(gfx);
                try {
                  final int xoffset = paperWidthInPixels * pageX;
                  final int yoffset = paperHeightInPixels * pageY;

                  int drawx = - xoffset;
                  int drawy = -yoffset;
                  
                  if (centerX) {
                    drawx = (paperWidthInPixels - theImage.getWidth()) / 2;
                  }
                  
                  if (centerY) {
                    drawy = (paperHeightInPixels - theImage.getHeight()) / 2;
                  }
                  gfx.drawImage(theImage, drawx, drawy, null);
                }
                finally {
                  gfx.dispose();
                }
              }
            };
          }
        }
      }
      else {
        // fill single page
        final double scaleX = (double) paperWidthInPixels / (double) renderedMap.getWidth();
        final double scaleY = (double) paperHeightInPixels / (double) renderedMap.getHeight();
        cfg.setScale(Math.min(scaleX, scaleY));
        final BufferedImage theImage = MindMapPanel.renderMindMapAsImage(theModel, cfg, false);
        this.pages = new PrintPage[][]{{
          new PrintPage() {
            @Override
            public void print(final Graphics g) {
              final Graphics2D gfx = (Graphics2D) g.create();
              MindMapPanel.prepareGraphicsForQuality(gfx);
              try {
                if (theImage != null) {
                  gfx.translate(((double) paperWidthInPixels - theImage.getWidth()) / 2, ((double) paperHeightInPixels - theImage.getHeight()) / 2);
                  gfx.drawImage(theImage, 0, 0, null);
                }
              }
              finally {
                gfx.dispose();
              }
            }
          }
        }};
      }
    }
  }

  public PrintPage[][] getPages() {
    return pages;
  }
}
