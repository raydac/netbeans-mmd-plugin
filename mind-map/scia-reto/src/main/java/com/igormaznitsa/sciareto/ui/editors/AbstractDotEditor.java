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

import com.igormaznitsa.meta.common.utils.IOUtils;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.sciareto.Context;
import com.igormaznitsa.sciareto.ui.SrI18n;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import net.sourceforge.plantuml.dot.ExeState;
import net.sourceforge.plantuml.dot.Graphviz;
import net.sourceforge.plantuml.dot.GraphvizUtils;
import net.sourceforge.plantuml.dot.ProcessState;

public abstract class AbstractDotEditor extends AbstractPlUmlEditor {

  private static final Set<ExportType> DEFAULT_SUPPORTED_EXPORT_TYPES = Collections.unmodifiableSet(
      EnumSet.of(
          ExportType.SVG,
          ExportType.PNG
      ));

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDotEditor.class);

  public AbstractDotEditor(@Nonnull final Context context, @Nonnull final File file) throws IOException {
    super(context, file);
  }

  @Override
  @Nonnull
  protected Set<ExportType> getAllowedExportTypes() {
    return DEFAULT_SUPPORTED_EXPORT_TYPES;
  }

  @Override
  protected abstract int countNewPages(@Nonnull final String text);

  @Override
  protected final boolean isCustomRendering() {
    return true;
  }

  @Override
  public boolean isExportImageAsFileAllowed() {
    return true;
  }

  @Override
  public boolean isSyntaxCorrect(@Nonnull final String text) {
    return true;
  }

  @Override
  @Nullable
  protected byte[] makeCustomExport(
      @Nonnull final ExportType exportType,
      final int pageIndex,
      @Nonnull final String text
  ) throws Exception {
    final String format;
    switch (exportType) {
      case SVG:
        format = "svg";
        break;
      case PNG:
        format = "png";
        break;
      default:
        throw new IllegalArgumentException("Unsupported export type: " + exportType);
    }
    return this.executeDot(text, format);
  }

  @Nonnull
  protected byte[] executeDot(@Nonnull final String script, @Nonnull final String type) {
    final Graphviz wizard = GraphvizUtils.create(null, script, type);

    final ExeState state = wizard.getExeState();
    if (state == ExeState.OK) {
      final ByteArrayOutputStream bos = new ByteArrayOutputStream();
      final ProcessState processState = wizard.createFile3(bos);
      IOUtils.closeQuietly(bos);
      final byte[] formedContent = bos.toByteArray();

      if (processState.differs(ProcessState.TERMINATED_OK())) {
        throw new IllegalStateException(String.format(SrI18n.getInstance().findBundle().getString("editorDot.cantRenderImage"),processState));
      } else if (formedContent.length == 0) {
        throw new IllegalArgumentException(SrI18n.getInstance().findBundle().getString("editorDot.cantRenderSyntaxError"));
      } else {
        return formedContent;
      }
    } else {
      throw new IllegalStateException(String.format(SrI18n.getInstance().findBundle().getString("editorDot.cantRenderStatusNotOk"),state.getTextMessage()));
    }
  }

  @Override
  protected final void doCustomRendering(
      @Nonnull final String text,
      final int pageIndex,
      @Nonnull final AtomicReference<BufferedImage> renderedImage,
      @Nonnull final AtomicReference<Exception> error
  ) {
    if (text.trim().isEmpty()) {
      error.set(new IllegalArgumentException(SrI18n.getInstance().findBundle().getString("editorDot.emptyText")));
      return;
    }

    try {
      final byte[] image = this.executeDot(text, "png");
      renderedImage.set(ImageIO.read(new ByteArrayInputStream(image)));
    } catch (Exception ex) {
      LOGGER.error("Can't render DOT script as PNG", ex);
      error.set(ex);
    }
  }

}
