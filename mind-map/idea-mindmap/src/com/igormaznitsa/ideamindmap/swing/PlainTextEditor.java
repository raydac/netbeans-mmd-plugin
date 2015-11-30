/*
 * Copyright 2015 Igor Maznitsa.
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

import com.igormaznitsa.ideamindmap.utils.AllIcons;
import com.igormaznitsa.ideamindmap.utils.IdeaUtils;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
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
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class PlainTextEditor extends JPanel {

  private static final Logger LOGGER = LoggerFactory.getLogger(PlainTextEditor.class);
  private static final ResourceBundle BUNDLE = java.util.ResourceBundle.getBundle("/i18n/Bundle");

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

    public String getSelectedText(){
      final SelectionModel model = this.getEditor().getSelectionModel();
      final int start = model.getSelectionStart();
      final int end = model.getSelectionEnd();
      return getDocument().getText(new TextRange(start,end));
    }

    public void replaceSelection(@NotNull final String clipboardText) {
      ApplicationManager.getApplication().runWriteAction(new Runnable() {
        @Override
        public void run() {
          CommandProcessor.getInstance().executeCommand(getProject(), new Runnable() {
            @Override
            public void run() {
              final SelectionModel model = getEditor().getSelectionModel();
              final int start = model.getSelectionStart();
              final int end = model.getSelectionEnd();
              getDocument().replaceString(start, end, "");
              getDocument().insertString(start, clipboardText);
            }
          },null, null, UndoConfirmationPolicy.DEFAULT, getDocument());
        }
      });
    }

    public void clear(){
      this.setText("");
    }
  }

  private final EmptyTextEditor editor;

  public PlainTextEditor(final Project project, final String text) {
    super(new BorderLayout());
    this.editor = new EmptyTextEditor(project);

    final JToolBar menu = new JToolBar();

    final JButton buttonImport = new JButton("Import", AllIcons.Buttons.IMPORT);
    final PlainTextEditor theInstance = this;

    buttonImport.addActionListener(new ActionListener() {
      @Override public void actionPerformed(ActionEvent e) {
        final File home = new File(System.getProperty("user.home")); //NOI18N

        final File toOpen = IdeaUtils.chooseFile(theInstance, true, BUNDLE.getString("PlainTextEditor.buttonLoadActionPerformed.title"), home, TEXT_FILE_FILTER);

        if (toOpen != null) {
          try {
            final String text = FileUtils.readFileToString(toOpen, "UTF-8"); //NOI18N
            editor.setText(text);
          }
          catch (Exception ex) {
            LOGGER.error("Error during text file loading", ex); //NOI18N
            Messages.showErrorDialog(BUNDLE.getString("PlainTextEditor.buttonLoadActionPerformed.msgError"), "Error");
          }
        }

      }
    });

    final JButton buttonExport = new JButton("Export", AllIcons.Buttons.EXPORT);
    buttonExport.addActionListener(new ActionListener() {
      @Override public void actionPerformed(ActionEvent e) {
        final File home = new File(System.getProperty("user.home")); //NOI18N
        final File toSave= IdeaUtils.chooseFile(theInstance, true, BUNDLE.getString("PlainTextEditor.buttonSaveActionPerformed.saveTitle"), home, TEXT_FILE_FILTER);
        if (toSave != null) {
          try {
            final String text = getText();
            FileUtils.writeStringToFile(toSave, text, "UTF-8"); //NOI18N
          }
          catch (Exception ex) {
            LOGGER.error("Error during text file saving", ex); //NOI18N
            Messages.showErrorDialog(BUNDLE.getString("PlainTextEditor.buttonSaveActionPerformed.msgError"),"Error");
          }
        }
      }
    });

    final JButton buttonCopy = new JButton("Copy", AllIcons.Buttons.COPY);
    buttonCopy.addActionListener(new ActionListener() {
      @Override public void actionPerformed(ActionEvent e) {
        final StringSelection stringSelection = new StringSelection(editor.getSelectedText());
        final Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
        clpbrd.setContents(stringSelection, null);
      }
    });

    final JButton buttonPaste = new JButton("Paste", AllIcons.Buttons.PASTE);
    buttonPaste.addActionListener(new ActionListener() {
      @Override public void actionPerformed(ActionEvent e) {
        try {
          final String clipboardText = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
          editor.replaceSelection(clipboardText);
        }
        catch (UnsupportedFlavorException ex) {
          // no text data in clipboard
        }
        catch (IOException ex) {
          LOGGER.error("Error during paste from clipboard", ex); //NOI18N
        }
      }
    });

    final JButton buttonClearAll = new JButton("Clear All", AllIcons.Buttons.CLEARALL);
    buttonClearAll.addActionListener(new ActionListener() {
      @Override public void actionPerformed(ActionEvent e) {
        editor.clear();
      }
    });

    menu.add(buttonImport);
    menu.add(buttonExport);
    menu.add(buttonCopy);
    menu.add(buttonPaste);
    menu.add(buttonClearAll);

    this.add(menu, BorderLayout.NORTH);
    this.add(editor, BorderLayout.CENTER);

    // I made so strange trick to move the caret into the start of document, all other ways didn't work :(
    SwingUtilities.invokeLater(new Runnable() {
      @Override public void run() {
        editor.replaceSelection(text);
      }
    });
  }

  public String getText() {
    return this.editor.getText();
  }

  public EmptyTextEditor getEditor() {
    return this.editor;
  }
}
