/*
 * Copyright 2016 Igor Maznitsa.
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
