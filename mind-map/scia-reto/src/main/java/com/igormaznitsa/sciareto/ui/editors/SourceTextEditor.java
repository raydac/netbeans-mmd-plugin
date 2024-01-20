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

import static java.util.Arrays.asList;

import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import com.igormaznitsa.sciareto.Context;
import com.igormaznitsa.sciareto.SciaRetoStarter;
import com.igormaznitsa.sciareto.ui.DialogProviderManager;
import com.igormaznitsa.sciareto.ui.FindTextScopeProvider;
import com.igormaznitsa.sciareto.ui.SrI18n;
import com.igormaznitsa.sciareto.ui.UiUtils;
import com.igormaznitsa.sciareto.ui.misc.MultiFileContainer;
import com.igormaznitsa.sciareto.ui.tabs.TabTitle;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.filechooser.FileFilter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.RUndoManager;

public final class SourceTextEditor extends AbstractTextEditor {

  public static final Set<String> SUPPORTED_EXTENSIONS;
  private static final Map<String, List<String>> SRC_EXTENSIONS =
      new HashMap<>();
  private static final Map<String, String> MAP_EXTENSION2TYPE = new HashMap<>();
  private static final List<FormatType> SUPPORTED_FORMATS;

  static {
    SRC_EXTENSIONS.put(SyntaxConstants.SYNTAX_STYLE_GO, List.of("go")); //NOI18N
    SRC_EXTENSIONS.put(SyntaxConstants.SYNTAX_STYLE_ACTIONSCRIPT, List.of("as")); //NOI18N
    SRC_EXTENSIONS.put(SyntaxConstants.SYNTAX_STYLE_ASSEMBLER_X86, List.of("asm")); //NOI18N
    SRC_EXTENSIONS.put(SyntaxConstants.SYNTAX_STYLE_BBCODE, List.of("bbcode")); //NOI18N
    SRC_EXTENSIONS.put(SyntaxConstants.SYNTAX_STYLE_C, asList("c", "h")); //NOI18N
    SRC_EXTENSIONS.put(SyntaxConstants.SYNTAX_STYLE_CLOJURE,
        asList("clj", "cljs", "cljc", "edn")); //NOI18N
    SRC_EXTENSIONS.put(SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS,
        asList("cc", "cpp", "cxx", "c++", "hpp")); //NOI18N
    SRC_EXTENSIONS.put(SyntaxConstants.SYNTAX_STYLE_CSHARP, List.of("cs")); //NOI18N
    SRC_EXTENSIONS.put(SyntaxConstants.SYNTAX_STYLE_CSS, List.of("css")); //NOI18N
    SRC_EXTENSIONS.put(SyntaxConstants.SYNTAX_STYLE_D, asList("d", "dd")); //NOI18N
    SRC_EXTENSIONS.put(SyntaxConstants.SYNTAX_STYLE_DART, List.of("dart")); //NOI18N
    SRC_EXTENSIONS.put(SyntaxConstants.SYNTAX_STYLE_DELPHI, List.of("pas")); //NOI18N
    SRC_EXTENSIONS.put(SyntaxConstants.SYNTAX_STYLE_DOCKERFILE,
        List.of("*dockerfile")); //NOI18N
    SRC_EXTENSIONS.put(SyntaxConstants.SYNTAX_STYLE_DTD, List.of("dtd")); //NOI18N
    SRC_EXTENSIONS.put(SyntaxConstants.SYNTAX_STYLE_FORTRAN,
        asList("f", "for", "f90", "f95")); //NOI18N
    SRC_EXTENSIONS.put(SyntaxConstants.SYNTAX_STYLE_GROOVY, List.of("groovy")); //NOI18N
    SRC_EXTENSIONS.put(SyntaxConstants.SYNTAX_STYLE_HOSTS,
        asList("*hosts", "*hosts.txt")); //NOI18N
    SRC_EXTENSIONS.put(SyntaxConstants.SYNTAX_STYLE_HTML, asList("htm", "html")); //NOI18N
    SRC_EXTENSIONS.put(SyntaxConstants.SYNTAX_STYLE_HTACCESS, List.of("htaccess")); //NOI18N
    SRC_EXTENSIONS.put(SyntaxConstants.SYNTAX_STYLE_INI, List.of("ini")); //NOI18N
    SRC_EXTENSIONS.put(SyntaxConstants.SYNTAX_STYLE_JAVA, List.of("java")); //NOI18N
    SRC_EXTENSIONS.put(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT, List.of("js")); //NOI18N
    SRC_EXTENSIONS.put(SyntaxConstants.SYNTAX_STYLE_JSON, List.of("json")); //NOI18N
    SRC_EXTENSIONS.put(SyntaxConstants.SYNTAX_STYLE_JSP, List.of("jsp")); //NOI18N
    SRC_EXTENSIONS.put(SyntaxConstants.SYNTAX_STYLE_KOTLIN, asList("kt", "kts")); //NOI18N
    SRC_EXTENSIONS.put(SyntaxConstants.SYNTAX_STYLE_LATEX, List.of("tex")); //NOI18N
    SRC_EXTENSIONS.put(SyntaxConstants.SYNTAX_STYLE_LESS, List.of("less")); //NOI18N
    SRC_EXTENSIONS.put(SyntaxConstants.SYNTAX_STYLE_LISP, asList("lisp", "lsp")); //NOI18N
    SRC_EXTENSIONS.put(SyntaxConstants.SYNTAX_STYLE_LUA, List.of("lua")); //NOI18N
    SRC_EXTENSIONS.put(SyntaxConstants.SYNTAX_STYLE_MAKEFILE, List.of("makefile")); //NOI18N
    SRC_EXTENSIONS.put(SyntaxConstants.SYNTAX_STYLE_MXML, List.of("mxml")); //NOI18N
    SRC_EXTENSIONS.put(SyntaxConstants.SYNTAX_STYLE_NSIS, List.of("nsi")); //NOI18N
    SRC_EXTENSIONS.put(SyntaxConstants.SYNTAX_STYLE_PERL, List.of("pl")); //NOI18N
    SRC_EXTENSIONS.put(SyntaxConstants.SYNTAX_STYLE_PHP, List.of("php")); //NOI18N
    SRC_EXTENSIONS.put(SyntaxConstants.SYNTAX_STYLE_PROPERTIES_FILE,
        List.of("properties")); //NOI18N
    SRC_EXTENSIONS.put(SyntaxConstants.SYNTAX_STYLE_PYTHON, List.of("py")); //NOI18N
    SRC_EXTENSIONS.put(SyntaxConstants.SYNTAX_STYLE_RUBY, List.of("rb")); //NOI18N
    SRC_EXTENSIONS.put(SyntaxConstants.SYNTAX_STYLE_SAS, List.of("sas")); //NOI18N
    SRC_EXTENSIONS.put(SyntaxConstants.SYNTAX_STYLE_SCALA, asList("scala", "sc")); //NOI18N
    SRC_EXTENSIONS.put(SyntaxConstants.SYNTAX_STYLE_SQL, List.of("sql")); //NOI18N
    SRC_EXTENSIONS.put(SyntaxConstants.SYNTAX_STYLE_TCL, List.of("tcl")); //NOI18N
    SRC_EXTENSIONS.put(SyntaxConstants.SYNTAX_STYLE_TYPESCRIPT, List.of("ts")); //NOI18N
    SRC_EXTENSIONS.put(SyntaxConstants.SYNTAX_STYLE_UNIX_SHELL, List.of("sh")); //NOI18N
    SRC_EXTENSIONS.put(SyntaxConstants.SYNTAX_STYLE_VISUAL_BASIC, List.of("vb")); //NOI18N
    SRC_EXTENSIONS.put(SyntaxConstants.SYNTAX_STYLE_WINDOWS_BATCH,
        asList("bat", "cmd")); //NOI18N
    SRC_EXTENSIONS.put(SyntaxConstants.SYNTAX_STYLE_XML, List.of("xml")); //NOI18N
    SRC_EXTENSIONS.put(SyntaxConstants.SYNTAX_STYLE_YAML, asList("yaml", "yml")); //NOI18N

    final Set<String> allEtensinsions = new HashSet<>();

    SUPPORTED_FORMATS = new ArrayList<>();

    SRC_EXTENSIONS.forEach((type, value) -> {
      SUPPORTED_FORMATS.add(new FormatType(type));
      value.stream().peek((s) -> {
        if (MAP_EXTENSION2TYPE.put(s, type) != null) {
          throw new Error("Detected duplicated extension : " + s); //NOI18N
        }
      }).forEachOrdered(allEtensinsions::add);
    });
    SUPPORTED_EXTENSIONS = Collections.unmodifiableSet(allEtensinsions);

    Collections.sort(SUPPORTED_FORMATS);
    SUPPORTED_FORMATS.add(0, new FormatType(SyntaxConstants.SYNTAX_STYLE_NONE));
  }

