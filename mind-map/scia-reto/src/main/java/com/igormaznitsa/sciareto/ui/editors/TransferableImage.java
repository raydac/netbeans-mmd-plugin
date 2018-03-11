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
    this.image = new WeakReference<Image>(Assertions.assertNotNull(image));
  }

  @Nonnull
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
  public DataFlavor[] getTransferDataFlavors() {
    return new DataFlavor[] {DataFlavor.imageFlavor};
  }

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
