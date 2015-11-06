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
package com.igormaznitsa.nbmindmap.nb.swing;

import com.igormaznitsa.nbmindmap.utils.NbUtils;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;
import org.apache.commons.io.FileUtils;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.indent.spi.CodeStylePreferences;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.NbDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PlainTextEditor extends javax.swing.JPanel implements CaretListener {

  private enum Wrapping {

    NONE("none", "off"),
    WORDS("words", "words"),
    CHARS("chars", "chars");

    private final String value;
    private final String display;

    private Wrapping (final String val, final String display) {
      this.value = val;
      this.display = display;
    }

    public String getValue () {
      return this.value;
    }

    public String getDisplay () {
      return this.display;
    }

    public Wrapping next () {
      final int index = this.ordinal() + 1;
      if (index >= Wrapping.values().length) {
        return NONE;
      }
      else {
        return Wrapping.values()[index];
      }
    }

    public static Wrapping findFor (final String text) {
      for (final Wrapping w : Wrapping.values()) {
        if (w.value.equalsIgnoreCase(text)) {
          return w;
        }
      }
      return NONE;
    }
  }

  private static final long serialVersionUID = 5847351391577028903L;
  private static final Logger logger = LoggerFactory.getLogger(PlainTextEditor.class);

  private final BaseDocument document;
  private Component lastComponent;
  private JEditorPane lastEditor;

  private final Wrapping oldWrapping;
  private Wrapping wrapping;

  private static final FileFilter TEXT_FILE_FILTER = new FileFilter() {

    @Override
    public boolean accept (final File f) {
      return f.isDirectory() || f.getName().toLowerCase(Locale.ENGLISH).endsWith(".txt"); //NOI18N
    }

    @Override
    public String getDescription () {
      return java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18n/Bundle").getString("PlainTextEditor.fileFilter.description");
    }
  };

  public PlainTextEditor (final String text) {
    initComponents();

    final JEditorPane editor = new JEditorPane();
    editor.setEditorKit(getEditorKit());
    this.document = Utilities.getDocument(editor);

    setText(text);

    final Preferences docPreferences = CodeStylePreferences.get(this.document).getPreferences();
    this.oldWrapping = Wrapping.findFor(docPreferences.get(SimpleValueNames.TEXT_LINE_WRAP, "none"));
    this.wrapping = oldWrapping;

    this.lastComponent = makeEditorForText(this.document);
    this.lastComponent.setPreferredSize(new Dimension(620, 440));
    this.add(this.lastComponent, BorderLayout.CENTER);

    this.labelWrapMode.setMinimumSize(new Dimension(55, this.labelWrapMode.getMinimumSize().height));

    updateBottomPanel();
  }

  private Component makeEditorForText (final Document document) {
    if (this.lastEditor != null) {
      this.lastEditor.removeCaretListener(this);
    }

    this.lastEditor = new JEditorPane();
    this.lastEditor.setEditorKit(getEditorKit());
    this.lastEditor.setDocument(document);

    this.lastEditor.addCaretListener(this);

    final Component result;
    if (document instanceof NbDocument.CustomEditor) {
      NbDocument.CustomEditor ce = (NbDocument.CustomEditor) document;
      result = ce.createEditor(this.lastEditor);
    }
    else {
      result = new JScrollPane(this.lastEditor);
    }

    this.caretUpdate(null);

    return result;
  }

  private static EditorKit getEditorKit () {
    return CloneableEditorSupport.getEditorKit("text/plain"); //NOI18N
  }

  public String getText () {
    try {
      return this.document.getText(0, this.document.getLength());
    }
    catch (BadLocationException e) {
      logger.error("Can't get text", e); //NOI18N
      return null;
    }
  }

  private void updateBottomPanel () {
    this.labelWrapMode.setText("Wrap: " + this.wrapping.getDisplay());
  }

  private void setText (final String text) {
    try {
      this.document.replace(0, 0, text, null);
    }
    catch (BadLocationException ex) {
      logger.error("Can't set text", ex); //NOI18N
      throw new RuntimeException(ex);
    }
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings ("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    mainToolBar = new javax.swing.JToolBar();
    buttonLoad = new javax.swing.JButton();
    buttonSave = new javax.swing.JButton();
    buttonCopy = new javax.swing.JButton();
    buttonPaste = new javax.swing.JButton();
    buttonClearAll = new javax.swing.JButton();
    jPanel1 = new javax.swing.JPanel();
    labelCursorPos = new javax.swing.JLabel();
    jSeparator2 = new javax.swing.JSeparator();
    labelWrapMode = new javax.swing.JLabel();
    filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(16, 0), new java.awt.Dimension(16, 0), new java.awt.Dimension(16, 32767));

    setLayout(new java.awt.BorderLayout());

    mainToolBar.setFloatable(false);
    mainToolBar.setRollover(true);

    buttonLoad.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/igormaznitsa/nbmindmap/icons/disk16.png"))); // NOI18N
    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18n/Bundle"); // NOI18N
    buttonLoad.setText(bundle.getString("PlainTextEditor.buttonImport")); // NOI18N
    buttonLoad.setToolTipText(bundle.getString("PlainTextEditor.buttonLoad.toolTipText")); // NOI18N
    buttonLoad.setFocusable(false);
    buttonLoad.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    buttonLoad.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    buttonLoad.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        buttonLoadActionPerformed(evt);
      }
    });
    mainToolBar.add(buttonLoad);

    buttonSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/igormaznitsa/nbmindmap/icons/file_save16.png"))); // NOI18N
    buttonSave.setText(bundle.getString("PlaintextEditor.buttonExport")); // NOI18N
    buttonSave.setToolTipText(bundle.getString("PlainTextEditor.buttonSave.toolTipText")); // NOI18N
    buttonSave.setFocusable(false);
    buttonSave.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    buttonSave.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    buttonSave.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        buttonSaveActionPerformed(evt);
      }
    });
    mainToolBar.add(buttonSave);

    buttonCopy.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/igormaznitsa/nbmindmap/icons/page_copy16.png"))); // NOI18N
    buttonCopy.setText(bundle.getString("PlainTextEditor.buttonCopy.text")); // NOI18N
    buttonCopy.setToolTipText(bundle.getString("PlainTextEditor.buttonCopy.toolTipText")); // NOI18N
    buttonCopy.setFocusable(false);
    buttonCopy.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    buttonCopy.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    buttonCopy.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        buttonCopyActionPerformed(evt);
      }
    });
    mainToolBar.add(buttonCopy);

    buttonPaste.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/igormaznitsa/nbmindmap/icons/paste_plain16.png"))); // NOI18N
    buttonPaste.setText(bundle.getString("PlainTextEditor.buttonPaste.text")); // NOI18N
    buttonPaste.setToolTipText(bundle.getString("PlainTextEditor.buttonPaste.toolTipText")); // NOI18N
    buttonPaste.setFocusable(false);
    buttonPaste.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    buttonPaste.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    buttonPaste.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        buttonPasteActionPerformed(evt);
      }
    });
    mainToolBar.add(buttonPaste);

    buttonClearAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/igormaznitsa/nbmindmap/icons/cross16.png"))); // NOI18N
    buttonClearAll.setText(bundle.getString("PlainTextEditor.buttonClearAll.text")); // NOI18N
    buttonClearAll.setToolTipText(bundle.getString("PlainTextEditor.buttonClearAll.toolTipText")); // NOI18N
    buttonClearAll.setFocusable(false);
    buttonClearAll.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    buttonClearAll.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    buttonClearAll.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        buttonClearAllActionPerformed(evt);
      }
    });
    mainToolBar.add(buttonClearAll);

    add(mainToolBar, java.awt.BorderLayout.PAGE_START);

    jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

    labelCursorPos.setText("...:..."); // NOI18N
    jPanel1.add(labelCursorPos);

    jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);
    jSeparator2.setPreferredSize(new java.awt.Dimension(8, 16));
    jPanel1.add(jSeparator2);

    labelWrapMode.setText("..."); // NOI18N
    labelWrapMode.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    labelWrapMode.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        labelWrapModeMouseClicked(evt);
      }
    });
    jPanel1.add(labelWrapMode);
    jPanel1.add(filler1);

    add(jPanel1, java.awt.BorderLayout.PAGE_END);
  }// </editor-fold>//GEN-END:initComponents

  private void buttonLoadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonLoadActionPerformed
    final File home = new File(System.getProperty("user.home")); //NOI18N
    final File toOpen = new FileChooserBuilder("user-home-dir"). //NOI18N
        setTitle(java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18n/Bundle").getString("PlainTextEditor.buttonLoadActionPerformed.title")).
        addFileFilter(TEXT_FILE_FILTER).setFileFilter(TEXT_FILE_FILTER).
        setFilesOnly(true).
        setDefaultWorkingDirectory(home).
        showOpenDialog();
    if (toOpen != null) {
      try {
        final String text = FileUtils.readFileToString(toOpen, "UTF-8"); //NOI18N
        setText(text);
      }
      catch (Exception ex) {
        logger.error("Error during text file loading", ex); //NOI18N
        NbUtils.msgError(java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18n/Bundle").getString("PlainTextEditor.buttonLoadActionPerformed.msgError"));
      }
    }
  }//GEN-LAST:event_buttonLoadActionPerformed

  private void buttonSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSaveActionPerformed
    final File home = new File(System.getProperty("user.home")); //NOI18N
    final File toSave = new FileChooserBuilder("user-home-dir").
        setTitle(java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18n/Bundle").getString("PlainTextEditor.buttonSaveActionPerformed.saveTitle")).
        addFileFilter(TEXT_FILE_FILTER).setFileFilter(TEXT_FILE_FILTER).
        setFilesOnly(true).
        setDefaultWorkingDirectory(home).
        showSaveDialog();
    if (toSave != null) {
      try {
        final String text = getText();
        FileUtils.writeStringToFile(toSave, text, "UTF-8"); //NOI18N
      }
      catch (Exception ex) {
        logger.error("Error during text file saving", ex); //NOI18N
        NbUtils.msgError(java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18n/Bundle").getString("PlainTextEditor.buttonSaveActionPerformed.msgError"));
      }
    }
  }//GEN-LAST:event_buttonSaveActionPerformed

  private void buttonCopyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCopyActionPerformed
    StringSelection stringSelection = new StringSelection(this.lastEditor.getSelectedText());
    final Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
    clpbrd.setContents(stringSelection, null);
  }//GEN-LAST:event_buttonCopyActionPerformed

  private void buttonPasteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonPasteActionPerformed
    try {
      this.lastEditor.replaceSelection((String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor));
    }
    catch (UnsupportedFlavorException ex) {
      // no text data in clipboard
    }
    catch (IOException ex) {
      logger.error("Error during paste from clipboard", ex); //NOI18N
    }

  }//GEN-LAST:event_buttonPasteActionPerformed

  private void buttonClearAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonClearAllActionPerformed
    this.lastEditor.setText(""); //NOI18N
  }//GEN-LAST:event_buttonClearAllActionPerformed

  private void labelWrapModeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_labelWrapModeMouseClicked
    this.wrapping = this.wrapping.next();
    writeWrappingCode(this.wrapping);
    final Component oldComponent = this.lastComponent;
    this.lastComponent = makeEditorForText(this.document);
    this.remove(oldComponent);
    this.add(this.lastComponent, BorderLayout.CENTER);
    updateBottomPanel();
  }//GEN-LAST:event_labelWrapModeMouseClicked


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton buttonClearAll;
  private javax.swing.JButton buttonCopy;
  private javax.swing.JButton buttonLoad;
  private javax.swing.JButton buttonPaste;
  private javax.swing.JButton buttonSave;
  private javax.swing.Box.Filler filler1;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JSeparator jSeparator2;
  private javax.swing.JLabel labelCursorPos;
  private javax.swing.JLabel labelWrapMode;
  private javax.swing.JToolBar mainToolBar;
  // End of variables declaration//GEN-END:variables

  private void writeWrappingCode (final Wrapping code) {
    final Preferences docPreferences = CodeStylePreferences.get(this.document).getPreferences();
    docPreferences.put(SimpleValueNames.TEXT_LINE_WRAP, code.getValue());
    try {
      docPreferences.flush();
    }
    catch (BackingStoreException ex) {
      logger.error("Can't write wrapping code", ex);
    }
  }

  public void dispose () {
    if (this.lastEditor != null) {
      this.lastEditor.removeCaretListener(this);
    }

    writeWrappingCode(this.oldWrapping); // restore old wrapping for mime type
    logger.info("PlainTextEditor has been disposed");
  }

  private static int getRow (final int pos, final JTextComponent editor) {
    int rn = (pos == 0) ? 1 : 0;
    try {
      int offs = pos;
      while (offs > 0) {
        offs = Utilities.getRowStart(editor, offs) - 1;
        rn++;
      }
    }
    catch (BadLocationException e) {
      e.printStackTrace();
    }
    return rn;
  }

  private static int getColumn (final int pos, final JTextComponent editor) {
    try {
      return pos - Utilities.getRowStart(editor, pos) + 1;
    }
    catch (BadLocationException e) {
      e.printStackTrace();
    }
    return -1;
  }

  @Override
  public void caretUpdate (final CaretEvent e) {
    final String text;
    if (this.lastEditor == null) {
      text = "...:...";
    }
    else {
      final int pos = this.lastEditor.getCaretPosition();
      final int col = getColumn(pos, this.lastEditor);
      final int row = getRow(pos, this.lastEditor);
      text = row + ":" + col;
    }
    this.labelCursorPos.setText(text);
  }

}
