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
package com.igormaznitsa.sciareto.ui.editors.mmeditors;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Utilities;
import org.apache.commons.io.FileUtils;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.sciareto.ui.DialogProviderManager;
import com.igormaznitsa.sciareto.ui.UiUtils;

public class NoteEditor extends javax.swing.JPanel {

  private static final long serialVersionUID = -1715683034655322518L;

  private static final Logger LOGGER = LoggerFactory.getLogger(NoteEditor.class);

  private enum Wrapping {

    NONE("none", "off"),
    CHAR_WRAP("char", "char"),
    WORD_WRAP("word", "word");

    private final String value;
    private final String display;

    private Wrapping(@Nonnull final String val, @Nonnull final String display) {
      this.value = val;
      this.display = display;
    }

    @Nonnull
    public String getValue() {
      return this.value;
    }

    @Nonnull
    public String getDisplay() {
      return this.display;
    }

    @Nonnull
    public Wrapping next() {
      final int index = this.ordinal() + 1;
      if (index >= Wrapping.values().length) {
        return NONE;
      } else {
        return Wrapping.values()[index];
      }
    }

    @Nonnull
    public static Wrapping findFor(@Nonnull final String text) {
      for (final Wrapping w : Wrapping.values()) {
        if (w.value.equalsIgnoreCase(text)) {
          return w;
        }
      }
      return NONE;
    }
  }

  private static final FileFilter TEXT_FILE_FILTER = new FileFilter() {

    @Override
    public boolean accept(final File f) {
      return f.isDirectory() || f.getName().toLowerCase(Locale.ENGLISH).endsWith(".txt"); //NOI18N
    }

    @Override
    public String getDescription() {
      return java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18n/Bundle").getString("PlainTextEditor.fileFilter.description");
    }
  };

  
  
  private Wrapping wrapping;

  public NoteEditor(@Nonnull final String text) {
    initComponents();
    this.setPreferredSize(new Dimension(640, 480));
    this.editorPane.setText(text);
    this.addAncestorListener(new AncestorListener() {
      @Override
      public void ancestorAdded(@Nonnull final AncestorEvent event) {
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            editorPane.requestFocusInWindow();
            updateCaretPos();
          }
        });
      }

      @Override
      public void ancestorRemoved(@Nonnull final AncestorEvent event) {
      }

