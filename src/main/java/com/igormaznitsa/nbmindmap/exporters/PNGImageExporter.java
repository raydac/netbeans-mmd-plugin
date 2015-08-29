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
package com.igormaznitsa.nbmindmap.exporters;

import com.igormaznitsa.nbmindmap.mmgui.Configuration;
import com.igormaznitsa.nbmindmap.mmgui.MindMapPanel;
import com.igormaznitsa.nbmindmap.utils.Icons;
import com.igormaznitsa.nbmindmap.utils.Logger;
import com.igormaznitsa.nbmindmap.utils.NbUtils;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileFilter;
import org.apache.commons.io.FileUtils;
import org.openide.filesystems.FileChooserBuilder;

public final class PNGImageExporter extends AbstractMindMapExporter {

  public PNGImageExporter() {
    super();
  }

  @Override
  public void doExport(final MindMapPanel viewPanel) throws IOException {
    final Configuration newConfig = new Configuration(viewPanel.getConfiguration(), false);
    newConfig.setScale(1.0f);

    final RenderedImage image = MindMapPanel.renderMindMapAsImage(viewPanel.getModel(), newConfig, true);

    if (image == null) {
      NbUtils.msgError("Can't export mind map as image, may there is not any topic!");
      return;
    }

    final ByteArrayOutputStream buff = new ByteArrayOutputStream(128000);
    ImageIO.write(image, "png", buff);

    final byte[] imageData = buff.toByteArray();

    final File home = new File(System.getProperty("user.home"));
    File fileToSaveImage = new FileChooserBuilder("user-dir").setTitle("Export as PNG Image").setDefaultWorkingDirectory(home).setFilesOnly(true).setFileFilter(new FileFilter() {

      @Override
      public boolean accept(File f) {
        return f.isDirectory() || (f.isFile() && f.getName().toLowerCase(Locale.ENGLISH).endsWith(".png"));
      }

      @Override
      public String getDescription() {
        return "PNG image (*.png)";
      }
    }).setApproveText("Save").showSaveDialog();

    fileToSaveImage = checkFile(fileToSaveImage, ".png");
    
    if (fileToSaveImage != null) {
      try {
        FileUtils.writeByteArrayToFile(fileToSaveImage, imageData);
      }
      catch (final IOException ex) {
        Logger.error("Can't save PNG image as " + fileToSaveImage, ex);
        NbUtils.msgError("Can't save PNG image for error! See the log!");
      }
    }

  }

  @Override
  public String getName() {
    return "PNG image";
  }

  @Override
  public String getReference() {
    return "Export of mind map as a PNG image file";
  }

  @Override
  public ImageIcon getIcon() {
    return Icons.FILE_EXTENSION_PNG.getIcon();
  }
}
