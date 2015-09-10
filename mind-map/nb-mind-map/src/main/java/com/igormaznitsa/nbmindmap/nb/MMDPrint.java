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
      cfg.setScale(1.0d);

      final MindMap theModel = new MindMap(panel.getModel());

      BufferedImage renderedMap = MindMapPanel.renderMindMapAsImage(theModel, cfg, false);

      if (renderedMap.getWidth() > paperWidthInPixels || renderedMap.getHeight() > paperHeightInPixels) {
        // split to pages
        final int pagesHorz = Math.max(1, renderedMap.getWidth() / paperWidthInPixels + (renderedMap.getWidth()%paperWidthInPixels !=0 ? 1 : 0));
        final int pagesVert = Math.max(1, renderedMap.getHeight() / paperHeightInPixels + (renderedMap.getHeight()% paperHeightInPixels != 0 ? 1 : 0));

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
                  
                  if (centerX) {
                    gfx.drawImage(theImage, (paperWidthInPixels-theImage.getWidth())/2, -yoffset, null);
                  }
                  else if (centerY) {
                    gfx.drawImage(theImage, -xoffset, (paperHeightInPixels - theImage.getHeight()) / 2, null);
                  }
                  else {
                    gfx.drawImage(theImage, -xoffset, -yoffset, null);
                  }
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