      @Override
      public void ancestorMoved(@Nonnull final AncestorEvent event) {
      }
    });

    this.editorPane.addCaretListener(new CaretListener() {
      @Override
      public void caretUpdate(@Nonnull final CaretEvent e) {
        updateCaretPos();
      }
    });

    this.wrapping = Wrapping.NONE;

    updateBottomPanel();
  }

  private void updateCaretPos(){
    final int pos = this.editorPane.getCaretPosition();
    final int col = getColumn(pos, this.editorPane);
    final int row = getRow(pos, this.editorPane);
    this.labelCursorPos.setText(row + ":" + col);
  }
  
  private static int getRow(final int pos, final JTextComponent editor) {
    int rn = (pos == 0) ? 1 : 0;
    try {
      int offs = pos;
      while (offs > 0) {
        offs = Utilities.getRowStart(editor, offs) - 1;
        rn++;
      }
    } catch (BadLocationException e) {
      LOGGER.error("Bad location", e);
    }
    return rn;
  }

  private static int getColumn(final int pos, final JTextComponent editor) {
    try {
      return pos - Utilities.getRowStart(editor, pos) + 1;
    } catch (BadLocationException e) {
      LOGGER.error("Bad location", e);
    }
    return -1;
  }

  @Nonnull
  public String getText() {
    return this.editorPane.getText();
  }

  public void dispose() {

  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form
   * Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    jToolBar1 = new javax.swing.JToolBar();
    buttonImport = new javax.swing.JButton();
    buttonExport = new javax.swing.JButton();
    buttonCopy = new javax.swing.JButton();
    buttonPaste = new javax.swing.JButton();
    buttonClear = new javax.swing.JButton();
    jPanel1 = new javax.swing.JPanel();
    labelCursorPos = new javax.swing.JLabel();
    jSeparator1 = new javax.swing.JSeparator();
    labelWrapMode = new javax.swing.JLabel();
    filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(16, 0), new java.awt.Dimension(16, 0), new java.awt.Dimension(16, 32767));
    jScrollPane2 = new javax.swing.JScrollPane();
    editorPane = new javax.swing.JTextArea();

    setLayout(new java.awt.BorderLayout());

    jToolBar1.setFloatable(false);
    jToolBar1.setRollover(true);

    buttonImport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/disk16.png"))); // NOI18N
    buttonImport.setText("Import");
    buttonImport.setFocusable(false);
    buttonImport.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    buttonImport.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    buttonImport.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        buttonImportActionPerformed(evt);
      }
    });
    jToolBar1.add(buttonImport);

    buttonExport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/file_save16.png"))); // NOI18N
    buttonExport.setText("Export");
    buttonExport.setFocusable(false);
    buttonExport.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    buttonExport.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    buttonExport.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        buttonExportActionPerformed(evt);
      }
    });
    jToolBar1.add(buttonExport);

    buttonCopy.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/page_copy16.png"))); // NOI18N
    buttonCopy.setText("Copy");
    buttonCopy.setFocusable(false);
    buttonCopy.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    buttonCopy.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    buttonCopy.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        buttonCopyActionPerformed(evt);
      }
    });
    jToolBar1.add(buttonCopy);

    buttonPaste.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/paste_plain16.png"))); // NOI18N
    buttonPaste.setText("Paste");
    buttonPaste.setFocusable(false);
    buttonPaste.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    buttonPaste.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    buttonPaste.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        buttonPasteActionPerformed(evt);
      }
    });
    jToolBar1.add(buttonPaste);

    buttonClear.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/cross16.png"))); // NOI18N
    buttonClear.setText("Clear All");
    buttonClear.setFocusable(false);
    buttonClear.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    buttonClear.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    buttonClear.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        buttonClearActionPerformed(evt);
      }
    });
    jToolBar1.add(buttonClear);

    add(jToolBar1, java.awt.BorderLayout.NORTH);

    jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

    labelCursorPos.setText("...:...");
    labelCursorPos.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        labelCursorPosMouseClicked(evt);
      }
    });
    jPanel1.add(labelCursorPos);

    jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
    jSeparator1.setPreferredSize(new java.awt.Dimension(8, 16));
    jPanel1.add(jSeparator1);

    labelWrapMode.setText("...");
    labelWrapMode.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        labelWrapModeMouseClicked(evt);
      }
    });
    jPanel1.add(labelWrapMode);
    jPanel1.add(filler1);

    add(jPanel1, java.awt.BorderLayout.PAGE_END);

    editorPane.setColumns(20);
    editorPane.setRows(5);
    jScrollPane2.setViewportView(editorPane);

    add(jScrollPane2, java.awt.BorderLayout.CENTER);
  }// </editor-fold>//GEN-END:initComponents

  private void labelCursorPosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_labelCursorPosMouseClicked

  }//GEN-LAST:event_labelCursorPosMouseClicked

  private void labelWrapModeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_labelWrapModeMouseClicked
    this.wrapping = this.wrapping.next();
    this.editorPane.setWrapStyleWord(this.wrapping != Wrapping.CHAR_WRAP);
    this.editorPane.setLineWrap(this.wrapping != Wrapping.NONE);
    updateBottomPanel();
  }//GEN-LAST:event_labelWrapModeMouseClicked

  private void buttonImportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonImportActionPerformed
    final File toOpen = DialogProviderManager.getInstance().getDialogProvider().msgOpenFileDialog("note-editor", UiUtils.BUNDLE.getString("PlainTextEditor.buttonLoadActionPerformed.title"), null, true, TEXT_FILE_FILTER, "Open"); //NOI18N
    if (toOpen != null) {
      try {
        final String text = FileUtils.readFileToString(toOpen, "UTF-8"); //NOI18N
        this.editorPane.setText(text);
      } catch (Exception ex) {
        LOGGER.error("Error during text file loading", ex); //NOI18N
        DialogProviderManager.getInstance().getDialogProvider().msgError(UiUtils.BUNDLE.getString("PlainTextEditor.buttonLoadActionPerformed.msgError"));
      }
    }
    
  }//GEN-LAST:event_buttonImportActionPerformed

  private void buttonExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonExportActionPerformed
    final File toSave = DialogProviderManager.getInstance().getDialogProvider().msgSaveFileDialog("note-editor", UiUtils.BUNDLE.getString("PlainTextEditor.buttonSaveActionPerformed.saveTitle"), null, true, TEXT_FILE_FILTER, "Save");
    if (toSave != null) {
      try {
        final String text = getText();
        FileUtils.writeStringToFile(toSave, text, "UTF-8"); //NOI18N
      } catch (Exception ex) {
        LOGGER.error("Error during text file saving", ex); //NOI18N
        DialogProviderManager.getInstance().getDialogProvider().msgError(UiUtils.BUNDLE.getString("PlainTextEditor.buttonSaveActionPerformed.msgError"));
      }
    }

  }//GEN-LAST:event_buttonExportActionPerformed

  private void buttonCopyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCopyActionPerformed
    StringSelection stringSelection = new StringSelection(this.editorPane.getSelectedText());
    final Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
    clpbrd.setContents(stringSelection, null);
  }//GEN-LAST:event_buttonCopyActionPerformed

  private void buttonPasteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonPasteActionPerformed
    try {
      this.editorPane.replaceSelection((String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor));
    } catch (UnsupportedFlavorException ex) {
      // no text data in clipboard
    } catch (IOException ex) {
      LOGGER.error("Error during paste from clipboard", ex); //NOI18N
    }
  }//GEN-LAST:event_buttonPasteActionPerformed

  private void buttonClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonClearActionPerformed
    this.editorPane.setText("");
  }//GEN-LAST:event_buttonClearActionPerformed

  private void updateBottomPanel() {
    this.labelWrapMode.setText("Wrap: " + this.wrapping.getDisplay());
  }


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton buttonClear;
  private javax.swing.JButton buttonCopy;
  private javax.swing.JButton buttonExport;
  private javax.swing.JButton buttonImport;
  private javax.swing.JButton buttonPaste;
  private javax.swing.JTextArea editorPane;
  private javax.swing.Box.Filler filler1;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JScrollPane jScrollPane2;
  private javax.swing.JSeparator jSeparator1;
  private javax.swing.JToolBar jToolBar1;
  private javax.swing.JLabel labelCursorPos;
  private javax.swing.JLabel labelWrapMode;
  // End of variables declaration//GEN-END:variables
}
