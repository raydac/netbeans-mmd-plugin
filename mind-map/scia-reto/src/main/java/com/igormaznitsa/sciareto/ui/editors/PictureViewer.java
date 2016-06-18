/*
 * Copyright 2016 Igor Maznitsa.
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
package com.igormaznitsa.sciareto.ui.editors;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileFilter;
import org.apache.commons.io.FilenameUtils;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.sciareto.Context;
import com.igormaznitsa.sciareto.ui.tabs.TabTitle;

public class PictureViewer extends AbstractScrollPane {

  private static final long serialVersionUID = 4262835444678960206L;

  private static final Logger LOGGER = LoggerFactory.getLogger(PictureViewer.class);
  
  private final TabTitle title;
  private final JLabel label;
  private final BufferedImage image;
  public static final Set<String> SUPPORTED_FORMATS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("png", "jpg", "gif")));

  public static final FileFilter IMAGE_FILE_FILTER = new FileFilter() {
    @Override
    public boolean accept(@Nonnull final File f) {
      if (f.isDirectory()) {
        return true;
      }
      final String ext = FilenameUtils.getExtension(f.getName()).toLowerCase(Locale.ENGLISH);
      return SUPPORTED_FORMATS.contains(ext);
    }

    @Override
    @Nonnull
    public String getDescription() {
      return "Image file (*.png,*.jpg,*.gif)";
    }
  };

  @Override
  @Nonnull
  public FileFilter getFileFilter() {
    return IMAGE_FILE_FILTER;
  }

  public PictureViewer(@Nonnull final Context context, @Nonnull final File file) throws IOException {
    super();
    this.title = new TabTitle(context, this, file);
    this.label = new JLabel();
    
    BufferedImage loaded = null;
    try {
      loaded = ImageIO.read(file);
    } catch (Exception ex) {
      LOGGER.error("Can't load image",ex);
      loaded = null;
    }

    this.image = loaded;
    
    this.label.setHorizontalTextPosition(JLabel.CENTER);
    this.label.setVerticalTextPosition(JLabel.CENTER);
    this.label.setHorizontalAlignment(SwingConstants.CENTER);
    this.label.setVerticalAlignment(SwingConstants.CENTER);
    
    if (this.image == null){
      this.label.setIcon(null);
      this.label.setText("Can't load image");
    } else {
      this.label.setIcon(new ImageIcon(this.image));
      this.label.setText("");
    }
    
    this.setViewportView(this.label);
    this.revalidate();
  }

  @Override
  public boolean saveDocument() {
    boolean result = false;
    final String ext = FilenameUtils.getExtension(this.title.getAssociatedFile().getName()).trim().toLowerCase(Locale.ENGLISH);
    if (SUPPORTED_FORMATS.contains(ext)) {
      try{
        ImageIO.write(this.image, ext, this.title.getAssociatedFile());
        result = true;
      }catch(Exception ex){
        LOGGER.error("Can't write image",ex);
      }
    } else {
      try {
        LOGGER.warn("unsupported image format, will be saved as png : "+ext);
        ImageIO.write(this.image, "png", this.title.getAssociatedFile());
        result = true;
      } catch (Exception ex) {
        LOGGER.error("Can't write image", ex);
      }
    }

    return result;
  }

  @Override
  @Nonnull
  public TabTitle getTabTitle() {
    return this.title;
  }

  @Override
  @Nonnull
  public JComponent getMainComponent() {
    return this;
  }

}
