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
package com.igormaznitsa.sciareto.ui.tree;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.igormaznitsa.meta.annotation.MustNotContainNull;

public final class FileTransferable implements Transferable {

  private final List<File> files;
  private final DataFlavor[] flavors;

  public FileTransferable(@Nonnull @MustNotContainNull final Collection<File> files) {
    this.files = Collections.unmodifiableList(new ArrayList<>(files));
    this.flavors = new DataFlavor[]{DataFlavor.javaFileListFlavor};
  }

  @Nonnull
  @MustNotContainNull
  public List<File> getFiles() {
    return this.files;
  }

  @Override
  @Nullable
  public Object getTransferData(@Nullable final DataFlavor flavor) throws UnsupportedFlavorException, IOException {
    if (isDataFlavorSupported(flavor)) {
      return this.files;
    } else {
      return null;
    }
  }

  @Override
  @Nonnull
  @MustNotContainNull
  public DataFlavor[] getTransferDataFlavors() {
    return this.flavors;
  }

  @Override
  public boolean isDataFlavorSupported(@Nullable final DataFlavor flavor) {
    return DataFlavor.javaFileListFlavor.equals(flavor);
  }
}
