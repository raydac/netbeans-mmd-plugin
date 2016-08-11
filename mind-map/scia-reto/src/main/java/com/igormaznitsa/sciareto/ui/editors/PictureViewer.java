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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.filechooser.FileFilter;
import org.apache.commons.io.FilenameUtils;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.sciareto.Context;
import com.igormaznitsa.sciareto.preferences.PreferencesManager;
import com.igormaznitsa.sciareto.ui.tabs.TabTitle;

public final class PictureViewer extends AbstractScrollPane {

  private static final long serialVersionUID = 4262835444678960206L;

  private static final Logger LOGGER = LoggerFactory.getLogger(PictureViewer.class);

  private final TabTitle title;
  private BufferedImage image;
  public static final Set<String> SUPPORTED_FORMATS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("png", "jpg", "gif")));

  private static final class ScalableImage extends JComponent {

    private static final long serialVersionUID = 6804581090800919466L;
    private BufferedImage image;
    
    private float scale = 1.0f;
    
    private static final float SCALE_STEP = 0.2f;
    
    private final MindMapPanelConfig config = new MindMapPanelConfig();
    
    public ScalableImage(){
      super();
      this.addMouseWheelListener(new MouseWheelListener() {
        @Override
        public void mouseWheelMoved(@Nonnull final MouseWheelEvent e) {
          if (!e.isConsumed() && ((e.getModifiers() & config.getScaleModifiers()) == config.getScaleModifiers())) {
            scale = Math.max(0.2f, Math.min(scale + (SCALE_STEP * -e.getWheelRotation()), 10.0f));
            revalidate();
            repaint();
          }
        }
      });
    }

    private void updateConfig(){
      this.config.loadFrom(PreferencesManager.getInstance().getPreferences());
    }

    public MindMapPanelConfig getConfig(){
      return this.config;
    }
    
    @Override
    @Nonnull
    public Dimension getMinimumSize() {
      return getPreferredSize();
    }

    @Override
    @Nonnull
    public Dimension getMaximumSize() {
      return getPreferredSize();
    }

    @Override
    @Nonnull
    public Dimension getPreferredSize() {
      if (image == null) {
        return new Dimension(16, 16);
      } else {
        return new Dimension(Math.round(this.image.getWidth() * this.scale), Math.round(this.image.getHeight() * this.scale));
      }
    }
    
    @Override
    public void paintComponent(@Nonnull final Graphics g){
      final Graphics2D gfx = (Graphics2D) g;
      gfx.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
      gfx.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      gfx.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

      final Rectangle bounds = this.getBounds();
      if (this.image == null){
        gfx.setColor(Color.BLACK);
        gfx.fillRect(0,0, bounds.width, bounds.height);
        gfx.setColor(Color.RED);
        final String text = "Can't load image, check the log!";
        gfx.drawString(text, (bounds.width-gfx.getFontMetrics().stringWidth(text))/2, (bounds.height - gfx.getFontMetrics().getMaxAscent()) / 2);
      }else{
        final Dimension size = getPreferredSize();
        gfx.drawImage(this.image, Math.max(0,(bounds.width-size.width)/2), Math.max(0, (bounds.height - size.height) / 2), size.width, size.height, null);
      }
    }
    
    public void setImage(@Nullable final BufferedImage image){
      this.image = image;
      this.scale = 1.0f;
      revalidate();
      repaint();
    }
    
  }

  private final ScalableImage imageViewer;

  @Override
  public void focusToEditor() {
  }
  
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
    this.imageViewer = new ScalableImage();

    loadContent(file);
  }

  @Override
  public void loadContent(@Nullable final File file) throws IOException {
    BufferedImage loaded = null;
    if (file != null) {
      try {
        loaded = ImageIO.read(file);
      } catch (Exception ex) {
        LOGGER.error("Can't load image", ex);
        loaded = null;
      }
    }

    this.image = loaded;

    this.imageViewer.setImage(this.image);
    this.setViewportView(this.imageViewer);
    this.revalidate();
  }

  @Override
  public boolean saveDocument() throws IOException {
    boolean result = false;
    final File docFile = this.title.getAssociatedFile();
    if (docFile != null) {
      final String ext = FilenameUtils.getExtension(docFile.getName()).trim().toLowerCase(Locale.ENGLISH);
      if (SUPPORTED_FORMATS.contains(ext)) {
        try {
          ImageIO.write(this.image, ext, docFile);
          result = true;
        } catch (Exception ex) {
          if (ex instanceof IOException) {
            throw (IOException) ex;
          }
          throw new IOException("Can't write image", ex);
        }
      } else {
        try {
          LOGGER.warn("unsupported image format, will be saved as png : " + ext);
          ImageIO.write(this.image, "png", docFile);
          result = true;
        } catch (Exception ex) {
          if (ex instanceof IOException) {
            throw (IOException) ex;
          }
          throw new IOException("Can't write image", ex);
        }
      }
    }
    return result;
  }

  @Override
  public void updateConfiguration() {
    this.imageViewer.updateConfig();
    revalidate();
    repaint();
  }

  @Override
  public boolean isEditable() {
    return false;
  }

  @Override
  public boolean isSaveable() {
    return false;
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
