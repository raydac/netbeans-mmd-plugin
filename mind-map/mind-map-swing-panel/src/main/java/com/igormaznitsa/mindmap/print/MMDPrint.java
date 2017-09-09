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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Dimension2D;

import javax.annotation.Nonnull;

import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.swing.panel.ui.gfx.MMGraphics2DWrapper;

public class MMDPrint {

  private static final Logger LOGGER = LoggerFactory.getLogger(MMDPrint.class);

  private static final PrintPage [][] NO_PAGES = new PrintPage[0][0];
  
  private final PrintPage[][] pages;

  public MMDPrint(@Nonnull final MindMapPanel panel, final int paperWidthInPixels, final int paperHeightInPixels, final double pageZoomFactor) {
    LOGGER.info(String.format("Request to prepare print pages for %dx%d with scale %f", paperWidthInPixels, paperHeightInPixels, pageZoomFactor));

    PrintPage [][] pgs = NO_PAGES;
    
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
      
      cfg.setScale(0.3d);

      final MindMap theModel = new MindMap(panel.getModel(), null);

      final Dimension2D modelImageSize = calculateSizeOfMapInPixels(theModel, null, cfg, false);

      if (theModel.getRoot() != null && modelImageSize != null) {
        final int modelWidth = (int) Math.round(modelImageSize.getWidth());
        final int modelHeight = (int) Math.round(modelImageSize.getHeight());
        int pagesHorz = 1 + modelWidth / paperWidthInPixels;
        int pagesVert = 1 + modelHeight / paperHeightInPixels;

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
                
                gfx.translate(-pageX * paperWidthInPixels, -pageY * paperHeightInPixels);
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
    this.pages = pgs;
  }

  @Nonnull
  @MustNotContainNull
  public PrintPage[][] getPages() {
    return this.pages.clone();
  }
}
