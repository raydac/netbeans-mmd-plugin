/*
 * Copyright 2019 Igor Maznitsa.
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

package com.igormaznitsa.mindmap.swing.panel.utils;

import com.igormaznitsa.meta.annotation.MustNotContainNull;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.annotation.Nonnull;

/**
 * Image selection for transfer image into clipboard.
 */
public class ImageSelection implements Transferable {

  private final Image image;

  public ImageSelection(@Nonnull final BufferedImage image) {
    this.image = image;
  }

  @Override
  @Nonnull
  @MustNotContainNull
  public DataFlavor[] getTransferDataFlavors() {
    return new DataFlavor[] {DataFlavor.imageFlavor};
  }

  @Override
  public boolean isDataFlavorSupported(@Nonnull final DataFlavor flavor) {
    return DataFlavor.imageFlavor.equals(flavor);
  }

  @Override
  @Nonnull
  public Object getTransferData(@Nonnull final DataFlavor flavor) throws UnsupportedFlavorException, IOException {
    if (!DataFlavor.imageFlavor.equals(flavor)) {
      throw new UnsupportedFlavorException(flavor);
    }
    return this.image;
  }

}
