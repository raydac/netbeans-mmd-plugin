/*
 * Copyright (C) 2020 Igor Maznitsa.
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.swing.filechooser.FileFilter;
import org.apache.commons.io.FilenameUtils;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;

public class DotScriptEditor extends AbstractDotEditor {
  
  public static final Set<String> SUPPORTED_EXTENSIONS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("gv","dot")));
  public static final String MIME = "text/vnd.graphviz";
  public static final String NEW_TEMPLATE = "digraph graphname {\na -> b -> c;\nb -> d;\n}";

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
      return "DOT script files (*.gv,*.dot)";
    }
  };

  public DotScriptEditor(@Nonnull final Context context, @Nonnull final File file) throws IOException {
    super(context, file);
  }

  @Override
  protected boolean isPageAllowed() {
    return false;
  }

  @Override
  protected int countNewPages(@Nonnull final String text) {
    return 1;
  }

  @Override
  @Nonnull
  protected String getSyntaxEditingStyle() {
    return MIME;
  }

  @Override
  protected void doPutMapping(@Nonnull final AbstractTokenMakerFactory f) {
  }

  @Override
  @Nonnull
  public String getDefaultExtension() {
    return "gv";
  }
  
  @Override
  @Nonnull
  public FileFilter getFileFilter() {
    return SRC_FILE_FILTER;
  }
  
}