  private final ScalableRsyntaxTextArea editor;
  private final TabTitle title;
  private final RUndoManager undoManager;
  private final JPanel mainPanel;
  private final JLabel labelWordWrap;
  private final FileFilter fileFilter = makeFileFilter();
  private boolean ignoreChange;
  private String originalText = "";
  private Wrap currentWrap;

  public SourceTextEditor(@Nonnull final Context context, @Nonnull File file, final int line,
                          final boolean noSyntax) throws IOException {
    super();
    this.editor = new ScalableRsyntaxTextArea(this.mindMapPanelConfig);
    this.editor.setPopupMenu(null);

    final String syntaxType;

    final String lowerCaseName = file.getName().toLowerCase(Locale.ENGLISH);
    String found = MAP_EXTENSION2TYPE.get(FilenameUtils.getExtension(lowerCaseName));
    if (found == null) {
      found = MAP_EXTENSION2TYPE.get("*" + lowerCaseName);
    }
    syntaxType = found;

    this.editor.setSyntaxEditingStyle(
        noSyntax || syntaxType == null ? SyntaxConstants.SYNTAX_STYLE_NONE : syntaxType);
    this.editor.setAntiAliasingEnabled(true);
    this.editor.setBracketMatchingEnabled(true);
    this.editor.setCodeFoldingEnabled(true);
    this.editor.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(@Nonnull final KeyEvent e) {
        if (!e.isConsumed() && e.getModifiers() == 0 && e.getKeyCode() == KeyEvent.VK_ESCAPE) {
          e.consume();
          context.hideFindTextPane();
        }
      }
    });

    this.editor.getCaret().setSelectionVisible(true);
    this.editor.addHyperlinkListener((HyperlinkEvent e) -> {
      if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
        try {
          UiUtils.browseURI(e.getURL().toURI(), false);
        } catch (URISyntaxException ex) {
          logger.error("Can't browse link: " + e.getURL());
        }
      }
    });

    this.mainPanel = new JPanel(new BorderLayout());

    final RTextScrollPane scrollPane = new RTextScrollPane(this.editor, true);
    this.mainPanel.add(scrollPane, BorderLayout.CENTER);

    final JPanel status = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    final JComboBox<FormatType> formatTypeCombo =
        new JComboBox<>(new DefaultComboBoxModel<>(
            SUPPORTED_FORMATS.toArray(new FormatType[0])));
    formatTypeCombo.setSelectedItem(new FormatType(this.editor.getSyntaxEditingStyle()));

    formatTypeCombo.addItemListener(item -> {
      if (item.getStateChange() == ItemEvent.SELECTED) {
        this.editor.setSyntaxEditingStyle(((FormatType) item.getItem()).getType());
        this.editor.revalidate();
        this.editor.repaint();
      }
    });

    final JLabel labelCursor = new JLabel("");

    this.editor.addCaretListener((CaretEvent e) -> labelCursor.setText(String.format("%d:%d", this.editor.getCaretLineNumber() + 1,
        this.editor.getCaretOffsetFromLineStart() + 1)));

    this.labelWordWrap = new JLabel();
    this.currentWrap = Wrap.NO_WRAP;
    this.labelWordWrap.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    this.labelWordWrap.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(@Nonnull final MouseEvent e) {
        if (!e.isConsumed() && !e.isPopupTrigger()) {
          switch (currentWrap) {
            case NO_WRAP:
              currentWrap = Wrap.LINE_WRAP;
              break;
            case LINE_WRAP:
              currentWrap = Wrap.NO_WRAP;
              break;
          }
          updateWrapState();
        }
      }
    });

    updateWrapState();

    status.add(Box.createHorizontalGlue());
    status.add(labelCursor);
    status.add(Box.createHorizontalStrut(16));
    status.add(new JLabel("|"));
    status.add(Box.createHorizontalStrut(16));
    status.add(this.labelWordWrap);
    status.add(Box.createHorizontalStrut(16));
    status.add(new JLabel("|"));
    status.add(Box.createHorizontalStrut(16));
    status.add(new JLabel("Syntax:"));
    status.add(formatTypeCombo);

    this.mainPanel.add(status, BorderLayout.SOUTH);

    this.title = new TabTitle(context, this, file);

    this.editor.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(@Nonnull final DocumentEvent e) {
        if (!ignoreChange) {
          title.setChanged(!originalText.equals(editor.getText()));
          backup();
        }
        context.notifyUpdateRedoUndo();
      }

      @Override
      public void removeUpdate(@Nonnull final DocumentEvent e) {
        if (!ignoreChange) {
          title.setChanged(!originalText.equals(editor.getText()));
          backup();
        }
        context.notifyUpdateRedoUndo();
      }

      @Override
      public void changedUpdate(@Nonnull final DocumentEvent e) {
        if (!ignoreChange) {
          title.setChanged(!originalText.equals(editor.getText()));
          backup();
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

    Arrays.stream(this.editor.getCaretListeners())
        .forEach(listener -> listener.caretUpdate(new CaretEvent(this.editor) {
          @Override
          public int getDot() {
            return editor.getCaretPosition();
          }

          @Override
          public int getMark() {
            return editor.getCaretPosition();
          }
        }));
    gotoLine(line);
  }

    @Override
    public boolean isSelectCommandAllowed(@Nonnull final SelectCommand command) {
        return true;
    }

    @Override
    public void doSelectCommand(@Nonnull final SelectCommand command) {
        switch(command) {
            case SELECT_ALL: this.editor.selectAll();break;
            case SELECT_NONE: this.editor.select(0, 0);break;
        }
    }
  
  @Nonnull
  public static FileFilter makeFileFilter() {
    return new FileFilter() {

      @Override
      public boolean accept(@Nonnull final File f) {
        if (f.isDirectory()) {
          return true;
        }
        return MAP_EXTENSION2TYPE.containsKey(
            FilenameUtils.getExtension(f.getName()).toLowerCase(Locale.ENGLISH));
      }

      @Override
      @Nonnull
      public String getDescription() {
        return SrI18n.getInstance().findBundle().getString("editorAbstractPlUml.fileFilter.source.description");
      }
    };
  }

  private void updateWrapState() {
    this.labelWordWrap.setText(this.currentWrap.getText());
    this.labelWordWrap.revalidate();
    this.labelWordWrap.repaint();

    switch (this.currentWrap) {
      case NO_WRAP: {
        this.editor.setLineWrap(false);
        this.editor.setWrapStyleWord(false);
      }
      break;
      case LINE_WRAP: {
        this.editor.setLineWrap(true);
        this.editor.setWrapStyleWord(true);
      }
      break;
    }
    this.editor.revalidate();
    this.editor.repaint();
  }

  @Override
  @Nonnull
  public FileFilter getFileFilter() {
    return fileFilter;
  }

  @Nullable
  @Override
  protected String getContentAsText() {
    return this.editor.getText();
  }

  @Nonnull
  @Override
  public String getDefaultExtension() {
    return "txt";
  }

  @Override
  public void doZoomReset() {
    this.editor.doZoomReset();
  }

  @Override
  public void doZoomOut() {
    this.editor.doZoomOut();
  }

  @Override
  public void doZoomIn() {
    this.editor.doZoomIn();
  }

  private void gotoLine(final int line) {
    if (line > 0) {
      try {
        this.editor.setCaretPosition(this.editor.getLineStartOffset(line - 1));
      } catch (Exception ex) {
        logger.warn("Can't focus to line : " + line);
      }
    }
  }

  @Override
  public void focusToEditor(final int line) {
    SwingUtilities.invokeLater(() -> {
      gotoLine(line);
      editor.requestFocusInWindow();
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
  public boolean isSavable() {
    return true;
  }

  @Override
  public void doUpdateConfiguration() {
    this.editor.updateConfig(this.mindMapPanelConfig);
  }

  @Override
  protected void onLoadContent(@Nonnull final TextFile textFile) throws IOException {
    this.ignoreChange = true;
    try {
      this.editor.setText(textFile.readContentAsUtf8());
      this.originalText = this.editor.getText();
      this.editor.setCaretPosition(0);
    } finally {
      this.ignoreChange = false;
    }

    this.undoManager.discardAllEdits();
    this.title.setChanged(false);

    this.mainPanel.revalidate();
    this.mainPanel.repaint();
  }

  @Override
  protected void onSelectedSaveDocumentAs(@Nonnull final File selectedFile) {
    this.currentTextFile.set(null);
  }

  @Override
  public boolean saveDocument() throws IOException {
    boolean result = false;

    final TextFile textFile = this.currentTextFile.get();
    final DialogProvider dialogProvider = DialogProviderManager.getInstance().getDialogProvider();

    if (this.title.isChanged()) {
      if (this.isOverwriteAllowed(textFile)) {
        File file = this.title.getAssociatedFile();
        if (file == null) {
          file = dialogProvider
              .msgSaveFileDialog(SciaRetoStarter.getApplicationFrame(), null, "sources-editor",
                  "Save sources", null, true, new FileFilter[] {getFileFilter()}, "Save");
          if (file == null) {
            return result;
          }
        }

        final String editorText = this.editor.getText();
        final byte[] content = editorText.getBytes(StandardCharsets.UTF_8);
        FileUtils.writeByteArrayToFile(file, content);
        this.currentTextFile.set(new TextFile(file, false, content));

        this.originalText = editorText;
        this.title.setChanged(false);
        this.deleteBackup();
        result = true;
      }
    } else {
      result = true;
    }
    return result;
  }

  @Override
  public boolean showSearchPane(@Nonnull final JPanel searchPanel) {
    this.mainPanel.add(searchPanel, BorderLayout.NORTH);
    return true;
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

  private enum Wrap {
    NO_WRAP(" No wrap "),
    LINE_WRAP("Line wrap");

    private final String text;

    Wrap(@Nonnull final String text) {
      this.text = text;
    }

    @Nonnull
    String getText() {
      return this.text;
    }
  }

  public static final class FormatType implements Comparable<FormatType> {

    private final String type;
    private final String name;

    private FormatType(@Nonnull final String type) {
      this.type = type;
      final int index = type.indexOf('/');
      this.name = index < 0 ? type : type.substring(index + 1).toUpperCase(Locale.ENGLISH);
    }

    @Nonnull
    public String getType() {
      return this.type;
    }

    @Nonnull
    @Override
    public String toString() {
      return this.name;
    }

    @Override
    public int hashCode() {
      return this.type.hashCode();
    }

    @Override
    public boolean equals(@Nullable final Object that) {
      if (that instanceof FormatType) {
        return this.type.equals(((FormatType) that).type);
      }
      return false;
    }

    @Override
    public int compareTo(@Nonnull final FormatType that) {
      return this.type.compareTo(that.type);
    }
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
    if (fileItem.getMainData() != null) {
      final String content = new String(fileItem.getMainData(), StandardCharsets.UTF_8);
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
}
