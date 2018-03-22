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
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import com.igormaznitsa.sciareto.Context;
import com.igormaznitsa.sciareto.Main;
import com.igormaznitsa.sciareto.preferences.PrefUtils;
import com.igormaznitsa.sciareto.preferences.PreferencesManager;
import com.igormaznitsa.sciareto.preferences.SpecificKeys;
import com.igormaznitsa.sciareto.ui.DialogProviderManager;
import com.igormaznitsa.sciareto.ui.FindTextScopeProvider;
import com.igormaznitsa.sciareto.ui.SystemUtils;
import com.igormaznitsa.sciareto.ui.tabs.TabTitle;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.OptionFlags;
import net.sourceforge.plantuml.SourceStringReader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.RUndoManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PlantUmlTextEditor extends AbstractEditor {

  public static final Font DEFAULT_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 14);
  public static final Set<String> SUPPORTED_EXTENSIONS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("pu", "puml", "plantuml")));
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
      return "Source files";
    }
  };
  private static final Logger LOGGER = LoggerFactory.getLogger(PlantUmlTextEditor.class);
  private static final Map<String, ImageIcon> iconCache = new HashMap<String, ImageIcon>();
  private static File lastExportedFile = null;
  private final RSyntaxTextArea editor;
  private final TabTitle title;
  private final RUndoManager undoManager;
  private final ScalableImage imageComponent;
  private final JSplitPane mainPanel;
  private final JPanel renderedPanel;
  private final JScrollPane renderedScrollPane;
  private boolean ignoreChange;

  public PlantUmlTextEditor(@Nonnull final Context context, @Nullable File file) throws IOException {
    super();
    initPlantUml();

    this.editor = new RSyntaxTextArea();
    this.editor.setPopupMenu(null);

    final String syntaxType = null;

    this.editor.setSyntaxEditingStyle(syntaxType == null ? SyntaxConstants.SYNTAX_STYLE_NONE : syntaxType);
    this.editor.setAntiAliasingEnabled(true);
    this.editor.setBracketMatchingEnabled(true);
    this.editor.setCodeFoldingEnabled(true);

    this.editor.getCaret().setSelectionVisible(true);
    this.editor.setFont(PreferencesManager.getInstance().getFont(PreferencesManager.getInstance().getPreferences(), SpecificKeys.PROPERTY_TEXT_EDITOR_FONT, DEFAULT_FONT));

    this.mainPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

    final RTextScrollPane scrollPane = new RTextScrollPane(this.editor, true);

    this.renderedPanel = new JPanel(new BorderLayout());
    this.imageComponent = new ScalableImage();
    this.renderedScrollPane = new EditorScrollPanel(this.imageComponent);

    final JToolBar menu = new JToolBar();

    final JButton buttonRrefresh = new JButton(loadMenuIcon("arrow_refresh"));
    buttonRrefresh.setToolTipText("Refresh image for text");
    buttonRrefresh.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        renderCurrentTextInPlantUml();
      }
    });

    final JButton buttonExportImage = new JButton(loadMenuIcon("picture_save"));
    buttonExportImage.setToolTipText("Export image as file");
    buttonExportImage.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        Main.getApplicationFrame().endFullScreenIfActive();
        exportAsFile();
      }
    });

    final JButton buttonClipboardImage = new JButton(loadMenuIcon("clipboard_sign"));
    buttonClipboardImage.setToolTipText("Copy image to clipboard");
    buttonClipboardImage.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        final BufferedImage image = imageComponent.getImage();
        if (image != null) {
          Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new TransferableImage(image), new ClipboardOwner() {
            @Override
            public void lostOwnership(@Nonnull final Clipboard clipboard, @Nonnull final Transferable contents) {
            }
          });
        }
      }
    });

    menu.add(buttonRrefresh);
    menu.add(buttonClipboardImage);
    menu.add(buttonExportImage);

    this.renderedPanel.add(menu, BorderLayout.NORTH);
    this.renderedPanel.add(this.renderedScrollPane, BorderLayout.CENTER);

    this.mainPanel.add(scrollPane);
    this.mainPanel.add(this.renderedPanel);

    this.mainPanel.setDividerLocation(0);
    this.mainPanel.setResizeWeight(0.0d);
    this.mainPanel.setOneTouchExpandable(true);

    this.title = new TabTitle(context, this, file);

    this.editor.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(@Nonnull final DocumentEvent e) {
        if (!ignoreChange) {
          title.setChanged(true);
        }
        context.notifyUpdateRedoUndo();
      }

      @Override
      public void removeUpdate(@Nonnull final DocumentEvent e) {
        if (!ignoreChange) {
          title.setChanged(true);
        }
        context.notifyUpdateRedoUndo();
      }

      @Override
      public void changedUpdate(@Nonnull final DocumentEvent e) {
        if (!ignoreChange) {
          title.setChanged(true);
        }
        context.notifyUpdateRedoUndo();
      }
    });

    this.undoManager = new RUndoManager(this.editor);

    loadContent(file);

    this.editor.discardAllEdits();
    this.undoManager.discardAllEdits();

    this.undoManager.updateActions();

    this.editor.getDocument().addUndoableEditListener(this.undoManager);
  }

  private void initPlantUml() {
    OptionFlags.getInstance().setDotExecutable(PrefUtils.getPlantUmlDotPath());
  }

  @Nonnull
  private static synchronized ImageIcon loadMenuIcon(@Nonnull final String name) {
    if (iconCache.containsKey(name)) {
      return iconCache.get(name);
    } else {
      final ImageIcon loaded = new javax.swing.ImageIcon(ClassLoader.getSystemResource("menu_icons/" + name + ".png"));
      iconCache.put(name, loaded);
      return loaded;
    }
  }

  @Override
  @Nonnull
  public FileFilter getFileFilter() {
    return SRC_FILE_FILTER;
  }

  private void exportAsFile() {
    final JFileChooser fileChooser = new JFileChooser(lastExportedFile);
    fileChooser.setAcceptAllFileFilterUsed(false);

    final FileFilter fileFiterSVG = new FileFilter() {
      @Override
      public boolean accept(@Nonnull final File f) {
        return f.isDirectory() || f.getName().toLowerCase(Locale.ENGLISH).endsWith(".svg");
      }

      @Nonnull
      @Override
      public String getDescription() {
        return "SVG images (*.svg)";
      }
    };

    final FileFilter fileFiterPNG = new FileFilter() {
      @Override
      public boolean accept(@Nonnull final File f) {
        return f.isDirectory() || f.getName().toLowerCase(Locale.ENGLISH).endsWith(".png");
      }

      @Nonnull
      @Override
      public String getDescription() {
        return "PNG images (*.png)";
      }
    };

    final FileFilter fileFiterLTX = new FileFilter() {
      @Override
      public boolean accept(@Nonnull final File f) {
        return f.isDirectory() || f.getName().toLowerCase(Locale.ENGLISH).endsWith(".tex");
      }

      @Nonnull
      @Override
      public String getDescription() {
        return "LaTeX text files (*.tex)";
      }
    };

    fileChooser.setApproveButtonText("Export");
    fileChooser.setDialogTitle("Export PlantUML image as File");
    fileChooser.setMultiSelectionEnabled(false);

    fileChooser.addChoosableFileFilter(fileFiterPNG);
    fileChooser.addChoosableFileFilter(fileFiterSVG);
    fileChooser.addChoosableFileFilter(fileFiterLTX);

    if (fileChooser.showSaveDialog(this.mainPanel) == JFileChooser.APPROVE_OPTION) {
      lastExportedFile = fileChooser.getSelectedFile();

      final FileFilter fileFilter = fileChooser.getFileFilter();

      final SourceStringReader reader = new SourceStringReader(this.editor.getText());

      final FileFormatOption option;

      final String ext;

      if (fileFilter == fileFiterSVG) {
        option = new FileFormatOption(FileFormat.SVG);
        ext = ".svg";
      } else if (fileFilter == fileFiterPNG) {
        option = new FileFormatOption(FileFormat.PNG);
        ext = ".png";
      } else if (fileFilter == fileFiterLTX) {
        option = new FileFormatOption(FileFormat.LATEX);
        ext = ".tex";
      } else {
        throw new Error("Unexpected situation");
      }

      final ByteArrayOutputStream buffer = new ByteArrayOutputStream(131072);
      if (!lastExportedFile.getName().contains(".") || !lastExportedFile.getName().endsWith(ext)) {
        lastExportedFile = new File(lastExportedFile.getParent(), lastExportedFile.getName() + ext);
      }

      try {
        reader.outputImage(buffer, option);
        FileUtils.writeByteArrayToFile(lastExportedFile, buffer.toByteArray());
        LOGGER.info("Exported plant uml image as file : " + lastExportedFile);
      } catch (Exception ex) {
        LOGGER.error("Can't export plant uml image", ex);
        JOptionPane.showMessageDialog(this.mainPanel, "Error during export, see log!", "Error", JOptionPane.ERROR_MESSAGE);
      }
    }

  }

  @Override
  public void focusToEditor() {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        editor.requestFocusInWindow();
      }
    });
  }

  @Override
  public boolean isRedo() {
    return this.undoManager.canRedo();
  }

  @Override
  public boolean isUndo() {
    return this.undoManager.canUndo();
  }

  @Override
  public boolean redo() {
    if (this.undoManager.canRedo()) {
      this.undoManager.redo();
    }
    return this.undoManager.canRedo();
  }

  @Override
  public boolean undo() {
    if (this.undoManager.canUndo()) {
      this.undoManager.undo();
    }
    return this.undoManager.canUndo();
  }

  @Override
  @Nonnull
  public JComponent getMainComponent() {
    return this.editor;
  }

  @Override
  public boolean isEditable() {
    return true;
  }

  @Override
  public boolean isSaveable() {
    return true;
  }

  @Override
  public void updateConfiguration() {
    initPlantUml();
    this.imageComponent.updateConfig();
    this.editor.setFont(PreferencesManager.getInstance().getFont(PreferencesManager.getInstance().getPreferences(), SpecificKeys.PROPERTY_TEXT_EDITOR_FONT, DEFAULT_FONT));
    this.editor.revalidate();
    this.editor.repaint();
  }

  @Override
  public void loadContent(@Nullable final File file) throws IOException {
    this.ignoreChange = true;
    try {
      if (file != null) {
        this.editor.setText(FileUtils.readFileToString(file, "UTF-8")); //NOI18N
        this.editor.setCaretPosition(0);
        renderCurrentTextInPlantUml();
      }
    } finally {
      this.ignoreChange = false;
    }

    this.undoManager.discardAllEdits();
    this.title.setChanged(false);

    this.mainPanel.revalidate();
    this.mainPanel.repaint();
  }

  @Override
  public boolean saveDocument() throws IOException {
    boolean result = false;
    if (this.title.isChanged()) {
      File file = this.title.getAssociatedFile();
      if (file == null) {
        file = DialogProviderManager.getInstance().getDialogProvider().msgSaveFileDialog(null, "sources-editor", "Save sources", null, true, getFileFilter(), "Save");
        if (file == null) {
          return result;
        }
      }
      SystemUtils.saveUTFText(file, this.editor.getText());
      this.title.setChanged(false);
      result = true;
      renderCurrentTextInPlantUml();
    } else {
      result = true;
    }
    return result;
  }

  @Override
  @Nonnull
  public TabTitle getTabTitle() {
    return this.title;
  }

  @Override
  @Nonnull
  public EditorContentType getEditorContentType() {
    return EditorContentType.SOURCES;
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

  private void renderCurrentTextInPlantUml() {
    final String text = this.editor.getText();

    final SourceStringReader reader = new SourceStringReader(text, "UTF-8");
    final ByteArrayOutputStream buffer = new ByteArrayOutputStream(131072);
    try {
      reader.outputImage(buffer, new FileFormatOption(FileFormat.PNG, false));
      this.imageComponent.setImage(ImageIO.read(new ByteArrayInputStream(buffer.toByteArray())));
      this.renderedScrollPane.revalidate();
    } catch (IOException e) {
      LOGGER.error("Can't render plant uml", e);
      this.renderedScrollPane.setViewportView(new JLabel("Error during rendering"));
    }
  }

  private boolean searchSubstring(@Nonnull final Pattern pattern, final boolean next) {
    final String currentText = this.editor.getText();
    int cursorPos = this.editor.getCaretPosition();
    final Matcher matcher = pattern.matcher(currentText);
    boolean result = false;
    if (next) {
      if (cursorPos < currentText.length()) {
        if (matcher.find(cursorPos) || matcher.find(0)) {
          final int foundPosition = matcher.start();
          this.editor.select(foundPosition, matcher.end());
          this.editor.getCaret().setSelectionVisible(true);
          result = true;
        }
      }
    } else {
      int lastFound = -1;
      int lastFoundEnd = -1;

      int maxPos = this.editor.getCaret().getMark() == this.editor.getCaret().getDot() ? this.editor.getCaretPosition() : this.editor.getSelectionStart();

      for (int i = 0; i < 2; i++) {
        while (matcher.find()) {
          final int pos = matcher.start();
          if (pos < maxPos) {
            lastFound = pos;
            lastFoundEnd = matcher.end();
          } else {
            break;
          }
        }
        if (lastFound >= 0) {
          break;
        }
        maxPos = currentText.length();
      }

      if (lastFound >= 0) {
        this.editor.select(lastFound, lastFoundEnd);
        this.editor.getCaret().setSelectionVisible(true);
        result = true;
      }
    }
    return result;
  }

  @Override
  public boolean findNext(@Nonnull final Pattern pattern, @Nonnull final FindTextScopeProvider provider) {
    return searchSubstring(pattern, true);
  }

  @Override
  public boolean findPrev(@Nonnull final Pattern pattern, @Nonnull final FindTextScopeProvider provider) {
    return searchSubstring(pattern, false);
  }

  @Override
  public boolean doesSupportPatternSearch() {
    return true;
  }

  @Override
  public boolean doCopy() {
    boolean result = false;

    final String selected = this.editor.getSelectedText();
    if (selected != null && !selected.isEmpty()) {
      final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      clipboard.setContents(new StringSelection(selected), null);
    }

    return result;
  }

  @Override
  public boolean doesSupportCutCopyPaste() {
    return true;
  }

  @Override
  public boolean isCutAllowed() {
    final String selected = this.editor.getSelectedText();
    return selected != null && !selected.isEmpty();
  }

  @Override
  public boolean doCut() {
    boolean result = false;

    final String selected = this.editor.getSelectedText();
    if (selected != null && !selected.isEmpty()) {
      final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      clipboard.setContents(new StringSelection(selected), null);
      this.editor.replaceSelection(""); //NOI18N
    }

    return result;
  }

  @Override
  public boolean doPaste() {
    boolean result = false;

    final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    String text = null;
    try {
      if (Utils.isDataFlavorAvailable(clipboard, DataFlavor.stringFlavor)) {
        text = clipboard.getData(DataFlavor.stringFlavor).toString();
      }
    } catch (Exception ex) {
      LOGGER.warn("Can't get data from clipboard : " + ex.getMessage()); //NOI18N
    }
    if (text != null) {
      this.editor.replaceSelection(text);
      result = true;
    }
    return result;
  }

  @Override
  public boolean isCopyAllowed() {
    final String selected = this.editor.getSelectedText();
    return selected != null && !selected.isEmpty();
  }

  @Override
  public boolean isPasteAllowed() {
    final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    return Utils.isDataFlavorAvailable(clipboard, DataFlavor.stringFlavor);
  }

}
