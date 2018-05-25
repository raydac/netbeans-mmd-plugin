/*
 * Copyright (C) 2018 Igor Maznitsa.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package com.igormaznitsa.sciareto.ui.misc;

import com.igormaznitsa.sciareto.ui.UiUtils;
import java.awt.Image;
import java.awt.image.ImageObserver;
import static java.awt.image.ImageObserver.ABORT;
import static java.awt.image.ImageObserver.ALLBITS;
import static java.awt.image.ImageObserver.FRAMEBITS;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.annotation.Nonnull;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

public final class BigLoaderIconAnimationConroller {
  
  public static final ImageIcon LOADING = new ImageIcon(UiUtils.class.getClassLoader().getResource("icons/bigloader.gif")); //NOI18N

  private static final class LoadingIconRedrawer {

    final JLabel label;

    LoadingIconRedrawer(@Nonnull final JLabel label) {
      this.label = label;
    }

    public void redraw() {
      this.label.repaint();
    }
  }

  private final List<LoadingIconRedrawer> registeredRedrawers = new CopyOnWriteArrayList<>();
 
  private static final BigLoaderIconAnimationConroller INSTANCE = new BigLoaderIconAnimationConroller();
 
  private BigLoaderIconAnimationConroller(){
    LOADING.setImageObserver(new ImageObserver() {
      @Override
      public boolean imageUpdate(Image img, int flags, int x, int y, int width, int height) {
        if ((flags & (FRAMEBITS | ALLBITS)) != 0) {
          for (final LoadingIconRedrawer redrawer : registeredRedrawers) {
            redrawer.redraw();
          }
        }
        return (flags & (ALLBITS | ABORT)) == 0;
      }
    });

  }
  
  @Nonnull
  public static BigLoaderIconAnimationConroller getInstance() {
    return INSTANCE;
  }
  
  public void registerLabel(@Nonnull final JLabel label) {
    this.registeredRedrawers.add(new LoadingIconRedrawer(label));
  }

  public void unregisterLabel(@Nonnull final JLabel label) {
    for (final LoadingIconRedrawer r : registeredRedrawers) {
      if (r.label == label) {  
        this.registeredRedrawers.remove(r);
      }
    }
  }

 
}
