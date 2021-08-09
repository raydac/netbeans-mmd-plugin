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

package com.igormaznitsa.ideamindmap.swing;

import com.igormaznitsa.ideamindmap.editor.MindMapDialogProvider;
import com.igormaznitsa.ideamindmap.utils.AllIcons;
import com.igormaznitsa.ideamindmap.utils.IdeaUtils;
import com.igormaznitsa.meta.common.utils.Assertions;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.swing.panel.ui.PasswordPanel;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactory;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactoryProvider;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.UndoConfirmationPolicy;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.FileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.TextRange;
import com.intellij.ui.EditorTextField;
import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import org.apache.commons.io.FileUtils;

public class PlainTextEditor extends JPanel {
  private static final long serialVersionUID = -125160747070513137L;

  private static final Logger LOGGER = LoggerFactory.getLogger(PlainTextEditor.class);
  private static final ResourceBundle BUNDLE = java.util.ResourceBundle.getBundle("i18n/Bundle");

  private static final UIComponentFactory UI_COMPO_FACTORY = UIComponentFactoryProvider.findInstance();

  private final MindMapDialogProvider dialogProvider;
  
  private volatile String password;
  private volatile String hint;

  private final String originalText;

  private boolean cancelled;

  private static final FileFilter TEXT_FILE_FILTER = new FileFilter() {

    @Override
    public boolean accept(final File f) {
      return f.isDirectory() || f.getName().toLowerCase(Locale.ENGLISH).endsWith(".txt"); //NOI18N
    }

    @Override
    public String getDescription() {
      return BUNDLE.getString("PlainTextEditor.fileFilter.description");
    }
  };
  private final EmptyTextEditor editor;

  public PlainTextEditor(final Project project, final NoteEditorData data) {
    super(new BorderLayout());
    
    this.dialogProvider = new MindMapDialogProvider(project);
    
    this.password = data.getPassword();
    this.hint = data.getHint();
    this.originalText = data.getText();

    this.editor = new EmptyTextEditor(project);

    final JToolBar menu = UI_COMPO_FACTORY.makeToolBar();
    menu.setFloatable(false);

    final JButton buttonImport = makeButton("Import", AllIcons.Buttons.IMPORT);
    final PlainTextEditor theInstance = this;

    buttonImport.addActionListener(e -> {
      final File home = new File(System.getProperty("user.home")); //NOI18N

      final File toOpen = IdeaUtils.chooseFile(theInstance, true, Utils.BUNDLE.getString("PlainTextEditor.buttonLoadActionPerformed.title"), home, TEXT_FILE_FILTER);

      if (toOpen != null) {
        try {
          final String text = FileUtils.readFileToString(toOpen, "UTF-8"); //NOI18N
          editor.setText(text);
        } catch (Exception ex) {
          LOGGER.error("Error during text file loading", ex); //NOI18N
          Messages.showErrorDialog(BUNDLE.getString("PlainTextEditor.buttonLoadActionPerformed.msgError"), "Error");
        }
      }

    });

    final JButton buttonExport = makeButton("Export", AllIcons.Buttons.EXPORT);
    buttonExport.addActionListener(e -> {
      final File home = new File(System.getProperty("user.home")); //NOI18N
      final File toSave = IdeaUtils.chooseFile(theInstance, true, BUNDLE.getString("PlainTextEditor.buttonSaveActionPerformed.saveTitle"), home, TEXT_FILE_FILTER);
      if (toSave != null) {
        try {
          final String text = getData().getText();
          FileUtils.writeStringToFile(toSave, text, "UTF-8"); //NOI18N
        } catch (Exception ex) {
          LOGGER.error("Error during text file saving", ex); //NOI18N
          Messages.showErrorDialog(BUNDLE.getString("PlainTextEditor.buttonSaveActionPerformed.msgError"), "Error");
        }
      }
    });

    final JButton buttonCopy = makeButton("Copy", AllIcons.Buttons.COPY);
    buttonCopy.addActionListener(e -> {
      final StringSelection stringSelection = new StringSelection(editor.getSelectedText());
      final Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
      clpbrd.setContents(stringSelection, null);
    });

    final JButton buttonPaste = makeButton("Paste", AllIcons.Buttons.PASTE);
    buttonPaste.addActionListener(e -> {
      try {
        final String clipboardText = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
        editor.replaceSelection(clipboardText);
      } catch (UnsupportedFlavorException ex) {
        // no text data in clipboard
      } catch (IOException ex) {
        LOGGER.error("Error during paste from clipboard", ex); //NOI18N
      }
    });

    final JButton buttonClearAll = makeButton("Clear All", AllIcons.Buttons.CLEARALL);
    buttonClearAll.addActionListener(e -> editor.clear());

    final JToggleButton buttonProtect = makeToggleButton("Protect", AllIcons.Buttons.PROTECT_OFF, AllIcons.Buttons.PROTECT_ON);
    buttonProtect.setSelected(data.isEncrypted());
    buttonProtect.addActionListener(e -> {
        final JToggleButton src = (JToggleButton) e.getSource();
        if (src.isSelected()) {
            final PasswordPanel passwordPanel = new PasswordPanel();
            if (dialogProvider.msgOkCancel(PlainTextEditor.this, Utils.BUNDLE.getString("PasswordPanel.dialogPassword.set.title"), passwordPanel)) {
                password = new String(passwordPanel.getPassword()).trim();
                hint = passwordPanel.getHint();
                if (password.isEmpty()) {
                    src.setSelected(false);
                }
            } else {
                src.setSelected(false);
            }
        } else {
            if (dialogProvider
                .msgConfirmOkCancel(PlainTextEditor.this, "Reset password",
                    "Do you really want reset password for the note?")) {
                password = null;
                hint = null;
            } else {
                src.setSelected(true);
            }
        }
    });
    
    menu.add(buttonImport);
    menu.add(buttonExport);
    menu.add(buttonCopy);
    menu.add(buttonPaste);
    menu.add(buttonClearAll);
    menu.add(buttonProtect);

    this.add(menu, BorderLayout.NORTH);
    this.add(editor, BorderLayout.CENTER);

    // I made so strange trick to move the caret into the start of document, all other ways didn't work :(
    SwingUtilities.invokeLater(() -> editor.replaceSelection(data.getText()));
  }

