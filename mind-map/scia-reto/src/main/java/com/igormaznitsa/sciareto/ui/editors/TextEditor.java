/*
 * Copyright 2016 Igor Maznitsa.
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

import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.undo.UndoManager;
import org.apache.commons.io.FileUtils;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.sciareto.Context;
import com.igormaznitsa.sciareto.preferences.PreferencesManager;
import com.igormaznitsa.sciareto.preferences.SpecificKeys;
import com.igormaznitsa.sciareto.ui.DialogProviderManager;
import com.igormaznitsa.sciareto.ui.SystemUtils;
import com.igormaznitsa.sciareto.ui.tabs.TabTitle;
import com.igormaznitsa.sciareto.ui.FindTextScopeProvider;

public final class TextEditor extends AbstractScrollPane {

  private static final long serialVersionUID = -8551212562825517869L;

  private static final Logger LOGGER = LoggerFactory.getLogger(TextEditor.class);

  public static final Font DEFAULT_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 14);

  private final JTextArea editor;
  private final TabTitle title;

  private boolean ignoreChange;

  private final UndoManager undoManager = new UndoManager();

  public static final FileFilter TXT_FILE_FILTER = new FileFilter() {

    @Override
    public boolean accept(@Nonnull final File f) {
      return f.isDirectory() || f.getName().endsWith(".txt");
    }

    @Override
    @Nonnull
    public String getDescription() {
      return "Text document (*.txt)";
    }
  };

  @Override
  @Nonnull
  public FileFilter getFileFilter() {
    return TXT_FILE_FILTER;
  }

  public TextEditor(@Nonnull final Context context, @Nullable File file) throws IOException {
    super();
    this.editor = new JTextArea();
    this.editor.getCaret().setSelectionVisible(true);
    this.editor.setFont(PreferencesManager.getInstance().getFont(PreferencesManager.getInstance().getPreferences(), SpecificKeys.PROPERTY_TEXT_EDITOR_FONT, DEFAULT_FONT));
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

    setViewportView(this.editor);

    loadContent(file);
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
  public EditorType getContentType() {
    return EditorType.TEXT;
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
        this.editor.setText(FileUtils.readFileToString(file, "UTF-8"));
        this.editor.setCaretPosition(0);
      }
    } finally {
      this.ignoreChange = false;
    }

    this.undoManager.discardAllEdits();
    this.title.setChanged(false);

    this.revalidate();
  }

  @Override
  public boolean saveDocument() throws IOException {
    boolean result = false;
    if (this.title.isChanged()) {
      File file = this.title.getAssociatedFile();
      if (file == null) {
        file = DialogProviderManager.getInstance().getDialogProvider().msgSaveFileDialog("text-editor", "Save Text document", null, true, getFileFilter(), "Save");
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
  public JComponent getMainComponent() {
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
      int pos = 0;

      int maxPos = this.editor.getCaret().getMark() == this.editor.getCaret().getDot() ? this.editor.getCaretPosition() : this.editor.getSelectionStart();

      for (int i = 0; i < 2; i++) {
        while (matcher.find()) {
          pos = matcher.start();
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

}
