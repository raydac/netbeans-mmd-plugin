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

import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.print.MMDPrintPanel;
import com.igormaznitsa.mindmap.print.PrintableObject;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import com.igormaznitsa.sciareto.Context;
import com.igormaznitsa.sciareto.Main;
import com.igormaznitsa.sciareto.preferences.PrefUtils;
import com.igormaznitsa.sciareto.preferences.PreferencesManager;
import com.igormaznitsa.sciareto.preferences.SpecificKeys;
import com.igormaznitsa.sciareto.ui.DialogProviderManager;
import com.igormaznitsa.sciareto.ui.FindTextScopeProvider;
import com.igormaznitsa.sciareto.ui.SystemUtils;
import com.igormaznitsa.sciareto.ui.UiUtils;
import com.igormaznitsa.sciareto.ui.tabs.TabTitle;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.OptionFlags;
import net.sourceforge.plantuml.SourceStringReader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
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
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.Timer;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import net.sourceforge.plantuml.UmlDiagram;
import net.sourceforge.plantuml.core.Diagram;
import net.sourceforge.plantuml.cucadiagram.dot.GraphvizUtils;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;

public final class PlantUmlTextEditor extends AbstractEditor {

  public static final Font DEFAULT_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 14);
  public static final Set<String> SUPPORTED_EXTENSIONS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("pu", "puml", "plantuml")));

  private static final Icon ICON_WARNING = new ImageIcon(UiUtils.loadIcon("warning16.png"));
  private static final Icon ICON_INFO = new ImageIcon(UiUtils.loadIcon("info16.png"));

  private static final Pattern NEWPAGE_PATTERN = Pattern.compile("^\\s*newpage($|\\s.*$)", Pattern.MULTILINE);

  private static final int AUTOUPDATE_DELAY = 5000;

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
  private static File lastExportedFile = null;
  private final RSyntaxTextArea editor;
  private final TabTitle title;
  private final RUndoManager undoManager;
  private final ScalableImage imageComponent;
  private final JSplitPane mainPanel;
  private final JPanel renderedPanel;
  private final JScrollPane renderedScrollPane;
  private final JLabel labelWarningNoGraphwiz;
  private boolean ignoreChange;

  private final JButton buttonPrevPage;
  private final JButton buttonNextPage;
  private final JLabel labelPageNumber;
  private int pageNumberToRender = 0;

  private final AtomicReference<Timer> autoRefreshTimer = new AtomicReference<>();

  public PlantUmlTextEditor(@Nonnull final Context context, @Nullable File file) throws IOException {
    super();
    initPlantUml();

    this.editor = new RSyntaxTextArea();
    this.editor.setPopupMenu(null);

    final AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance();
    atmf.putMapping("text/plantuml", "com.igormaznitsa.sciareto.ui.editors.PlantUmlTokenMaker");

    this.editor.setSyntaxEditingStyle("text/plantuml");
    this.editor.setAntiAliasingEnabled(true);
    this.editor.setBracketMatchingEnabled(true);
    this.editor.setCodeFoldingEnabled(false);

    final Font mainFont = PreferencesManager.getInstance().getFont(PreferencesManager.getInstance().getPreferences(), SpecificKeys.PROPERTY_TEXT_EDITOR_FONT, DEFAULT_FONT);
    this.editor.setFont(mainFont);

    final SyntaxScheme scheme = this.editor.getSyntaxScheme();

    if (mainFont != null) {
      scheme.getStyle(Token.RESERVED_WORD).font = mainFont.deriveFont(Font.BOLD);
      scheme.getStyle(Token.IDENTIFIER).font = mainFont.deriveFont(Font.ITALIC);
    }

    scheme.getStyle(Token.COMMENT_EOL).foreground = Color.LIGHT_GRAY;
    this.editor.revalidate();

    this.editor.getCaret().setSelectionVisible(true);

    this.mainPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

    this.mainPanel.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {
      @Override
      public void propertyChange(@Nonnull final PropertyChangeEvent evt) {
        if (isTextEditorVisible()) {
          editor.requestFocusInWindow();
        } else {
          imageComponent.requestFocusInWindow();
        }
      }
    });

    final RTextScrollPane scrollPane = new RTextScrollPane(this.editor, true);

    this.renderedPanel = new JPanel(new BorderLayout());
    this.imageComponent = new ScalableImage();
    this.renderedScrollPane = new EditorScrollPanel(this.imageComponent);
    this.renderedScrollPane.setWheelScrollingEnabled(true);
    this.renderedScrollPane.getVerticalScrollBar().setBlockIncrement(IMG_BLOCK_INCREMENT);
    this.renderedScrollPane.getVerticalScrollBar().setUnitIncrement(IMG_UNIT_INCREMENT);
    this.renderedScrollPane.getHorizontalScrollBar().setBlockIncrement(IMG_BLOCK_INCREMENT);
    this.renderedScrollPane.getHorizontalScrollBar().setUnitIncrement(IMG_UNIT_INCREMENT);

    final JPanel menu = new JPanel(new GridBagLayout());
    final GridBagConstraints gbdata = new GridBagConstraints(GridBagConstraints.RELATIVE, 0, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0);

    final JButton buttonRefresh = new JButton(loadMenuIcon("arrow_refresh"));
    buttonRefresh.setToolTipText("Refresh image for text");
    buttonRefresh.addActionListener(new ActionListener() {
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

    final JButton buttonClipboardImage = new JButton(loadMenuIcon("clipboard_image"));
    buttonClipboardImage.setToolTipText("Copy image to clipboard");
    buttonClipboardImage.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        final BufferedImage image = imageComponent.getImage();
        if (image != null) {
          Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new TransferableImage(image), null);
        }
      }
    });

    final JButton buttonClipboardText = new JButton(loadMenuIcon("clipboard_text"));
    buttonClipboardText.setToolTipText("Copy script to clipboard");
    buttonClipboardText.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(editor.getText()), null);
      }
    });

    this.buttonPrevPage = new JButton(loadMenuIcon("resultset_previous"));
    this.buttonPrevPage.setToolTipText("Previous page");
    this.buttonPrevPage.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        pageNumberToRender--;
        renderCurrentTextInPlantUml();
      }
    });

    this.buttonNextPage = new JButton(loadMenuIcon("resultset_next"));
    this.buttonNextPage.setToolTipText("Next page");
    this.buttonNextPage.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        pageNumberToRender++;
        renderCurrentTextInPlantUml();
      }
    });

    this.labelPageNumber = new JLabel();

    this.labelWarningNoGraphwiz = makeLinkLabel("You should install Graphviz!", "https://www.graphviz.org/download/", "Open download page", ICON_WARNING);

    final JButton buttonPrintImage = new JButton(loadMenuIcon("printer"));
    buttonPrintImage.setToolTipText("Print current page");
    buttonPrintImage.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(@Nonnull final ActionEvent e) {
        Main.getApplicationFrame().endFullScreenIfActive();
        final MMDPrintPanel printPanel = new MMDPrintPanel(DialogProviderManager.getInstance().getDialogProvider(), null, PrintableObject.newBuild().image(imageComponent.getImage()).build());
        UiUtils.makeOwningDialogResizable(printPanel);
        JOptionPane.showMessageDialog(mainPanel, printPanel, "Print PlantUML image", JOptionPane.PLAIN_MESSAGE);
      }
    });

    menu.add(buttonRefresh, gbdata);
    menu.add(buttonClipboardImage, gbdata);
    menu.add(buttonClipboardText, gbdata);
    menu.add(buttonExportImage, gbdata);
    menu.add(buttonPrintImage, gbdata);
    menu.add(Box.createHorizontalStrut(16), gbdata);
    menu.add(this.buttonPrevPage, gbdata);
    menu.add(this.labelPageNumber, gbdata);
    menu.add(this.buttonNextPage, gbdata);

    gbdata.fill = GridBagConstraints.HORIZONTAL;
    gbdata.weightx = 10000;
    menu.add(Box.createHorizontalBox(), gbdata);
    gbdata.weightx = 1;
    gbdata.fill = GridBagConstraints.NONE;

    menu.add(makeLinkLabel("PlantUML Reference", "http://plantuml.com/PlantUML_Language_Reference_Guide.pdf", "Open PlantUL manual", ICON_INFO), gbdata);
    menu.add(makeLinkLabel("AsciiMath Reference", "http://asciimath.org/", "Open AsciiMath manual", ICON_INFO), gbdata);
    menu.add(this.labelWarningNoGraphwiz, gbdata);

    this.renderedPanel.add(menu, BorderLayout.NORTH);
    this.renderedPanel.add(this.renderedScrollPane, BorderLayout.CENTER);

    this.mainPanel.add(scrollPane);
    this.mainPanel.add(this.renderedPanel);

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

    this.editor.addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        if (!isTextEditorVisible()) {
          buttonRefresh.requestFocus();
        }
      }
    });

    this.undoManager = new RUndoManager(this.editor);

    loadContent(file);

    this.editor.discardAllEdits();
    this.undoManager.discardAllEdits();

    this.undoManager.updateActions();

    this.editor.getDocument().addUndoableEditListener(this.undoManager);

    updateGraphvizLabelVisibility();

    this.hideTextPanel();
  }

  public void hideTextPanel() {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        mainPanel.setDividerLocation(0);
      }
    });

    this.editor.addKeyListener(new KeyAdapter() {
      @Override
      public void keyTyped(final @Nonnull KeyEvent e) {
        if (autoRefreshTimer.get() == null) {
          final Timer oneTimeRefreshTimer = new Timer(AUTOUPDATE_DELAY, new ActionListener() {
            @Override
            public void actionPerformed(@Nonnull final ActionEvent e) {
              try {
                final String txt = editor.getText();
                if (isSyntaxCorrect(txt)) {
                  renderCurrentTextInPlantUml();
                } else {
                  autoRefreshTimer.set(null);
                }
              } catch (final InterruptedException ex){
                Thread.currentThread().interrupt();
              } catch (final Exception ex) {
                LOGGER.error("Exception in auto-refresh processing", ex);
              }
            }
          });
          oneTimeRefreshTimer.setRepeats(false);
          if (autoRefreshTimer.compareAndSet(null, oneTimeRefreshTimer)) {
            oneTimeRefreshTimer.start();
          }
        }
      }
    });
  }

  @Override
  protected void doDispose() {
    stopAutoupdateTimer();
  }

  @Override
  public boolean saveDocumentAs() throws IOException {
    stopAutoupdateTimer();
    return super.saveDocumentAs();
  }

  private void stopAutoupdateTimer() {
    final Timer refTimer = this.autoRefreshTimer.getAndSet(null);
    if (refTimer != null) {
      refTimer.stop();
    }
  }
  
  private int countNewPages(@Nonnull final String text) {
    int count = 1;
    final Matcher matcher = NEWPAGE_PATTERN.matcher(text);
    while (matcher.find()) {
      count++;
    }
    return count;
  }

  @Nonnull
  private JLabel makeLinkLabel(@Nonnull final String text, @Nonnull final String uri, @Nonnull final String toolTip, @Nonnull final Icon icon) {
    final JLabel result = new JLabel(text, icon, JLabel.RIGHT);
    final Font font = result.getFont().deriveFont(Font.BOLD);
    final Map attributes = font.getAttributes();
    attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
    result.setFont(font.deriveFont(attributes));
    result.setForeground(UiUtils.DARK_THEME ? Color.YELLOW : Color.BLUE);
    result.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    result.setToolTipText(toolTip);
    result.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(@Nonnull final MouseEvent e) {
        try {
          UiUtils.browseURI(new URI(uri), false);
        } catch (URISyntaxException ex) {
          LOGGER.error("Can't open URI", ex);
        }
      }
    });
    result.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 16));
    return result;
  }

  private void updateGraphvizLabelVisibility() {
    boolean show;
    try {
      final File graphvizFile = GraphvizUtils.create(null, "png").getDotExe();
      show = graphvizFile == null || !graphvizFile.isFile();
    } catch (Exception ex) {
      show = false;
    }
    this.labelWarningNoGraphwiz.setVisible(show);
  }

  public static boolean isSyntaxCorrect(@Nonnull final String text) throws InterruptedException {
    boolean result = false;
    final SourceStringReader reader = new SourceStringReader(text);
    reader.getBlocks();
    final Diagram system = reader.getBlocks().get(0).getDiagram();
    if (system instanceof UmlDiagram) {
      result = true;
    }
    return result;
  }

  private void initPlantUml() {
    OptionFlags.getInstance().setDotExecutable(PrefUtils.getPlantUmlDotPath());
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

    final FileFilter fileFiterASC = new FileFilter() {
      @Override
      public boolean accept(@Nonnull final File f) {
        return f.isDirectory() || f.getName().toLowerCase(Locale.ENGLISH).endsWith(".txt");
      }

      @Nonnull
      @Override
      public String getDescription() {
        return "ASC text files (*.txt)";
      }
    };

    fileChooser.setApproveButtonText("Export");
    fileChooser.setDialogTitle("Export PlantUML image as File");
    fileChooser.setMultiSelectionEnabled(false);

    fileChooser.addChoosableFileFilter(fileFiterPNG);
    fileChooser.addChoosableFileFilter(fileFiterSVG);
    fileChooser.addChoosableFileFilter(fileFiterLTX);
    fileChooser.addChoosableFileFilter(fileFiterASC);

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
      } else if (fileFilter == fileFiterASC) {
        option = new FileFormatOption(FileFormat.ATXT);
        ext = ".txt";
      } else {
        throw new Error("Unexpected situation");
      }

      final ByteArrayOutputStream buffer = new ByteArrayOutputStream(131072);
      if (!lastExportedFile.getName().contains(".") || !lastExportedFile.getName().endsWith(ext)) {
        lastExportedFile = new File(lastExportedFile.getParent(), lastExportedFile.getName() + ext);
      }

      if (this.pageNumberToRender > 0) {
        try {
          reader.outputImage(buffer, this.pageNumberToRender - 1, option);
          final byte[] bytearray = buffer.toByteArray();
          if (bytearray.length < 10000 && new String(bytearray, "UTF-8").contains("java.lang.UnsupportedOperationException:")) {
            throw new IOException("Detected exception footstep in generated file!");
          }
          FileUtils.writeByteArrayToFile(lastExportedFile, buffer.toByteArray());
          LOGGER.info("Exported plant uml image as file : " + lastExportedFile);
        } catch (IOException ex) {
          LOGGER.error("Can't export plant uml image", ex);
          JOptionPane.showMessageDialog(this.mainPanel, "Can't export! May be the format is not supported by the diagram type!", "Error", JOptionPane.ERROR_MESSAGE);
        }
      } else {
        LOGGER.warn("Page number is <= 0");
        JOptionPane.showMessageDialog(this.mainPanel, "Can't export! Page is not selected!", "Warning", JOptionPane.WARNING_MESSAGE);
      }
    }

  }

  @Override
  public void focusToEditor() {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        imageComponent.requestFocusInWindow();
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

  private boolean isTextEditorVisible() {
    return this.mainPanel.getDividerLocation() > 4;
  }

  @Override
  public boolean redo() {
    if (this.undoManager.canRedo() && isTextEditorVisible()) {
      try {
        this.undoManager.redo();
      } catch (final CannotRedoException ex) {
        LOGGER.warn("Can't make redo in plantUML editor : " + ex.getMessage());
      } finally {
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            renderCurrentTextInPlantUml();
          }
        });
      }
    }
    return this.undoManager.canRedo();
  }

  @Override
  public boolean undo() {
    if (this.undoManager.canUndo() && isTextEditorVisible()) {
      try {
        this.undoManager.undo();
      } catch (final CannotUndoException ex) {
        LOGGER.warn("Can't make undo in plantUML editor : " + ex.getMessage());
      } finally {
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            renderCurrentTextInPlantUml();
          }
        });
      }
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
    updateGraphvizLabelVisibility();
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
    return EditorContentType.PLANTUML;
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
    stopAutoupdateTimer();
    
    final String text = this.editor.getText();

    final SourceStringReader reader = new SourceStringReader(text, "UTF-8");
    final ByteArrayOutputStream buffer = new ByteArrayOutputStream(131072);
    try {
      final int totalPages = Math.max(countNewPages(text), reader.getBlocks().size());
      final int imageIndex = Math.max(1, Math.min(this.pageNumberToRender, totalPages));

      reader.outputImage(buffer, imageIndex - 1, new FileFormatOption(FileFormat.PNG, false));
      this.imageComponent.setImage(ImageIO.read(new ByteArrayInputStream(buffer.toByteArray())), false);
      updatePageNumberInfo(imageIndex, totalPages);

      this.renderedScrollPane.revalidate();
    } catch (IOException e) {
      LOGGER.error("Can't render plant uml", e);
      this.renderedScrollPane.setViewportView(new JLabel("Error during rendering"));
      updatePageNumberInfo(-1, -1);
    }
  }

  private void updatePageNumberInfo(final int pageNumber, final int totalPages) {
    if (pageNumber < 0) {
      this.pageNumberToRender = 1;
      this.buttonNextPage.setEnabled(false);
      this.buttonPrevPage.setEnabled(false);
      this.labelPageNumber.setText("<html><b>&nbsp;&nbsp;--/--&nbsp;&nbsp;</b></html>");
    } else {
      this.pageNumberToRender = pageNumber;
      this.buttonPrevPage.setEnabled(pageNumber > 1);
      this.buttonNextPage.setEnabled(pageNumber < totalPages);
      this.labelPageNumber.setText("<html><b>&nbsp;&nbsp;" + pageNumber + '/' + totalPages + "&nbsp;&nbsp;</b><html>");
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
