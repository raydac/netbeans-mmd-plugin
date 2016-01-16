/*
 * Copyright 2015 Igor Maznitsa.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not usne this file except in compliance with the License.
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
package com.igormaznitsa.mindmap.exporters;

import static com.igormaznitsa.mindmap.exporters.AbstractMindMapExporter.BUNDLE;
import static com.igormaznitsa.mindmap.exporters.AbstractMindMapExporter.selectFileForFileFilter;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.mindmap.swing.panel.utils.Icons;
import java.awt.image.RenderedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import org.apache.commons.io.IOUtils;

public final class PNGImageExporter extends AbstractMindMapExporter {

  private static final Logger LOGGER = LoggerFactory.getLogger(PNGImageExporter.class);
  
  public PNGImageExporter() {
    super();
  }

  @Override
  public void doExport(final MindMapPanel panel, final OutputStream out) throws IOException {
    final Boolean expandAllNodes = panel.getController().getDialogProvider(panel).msgConfirmYesNoCancel(BUNDLE.getString("PNGImageExporter.titleExpandAllNodes"),BUNDLE.getString("PNGImageExporter.textQuestionExpandAllNodes"));
    if (expandAllNodes == null) return;
    
    final MindMapPanelConfig newConfig = new MindMapPanelConfig(panel.getConfiguration(), false);
    newConfig.setScale(1.0f);

    final RenderedImage image = MindMapPanel.renderMindMapAsImage(panel.getModel(), newConfig, expandAllNodes);

    if (image == null) {
      if (out  == null){
      LOGGER.error("Can't render map as image");
      panel.getController().getDialogProvider(panel).msgError(BUNDLE.getString("PNGImageExporter.msgErrorDuringRendering"));
      return;
      }else{
        throw new IOException("Can't render image");
      }
    }
    
    final ByteArrayOutputStream buff = new ByteArrayOutputStream(128000);
    ImageIO.write(image, "png", buff);//NOI18N

    final byte[] imageData = buff.toByteArray();

    File fileToSaveMap = null;
    OutputStream theOut = out;
    if (theOut == null) {
      fileToSaveMap = selectFileForFileFilter(panel, BUNDLE.getString("PNGImageExporter.saveDialogTitle"), ".png", BUNDLE.getString("PNGImageExporter.filterDescription"), BUNDLE.getString("PNGImageExporter.approveButtonText"));
      fileToSaveMap = checkFileAndExtension(panel, fileToSaveMap, ".png");//NOI18N
      theOut = fileToSaveMap == null ? null : new BufferedOutputStream(new FileOutputStream(fileToSaveMap, false));
    }
    if (theOut != null) {
      try {
        IOUtils.write(imageData, theOut);
      }
      finally {
        if (fileToSaveMap != null) {
          IOUtils.closeQuietly(theOut);
        }
      }
    }
  }

  @Override
  public String getName() {
    return BUNDLE.getString("PNGImageExporter.exporterName");
  }

  @Override
  public String getReference() {
    return BUNDLE.getString("PNGImageExporter.exporterReference");
  }

  @Override
  public ImageIcon getIcon() {
    return Icons.ICO_PNG.getIcon();
  }
}
