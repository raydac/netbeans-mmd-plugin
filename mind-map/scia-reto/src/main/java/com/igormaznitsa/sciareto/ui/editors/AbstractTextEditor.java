/*
 * Copyright (C) 2015-2022 Igor A. Maznitsa
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package com.igormaznitsa.sciareto.ui.editors;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nonnull;

public abstract class AbstractTextEditor extends AbstractEditor {

  protected final AtomicReference<TextFile> currentTextFile = new AtomicReference<>();

  public AbstractTextEditor() {
    super();
  }

  @Override
  public final void loadContent(@Nonnull final File file) throws IOException {
    final TextFile fileToLoad;
    final TextFile foundBackupContent = this.restoreFromBackup(file);
    if (foundBackupContent == null) {
      fileToLoad = new TextFile(file);
    } else {
      fileToLoad = new TextFile(file, true, requireNonNull(foundBackupContent.getContent()));
    }
    this.currentTextFile.set(fileToLoad);
    this.onLoadContent(fileToLoad);
  }

  protected abstract void onLoadContent(@Nonnull final TextFile textFile) throws IOException;
}
