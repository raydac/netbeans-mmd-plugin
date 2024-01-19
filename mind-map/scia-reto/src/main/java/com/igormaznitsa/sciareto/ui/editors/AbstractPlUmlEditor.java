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

import static org.apache.commons.text.StringEscapeUtils.escapeHtml3;

import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.meta.annotation.UiThread;
import com.igormaznitsa.meta.common.utils.Assertions;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.print.MMDPrintPanel;
import com.igormaznitsa.mindmap.print.PrintableObject;
import com.igormaznitsa.mindmap.swing.panel.utils.ImageSelection;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactoryProvider;
import com.igormaznitsa.sciareto.Context;
import com.igormaznitsa.sciareto.SciaRetoStarter;
import com.igormaznitsa.sciareto.preferences.AdditionalPreferences;
import com.igormaznitsa.sciareto.ui.DialogProviderManager;
import com.igormaznitsa.sciareto.ui.FindTextScopeProvider;
import com.igormaznitsa.sciareto.ui.MainFrame;
import com.igormaznitsa.sciareto.ui.ScaleStatusIndicator;
import com.igormaznitsa.sciareto.ui.SrI18n;
import com.igormaznitsa.sciareto.ui.UiUtils;
import com.igormaznitsa.sciareto.ui.misc.BigLoaderIconAnimationConroller;
import com.igormaznitsa.sciareto.ui.misc.MultiFileContainer;
import com.igormaznitsa.sciareto.ui.misc.SplitPaneExt;
import com.igormaznitsa.sciareto.ui.tabs.TabExporter;
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
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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
import javax.swing.JMenuItem;
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
import javax.swing.undo.UndoManager;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.OptionFlags;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.core.Diagram;
import net.sourceforge.plantuml.core.DiagramDescription;
import net.sourceforge.plantuml.dot.GraphvizUtils;
import net.sourceforge.plantuml.error.PSystemError;
import org.apache.commons.io.FileUtils;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rtextarea.RTextScrollPane;
import reactor.core.Disposable;
import reactor.core.publisher.DirectProcessor;

public abstract class AbstractPlUmlEditor extends AbstractTextEditor {

  protected static final Icon ICON_WARNING = new ImageIcon(UiUtils.loadIcon("warning16.png"));
  protected static final Icon ICON_INFO = new ImageIcon(UiUtils.loadIcon("info16.png"));

  private static final Pattern NEWPAGE_PATTERN =
      Pattern.compile("^\\s*newpage($|\\s.*$)", Pattern.MULTILINE);

  private static final int DELAY_AUTOREFRESH_SECONDS = 5;
  private static final Set<ExportType> DEFAULT_EXPORT_TYPES =
      Collections.unmodifiableSet(EnumSet.allOf(ExportType.class));
  protected final ScalableRsyntaxTextArea editor;
  private final JLabel progressLabel = new JLabel(BigLoaderIconAnimationConroller.LOADING);
  private final TabTitle title;
  private final ScalableImage imageComponent;
  private final JSplitPane mainPanel;
  private final JPanel renderedPanel;
  private final JScrollPane renderedScrollPane;
  private final JLabel labelWarningNoGraphwiz;
  private final JButton buttonPrevPage;
  private final JButton buttonNextPage;
  private final JLabel labelPageNumber;
  private final JCheckBox autoRefresh;
  private final JPanel editorPanel;
  private final JPanel menu;
  private final DirectProcessor<Boolean> eventProcessor = DirectProcessor.create();
  private final Disposable eventChain;
  private final ScaleStatusIndicator scaleLabel;
  private volatile LastRendered lastSuccessfullyRenderedText = null;
  private File lastExportedFile = null;
  private boolean ignoreChange;
  private int pageNumberToRender = 0;

  protected AbstractPlUmlEditor(@Nonnull final Context context, @Nonnull File file)
      throws IOException {
    super();
    initPlantUml();

    this.editor = new ScalableRsyntaxTextArea(this.mindMapPanelConfig);
    this.editor.setPopupMenu(null);

    doPutMapping((AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance());
    this.editor.setSyntaxEditingStyle(getSyntaxEditingStyle());

    this.editor.setAntiAliasingEnabled(true);
    this.editor.setBracketMatchingEnabled(true);
    this.editor.setCodeFoldingEnabled(false);

    this.editor.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(@Nonnull final KeyEvent e) {
        if (!e.isConsumed() && e.getModifiers() == 0 && e.getKeyCode() == KeyEvent.VK_ESCAPE) {
          e.consume();
          context.hideFindTextPane();
        }
      }
    });

    final AutoCompletion autoCompletion =
        new AutoCompletion(new PlantUmlTokenMaker().makeCompletionProvider());
    autoCompletion.setAutoActivationEnabled(true);
    autoCompletion.setAutoCompleteSingleChoices(false);
    autoCompletion.install(this.editor);

