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
package com.igormaznitsa.sciareto.ui.editors;

import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.meta.common.utils.Assertions;

import javax.annotation.Nonnull;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.lang.ref.WeakReference;

public class TransferableImage implements Transferable {

  private final WeakReference<Image> image;

  public TransferableImage(@Nonnull final Image image) {
    this.image = new WeakReference<>(Assertions.assertNotNull(image));
  }

  @Nonnull
  @Override
  public Object getTransferData(@Nonnull final DataFlavor flavor) throws UnsupportedFlavorException, IOException {
    final Image theImage = this.image.get();
    if (flavor.equals(DataFlavor.imageFlavor) && theImage != null) {
      return theImage;
    } else {
      throw new UnsupportedFlavorException(flavor);
    }
  }

  @Nonnull
  @MustNotContainNull
  @Override
  public DataFlavor[] getTransferDataFlavors() {
    return new DataFlavor[] {DataFlavor.imageFlavor};
  }

  @Override
  public boolean isDataFlavorSupported(@Nonnull DataFlavor flavor) {
    final Image theimage = this.image.get();
    if (theimage == null) {
      return false;
    }
    boolean result = false;
    for (final DataFlavor f : getTransferDataFlavors()) {
      if (flavor.equals(f)) {
        result = true;
        break;
      }
    }

    return result;
  }
}