  public boolean isChanged() {
    return !this.originalText.equals(this.getEditor().getText());
  }

  public boolean isCancelled(){
    return this.cancelled;
  }

  public void cancel() {
    this.cancelled = true;
  }

  @Nonnull
  private JButton makeButton(@Nullable final String text, @Nullable final Icon icon) {
    final JButton result = UI_COMPO_FACTORY.makeButton();
    result.setText(text);
    result.setIcon(icon);
    return result;
  }

  @Nonnull
  private JToggleButton makeToggleButton(@Nullable final String text, @Nullable final Icon icon, @Nullable final Icon selectedIcon) {
    final JToggleButton result = UI_COMPO_FACTORY.makeToggleButton();
    result.setText(text);
    result.setIcon(icon);
    result.setSelectedIcon(selectedIcon);
    return result;
  }

  @Nullable
  public NoteEditorData getData() {
    return this.cancelled ? null : new NoteEditorData(this.editor.getText(),this.password, this.hint);
  }

  public EmptyTextEditor getEditor() {
    return this.editor;
  }

  public static class EmptyTextEditor extends EditorTextField {

    public EmptyTextEditor(final Project project) {
      super("", project, FileTypes.PLAIN_TEXT);
      setOneLineMode(false);
      setAutoscrolls(true);
    }

    @Override
    protected EditorEx createEditor() {
      final EditorEx result = super.createEditor();
      result.setVerticalScrollbarVisible(true);
      result.setHorizontalScrollbarVisible(true);
      return result;
    }

    public String getSelectedText() {
      final SelectionModel model = Assertions.assertNotNull(this.getEditor()).getSelectionModel();
      final int start = model.getSelectionStart();
      final int end = model.getSelectionEnd();
      return getDocument().getText(new TextRange(start, end));
    }

    public void replaceSelection(@Nonnull final String clipboardText) {
      ApplicationManager.getApplication().runWriteAction(() -> CommandProcessor.getInstance().executeCommand(getProject(), () -> {
        final SelectionModel model = Assertions.assertNotNull(getEditor()).getSelectionModel();
        final int start = model.getSelectionStart();
        final int end = model.getSelectionEnd();
        getDocument().replaceString(start, end, "");
        getDocument().insertString(start, clipboardText);
      }, null, null, UndoConfirmationPolicy.DEFAULT, getDocument()));
    }

    public void clear() {
      this.setText("");
    }
  }
}
