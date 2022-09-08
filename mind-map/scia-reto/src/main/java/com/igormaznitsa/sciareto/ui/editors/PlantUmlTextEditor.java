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
import com.igormaznitsa.sciareto.ui.UiUtils;
import java.awt.GridBagConstraints;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import org.apache.commons.io.FilenameUtils;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;

public final class PlantUmlTextEditor extends AbstractPlUmlEditor {

  public static final Set<String> SUPPORTED_EXTENSIONS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("pu", "puml", "plantuml")));
  private static final String MIME = "text/plantuml";
  public static final String NEW_TEMPLATE = "@startuml\n"
          + "Alice->Alice: This is a signal to self.\\nIt also demonstrates\\nmultiline \\ntext\n"
          + "@enduml";

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
      return "PlantUML files (*.puml, *.pu, *.plantuml)";
    }
  };

  @Override
  protected void addComponentsToLeftPart(@Nonnull final JPanel menuPanel, @Nonnull final GridBagConstraints constraints) {
    menuPanel.add(makeLinkLabel("PlantUML", () -> UiUtils.openLocalResourceInDesktop("help/PlantUML_Language_Reference_Guide_en.pdf"), "Open PlantUL manual", ICON_INFO), constraints);
    menuPanel.add(makeLinkLabel("AsciiMath", "http://asciimath.org/", "Open AsciiMath manual", ICON_INFO), constraints);
//    this.menu.add(makeLinkLabel("LatexMath", "https://en.wikibooks.org/wiki/LaTeX/Mathematics", "Open LatexMath manual", ICON_INFO), gbdata);
  }

  @Override
  protected void doPutMapping(@Nonnull final AbstractTokenMakerFactory f) {
    f.putMapping(MIME, "com.igormaznitsa.sciareto.ui.editors.PlantUmlTokenMaker");
  }

  @Override
  @Nonnull
  protected String getSyntaxEditingStyle() {
    return MIME;
  }

  public PlantUmlTextEditor(@Nonnull final Context context, @Nonnull File file) throws IOException {
    super(context, file);
  }

  @Nonnull
  @Override
  public String getDefaultExtension() {
    return "puml";
  }

  @Override
  @Nonnull
  public FileFilter getFileFilter() {
    return SRC_FILE_FILTER;
  }

}
