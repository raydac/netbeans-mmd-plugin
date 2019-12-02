/*
 * Copyright (C) 2019 Igor Maznitsa.
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

import com.igormaznitsa.meta.annotation.UiThread;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.print.MMDPrintPanel;
import com.igormaznitsa.mindmap.print.PrintableObject;
import com.igormaznitsa.mindmap.swing.panel.utils.ImageSelection;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import com.igormaznitsa.sciareto.Context;
import com.igormaznitsa.sciareto.Main;
import com.igormaznitsa.sciareto.preferences.PrefUtils;
import com.igormaznitsa.sciareto.ui.DialogProviderManager;
import com.igormaznitsa.sciareto.ui.FindTextScopeProvider;
import com.igormaznitsa.sciareto.ui.MainFrame;
import com.igormaznitsa.sciareto.ui.ScaleStatusIndicator;
import com.igormaznitsa.sciareto.ui.SystemUtils;
import com.igormaznitsa.sciareto.ui.UiUtils;
import static com.igormaznitsa.sciareto.ui.editors.AbstractEditor.loadMenuIcon;
import com.igormaznitsa.sciareto.ui.misc.BigLoaderIconAnimationConroller;
import com.igormaznitsa.sciareto.ui.tabs.TabTitle;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
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
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.OptionFlags;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.core.Diagram;
import net.sourceforge.plantuml.core.DiagramDescription;
import net.sourceforge.plantuml.cucadiagram.dot.GraphvizUtils;
import net.sourceforge.plantuml.error.PSystemError;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.RUndoManager;
import reactor.core.Disposable;
import reactor.core.publisher.DirectProcessor;

public abstract class AbstractPlUmlEditor extends AbstractEditor {

  protected static final Font DEFAULT_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 14);
  protected static final Icon ICON_WARNING = new ImageIcon(UiUtils.loadIcon("warning16.png"));
  protected static final Icon ICON_INFO = new ImageIcon(UiUtils.loadIcon("info16.png"));

  private static final Pattern NEWPAGE_PATTERN = Pattern.compile("^\\s*newpage($|\\s.*$)", Pattern.MULTILINE);

  private static final int DELAY_AUTOREFRESH_SECONDS = 5;
  private final JLabel progressLabel = new JLabel(BigLoaderIconAnimationConroller.LOADING);

  private volatile LastRendered lastSuccessfulyRenderedText = null;

  protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
  private File lastExportedFile = null;
  private final ScalableRsyntaxTextArea editor;
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
  private final JCheckBox autoRefresh;
  private int pageNumberToRender = 0;

  private final JPanel menu;

  private final DirectProcessor<Boolean> eventProcessor = DirectProcessor.create();
  private final Disposable eventChain;

  public AbstractPlUmlEditor(@Nonnull final Context context, @Nullable File file) throws IOException {
    super();
    initPlantUml();

    this.editor = new ScalableRsyntaxTextArea();
    this.editor.setPopupMenu(null);

    doPutMapping((AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance());
    this.editor.setSyntaxEditingStyle(getSyntaxEditingStyle());

    this.editor.setAntiAliasingEnabled(true);
    this.editor.setBracketMatchingEnabled(true);
    this.editor.setCodeFoldingEnabled(false);

    final AutoCompletion autoCompletion = new AutoCompletion(new PlantUmlTokenMaker().makeCompletionProvider());
    autoCompletion.setAutoActivationEnabled(true);
    autoCompletion.setAutoCompleteSingleChoices(false);
    autoCompletion.install(this.editor);

    final SyntaxScheme scheme = this.editor.getSyntaxScheme();
    final Font editorFont = this.editor.getFont();

    if (editorFont != null) {
      scheme.getStyle(Token.RESERVED_WORD).font = editorFont.deriveFont(Font.BOLD);
      scheme.getStyle(Token.IDENTIFIER).font = editorFont.deriveFont(Font.ITALIC);
    }

    scheme.getStyle(Token.COMMENT_EOL).foreground = Color.LIGHT_GRAY;
    this.editor.revalidate();

    this.editor.getCaret().setSelectionVisible(true);

    this.mainPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

    final RTextScrollPane scrollPane = new RTextScrollPane(this.editor, true);

    this.renderedPanel = new JPanel(new BorderLayout());
    this.renderedScrollPane = new EditorScrollPanel();
    this.imageComponent = new ScalableImage();

    this.mainPanel.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, (@Nonnull final PropertyChangeEvent evt) -> {
      if (isTextEditorVisible()) {
        editor.requestFocusInWindow();
      } else {
        imageComponent.requestFocusInWindow();
      }
    });

    final ScaleStatusIndicator scaleLabel = new ScaleStatusIndicator(this.imageComponent);
    this.renderedScrollPane.setViewportView(this.imageComponent);

    this.renderedScrollPane.getVerticalScrollBar().setBlockIncrement(ScalableImage.IMG_BLOCK_INCREMENT);
    this.renderedScrollPane.getVerticalScrollBar().setUnitIncrement(ScalableImage.IMG_UNIT_INCREMENT);
    this.renderedScrollPane.getHorizontalScrollBar().setBlockIncrement(ScalableImage.IMG_BLOCK_INCREMENT);
    this.renderedScrollPane.getHorizontalScrollBar().setUnitIncrement(ScalableImage.IMG_UNIT_INCREMENT);

    this.renderedScrollPane.setWheelScrollingEnabled(true);

    this.menu = new JPanel(new GridBagLayout());
    final GridBagConstraints gbdata = new GridBagConstraints(GridBagConstraints.RELATIVE, 0, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new Insets(2, 0, 0, 0), 0, 0);

    final JButton buttonRefresh = new JButton(loadMenuIcon("arrow_refresh"));
    buttonRefresh.setToolTipText("Refresh image for text");
    buttonRefresh.addActionListener((ActionEvent e) -> {
      startRenderScript();
    });

    final JButton buttonEditScript = new JButton(loadMenuIcon("edit_script")) {
      @Override
      public void setEnabled(final boolean flag) {
        super.setEnabled(true);
      }

      @Override
      public boolean isEnabled() {
        return true;
      }
    };

    buttonEditScript.setToolTipText("Edit script");
    buttonEditScript.addActionListener((ActionEvent e) -> {
      if (isTextEditorVisible()) {
        mainPanel.setDividerLocation(0);
        imageComponent.requestFocus();
      } else {
        mainPanel.setDividerLocation(256);
        editor.requestFocus();
      }
    });

    final JButton buttonExportImage = new JButton(loadMenuIcon("picture_save"));
    buttonExportImage.setToolTipText("Export image as file");
    buttonExportImage.addActionListener((ActionEvent e) -> {
      Main.getApplicationFrame().endFullScreenIfActive();
      exportAsFile();
    });

    final JButton buttonClipboardImage = new JButton(loadMenuIcon("clipboard_image"));
    buttonClipboardImage.setToolTipText("Copy image to clipboard");
    buttonClipboardImage.addActionListener((ActionEvent e) -> {
      final BufferedImage image = imageComponent.getImage();
      if (image != null) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new ImageSelection(image), null);
      }
    });

    final JButton buttonClipboardText = new JButton(loadMenuIcon("clipboard_text"));
    buttonClipboardText.setToolTipText("Copy script to clipboard");
    buttonClipboardText.addActionListener((ActionEvent e) -> {
      Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(editor.getText()), null);
    });

    this.buttonPrevPage = new JButton(loadMenuIcon("resultset_previous"));
    this.buttonPrevPage.setToolTipText("Previous page");
    this.buttonPrevPage.addActionListener((ActionEvent e) -> {
      pageNumberToRender--;
      startRenderScript();
    });

    this.buttonNextPage = new JButton(loadMenuIcon("resultset_next"));
    this.buttonNextPage.setToolTipText("Next page");
    this.buttonNextPage.addActionListener((ActionEvent e) -> {
      pageNumberToRender++;
      startRenderScript();
    });

    this.labelPageNumber = new JLabel();

    this.labelWarningNoGraphwiz = makeLinkLabel("You should install Graphviz!", "https://www.graphviz.org/download/", "Open download page", ICON_WARNING);

    final JButton buttonPrintImage = new JButton(loadMenuIcon("printer"));
    buttonPrintImage.setToolTipText("Print current page");
    buttonPrintImage.addActionListener((@Nonnull final ActionEvent e) -> {
      Main.getApplicationFrame().endFullScreenIfActive();
      final MMDPrintPanel printPanel = new MMDPrintPanel(DialogProviderManager.getInstance().getDialogProvider(), null, PrintableObject.newBuild().image(imageComponent.getImage()).build());
      UiUtils.makeOwningDialogResizable(printPanel);
      JOptionPane.showMessageDialog(mainPanel, printPanel, "Print PlantUML image", JOptionPane.PLAIN_MESSAGE);
    });

    final JButton buttonAscImageToClipboard = new JButton(loadMenuIcon("clipboard_asc"));
    buttonAscImageToClipboard.setToolTipText("Copy ASCII image to clipboard");
    buttonAscImageToClipboard.addActionListener((@Nonnull final ActionEvent e) -> {
      final String text = renderPageAsAscII();
      if (text == null) {
        JOptionPane.showMessageDialog(mainPanel, "Can't generate ASCII for the diagram.\nMay be it is unsupported for such kind of diagrams.", "Can't generate", JOptionPane.WARNING_MESSAGE);
      } else {
        final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        if (clipboard != null) {
          clipboard.setContents(new StringSelection(text), null);
        }
      }
    });

    this.autoRefresh = new JCheckBox("Auto-refresh", true);
    this.autoRefresh.setToolTipText(String.format("Refresh rendered image during typing (in %d seconds)", DELAY_AUTOREFRESH_SECONDS));

    this.autoRefresh.addActionListener((@Nonnull final ActionEvent e) -> {
      if (!autoRefresh.isSelected()) {
        LOGGER.info("Auto-refresh is turned off");
      } else {
        LOGGER.info("Auto-refresh is turned on");
      }
    });

    this.menu.add(buttonRefresh, gbdata);
    this.menu.add(buttonEditScript, gbdata);
    this.menu.add(buttonClipboardImage, gbdata);
    this.menu.add(buttonAscImageToClipboard, gbdata);
    this.menu.add(buttonClipboardText, gbdata);
    this.menu.add(buttonExportImage, gbdata);
    this.menu.add(buttonPrintImage, gbdata);
    this.menu.add(Box.createHorizontalStrut(16), gbdata);
    this.menu.add(this.buttonPrevPage, gbdata);
    this.menu.add(this.labelPageNumber, gbdata);
    this.menu.add(this.buttonNextPage, gbdata);
    this.menu.add(this.autoRefresh, gbdata);

    gbdata.fill = GridBagConstraints.HORIZONTAL;
    gbdata.weightx = 10000;
    this.menu.add(Box.createHorizontalBox(), gbdata);
    gbdata.weightx = 1;
    gbdata.fill = GridBagConstraints.VERTICAL;
    this.menu.add(scaleLabel, gbdata);
    this.menu.add(Box.createHorizontalStrut(16), gbdata);

    addComponentsToLeftPart(this.menu, gbdata);
    this.menu.add(this.labelWarningNoGraphwiz, gbdata);

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

    this.eventChain = eventProcessor.publishOn(MainFrame.REACTOR_SCHEDULER)
            .buffer(Duration.ofSeconds(DELAY_AUTOREFRESH_SECONDS))
            .filter(x -> !x.isEmpty() && this.autoRefresh.isSelected())
            .publishOn(UiUtils.SWING_SCHEDULER)
            .subscribe(x -> {
              final String txt = editor.getText();
              final LastRendered lastRendered = this.lastSuccessfulyRenderedText;
              if ((lastRendered == null || !txt.equals(lastRendered.text)) && isSyntaxCorrect(txt)) {
                startRenderScript();
              }
            });

  }

  @Nonnull
  protected abstract String getSyntaxEditingStyle();

  protected abstract void doPutMapping(@Nonnull AbstractTokenMakerFactory f);

  protected void addComponentsToLeftPart(@Nonnull final JPanel menuPanel, @Nonnull final GridBagConstraints constraints) {

  }

  @Nonnull
  protected JLabel makeLinkLabel(@Nonnull final String text, @Nonnull final String uri, @Nonnull final String toolTip, @Nonnull final Icon icon) {
    return this.makeLinkLabel(text, () -> {
      try {
        UiUtils.browseURI(new URI(uri), false);
      } catch (URISyntaxException ex) {
        LOGGER.error("Can't open URI", ex);
      }
    }, toolTip, icon);
  }

  public boolean isSyntaxCorrect(@Nonnull final String text) {
    boolean result = false;
    final SourceStringReader reader = new SourceStringReader(text);
    reader.getBlocks();
    try {
      final Diagram system = reader.getBlocks().get(0).getDiagram();
      if (!(system instanceof PSystemError)) {
        result = true;
      }
    } catch (Exception ex) {
      LOGGER.warn("Detected exception in syntax check : " + ex.getMessage());
      result = false;
    }
    return result;
  }

  public void hideTextPanel() {
    SwingUtilities.invokeLater(() -> {
      mainPanel.setDividerLocation(0);
    });

    this.editor.addKeyListener(new KeyAdapter() {
      @Override
      public void keyTyped(final @Nonnull KeyEvent e) {
        try {
          eventProcessor.onNext(true);
        } catch (Exception ex) {
          LOGGER.info("Exception on next event: " + ex.getMessage());
        }
      }
    });
  }

  private void setMenuItemsEnable(final boolean enable) {
    for (final Component c : this.menu.getComponents()) {
      if (c instanceof JButton) {
        c.setEnabled(enable);
      }
    }
  }

  @Override
  protected void doDispose() {
    eventProcessor.onComplete();
    eventChain.dispose();
  }

  protected int countNewPages(@Nonnull final String text) {
    int count = 1;
    final Matcher matcher = NEWPAGE_PATTERN.matcher(text);
    while (!Thread.currentThread().isInterrupted() && matcher.find()) {
      count++;
    }
    return count;
  }

  @Nonnull
  protected JLabel makeLinkLabel(@Nonnull final String text, @Nonnull final Runnable onClick, @Nonnull final String toolTip, @Nonnull final Icon icon) {
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
        onClick.run();
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

  private void initPlantUml() {
    OptionFlags.getInstance().setDotExecutable(PrefUtils.getPlantUmlDotPath());
  }

  private void exportAsFile() {
    final JFileChooser fileChooser = new JFileChooser(lastExportedFile == null ? this.getTabTitle().getAssociatedFile().getParentFile() : lastExportedFile);
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
  public void focusToEditor(final int line) {
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
            startRenderScript();
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
            startRenderScript();
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
    this.editor.updateConfig();

    final SyntaxScheme scheme = this.editor.getSyntaxScheme();
    final Font editorFont = this.editor.getFont();
    if (editorFont != null) {
      scheme.getStyle(Token.RESERVED_WORD).font = editorFont.deriveFont(Font.BOLD);
      scheme.getStyle(Token.IDENTIFIER).font = editorFont.deriveFont(Font.ITALIC);
    }

    this.editor.repaint();
  }

  @Override
  public void loadContent(@Nullable final File file) throws IOException {
    this.ignoreChange = true;
    try {
      if (file != null) {
        this.editor.setText(FileUtils.readFileToString(file, "UTF-8")); //NOI18N
        this.editor.setCaretPosition(0);
        startRenderScript();
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
        file = DialogProviderManager.getInstance()
                .getDialogProvider()
                .msgSaveFileDialog(Main.getApplicationFrame(), "sources-editor", "Save sources", null, true, new FileFilter[]{getFileFilter()}, "Save");
        if (file == null) {
          return result;
        }
      }
      SystemUtils.saveUTFText(file, this.editor.getText());
      this.title.setChanged(false);
      result = true;
      startRenderScript();
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

  private void startRenderScript() {
    final String theText = this.editor.getText();

    final SourceStringReader reader = new SourceStringReader(theText, "UTF-8");
    final int totalPages = Math.max(countNewPages(theText), reader.getBlocks().size());
    final int imageIndex = Math.max(1, Math.min(this.pageNumberToRender, totalPages));

    if (imageIndex != this.pageNumberToRender) {
      this.pageNumberToRender = imageIndex;
    }

    final LastRendered currentText = new LastRendered(imageIndex, theText);

    if (!currentText.equals(this.lastSuccessfulyRenderedText)) {
      updatePageNumberInfo(currentText.page, totalPages);

      Future<BufferedImage> renderImage = null;

      final AtomicInteger dividerLocation = new AtomicInteger(Math.max(0, this.mainPanel.getDividerLocation()));
      final PropertyChangeListener dividerListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
          dividerLocation.set(Math.max(0, mainPanel.getDividerLocation()));
        }
      };

      this.mainPanel.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, dividerListener);

      MainFrame.REACTOR_SCHEDULER.schedule(() -> {
        BigLoaderIconAnimationConroller.getInstance().registerLabel(progressLabel);
        try {
          try {
            SwingUtilities.invokeAndWait(() -> {
              setMenuItemsEnable(false);
              renderedPanel.remove(renderedScrollPane);
              for (final Component c : renderedPanel.getComponents()) {
                if ("ERROR_LABEL".equals(c.getName())) {
                  renderedPanel.remove(c);
                  break;
                }
              }
              renderedPanel.add(progressLabel, BorderLayout.CENTER);
              mainPanel.setDividerLocation(dividerLocation.get());
            });
          } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return;
          } catch (InvocationTargetException ex) {
            throw new RuntimeException(ex);
          }

          final AtomicReference<Exception> detectedError = new AtomicReference<>();
          final AtomicReference<BufferedImage> generatedImage = new AtomicReference<>();

          final ByteArrayOutputStream buffer = new ByteArrayOutputStream(131072);
          try {
            final DiagramDescription description = reader.outputImage(buffer, imageIndex - 1, new FileFormatOption(FileFormat.PNG, false));
            generatedImage.set(ImageIO.read(new ByteArrayInputStream(buffer.toByteArray())));
          } catch (Exception ex) {
            detectedError.set(ex);
          }

          SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
              mainPanel.removePropertyChangeListener(dividerListener);

              final Exception error = detectedError.get();
              if (error == null) {
                lastSuccessfulyRenderedText = currentText;
                imageComponent.setImage(generatedImage.get(), false);
                renderedScrollPane.revalidate();
                renderedPanel.remove(progressLabel);
                renderedPanel.add(renderedScrollPane, BorderLayout.CENTER);
                setMenuItemsEnable(true);
              } else {
                final JLabel errorLabel = new JLabel("<html><h1>ERROR: " + StringEscapeUtils.escapeHtml(error.getMessage()) + "</h1></html>", JLabel.CENTER);
                errorLabel.setName("ERROR_LABEL");
                renderedPanel.remove(progressLabel);
                renderedPanel.add(errorLabel, BorderLayout.CENTER);
              }

              mainPanel.setDividerLocation(dividerLocation.get());
            }
          });

        } finally {
          BigLoaderIconAnimationConroller.getInstance().unregisterLabel(progressLabel);
        }

      });
    }
  }

  @Nullable
  @UiThread
  private String renderPageAsAscII() {
    final String theText = this.editor.getText();

    final SourceStringReader reader = new SourceStringReader(theText, "UTF-8");
    final int totalPages = Math.max(countNewPages(theText), reader.getBlocks().size());

    final int imageIndex = Math.max(1, Math.min(this.pageNumberToRender, totalPages));

    final ByteArrayOutputStream utfBuffer = new ByteArrayOutputStream();

    try {
      final DiagramDescription description = reader.outputImage(utfBuffer, imageIndex - 1, new FileFormatOption(FileFormat.ATXT, false));
      final String result = new String(utfBuffer.toByteArray(), StandardCharsets.UTF_8);
      if (result.contains("java.lang.UnsupportedOperationException: ATXT")) {
        throw new UnsupportedOperationException("ATXT is not supported for the diagram");
      }
      return result;
    } catch (Exception ex) {
      LOGGER.error("Can't export ASCII image", ex);
      return null;
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

  protected static final class LastRendered {

    private final int page;
    private final String text;

    public LastRendered(final int page, @Nonnull final String text) {
      this.page = page;
      this.text = text;
    }

    @Override
    public int hashCode() {
      return this.text.hashCode() ^ this.page;
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      final LastRendered other = (LastRendered) obj;
      if (this.page != other.page) {
        return false;
      }
      return Objects.equals(this.text, other.text);
    }
  }

}
