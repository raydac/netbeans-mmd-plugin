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
package com.igormaznitsa.nbmindmap.gui.icons;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import javax.imageio.ImageIO;
import org.apache.commons.io.IOUtils;

public enum ScalableIcon {
  FILE("folder_link.png"),
  SOURCE("brick_link.png"),
  TEXT("label_link.png"),
  LINK("world_link.png");

  public static final int BASE_WIDTH = 16;
  public static final int BASE_HEIGHT = 16;
  
  private float currentScaleFactor = -1.0f;
  
  private final Image baseImage;
  private Image scaledCachedImage;
  
  private final float baseScaleX;
  private final float baseScaleY;
  
  private ScalableIcon(final String name) {
    final InputStream in = ScalableIcon.class.getClassLoader().getResourceAsStream("com/igormaznitsa/nbmindmap/icons/"+name);
    try{
      this.baseImage = ImageIO.read(in);
      this.scaledCachedImage = null;
      this.baseScaleX = (float) BASE_WIDTH / (float)this.baseImage.getWidth(null);
      this.baseScaleY = (float) BASE_HEIGHT / (float)this.baseImage.getHeight(null);
    }
    catch (Exception ex) {
      throw new Error("Can't load resource image "+name);
    }finally{
      IOUtils.closeQuietly(in);
    }
  }

  public synchronized float getScaleFactor(){
    return this.currentScaleFactor;
  }
  
  public synchronized Image getImage(final float scale){
    if (Float.compare(this.currentScaleFactor, scale)!=0){
      this.scaledCachedImage = null;
    }
    
    if (this.scaledCachedImage == null){
      this.currentScaleFactor = scale;
      
      final int imgw = this.baseImage.getWidth(null);
      final int imgh = this.baseImage.getHeight(null);
      final int scaledW = Math.round((float)imgw*this.baseScaleX*scale);
      final int scaledH = Math.round((float)imgh*this.baseScaleY*scale);
    
      final BufferedImage img = new BufferedImage(scaledW, scaledH, BufferedImage.TYPE_INT_ARGB);
      final Graphics2D g = (Graphics2D)img.getGraphics();
      g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
      g.drawImage(this.baseImage,0,0,scaledW, scaledH, null);
      g.dispose();
      
      this.scaledCachedImage = img;
    }
    return this.scaledCachedImage;
  }
}
