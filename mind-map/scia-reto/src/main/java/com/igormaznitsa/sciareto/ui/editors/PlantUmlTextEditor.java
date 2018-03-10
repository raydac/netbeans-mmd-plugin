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
import com.igormaznitsa.sciareto.preferences.PreferencesManager;
import com.igormaznitsa.sciareto.preferences.SpecificKeys;
import com.igormaznitsa.sciareto.ui.DialogProviderManager;
import com.igormaznitsa.sciareto.ui.FindTextScopeProvider;
import com.igormaznitsa.sciareto.ui.SystemUtils;
import com.igormaznitsa.sciareto.ui.tabs.TabTitle;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.RUndoManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.Segment;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PlantUmlTextEditor extends AbstractEditor {

  private static final Logger LOGGER = LoggerFactory.getLogger(PlantUmlTextEditor.class);

  public static final Font DEFAULT_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 14);

  private final RSyntaxTextArea editor;
  private final TabTitle title;

  private boolean ignoreChange;

  private final RUndoManager undoManager;

  private final JSplitPane mainPanel;

  public static final Set<String> SUPPORTED_EXTENSIONS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("pu","puml","plantml")));

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

  @Override
  @Nonnull
  public FileFilter getFileFilter() {
    return SRC_FILE_FILTER;
  }

  public PlantUmlTextEditor(@Nonnull final Context context, @Nullable File file) throws IOException {
    super();
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
    this.mainPanel.setDividerLocation(0.8d);
    this.mainPanel.setResizeWeight(1.0d);
    this.mainPanel.setOneTouchExpandable(true);

    final RTextScrollPane scrollPane = new RTextScrollPane(this.editor, true);
    this.mainPanel.add(new JPanel());
    this.mainPanel.add(scrollPane);

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
      }
    }
    finally {
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
        file = DialogProviderManager.getInstance().getDialogProvider().msgSaveFileDialog(null,"sources-editor", "Save sources", null, true, getFileFilter(), "Save");
        if (file == null) {
          return result;
        }
      }
      SystemUtils.saveUTFText(file, this.editor.getText());
      this.title.setChanged(false);
      result = true;
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
      if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
        text = clipboard.getData(DataFlavor.stringFlavor).toString();
      }
    }
    catch (Exception ex) {
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
    return clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor);
  }

}
