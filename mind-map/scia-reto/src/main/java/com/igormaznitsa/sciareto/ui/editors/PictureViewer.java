/*
 * Copyright 2015-2018 Igor Maznitsa.
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

package com.igormaznitsa.sciareto.ui.editors;

import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.sciareto.Context;
import com.igormaznitsa.sciareto.ui.FindTextScopeProvider;
import com.igormaznitsa.sciareto.ui.tabs.TabTitle;
import org.apache.commons.io.FilenameUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public final class PictureViewer extends AbstractEditor {

  public static final Set<String> SUPPORTED_FORMATS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("png", "jpg", "gif"))); //NOI18N
  public static final FileFilter IMAGE_FILE_FILTER = new FileFilter() {
    @Override
    public boolean accept(@Nonnull final File f) {
      if (f.isDirectory()) {
        return true;
      }
      final String ext = FilenameUtils.getExtension(f.getName()).toLowerCase(Locale.ENGLISH);
      return SUPPORTED_FORMATS.contains(ext);
    }

    @Override
    @Nonnull
    public String getDescription() {
      return "Image file (*.png,*.jpg,*.gif)";
    }
  };
  private static final Logger LOGGER = LoggerFactory.getLogger(PictureViewer.class);
  private final TabTitle title;
  private final JScrollPane scrollPane = new EditorScrollPanel();

  private final ScalableImage imageViewer;
  private transient BufferedImage image;

  public PictureViewer(@Nonnull final Context context, @Nonnull final File file) throws IOException {
    super();
    this.title = new TabTitle(context, this, file);
    this.imageViewer = new ScalableImage();

    loadContent(file);
  }

  @Override
  public void focusToEditor() {
  }

  @Override
  @Nonnull
  public FileFilter getFileFilter() {
    return IMAGE_FILE_FILTER;
  }

  @Override
  public void loadContent(@Nullable final File file) throws IOException {
    BufferedImage loaded = null;
    if (file != null) {
      try {
        loaded = ImageIO.read(file);
      } catch (Exception ex) {
        LOGGER.error("Can't load image", ex); //NOI18N
        loaded = null;
      }
    }

    this.image = loaded;

    this.imageViewer.setImage(this.image);
    this.scrollPane.setViewportView(this.imageViewer);
    this.scrollPane.revalidate();
  }

  @Override
  public boolean saveDocument() throws IOException {
    boolean result = false;
    final File docFile = this.title.getAssociatedFile();
    if (docFile != null) {
      final String ext = FilenameUtils.getExtension(docFile.getName()).trim().toLowerCase(Locale.ENGLISH);
      if (SUPPORTED_FORMATS.contains(ext)) {
        try {
          ImageIO.write(this.image, ext, docFile);
          result = true;
        } catch (Exception ex) {
          if (ex instanceof IOException) {
            throw (IOException) ex;
          }
          throw new IOException("Can't write image", ex); //NOI18N
        }
      } else {
        try {
          LOGGER.warn("unsupported image format, will be saved as png : " + ext); //NOI18N
          ImageIO.write(this.image, "png", docFile); //NOI18N
          result = true;
        } catch (Exception ex) {
          if (ex instanceof IOException) {
            throw (IOException) ex;
          }
          throw new IOException("Can't write image", ex); //NOI18N
        }
      }
    }
    return result;
  }

  @Override
  public void updateConfiguration() {
    this.imageViewer.updateConfig();
    this.scrollPane.revalidate();
    this.scrollPane.repaint();
  }

  @Override
  public boolean isEditable() {
    return false;
  }

  @Override
  public boolean isSaveable() {
    return false;
  }

  @Override
  @Nonnull
  public TabTitle getTabTitle() {
    return this.title;
  }

  @Override
  @Nonnull
  public EditorContentType getEditorContentType() {
    return EditorContentType.IMAGE;
  }

  @Override
  @Nonnull
  public JComponent getMainComponent() {
    return this.imageViewer;
  }

  @Override
  @Nonnull
  public JComponent getContainerToShow() {
    return this.scrollPane;
  }

  @Override
  @Nonnull
  public AbstractEditor getEditor() {
    return this;
  }

  @Override
  public boolean isRedo() {
    return false;
  }

  @Override
  public boolean isUndo() {
    return false;
  }

  @Override
  public boolean redo() {
    return false;
  }

  @Override
  public boolean undo() {
    return false;
  }

  @Override
  public boolean findNext(@Nonnull final Pattern pattern, @Nonnull final FindTextScopeProvider provider) {
    return false;
  }

  @Override
  public boolean findPrev(@Nonnull final Pattern pattern, @Nonnull final FindTextScopeProvider provider) {
    return false;
  }

  @Override
  public boolean doesSupportPatternSearch() {
    return false;
  }

  @Override
  public boolean doesSupportCutCopyPaste() {
    return false;
  }

  @Override
  public boolean isCutAllowed() {
    return false;
  }

  @Override
  public boolean doCut() {
    return false;
  }

  @Override
  public boolean isCopyAllowed() {
    return false;
  }

  @Override
  public boolean isPasteAllowed() {
    return false;
  }

  @Override
  public boolean doCopy() {
    return false;
  }

  @Override
  public boolean doPaste() {
    return false;
  }
}
