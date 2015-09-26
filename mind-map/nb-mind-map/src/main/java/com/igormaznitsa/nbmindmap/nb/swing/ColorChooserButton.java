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
package com.igormaznitsa.nbmindmap.nb.swing;

import com.igormaznitsa.nbmindmap.utils.NbUtils;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import javax.swing.DefaultButtonModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ColorChooserButton extends JButton {

  private static final long serialVersionUID = -354752410805059103L;
  
  private static final Logger logger = LoggerFactory.getLogger(ColorChooserButton.class);
  
  private Color value = null;

  private volatile boolean lastResultOk;

  public static final Color DIFF_COLORS = new Color(0,true);
  
  public ColorChooserButton() {
    super();

    this.setModel(new DefaultButtonModel() {
      private static final long serialVersionUID = 3109256773218160485L;

      @Override
      protected void fireActionPerformed(ActionEvent e) {
        final PropertyEditor editor = PropertyEditorManager.findEditor(Color.class);
        if (editor == null) {
          logger.error("Can't find registered color editor");
          NbUtils.msgError("Can't find color editor! unexpected state! Contact developer!");
          return;
        }

        editor.setValue(value);

        final DialogDescriptor descriptor = new DialogDescriptor(
                editor.getCustomEditor(),
                String.format(java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18n/Bundle").getString("ColorChoosingButton.dialogTitle"), getText())
        );

        DialogDisplayer.getDefault().createDialog(descriptor).setVisible(true);
        if (descriptor.getValue() == DialogDescriptor.OK_OPTION) {
          setValue((Color) editor.getValue());
          lastResultOk = true;
        }
        else {
          lastResultOk = false;
        }

        super.fireActionPerformed(e);
      }
    });

    setValue(Color.BLACK);
  }

  public boolean isLastOkPressed() {
    return this.lastResultOk;
  }

  private static ImageIcon makeColorIconForColor(final Color color) {
    final Image img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
    final Graphics gfx = img.getGraphics();
    try {
      if (color == null){
        gfx.setColor(NbUtils.DARK_THEME ? Color.darkGray : Color.white);
        gfx.fillRect(0, 0, 16, 16);
        gfx.setColor(NbUtils.DARK_THEME ? Color.yellow : Color.black);
        gfx.drawRect(0, 0, 15, 15);
        gfx.drawLine(0, 0, 15, 15);
      }else if (color == DIFF_COLORS) {
        gfx.setColor(Color.red);
        gfx.fillRect(0, 0, 8, 8);
        gfx.setColor(Color.green);
        gfx.fillRect(8, 0, 8, 8);
        gfx.setColor(Color.blue);
        gfx.fillRect(0, 8, 8, 8);
        gfx.setColor(Color.yellow);
        gfx.fillRect(8, 8, 8, 8);
      }else{
        gfx.setColor(color);
        gfx.fillRect(0, 0, 16, 16);
        gfx.setColor(Color.black);
        gfx.drawRect(0, 0, 15, 15);
      }
    }
    finally {
      gfx.dispose();
    }
    return new ImageIcon(img);
  }

  public void setValue(final Color color) {
    this.value = color;
    this.setIcon(makeColorIconForColor(this.value));
  }

  public Color getValue() {
    return this.value;
  }
}
