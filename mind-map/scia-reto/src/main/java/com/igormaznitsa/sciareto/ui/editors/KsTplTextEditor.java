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

import com.igormaznitsa.sciareto.Context;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.filechooser.FileFilter;
import org.apache.commons.io.FilenameUtils;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;

public final class KsTplTextEditor extends AbstractPlUmlEditor {

  public static final Set<String> SUPPORTED_EXTENSIONS = Collections.singleton("kstpl");
  private static final String MIME = "text/kstpl";

  public static final FileFilter SRC_FILE_FILTER = new FileFilter() {

    @Override
    public boolean accept(@Nonnull final File f) {
      if (f.isDirectory()) {
        return true;
      }
      return SUPPORTED_EXTENSIONS.contains(FilenameUtils.getExtension(f.getName()).toLowerCase(Locale.ENGLISH));
    }

    @Override
    @Nonnull
    public String getDescription() {
      return "KStream topology files (*.kstpl)";
    }
  };

  public KsTplTextEditor(@Nonnull final Context context, @Nullable File file) throws IOException {
    super(context, file);
  }

  @Override
  protected void doPutMapping(@Nonnull final AbstractTokenMakerFactory f) {
    f.putMapping(MIME, "com.igormaznitsa.sciareto.ui.editors.KsTplTokenMaker");
  }

  @Override
  @Nonnull
  protected String getSyntaxEditingStyle() {
    return MIME;
  }

  @Nonnull
  @Override
  public String getDefaultExtension() {
    return "kstpl";
  }

  @Override
  @Nonnull
  public FileFilter getFileFilter() {
    return SRC_FILE_FILTER;
  }
}
