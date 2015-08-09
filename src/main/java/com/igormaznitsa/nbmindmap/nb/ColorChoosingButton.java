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

import com.igormaznitsa.nbmindmap.utils.NbUtils;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;

public class ColorChoosingButton extends JButton {
  private static final long serialVersionUID = -354752410805059103L;
  private Color value = null;
  
  private boolean lastResultOk;
  
  public ColorChoosingButton(){
    super();
  
    addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        final JColorChooser colorChooser = new JColorChooser(value);
        if (NbUtils.msgComponentOkCancel("Color for '"+getText()+'\'', colorChooser)){
          setValue(colorChooser.getColor());
          lastResultOk = true;
        }else{
          lastResultOk = false;
        }
      }
    });
    
    setValue(Color.BLACK);
  }
  
  public boolean isLastOkPressed(){
    return this.lastResultOk;
  }
  
  private static ImageIcon makeColorIconForColor(final Color color){
    final Image img = new BufferedImage(16,16,BufferedImage.TYPE_INT_RGB);
    final Graphics gfx = img.getGraphics();
    try{
      gfx.setColor(color);
      gfx.fillRect(0, 0, 16, 16);
      gfx.setColor(Color.BLACK);
      gfx.drawRect(0, 0, 16, 16);
    }finally{
      gfx.dispose();
    }
    return new ImageIcon(img);
  }
  
  public void setValue(final Color color){
    this.value = color;
    this.setIcon(makeColorIconForColor(this.value));
  }
  
  public Color getValue(){
    return this.value;
  }
}