    final SyntaxScheme scheme = this.editor.getSyntaxScheme();
    final Font editorFont = this.editor.getFont();

    if (editorFont != null) {
      scheme.getStyle(Token.RESERVED_WORD).font = editorFont.deriveFont(Font.BOLD);
      scheme.getStyle(Token.RESERVED_WORD_2).font = editorFont.deriveFont(Font.BOLD | Font.ITALIC);
      scheme.getStyle(Token.OPERATOR).font = editorFont.deriveFont(Font.BOLD);
      scheme.getStyle(Token.IDENTIFIER).font = editorFont.deriveFont(Font.ITALIC);
    }

    scheme.getStyle(Token.COMMENT_EOL).foreground = Color.LIGHT_GRAY;
    this.editor.revalidate();

    this.editor.getCaret().setSelectionVisible(true);

    this.mainPanel = new SplitPaneExt(JSplitPane.VERTICAL_SPLIT);

    final RTextScrollPane scrollPane = new RTextScrollPane(this.editor, true);

    this.renderedPanel = new JPanel(new BorderLayout());
    this.renderedScrollPane = new EditorScrollPanel();
    this.imageComponent = new ScalableImage(this.mindMapPanelConfig);

    this.mainPanel.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY,
        (@Nonnull final PropertyChangeEvent evt) -> {
          if (isTextEditorVisible()) {
            editor.requestFocusInWindow();
          } else {
            imageComponent.requestFocusInWindow();
          }
        });

    this.scaleLabel =
        new ScaleStatusIndicator(this.imageComponent, UiUtils.figureOutThatDarkTheme());
    this.renderedScrollPane.setViewportView(this.imageComponent);

    this.renderedScrollPane.getVerticalScrollBar()
        .setBlockIncrement(ScalableImage.IMG_BLOCK_INCREMENT);
    this.renderedScrollPane.getVerticalScrollBar()
        .setUnitIncrement(ScalableImage.IMG_UNIT_INCREMENT);
    this.renderedScrollPane.getHorizontalScrollBar()
        .setBlockIncrement(ScalableImage.IMG_BLOCK_INCREMENT);
    this.renderedScrollPane.getHorizontalScrollBar()
        .setUnitIncrement(ScalableImage.IMG_UNIT_INCREMENT);

    this.renderedScrollPane.setWheelScrollingEnabled(true);

    this.menu = new JPanel(new GridBagLayout());
    final GridBagConstraints gbdata =
        new GridBagConstraints(GridBagConstraints.RELATIVE, 0, 1, 1, 1, 1, GridBagConstraints.WEST,
            GridBagConstraints.VERTICAL, new Insets(2, 0, 0, 0), 0, 0);

    final JButton buttonRefresh = new JButton(loadMenuIcon("arrow_refresh"));
    buttonRefresh.setToolTipText(
        this.bundle.getString("editorAbstractPlUml.buttonRefresh.tooltip"));
    buttonRefresh.addActionListener((ActionEvent e) -> {
      startRenderScript();
    });

    final JButton buttonEditScript = new JButton(loadMenuIcon("edit_script")) {
      @Override
      public boolean isEnabled() {
        return true;
      }

      @Override
      public void setEnabled(final boolean flag) {
        super.setEnabled(true);
      }
    };

    buttonEditScript.setToolTipText(
        this.bundle.getString("editorAbstractPlUml.buttonEditScript.tooltip"));
    buttonEditScript.addActionListener((ActionEvent e) -> {
      if (isTextEditorVisible()) {
        mainPanel.setDividerLocation(0);
        imageComponent.requestFocus();
      } else {
        mainPanel.setDividerLocation(256);
        editor.requestFocus();
      }
    });

    final JButton buttonExportImage;
    if (this.isExportImageAsFileAllowed()) {
      buttonExportImage = new JButton(loadMenuIcon("picture_save"));
      buttonExportImage.setToolTipText(
          this.bundle.getString("editorAbstractPlUml.buttonExportImageFile.tooltip"));
      buttonExportImage.addActionListener((ActionEvent e) -> {
        SciaRetoStarter.getApplicationFrame().endFullScreenIfActive();
        exportAsFile();
      });
    } else {
      buttonExportImage = null;
    }

    final JButton buttonClipboardImage;
    if (this.isCopyImageToClipboardAllowed()) {
      buttonClipboardImage = new JButton(loadMenuIcon("clipboard_image"));
      buttonClipboardImage.setToolTipText(
          this.bundle.getString("editorAbstractPlUml.buttonExportClipboardImage.tooltip"));
      buttonClipboardImage.addActionListener((ActionEvent e) -> {
        final BufferedImage image = imageComponent.getImage();
        if (image != null) {
          Toolkit.getDefaultToolkit().getSystemClipboard()
              .setContents(new ImageSelection(image), null);
        }
      });
    } else {
      buttonClipboardImage = null;
    }

    final JButton buttonClipboardText;
    if (this.isCopyScriptToClipboardAllowed()) {
      buttonClipboardText = new JButton(loadMenuIcon("clipboard_text"));
      buttonClipboardText.setToolTipText(
          this.bundle.getString("editorAbstractPlUml.buttonExportClipboardText.tooltip"));
      buttonClipboardText.addActionListener((ActionEvent e) -> {
        Toolkit.getDefaultToolkit().getSystemClipboard()
            .setContents(new StringSelection(editor.getText()), null);
      });
    } else {
      buttonClipboardText = null;
    }

    if (isPageAllowed()) {
      this.buttonPrevPage = new JButton(loadMenuIcon("resultset_previous"));
      this.buttonPrevPage.setToolTipText(
          this.bundle.getString("editorAbstractPlUml.buttonPrevPage.tooltip"));
      this.buttonPrevPage.addActionListener((ActionEvent e) -> {
        pageNumberToRender--;
        startRenderScript();
      });

      this.buttonNextPage = new JButton(loadMenuIcon("resultset_next"));
      this.buttonNextPage.setToolTipText(
          this.bundle.getString("editorAbstractPlUml.buttonNextPage.tooltip"));
      this.buttonNextPage.addActionListener((ActionEvent e) -> {
        pageNumberToRender++;
        startRenderScript();
      });

      this.labelPageNumber = new JLabel();
    } else {
      this.buttonNextPage = null;
      this.buttonPrevPage = null;
      this.labelPageNumber = null;
    }

    this.labelWarningNoGraphwiz =
        makeLinkLabel(this.bundle.getString("editorAbstractPlUml.labelWranGraphviz.text"),
            "https://www.graphviz.org/download/",
            this.bundle.getString("editorAbstractPlUml.labelWranGraphviz.title"), ICON_WARNING);

    final JButton buttonPrintImage;
    if (isPrintImageAllowed()) {
      buttonPrintImage = new JButton(loadMenuIcon("printer"));
      buttonPrintImage.setToolTipText(
          this.bundle.getString("editorAbstractPlUml.buttonPrint.tooltip"));
      buttonPrintImage.addActionListener((@Nonnull final ActionEvent e) -> {
        SciaRetoStarter.getApplicationFrame().endFullScreenIfActive();
        final MMDPrintPanel printPanel =
            new MMDPrintPanel(
                UIComponentFactoryProvider.findInstance(),
                DialogProviderManager.getInstance().getDialogProvider(), null,
                PrintableObject.newBuild().image(imageComponent.getImage()).build());
        UiUtils.makeOwningDialogResizable(printPanel);
        JOptionPane.showMessageDialog(mainPanel, printPanel,
            this.bundle.getString("editorAbstractPlUml.msgDialogPrint.title"),
            JOptionPane.PLAIN_MESSAGE);
      });
    } else {
      buttonPrintImage = null;
    }

    final JButton buttonAscImageToClipboard;
    if (isCopyAsAscIIImageInClipboardAllowed()) {
      buttonAscImageToClipboard = new JButton(loadMenuIcon("clipboard_asc"));
      buttonAscImageToClipboard.setToolTipText(
          this.bundle.getString("editorAbstractPlUml.buttonAscImageToClipboard.tooltip"));
      buttonAscImageToClipboard.addActionListener((@Nonnull final ActionEvent e) -> {
        final String text = renderPageAsAscII();
        if (text == null) {
          JOptionPane.showMessageDialog(mainPanel,
              this.bundle.getString("editorAbstractPlUml.errorMsgDialog.ascii.text"),
              this.bundle.getString("editorAbstractPlUml.errorMsgDialog.ascii.title"),
              JOptionPane.WARNING_MESSAGE);
        } else {
          final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
          if (clipboard != null) {
            clipboard.setContents(new StringSelection(text), null);
          }
        }
      });
    } else {
      buttonAscImageToClipboard = null;
    }

    this.autoRefresh =
        new JCheckBox(this.bundle.getString("editorAbstractPlUml.checkBoxAutorefresh.title"), true);
    this.autoRefresh.setToolTipText(String
        .format(this.bundle.getString("editorAbstractPlUml.checkBoxAutorefresh.tooltip"),
            DELAY_AUTOREFRESH_SECONDS));

    this.autoRefresh.addActionListener((@Nonnull final ActionEvent e) -> {
      if (!autoRefresh.isSelected()) {
        logger.info("Auto-refresh is turned off");
      } else {
        logger.info("Auto-refresh is turned on");
      }
    });

    this.menu.add(buttonRefresh, gbdata);
    this.menu.add(buttonEditScript, gbdata);
    if (buttonClipboardImage != null) {
      this.menu.add(buttonClipboardImage, gbdata);
    }
    if (buttonAscImageToClipboard != null) {
      this.menu.add(buttonAscImageToClipboard, gbdata);
    }
    if (buttonClipboardText != null) {
      this.menu.add(buttonClipboardText, gbdata);
    }
    if (buttonExportImage != null) {
      this.menu.add(buttonExportImage, gbdata);
    }
    if (buttonPrintImage != null) {
      this.menu.add(buttonPrintImage, gbdata);
    }
    addCustomComponents(this.menu, gbdata);
    this.menu.add(Box.createHorizontalStrut(16), gbdata);
    if (this.buttonPrevPage != null) {
      this.menu.add(this.buttonPrevPage, gbdata);
    }
    if (this.labelPageNumber != null) {
      this.menu.add(this.labelPageNumber, gbdata);
    }
    if (this.buttonNextPage != null) {
      this.menu.add(this.buttonNextPage, gbdata);
    }
    this.menu.add(this.autoRefresh, gbdata);

    gbdata.fill = GridBagConstraints.HORIZONTAL;
    gbdata.weightx = 10000;
    this.menu.add(Box.createHorizontalBox(), gbdata);
    gbdata.weightx = 1;
    gbdata.fill = GridBagConstraints.VERTICAL;
    this.menu.add(this.scaleLabel, gbdata);
    this.menu.add(Box.createHorizontalStrut(16), gbdata);

    addComponentsToLeftPart(this.menu, gbdata);
    this.menu.add(this.labelWarningNoGraphwiz, gbdata);

    this.renderedPanel.add(menu, BorderLayout.NORTH);
    this.renderedPanel.add(this.renderedScrollPane, BorderLayout.CENTER);

    this.editorPanel = new JPanel(new BorderLayout());
    this.editorPanel.add(scrollPane, BorderLayout.CENTER);

    this.mainPanel.add(this.editorPanel);
    this.mainPanel.add(this.renderedPanel);

    this.mainPanel.setResizeWeight(0.0d);
    this.mainPanel.setOneTouchExpandable(true);

    this.title = new TabTitle(context, this, file);

    this.editor.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(@Nonnull final DocumentEvent e) {
        if (!ignoreChange) {
          title.setChanged(true);
          backup();
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

    loadContent(file);

    this.editor.discardAllEdits();

    updateGraphvizLabelVisibility();

    this.hideTextPanel();

    this.eventChain = eventProcessor
        .publishOn(MainFrame.REACTOR_SCHEDULER)
        .buffer(Duration.ofSeconds(DELAY_AUTOREFRESH_SECONDS))
        .filter(x -> !x.isEmpty() && this.autoRefresh.isSelected())
        .subscribe(x -> SwingUtilities.invokeLater(() -> {
          final String txt = editor.getText();
          final LastRendered lastRendered = this.lastSuccessfullyRenderedText;
          if ((lastRendered == null || !txt.equals(lastRendered.editorText)) &&
              isSyntaxCorrect(txt)) {
            startRenderScript();
          }
        }));

  }

  @Nonnull
  @MustNotContainNull
  @Override
  public List<TabExporter> findExporters() {
    final List<TabExporter> exporters = new ArrayList<>();
    if (this.isExportImageAsFileAllowed()) {
      final ResourceBundle bundle = SrI18n.getInstance().findBundle();
      exporters.add(new TabExporter() {
        private final String name = bundle.getString("mainMenu.itemFile.itemExport.Image");

        @Nonnull
        @Override
        public JMenuItem makeMenuItem() {
          final JMenuItem item = new JMenuItem(name);
          item.addActionListener(a -> {
            exportAsFile();
          });
          return item;
        }

        @Nonnull
        @Override
        public String getTitle() {
          return this.name;
        }
      });
    }
    return exporters;
  }

  @Override
  public boolean isSelectCommandAllowed(@Nonnull final SelectCommand command) {
    return this.isTextEditorVisible();
  }

  @Override
  public void doSelectCommand(@Nonnull final SelectCommand command) {
    if (this.isTextEditorVisible()) {
      switch (command) {
        case SELECT_ALL: {
          this.editor.selectAll();
        }
        break;
        case SELECT_NONE: {
          this.editor.select(0, 0);
        }
        break;
      }
    }
  }

  @Override
  public void doZoomReset() {
    if (this.isTextEditorVisible()) {
      this.editor.doZoomReset();
    } else {
      this.scaleLabel.doZoomReset();
    }
  }

  @Override
  public void doZoomOut() {
    if (this.isTextEditorVisible()) {
      this.editor.doZoomOut();
    } else {
      this.scaleLabel.doZoomOut();
    }
  }

  @Override
  public void doZoomIn() {
    if (this.isTextEditorVisible()) {
      this.editor.doZoomIn();
    } else {
      this.scaleLabel.doZoomIn();
    }
  }

  @Override
  public boolean showSearchPane(@Nonnull final JPanel searchPanel) {
    this.editorPanel.add(searchPanel, BorderLayout.NORTH);
    if (this.mainPanel.getDividerLocation() < 96) {
      this.mainPanel.setDividerLocation(96);
    }
    return true;
  }

  protected void addCustomComponents(@Nonnull final JPanel panel,
                                     @Nonnull final GridBagConstraints gbdata) {

  }

  protected boolean isCopyScriptToClipboardAllowed() {
    return true;
  }

  protected boolean isPageAllowed() {
    return true;
  }

  protected boolean isPrintImageAllowed() {
    return true;
  }

  protected boolean isCopyAsAscIIImageInClipboardAllowed() {
    return this.getAllowedExportTypes().contains(ExportType.ASC);
  }

  protected boolean isCopyImageToClipboardAllowed() {
    return this.getAllowedExportTypes().contains(ExportType.PNG);
  }

  protected boolean isExportImageAsFileAllowed() {
    return this.getAllowedExportTypes().contains(ExportType.ASC);
  }

  @Nonnull
  protected abstract String getSyntaxEditingStyle();

  protected abstract void doPutMapping(@Nonnull AbstractTokenMakerFactory f);

  protected void addComponentsToLeftPart(@Nonnull final JPanel menuPanel,
                                         @Nonnull final GridBagConstraints constraints) {

  }

  @Nonnull
  protected JLabel makeLinkLabel(@Nonnull final String text, @Nonnull final String uri,
                                 @Nonnull final String toolTip, @Nonnull final Icon icon) {
    return this.makeLinkLabel(text, () -> {
      try {
        UiUtils.browseURI(new URI(uri), false);
      } catch (URISyntaxException ex) {
        logger.error("Can't open URI", ex);
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
      logger.warn("Detected exception in syntax check : " + ex.getMessage());
    }
    return result;
  }

  @Nullable
  @Override
  public MultiFileContainer.FileItem makeFileItem() throws IOException {
    final byte [] content = this.editor.getText().getBytes(StandardCharsets.UTF_8);
    final String caretPosition = Integer.toString(this.editor.getCaretPosition());

    return new MultiFileContainer.FileItem(this.getTabTitle().isChanged(), caretPosition, this.currentTextFile.get()
        .getFile(), null, content, this.editor.serializeEditHistory(5));
  }

  @Override
  public void restoreFromFileItem(@Nonnull MultiFileContainer.FileItem fileItem)
      throws IOException {
    this.getTabTitle().setAssociatedFile(fileItem.getFile());
    if (fileItem.getCurrent() != null) {
      final String content = new String(fileItem.getCurrent(), StandardCharsets.UTF_8);
      this.editor.setText(content);
    }

    this.editor.deserializeEditHistory(fileItem.getHistory());
    this.title.setChanged(fileItem.isChanged());

    final String position = fileItem.getPosition();
    if (!position.trim().isEmpty()) {
      try {
        final int caretPosition = Integer.parseInt(position.trim());
        this.editor.setCaretPosition(caretPosition);
      } catch (Exception ex) {
        // ignore
      }
    }
  }


  public void hideTextPanel() {
    SwingUtilities.invokeLater(() -> mainPanel.setDividerLocation(0));

    this.editor.addKeyListener(new KeyAdapter() {
      @Override
      public void keyTyped(final @Nonnull KeyEvent e) {
        try {
          eventProcessor.onNext(true);
        } catch (Exception ex) {
          logger.info("Exception on next event: " + ex.getMessage());
        }
      }
    });
  }

  private void setMenuItemsEnable(final boolean enable) {
    for (final Component c : this.menu.getComponents()) {
      if (!(c instanceof JLabel)) {
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
  protected JLabel makeLinkLabel(@Nonnull final String text, @Nonnull final Runnable onClick,
                                 @Nonnull final String toolTip, @Nonnull final Icon icon) {
    final JLabel result = new JLabel(text, icon, JLabel.RIGHT);
    final Font font = result.getFont().deriveFont(Font.BOLD);
    final Map attributes = font.getAttributes();
    attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
    result.setFont(font.deriveFont(attributes));
    result.setForeground(
        UiUtils.figureOutThatDarkTheme() ? Color.YELLOW.darker() : Color.BLUE.brighter());
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
    OptionFlags.getInstance().setDotExecutable(this.mindMapPanelConfig.getOptionalProperty(
        AdditionalPreferences.PROPERTY_PLANTUML_DOT_PATH, null));
  }

  @Nonnull
  protected Set<ExportType> getAllowedExportTypes() {
    return DEFAULT_EXPORT_TYPES;
  }

  @Nullable
  protected byte[] makeCustomExport(
      @Nonnull final ExportType exportType,
      final int pageIndex,
      @Nonnull final String text
  ) throws Exception {
    return null;
  }

  private void exportAsFile() {
    final JFileChooser fileChooser = new JFileChooser(
        lastExportedFile == null ? this.getTabTitle().getAssociatedFile().getParentFile() :
            lastExportedFile);
    fileChooser.setAcceptAllFileFilterUsed(false);

    final FileFilter fileFiterSVG = new FileFilter() {
      @Override
      public boolean accept(@Nonnull final File f) {
        return f.isDirectory() || f.getName().toLowerCase(Locale.ENGLISH).endsWith(".svg");
      }

      @Nonnull
      @Override
      public String getDescription() {
        return bundle.getString("editorAbstractPlUml.fileFilter.svg.description");
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
        return bundle.getString("editorAbstractPlUml.fileFilter.png.description");
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
        return bundle.getString("editorAbstractPlUml.fileFilter.latex.description");
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
        return bundle.getString("editorAbstractPlUml.fileFilter.ascii.description");
      }
    };

    fileChooser.setApproveButtonText(bundle.getString("editorAbstractPlUml.fileChooser.approve"));
    fileChooser.setDialogTitle(bundle.getString("editorAbstractPlUml.fileChooser.title"));
    fileChooser.setMultiSelectionEnabled(false);

    final Set<ExportType> allowedExportTypes = this.getAllowedExportTypes();

    if (allowedExportTypes.contains(ExportType.PNG)) {
      fileChooser.addChoosableFileFilter(fileFiterPNG);
    }
    if (allowedExportTypes.contains(ExportType.SVG)) {
      fileChooser.addChoosableFileFilter(fileFiterSVG);
    }
    if (allowedExportTypes.contains(ExportType.LATEX)) {
      fileChooser.addChoosableFileFilter(fileFiterLTX);
    }
    if (allowedExportTypes.contains(ExportType.ASC)) {
      fileChooser.addChoosableFileFilter(fileFiterASC);
    }

    if (fileChooser.showSaveDialog(this.mainPanel) == JFileChooser.APPROVE_OPTION) {
      lastExportedFile = fileChooser.getSelectedFile();

      final FileFilter fileFilter = fileChooser.getFileFilter();

      final String textToRender = this.editor.getText();

      final FileFormatOption option;

      final String ext;

      final ExportType exportType;
      if (fileFilter == fileFiterSVG) {
        exportType = ExportType.SVG;
        option = new FileFormatOption(FileFormat.SVG);
        ext = ".svg";
      } else if (fileFilter == fileFiterPNG) {
        exportType = ExportType.PNG;
        option = new FileFormatOption(FileFormat.PNG);
        ext = ".png";
      } else if (fileFilter == fileFiterLTX) {
        exportType = ExportType.LATEX;
        option = new FileFormatOption(FileFormat.LATEX);
        ext = ".tex";
      } else if (fileFilter == fileFiterASC) {
        exportType = ExportType.ASC;
        option = new FileFormatOption(FileFormat.ATXT);
        ext = ".txt";
      } else {
        throw new Error("Unexpected situation");
      }

      if (!lastExportedFile.getName().contains(".") || !lastExportedFile.getName().endsWith(ext)) {
        lastExportedFile = new File(lastExportedFile.getParent(), lastExportedFile.getName() + ext);
      }

      final int pageIndex = this.pageNumberToRender - 1;
      if (pageIndex >= 0) {
        final byte[] bytearray;
        try {
          if (this.isCustomRendering()) {
            bytearray = Assertions.assertNotNull("Unexpected NULL result",
                this.makeCustomExport(exportType, pageIndex, textToRender));
          } else {
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream(131072);
            final SourceStringReader reader = new SourceStringReader(textToRender);
            reader.outputImage(buffer, pageIndex, option);
            bytearray = buffer.toByteArray();
          }
        } catch (Exception ex) {
          logger.error("Can't render script as image", ex);
          JOptionPane
              .showMessageDialog(this.mainPanel,
                  bundle.getString("editorAbstractPlUml.renderError.text"),
                  bundle.getString("editorAbstractPlUml.renderError.title"),
                  JOptionPane.ERROR_MESSAGE);
          return;
        }

        try {
          if (bytearray.length < 10000 && new String(bytearray, StandardCharsets.UTF_8)
              .contains("java.lang.UnsupportedOperationException:")) {
            throw new IOException("Detected exception footstep in generated file!");
          }
          FileUtils.writeByteArrayToFile(lastExportedFile, bytearray);
          logger.info("Exported script as file : " + lastExportedFile);
        } catch (IOException ex) {
          logger.error("Can't export script as image", ex);
          JOptionPane.showMessageDialog(this.mainPanel,
              bundle.getString("editorAbstractPlUml.renderError.cantexport.text"),
              bundle.getString("editorAbstractPlUml.renderError.title"),
              JOptionPane.ERROR_MESSAGE);
        }
      } else {
        logger.warn("Page number is <= 0");
        JOptionPane
            .showMessageDialog(this.mainPanel,
                bundle.getString("editorAbstractPlUml.renderWarning.text"),
                bundle.getString("editorAbstractPlUml.renderWarning.title"),
                JOptionPane.WARNING_MESSAGE);
      }
    }

  }

  @Nullable
  @Override
  protected String getContentAsText() {
    return this.editor.getText();
  }

  @Override
  public void focusToEditor(final int line) {
    SwingUtilities.invokeLater(imageComponent::requestFocusInWindow);
  }

  @Override
  public boolean isRedo() {
    return this.editor.canRedo();
  }

  @Override
  public boolean isUndo() {
    return this.editor.canUndo();
  }

  private boolean isTextEditorVisible() {
    return this.mainPanel.getDividerLocation() > 4;
  }

  @Override
  public boolean redo() {
    if (this.editor.canRedo() && isTextEditorVisible()) {
      try {
        this.editor.redoLastAction();
      } catch (final CannotRedoException ex) {
        logger.warn("Can't make redo in plantUML editor : " + ex.getMessage());
      } finally {
        SwingUtilities.invokeLater(() -> {
          backup();
          startRenderScript();
        });
      }
    }
    return this.editor.canRedo();
  }

  @Override
  public boolean undo() {
    if (this.editor.canUndo() && isTextEditorVisible()) {
      try {
        this.editor.undoLastAction();
      } catch (final CannotUndoException ex) {
        logger.warn("Can't make undo in plantUML editor : " + ex.getMessage());
      } finally {
        SwingUtilities.invokeLater(() -> {
          backup();
          startRenderScript();
        });
      }
    }
    return this.editor.canUndo();
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
  public boolean isSavable() {
    return true;
  }

  @Override
  public void doUpdateConfiguration() {
    initPlantUml();
    updateGraphvizLabelVisibility();
    this.imageComponent.updateConfig(this.mindMapPanelConfig);
    this.editor.updateConfig(this.mindMapPanelConfig);

    final SyntaxScheme scheme = this.editor.getSyntaxScheme();
    final Font editorFont = this.editor.getFont();
    if (editorFont != null) {
      scheme.getStyle(Token.RESERVED_WORD).font = editorFont.deriveFont(Font.BOLD);
      scheme.getStyle(Token.IDENTIFIER).font = editorFont.deriveFont(Font.ITALIC);
    }

    this.editor.repaint();
  }

  protected void onEditorInitialSetText(@Nonnull final String editorText) {

  }

  @Override
  protected void onLoadContent(@Nonnull final TextFile textFile) throws IOException {
    this.ignoreChange = true;
    try {
      final String content = textFile.readContentAsUtf8();
      this.editor.setText(content);
      this.editor.setCaretPosition(0);
      this.onEditorInitialSetText(content);
      startRenderScript();
    } finally {
      this.ignoreChange = false;
    }

    this.editor.discardAllEdits();
    this.title.setChanged(false);

    this.mainPanel.revalidate();
    this.mainPanel.repaint();
  }

  @Nonnull
  protected String getEditorTextForSave() {
    return this.editor.getText();
  }

  @Override
  public boolean saveDocument() throws IOException {
    boolean result = false;
    if (this.title.isChanged()) {
      final TextFile textFile = this.currentTextFile.get();
      if (this.isOverwriteAllowed(textFile)) {
        File file = this.title.getAssociatedFile();
        if (file == null) {
          return this.saveDocumentAs();
        }

        final byte[] content = this.getEditorTextForSave().getBytes(StandardCharsets.UTF_8);
        FileUtils.writeByteArrayToFile(file, content);
        this.currentTextFile.set(new TextFile(file, false, content));
        this.title.setChanged(false);
        this.deleteBackup();
        result = true;
        startRenderScript();
      }
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

  @Nonnull
  protected String preprocessEditorText(@Nonnull final String text) {
    return text;
  }

  protected void resetLastRendered() {
    this.lastSuccessfullyRenderedText = null;
  }

  protected boolean isCustomRendering() {
    return false;
  }

  protected void doCustomRendering(
      @Nonnull final String text,
      final int pageIndex,
      @Nonnull final AtomicReference<BufferedImage> renderedImage,
      @Nonnull final AtomicReference<Exception> error
  ) {

  }

  protected final void startRenderScript() {
    try {
      final String editorText = this.editor.getText();
      final String theText = this.preprocessEditorText(editorText);

      final boolean customRendering = this.isCustomRendering();

      final SourceStringReader reader;
      final int totalPages;
      final int imageIndex;

      if (customRendering) {
        reader = null;
        totalPages = countNewPages(theText);
      } else {
        reader = new SourceStringReader(theText, "UTF-8");
        totalPages = Math.max(countNewPages(theText), reader.getBlocks().size());
      }
      imageIndex = Math.max(1, Math.min(this.pageNumberToRender, totalPages));

      if (imageIndex != this.pageNumberToRender) {
        this.pageNumberToRender = imageIndex;
      }

      final LastRendered currentText = new LastRendered(imageIndex, editorText);

      if (!currentText.equals(this.lastSuccessfullyRenderedText)) {
        if (this.labelPageNumber != null) {
          updatePageNumberInfo(currentText.page, totalPages);
        }

        final AtomicInteger dividerLocation =
            new AtomicInteger(Math.max(0, this.mainPanel.getDividerLocation()));
        final PropertyChangeListener dividerListener =
            evt -> dividerLocation.set(Math.max(0, mainPanel.getDividerLocation()));

        this.mainPanel
            .addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, dividerListener);

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

            if (customRendering) {
              this.doCustomRendering(currentText.editorText, imageIndex - 1, generatedImage,
                  detectedError);
            } else {
              final ByteArrayOutputStream buffer = new ByteArrayOutputStream(131072);
              try {
                final DiagramDescription description = reader.outputImage(buffer, imageIndex - 1,
                    new FileFormatOption(FileFormat.PNG, false));
                generatedImage.set(ImageIO.read(new ByteArrayInputStream(buffer.toByteArray())));
              } catch (Exception ex) {
                detectedError.set(ex);
              }
            }

            SwingUtilities.invokeLater(() -> {
              mainPanel.removePropertyChangeListener(dividerListener);

              final Exception error = detectedError.get();
              if (error == null) {
                lastSuccessfullyRenderedText = currentText;
                imageComponent.setImage(generatedImage.get(), false);
                renderedScrollPane.revalidate();
                renderedPanel.remove(progressLabel);
                renderedPanel.add(renderedScrollPane, BorderLayout.CENTER);
                setMenuItemsEnable(true);
              } else {
                final JLabel errorLabel = new JLabel(
                    "<html><h1>ERROR: " + escapeHtml3(error.getMessage()) + "</h1></html>",
                    JLabel.CENTER);
                errorLabel.setName("ERROR_LABEL");
                renderedPanel.remove(progressLabel);
                renderedPanel.add(errorLabel, BorderLayout.CENTER);
              }

              mainPanel.setDividerLocation(dividerLocation.get());
            });

          } finally {
            this.lastSuccessfullyRenderedText = null;
            BigLoaderIconAnimationConroller.getInstance().unregisterLabel(progressLabel);
          }
        });
      }
    } catch (final Exception ex) {
      logger.error("Error of script rendering:" + ex);
      SwingUtilities.invokeLater(() -> {
        final JLabel errorLabel =
            new JLabel("<html><h1>ERROR: " + escapeHtml3(ex.getMessage()) + "</h1></html>",
                JLabel.CENTER);
        errorLabel.setName("ERROR_LABEL");
        renderedPanel.remove(progressLabel);
        renderedPanel.add(errorLabel, BorderLayout.CENTER);
        renderedPanel.revalidate();
        renderedPanel.repaint();
      });
    }
  }

  @Nullable
  @UiThread
  private String renderPageAsAscII() {
    final String theText = this.preprocessEditorText(this.editor.getText());

    final SourceStringReader reader = new SourceStringReader(theText, "UTF-8");
    final int totalPages = Math.max(countNewPages(theText), reader.getBlocks().size());

    final int imageIndex = Math.max(1, Math.min(this.pageNumberToRender, totalPages));

    final ByteArrayOutputStream utfBuffer = new ByteArrayOutputStream();

    try {
      final DiagramDescription description = reader
          .outputImage(utfBuffer, imageIndex - 1, new FileFormatOption(FileFormat.ATXT, false));
      final String result = new String(utfBuffer.toByteArray(), StandardCharsets.UTF_8);
      if (result.contains("java.lang.UnsupportedOperationException: ATXT")) {
        throw new UnsupportedOperationException("ATXT is not supported for the diagram");
      }
      return result;
    } catch (Exception ex) {
      logger.error("Can't export ASCII image", ex);
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
      this.labelPageNumber.setText(
          "<html><b>&nbsp;&nbsp;" + pageNumber + '/' + totalPages + "&nbsp;&nbsp;</b><html>");
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

      int maxPos = this.editor.getCaret().getMark() == this.editor.getCaret().getDot() ?
          this.editor.getCaretPosition() : this.editor.getSelectionStart();

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
  public boolean findNext(@Nonnull final Pattern pattern,
                          @Nonnull final FindTextScopeProvider provider) {
    return searchSubstring(pattern, true);
  }

  @Override
  public boolean findPrev(@Nonnull final Pattern pattern,
                          @Nonnull final FindTextScopeProvider provider) {
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
      logger.warn("Can't get data from clipboard : " + ex.getMessage()); //NOI18N
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

  public enum ExportType {
    SVG,
    PNG,
    LATEX,
    ASC
  }

  protected static final class LastRendered {

    private final int page;
    private final String editorText;

    public LastRendered(final int page, @Nonnull final String editorText) {
      this.page = page;
      this.editorText = editorText;
    }

    @Override
    public int hashCode() {
      return this.editorText.hashCode() ^ this.page;
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
      return Objects.equals(this.editorText, other.editorText);
    }
  }

}
