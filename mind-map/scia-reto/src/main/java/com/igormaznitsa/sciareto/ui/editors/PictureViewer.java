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

import com.igormaznitsa.mindmap.print.MMDPrintPanel;
import com.igormaznitsa.mindmap.print.PrintableObject;
import com.igormaznitsa.mindmap.swing.panel.utils.ImageSelection;
import com.igormaznitsa.sciareto.Context;
import com.igormaznitsa.sciareto.Main;
import com.igormaznitsa.sciareto.ui.DialogProviderManager;
import com.igormaznitsa.sciareto.ui.FindTextScopeProvider;
import com.igormaznitsa.sciareto.ui.ScaleStatusIndicator;
import com.igormaznitsa.sciareto.ui.UiUtils;
import com.igormaznitsa.sciareto.ui.tabs.TabTitle;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileFilter;
import org.apache.commons.io.FilenameUtils;

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
  private final TabTitle title;
  private final JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
  private final JScrollPane scrollPane = new EditorScrollPanel();

  private final ScalableImage imageViewer;
  private transient BufferedImage image;
  private final ScaleStatusIndicator scaleLabel;

  private final JLabel imageInfoLabel;

  public PictureViewer(@Nonnull final Context context, @Nonnull final File file) throws IOException {
    super();
    this.title = new TabTitle(context, this, file);
    this.imageViewer = new ScalableImage();
    this.scaleLabel = new ScaleStatusIndicator(this.imageViewer);

    this.scrollPane.getVerticalScrollBar().setBlockIncrement(ScalableImage.IMG_BLOCK_INCREMENT);
    this.scrollPane.getVerticalScrollBar().setUnitIncrement(ScalableImage.IMG_UNIT_INCREMENT);
    this.scrollPane.getHorizontalScrollBar().setBlockIncrement(ScalableImage.IMG_BLOCK_INCREMENT);
    this.scrollPane.getHorizontalScrollBar().setUnitIncrement(ScalableImage.IMG_UNIT_INCREMENT);

    this.scrollPane.setWheelScrollingEnabled(true);

    final JPanel toolbar = new JPanel(new GridBagLayout());

    final JButton buttonPrintImage = new JButton(loadMenuIcon("printer"));
    buttonPrintImage.setToolTipText("Print image");
    buttonPrintImage.setFocusPainted(false);
    buttonPrintImage.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(@Nonnull final ActionEvent e) {
        Main.getApplicationFrame().endFullScreenIfActive();
        final MMDPrintPanel printPanel = new MMDPrintPanel(DialogProviderManager.getInstance().getDialogProvider(), null, PrintableObject.newBuild().image(imageViewer.getImage()).build());
        UiUtils.makeOwningDialogResizable(printPanel);
        JOptionPane.showMessageDialog(mainPanel, printPanel, "Print image", JOptionPane.PLAIN_MESSAGE);
      }
    });

    final JButton buttonClipboardImage = new JButton(loadMenuIcon("clipboard_image"));
    buttonClipboardImage.setToolTipText("Copy image to clipboard");

    buttonClipboardImage.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        final BufferedImage image = imageViewer.getImage();
        if (image != null) {
          Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new ImageSelection(image), null);
        }
      }
    });

    final GridBagConstraints bc = new GridBagConstraints(GridBagConstraints.RELATIVE, 0, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0);

    toolbar.add(buttonClipboardImage, bc);
    toolbar.add(buttonPrintImage, bc);
    this.imageInfoLabel = new JLabel();
    toolbar.add(this.imageInfoLabel, bc);
    bc.weightx = 1000.0d;
    toolbar.add(Box.createHorizontalGlue(), bc);
    bc.weightx = 1.0d;
    toolbar.add(this.scaleLabel, bc);

    this.mainPanel.add(toolbar, BorderLayout.NORTH);
    this.mainPanel.add(this.scrollPane, BorderLayout.CENTER);

    loadContent(file);
  }

  @Nonnull
  @Override
  public String getDefaultExtension() {
    return "png";
  }

  @Override
  public void focusToEditor(final int line) {
  }

  @Override
  @Nonnull
  public FileFilter getFileFilter() {
    return IMAGE_FILE_FILTER;
  }

  @Override
  public boolean doesSupportTextSearch() {
    return false;
  }

  @Override
  public void loadContent(@Nullable final File file) throws IOException {
    BufferedImage loaded = null;
    if (file != null) {
      try {
        loaded = ImageIO.read(file);
      } catch (Exception ex) {
        logger.error("Can't load image", ex); //NOI18N
        loaded = null;
      }
    }

    this.image = loaded;

    this.imageInfoLabel.setText(String.format("<html><b>&nbsp;Width=%d px,&nbsp;Height=%d px</b></html>", this.image.getWidth(null), this.image.getHeight(null)));

    this.imageViewer.setImage(this.image, true);
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
          deleteBackup();
          result = true;
        } catch (Exception ex) {
          if (ex instanceof IOException) {
            throw (IOException) ex;
          }
          throw new IOException("Can't write image", ex); //NOI18N
        }
      } else {
        try {
          logger.warn("unsupported image format, will be saved as png : " + ext); //NOI18N
          ImageIO.write(this.image, "png", docFile); //NOI18N
          deleteBackup();
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

  @Nullable
  @Override
  protected String getContentAsText() {
    return null;
  }

  @Override
  @Nonnull
  public EditorContentType getEditorContentType() {
    return EditorContentType.IMAGE;
  }

  @Override
  @Nonnull
  public JComponent getMainComponent() {
    return this.mainPanel;
  }

  @Override
  @Nonnull
  public JComponent getContainerToShow() {
    return this.mainPanel;
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
