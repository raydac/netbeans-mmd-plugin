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

import static com.igormaznitsa.sciareto.ui.UiUtils.findTextBundle;

import com.igormaznitsa.sciareto.Context;
import com.igormaznitsa.sciareto.ui.UiUtils;
import java.awt.GridBagConstraints;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import org.apache.commons.io.FilenameUtils;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;

public final class PlantUmlTextEditor extends AbstractPlUmlEditor {

  public static final Set<String> SUPPORTED_EXTENSIONS =
      Set.of("pu", "puml", "plantuml");
  public static final String NEW_TEMPLATE = "@startuml\n"
      + "Alice->Alice: This is a signal to self.\\nIt also demonstrates\\nmultiline \\ntext\n"
      + "@enduml";
  private static final String MIME = "text/plantuml";

  private final FileFilter sourceFileFilter = makeFileFilter();

  public PlantUmlTextEditor(@Nonnull final Context context, @Nonnull File file) throws IOException {
    super(context, file);
  }

  public static FileFilter makeFileFilter() {
    return new FileFilter() {

      @Override
      public boolean accept(@Nonnull final File f) {
        if (f.isDirectory()) {
          return true;
        }
        return SUPPORTED_EXTENSIONS.contains(
            FilenameUtils.getExtension(f.getName()).toLowerCase(Locale.ENGLISH));
      }

      @Override
      @Nonnull
      public String getDescription() {
        return findTextBundle().getString("editorAbstractPlUml.fileFilter.puml.description");
      }
    };
  }

  @Override
  protected void addComponentsToLeftPart(@Nonnull final JPanel menuPanel,
                                         @Nonnull final GridBagConstraints constraints) {
    menuPanel.add(makeLinkLabel(bundle.getString("editorPlantUml.buttonPlantUmlManual.title"),
        () -> UiUtils.openLocalResourceInDesktop("help/PlantUML_Language_Reference_Guide_en.pdf"),
        bundle.getString("editorPlantUml.buttonPlantUmlManual.tooltip"), ICON_INFO), constraints);
    menuPanel.add(makeLinkLabel(bundle.getString("editorPlantUml.buttonAsciiMathManual.title"),
        "http://asciimath.org/", bundle.getString("editorPlantUml.buttonAsciiMathManual.tooltip"),
        ICON_INFO), constraints);
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

  @Nonnull
  @Override
  public String getDefaultExtension() {
    return "puml";
  }

  @Override
  @Nonnull
  public FileFilter getFileFilter() {
    return sourceFileFilter;
  }

}
