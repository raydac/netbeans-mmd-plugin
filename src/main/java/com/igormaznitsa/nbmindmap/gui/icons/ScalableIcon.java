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

import java.awt.Image;
import java.awt.image.BufferedImage;

public enum ScalableIcon {
  FILE("", 16, 16),
  SOURCE("", 16, 16),
  TEXT("", 16, 16),
  LINK("", 16, 16);
       
  private float scale = -1.0f;
  private final BufferedImage baseImage;
  private Image scaledCachedImage;
  private final int baseWidth;
  private final int baseHeight;

  private ScalableIcon(final String name, final int baseWidth, final int baseHeight) {
    this.baseWidth = baseWidth;
    this.baseHeight = baseHeight;
    this.baseImage = null;
    this.scaledCachedImage = null;
  }

  public synchronized float getScaleFactor(){
    return this.scale;
  }
  
  public synchronized Image getImage(){
    return this.scaledCachedImage;
  }
  
  public synchronized Image getImage(final float scale) {
    return null;
  }
}
