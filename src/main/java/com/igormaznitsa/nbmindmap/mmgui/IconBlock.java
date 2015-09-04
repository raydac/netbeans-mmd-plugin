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
package com.igormaznitsa.nbmindmap.mmgui;

import com.igormaznitsa.nbmindmap.utils.ScalableIcon;
import com.igormaznitsa.nbmindmap.model.Extra;
import static com.igormaznitsa.nbmindmap.model.Extra.ExtraType.FILE;
import static com.igormaznitsa.nbmindmap.model.Extra.ExtraType.LINK;
import static com.igormaznitsa.nbmindmap.model.Extra.ExtraType.NOTE;
import static com.igormaznitsa.nbmindmap.model.Extra.ExtraType.TOPIC;
import com.igormaznitsa.nbmindmap.model.Topic;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

public class IconBlock {
  private final Rectangle2D bounds = new Rectangle2D.Double();
  private final Topic model;
  private double scale = 1.0d;
  private boolean contentPresented;
  
  private Extra<?> [] currentExtras = null;
  
  public IconBlock(final Topic model){
    this.model = model;
  }
  
  public void setCoordOffset(final double x, final double y) {
    this.bounds.setRect(x, y, this.bounds.getWidth(), this.bounds.getHeight());
  }

  public void updateSize(final Graphics2D gfx, final Configuration cfg) {
    final int numberOfIcons = this.model.getNumberOfExtras();
    this.scale = cfg.getScale();
    if (numberOfIcons == 0) {
      this.bounds.setRect(0d, 0d, 0d, 0d);
      this.contentPresented = false;
    }else{
      final double scaledIconWidth = ScalableIcon.BASE_WIDTH * this.scale;
      final double scaledIconHeight = ScalableIcon.BASE_HEIGHT * this.scale;
      this.bounds.setRect(0d, 0d, scaledIconWidth * numberOfIcons, scaledIconHeight);
      this.contentPresented = true;
      this.currentExtras = new Extra<?>[numberOfIcons];
      int index = 0;
      for(final Extra<?> e : this.model.getExtras().values()){
        this.currentExtras[index++] = e;
      }
    }
  }

  public boolean hasContent(){
    return this.currentExtras!=null && this.contentPresented;
  }
  
  public void paint(final Graphics2D gfx) {
    final int numberOfIcons = this.model.getNumberOfExtras();
    if (numberOfIcons!=0){
      double offsetX = this.bounds.getX();
      final int offsetY = (int)Math.round(this.bounds.getY());
      final double scaledIconWidth = ScalableIcon.BASE_WIDTH * this.scale;
      for(final Extra<?> e : this.currentExtras){
        final ScalableIcon ico;
        switch (e.getType()) {
          case FILE:
            ico = ScalableIcon.FILE;
            break;
          case LINK:
            ico = ScalableIcon.LINK;
            break;
          case NOTE:
            ico = ScalableIcon.TEXT;
            break;
          case LINE:
            ico = ScalableIcon.LINE;
            break;
          case TOPIC:
            ico = ScalableIcon.TOPIC;
            break;
          default:
            throw new Error("Unexpected extras"); //NOI18N
        }
        gfx.drawImage(ico.getImage(this.scale), (int) Math.round(offsetX), offsetY, null);
        offsetX += scaledIconWidth;
      }
    }
  }

  public Extra<?> findExtraForPoint(final double x, final double y){
    Extra<?> result = null;
    if (this.hasContent()  && this.bounds.contains(x,y)){
      final double iconWidth = this.scale * ScalableIcon.BASE_WIDTH;
      final int index = (int)((x-this.bounds.getX()) / iconWidth);
      result = index >=0 && index < this.currentExtras.length ? this.currentExtras[index] : null;
    }
    return result;
  }
  
  public Rectangle2D getBounds() {
    return this.bounds;
  }

}
