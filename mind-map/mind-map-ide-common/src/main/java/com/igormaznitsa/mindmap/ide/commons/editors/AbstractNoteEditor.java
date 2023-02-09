package com.igormaznitsa.mindmap.ide.commons.editors;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.igormaznitsa.mindmap.ide.commons.SwingUtils;
import com.igormaznitsa.mindmap.ide.commons.preferences.MmcI18n;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.swing.i18n.MmdI18n;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.panel.ui.PasswordPanel;
import com.igormaznitsa.mindmap.swing.panel.utils.Focuser;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactory;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactoryProvider;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Supplier;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.Box.Filler;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import javax.swing.text.Utilities;
import javax.swing.undo.UndoManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

public abstract class AbstractNoteEditor {

  public static final Font DEFAULT_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 14);
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractNoteEditor.class);
  private final DialogProvider dialogProvider;
  private final String originalText;
  private final Supplier<Component> parentSupplier;
  private final UIComponentFactory uiComponentFactory;
  private final ResourceBundle bundle;
  private boolean cancelled;
  private Wrapping wrapping;
  private String password;
  private String hint;
  private JButton buttonBrowse;
  private JButton buttonClear;
  private JButton buttonCopy;
  private JButton buttonExport;
  private JButton buttonImport;
  private JButton buttonPaste;
  private JButton buttonRedo;
  private JButton buttonUndo;
  private JTextArea editorPane;
  private final UndoManager undoManager = new UndoManager() {
    @Override
    public void undoableEditHappened(final UndoableEditEvent e) {
      super.undoableEditHappened(e);
      updateRedoUndoState();
    }

  };
  private Filler wrapFiller;
  private JPanel bottomPanel;
  private JScrollPane editorScrollPane;
  private JSeparator wrapSeparator;
  private JPanel buttonBarPanel;
  private JLabel labelCursorPos;
  private JLabel labelWrapMode;
  private JToggleButton toggleButtonEncrypt;
  private JPanel mainPanel;

  public AbstractNoteEditor(
      final Supplier<Component> parentSupplier,
      final UIComponentFactory uiComponentFactory,
      final DialogProvider dialogProvider,
      final AbstractNoteEditorData data) {
    this.uiComponentFactory = uiComponentFactory;
    this.parentSupplier = parentSupplier;
    this.dialogProvider = dialogProvider;
    this.bundle = MmcI18n.getInstance().findBundle();
    initComponents();

    if (data.isEncrypted()) {
      this.toggleButtonEncrypt.setSelected(true);
    }

    this.buttonRedo.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke
            .getKeyStroke(KeyEvent.VK_Z,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.SHIFT_MASK),
        "do-redo");
    this.buttonRedo.getActionMap().put("do-redo", new AbstractAction() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        doRedo();
      }
    });

    this.buttonUndo.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
        KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
        "do-undo"); //NOI18N
    this.buttonUndo.getActionMap().put("do-undo", new AbstractAction() { //NOI18N
      private static final long serialVersionUID = -5644390861803492172L;

      @Override
      public void actionPerformed(final ActionEvent e) {
        doUndo();
      }
    });

    this.editorPane.setComponentPopupMenu(
        SwingUtils.addTextActions(UIComponentFactoryProvider.findInstance().makePopupMenu()));

    this.editorPane.getActionMap()
        .put(DefaultEditorKit.selectWordAction, new TextAction(DefaultEditorKit.selectWordAction) {
          private static final long serialVersionUID = -6477916799997545798L;
          private final Action start = new TextAction("wordStart") { //NOI18N
            private static final long serialVersionUID = 4377386270269629176L;

            @Override
            public void actionPerformed(final ActionEvent e) {
              final JTextComponent target = getTextComponent(e);
              try {
                if (target != null) {
                  int offs = target.getCaretPosition();
                  final Document doc = target.getDocument();
                  final String text = doc.getText(0, doc.getLength());
                  int startOffs = offs;
                  if (startOffs < text.length()) {
                    for (int i = offs; i >= 0; i--) {
                      if (!isWhitespaceOrControl(text.charAt(i))) {
                        startOffs = i;
                      } else {
                        break;
                      }
                    }
                    target.setCaretPosition(startOffs);
                  }
                }
              } catch (BadLocationException ex) {
                UIManager.getLookAndFeel().provideErrorFeedback(target);
              }
            }
          };
          private final Action end = new TextAction("wordEnd") { //NOI18N
            private static final long serialVersionUID = 4377386270269629176L;

            @Override
            public void actionPerformed(final ActionEvent e) {
              final JTextComponent target = getTextComponent(e);
              try {
                if (target != null) {
                  int offs = target.getCaretPosition();

                  final Document doc = target.getDocument();
                  final String text = doc.getText(0, doc.getLength());
                  int endOffs = offs;
                  for (int i = offs; i < text.length(); i++) {
                    endOffs = i;
                    if (isWhitespaceOrControl(text.charAt(i))) {
                      break;
                    }
                  }
                  if (endOffs < text.length() && !isWhitespaceOrControl(text.charAt(endOffs))) {
                    endOffs++;
                  }
                  target.moveCaretPosition(endOffs);
                }
              } catch (BadLocationException ex) {
                UIManager.getLookAndFeel().provideErrorFeedback(target);
              }
            }
          };

          @Override
          public void actionPerformed(final ActionEvent e) {
            this.start.actionPerformed(e);
            this.end.actionPerformed(e);
          }

        });

    this.buttonBarPanel.doLayout();

    this.mainPanel.setPreferredSize(
        new Dimension(Math.max(this.buttonBarPanel.getPreferredSize().width, 640), 480));
    this.editorPane.setFont(this.findEditorFont(DEFAULT_FONT));
    this.originalText = data.getText();
    this.editorPane.setText(data.getText());

    this.password = data.getPassword();
    this.hint = data.getHint();
    if (this.password != null) {
      this.toggleButtonEncrypt.setSelected(true);
    }

    this.mainPanel.addAncestorListener(new AncestorListener() {
      @Override
      public void ancestorAdded(final AncestorEvent event) {
        SwingUtilities.invokeLater(() -> {
          editorPane.grabFocus();
          updateCaretPos();
        });
      }

      @Override
      public void ancestorRemoved(final AncestorEvent event) {
      }

      @Override
      public void ancestorMoved(final AncestorEvent event) {
      }
    });

    this.editorPane.addCaretListener(e -> updateCaretPos());

    this.wrapping = Wrapping.WORD_WRAP;
    editorPane.setCaretPosition(0);
    updateWrapping();

    this.editorPane.getDocument().addUndoableEditListener(this.undoManager);
    updateRedoUndoState();

    SwingUtils.makeOwningDialogResizable(this.mainPanel);

    new Focuser(this.editorPane);
  }

  private static boolean isWhitespaceOrControl(final char c) {
    return Character.isISOControl(c) || Character.isWhitespace(c);
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
      LOGGER.error("Bad location", e); //NOI18N
    }
    return rn;
  }

  private static int getColumn(final int pos, final JTextComponent editor) {
    try {
      return pos - Utilities.getRowStart(editor, pos) + 1;
    } catch (BadLocationException e) {
      LOGGER.error("Bad location", e); //NOI18N
    }
    return -1;
  }

  protected JComponent preprocessToolBarButton(final JComponent button) {
    return button;
  }

  public JPanel getPanel() {
    return this.mainPanel;
  }

  private FileFilter makeFileFilter() {
    return new FileFilter() {

      @Override
      public boolean accept(final File f) {
        return f.isDirectory() || f.getName().toLowerCase(Locale.ENGLISH).endsWith(".txt"); //NOI18N
      }

      @Override
      public String getDescription() {
        return bundle.getString("PlainTextEditor.fileFilter.description");
      }
    };
  }

  protected Font findEditorFont(final Font defaultFont) {
    return defaultFont;
  }

  public boolean isCancelled() {
    return this.cancelled;
  }

  public void cancel() {
    this.cancelled = true;
  }

  public boolean isTextChanged() {
    return !this.originalText.equals(this.editorPane.getText());
  }

  private void updateRedoUndoState() {
    this.buttonUndo.setEnabled(this.undoManager.canUndo());
    this.buttonRedo.setEnabled(this.undoManager.canRedo());
  }

  private void doUndo() {
    if (this.undoManager.canUndo()) {
      this.undoManager.undo();
    }
    updateRedoUndoState();
  }

  private void doRedo() {
    if (this.undoManager.canRedo()) {
      this.undoManager.redo();
    }
    updateRedoUndoState();
  }

  private void updateCaretPos() {
    final int pos = this.editorPane.getCaretPosition();
    final int col = getColumn(pos, this.editorPane);
    final int row = getRow(pos, this.editorPane);
    this.labelCursorPos.setText(row + ":" + col); //NOI18N

    final String selectedText = this.editorPane.getSelectedText();
    if (StringUtils.isEmpty(selectedText)) {
      this.buttonCopy.setEnabled(false);
      this.buttonBrowse.setEnabled(false);
    } else {
      this.buttonCopy.setEnabled(true);
      try {
        final URI uri = URI.create(selectedText.trim());
        this.buttonBrowse.setEnabled(uri.isAbsolute());
      } catch (Exception ex) {
        this.buttonBrowse.setEnabled(false);
      }
    }
  }

  public void dispose() {

  }

  private void labelCursorPosMouseClicked(MouseEvent evt) {
  }

  private void labelWrapModeMouseClicked(MouseEvent evt) {
    this.wrapping = this.wrapping.next();
    updateWrapping();
  }

  private void updateWrapping() {
    this.editorPane.setWrapStyleWord(this.wrapping != Wrapping.CHAR_WRAP);
    this.editorPane.setLineWrap(this.wrapping != Wrapping.NONE);
    updateBottomPanel();
  }

  private void buttonImportActionPerformed(
      ActionEvent evt) {
    final File toOpen = this.dialogProvider
        .msgOpenFileDialog(this.parentSupplier.get(), null, "note-editor",
            this.bundle.getString("PlainTextEditor.buttonLoadActionPerformed.title"), null, true,
            new FileFilter[] {makeFileFilter()},
            this.bundle.getString("PlainTextEditor.buttonLoadActionPerformed.approve")); //NOI18N
    if (toOpen != null) {
      try {
        final String text = FileUtils.readFileToString(toOpen, UTF_8);
        this.editorPane.setText(text);
      } catch (Exception ex) {
        LOGGER.error("Error during text file loading", ex);
        this.dialogProvider.msgError(this.parentSupplier.get(),
            this.bundle.getString("PlainTextEditor.buttonLoadActionPerformed.msgError"));
      }
    }

  }

  private void buttonCopyActionPerformed(final ActionEvent evt) {
    StringSelection stringSelection = new StringSelection(this.editorPane.getSelectedText());
    final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    clipboard.setContents(stringSelection, null);
  }

  private void buttonPasteActionPerformed(ActionEvent evt) {
    try {
      this.editorPane.replaceSelection((String) Toolkit.getDefaultToolkit().getSystemClipboard()
          .getData(DataFlavor.stringFlavor));
    } catch (UnsupportedFlavorException ex) {
      // no text data in clipboard
    } catch (IOException ex) {
      LOGGER.error("Error during paste from clipboard", ex); //NOI18N
    }
  }

  private void buttonClearActionPerformed(ActionEvent evt) {
    this.editorPane.setText("");
  }

  private void buttonBrowseActionPerformed(ActionEvent evt) {
    final String selectedText = this.editorPane.getSelectedText().trim();
    try {
      onBrowseUri(URI.create(selectedText), false);
    } catch (Exception ex) {
      LOGGER.error("Can't open link : " + selectedText);
      this.dialogProvider
          .msgError(this.parentSupplier.get(),
              String.format(this.bundle.getString("PlainTextEditor.msgBrowseLinkError.text"),
                  selectedText));
    }
  }

  public abstract void onBrowseUri(final URI uri, final boolean flag) throws Exception;

  private void buttonUndoActionPerformed(
      ActionEvent evt) {
    doUndo();
  }

  private void buttonRedoActionPerformed(ActionEvent evt) {
    doRedo();
  }

  private void updateBottomPanel() {
    this.labelWrapMode.setText(
        String.format(this.bundle.getString("PlainTextEditor.wrap.prefix"),
            this.wrapping.getDisplay()));
  }

  public AbstractNoteEditorData getData() {
    return this.cancelled ? null :
        new AbstractNoteEditorData(this.editorPane.getText(), this.password, this.hint);
  }

  protected Icon findToolbarIconForId(IconId iconId) {
    return null;
  }

  private void initComponents() {
    this.mainPanel = this.uiComponentFactory.makePanel();
    this.mainPanel.setLayout(new BorderLayout());

    this.buttonBarPanel = this.uiComponentFactory.makePanel();
    this.buttonBarPanel.setLayout(new GridBagLayout());
    final GridBagConstraints constraints = new GridBagConstraints();
    constraints.insets = new Insets(4, 4, 4, 4);
    constraints.fill = GridBagConstraints.BOTH;
    constraints.gridy = 0;
    constraints.weightx = 1;
    constraints.weighty = 1;

    this.buttonUndo = this.uiComponentFactory.makeButton();
    this.buttonRedo = this.uiComponentFactory.makeButton();
    this.buttonImport = this.uiComponentFactory.makeButton();
    this.buttonExport = this.uiComponentFactory.makeButton();
    this.buttonCopy = this.uiComponentFactory.makeButton();
    this.buttonPaste = this.uiComponentFactory.makeButton();
    this.buttonBrowse = this.uiComponentFactory.makeButton();
    this.buttonClear = this.uiComponentFactory.makeButton();
    this.toggleButtonEncrypt = this.uiComponentFactory.makeToggleButton();
    this.bottomPanel = this.uiComponentFactory.makePanel();
    this.labelCursorPos = this.uiComponentFactory.makeLabel();
    this.wrapSeparator = this.uiComponentFactory.makeMenuSeparator();
    this.labelWrapMode = this.uiComponentFactory.makeLabel();
    this.wrapFiller =
        new Filler(new Dimension(16, 0), new Dimension(16, 0),
            new Dimension(16, 32767));
    this.editorScrollPane = this.uiComponentFactory.makeScrollPane();
    this.editorPane = this.uiComponentFactory.makeTextArea();

    this.buttonUndo
        .setIcon(this.findToolbarIconForId(IconId.UNDO));
    this.buttonUndo.setMnemonic('u');
    this.buttonUndo.setText(this.bundle.getString("NoteEditor.buttonUndo.text"));
    this.buttonUndo.setFocusable(false);
    this.buttonUndo.setHorizontalTextPosition(SwingConstants.CENTER);
    this.buttonUndo.setNextFocusableComponent(this.buttonRedo);
    this.buttonUndo.setVerticalTextPosition(SwingConstants.BOTTOM);
    this.buttonUndo.addActionListener(this::buttonUndoActionPerformed);
    this.buttonBarPanel.add(this.preprocessToolBarButton(this.buttonUndo), constraints);

    this.buttonRedo
        .setIcon(this.findToolbarIconForId(IconId.REDO));
    this.buttonRedo.setMnemonic('r');
    this.buttonRedo.setText(this.bundle.getString("NoteEditor.buttonRedo.text"));
    this.buttonRedo.setFocusable(false);
    this.buttonRedo.setHorizontalTextPosition(SwingConstants.CENTER);
    this.buttonRedo.setNextFocusableComponent(this.buttonImport);
    this.buttonRedo.setVerticalTextPosition(SwingConstants.BOTTOM);
    this.buttonRedo.addActionListener(this::buttonRedoActionPerformed);
    this.buttonBarPanel.add(this.preprocessToolBarButton(this.buttonRedo), constraints);

    this.buttonImport
        .setIcon(this.findToolbarIconForId(IconId.IMPORT));
    this.buttonImport.setMnemonic('i');
    this.buttonImport.setText(this.bundle.getString("NoteEditor.buttonImport.text"));
    this.buttonImport.setToolTipText(this.bundle.getString("NoteEditor.buttonImport.tooltip"));
    this.buttonImport.setFocusable(false);
    this.buttonImport.setHorizontalTextPosition(SwingConstants.CENTER);
    this.buttonImport.setNextFocusableComponent(this.buttonExport);
    this.buttonImport.setVerticalTextPosition(SwingConstants.BOTTOM);
    this.buttonImport.addActionListener(this::buttonImportActionPerformed);
    this.buttonBarPanel.add(this.preprocessToolBarButton(this.buttonImport), constraints);

    this.buttonExport.setIcon(this.findToolbarIconForId(IconId.EXPORT));
    this.buttonExport.setMnemonic('e');
    this.buttonExport.setText(this.bundle.getString("NoteEditor.buttonExport.text"));
    this.buttonExport.setToolTipText(this.bundle.getString("NoteEditor.buttonExport.tooltip"));
    this.buttonExport.setFocusable(false);
    this.buttonExport.setHorizontalTextPosition(SwingConstants.CENTER);
    this.buttonExport.setNextFocusableComponent(this.buttonCopy);
    this.buttonExport.setVerticalTextPosition(SwingConstants.BOTTOM);
    this.buttonExport.addActionListener(this::buttonExportActionPerformed);
    this.buttonBarPanel.add(this.preprocessToolBarButton(this.buttonExport), constraints);

    this.buttonCopy.setIcon(this.findToolbarIconForId(IconId.COPY));
    this.buttonCopy.setMnemonic('c');
    this.buttonCopy.setText(this.bundle.getString("NoteEditor.buttonCopy.text"));
    this.buttonCopy.setToolTipText(this.bundle.getString("NoteEditor.buttonCopy.tooltip"));
    this.buttonCopy.setFocusable(false);
    this.buttonCopy.setHorizontalTextPosition(SwingConstants.CENTER);
    this.buttonCopy.setNextFocusableComponent(this.buttonPaste);
    this.buttonCopy.setVerticalTextPosition(SwingConstants.BOTTOM);
    this.buttonCopy.addActionListener(this::buttonCopyActionPerformed);
    this.buttonBarPanel.add(this.preprocessToolBarButton(this.buttonCopy), constraints);

    this.buttonPaste.setIcon(this.findToolbarIconForId(IconId.PASTE));
    this.buttonPaste.setMnemonic('p');
    this.buttonPaste.setText(this.bundle.getString("NoteEditor.buttonPaste.text"));
    this.buttonPaste.setToolTipText(this.bundle.getString("NoteEditor.buttonPaste.tooltip"));
    this.buttonPaste.setFocusable(false);
    this.buttonPaste.setHorizontalTextPosition(SwingConstants.CENTER);
    this.buttonPaste.setNextFocusableComponent(this.buttonBrowse);
    this.buttonPaste.setVerticalTextPosition(SwingConstants.BOTTOM);
    this.buttonPaste.addActionListener(this::buttonPasteActionPerformed);
    this.buttonBarPanel.add(this.preprocessToolBarButton(this.buttonPaste), constraints);

    this.buttonBrowse
        .setIcon(this.findToolbarIconForId(IconId.BROWSE));
    this.buttonBrowse.setMnemonic('b');
    this.buttonBrowse.setText(this.bundle.getString("NoteEditor.buttonBrowse.text"));
    this.buttonBrowse.setToolTipText(this.bundle.getString("NoteEditor.buttonBrowse.tooltip"));
    this.buttonBrowse.setFocusable(false);
    this.buttonBrowse.setHorizontalTextPosition(SwingConstants.CENTER);
    this.buttonBrowse.setNextFocusableComponent(this.buttonClear);
    this.buttonBrowse.setVerticalTextPosition(SwingConstants.BOTTOM);
    this.buttonBrowse.addActionListener(this::buttonBrowseActionPerformed);
    this.buttonBarPanel.add(this.preprocessToolBarButton(this.buttonBrowse), constraints);

    this.buttonClear
        .setIcon(this.findToolbarIconForId(IconId.CLEARALL));
    this.buttonClear.setMnemonic('a');
    this.buttonClear.setText(this.bundle.getString("NoteEditor.buttonClearAll.text"));
    this.buttonClear.setToolTipText(this.bundle.getString("NoteEditor.buttonClearAll.tooltip"));
    this.buttonClear.setFocusable(false);
    this.buttonClear.setHorizontalTextPosition(SwingConstants.CENTER);
    this.buttonClear.setNextFocusableComponent(this.editorPane);
    this.buttonClear.setVerticalTextPosition(SwingConstants.BOTTOM);
    this.buttonClear.addActionListener(this::buttonClearActionPerformed);
    this.buttonBarPanel.add(this.preprocessToolBarButton(this.buttonClear), constraints);

    this.toggleButtonEncrypt.setIcon(this.findToolbarIconForId(IconId.PASSWORD_OFF));
    this.toggleButtonEncrypt.setSelectedIcon(this.findToolbarIconForId(IconId.PASSWORD_ON));
    this.toggleButtonEncrypt.setText(this.bundle.getString("NoteEditor.buttonProtect.text"));
    this.toggleButtonEncrypt.setToolTipText(
        this.bundle.getString("NoteEditor.buttonProtect.tooltip"));
    this.toggleButtonEncrypt.setFocusable(false);
    this.toggleButtonEncrypt.setHorizontalTextPosition(SwingConstants.CENTER);
    this.toggleButtonEncrypt.setVerticalTextPosition(SwingConstants.BOTTOM);
    this.toggleButtonEncrypt.addActionListener(this::toggleButtonEncryptActionPerformed);
    this.buttonBarPanel.add(this.preprocessToolBarButton(this.toggleButtonEncrypt), constraints);

    constraints.weightx = 1000;
    this.buttonBarPanel.add(Box.createHorizontalGlue(), constraints);

    this.mainPanel.add(this.buttonBarPanel, BorderLayout.NORTH);

    this.bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

    this.labelCursorPos.setText("...:...");
    this.labelCursorPos.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(MouseEvent evt) {
        labelCursorPosMouseClicked(evt);
      }
    });
    this.bottomPanel.add(this.labelCursorPos);

    this.wrapSeparator.setOrientation(SwingConstants.VERTICAL);
    this.wrapSeparator.setPreferredSize(new Dimension(8, 16));
    this.bottomPanel.add(this.wrapSeparator);

    this.labelWrapMode.setText("...");
    this.labelWrapMode.setCursor(new Cursor(Cursor.HAND_CURSOR));
    this.labelWrapMode.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(MouseEvent evt) {
        labelWrapModeMouseClicked(evt);
      }
    });
    this.bottomPanel.add(this.labelWrapMode);
    this.bottomPanel.add(this.wrapFiller);

    this.mainPanel.add(this.bottomPanel, BorderLayout.PAGE_END);

    this.editorPane.setColumns(20);
    this.editorPane.setRows(5);
    this.editorPane.setNextFocusableComponent(this.buttonUndo);
    this.editorScrollPane.setViewportView(this.editorPane);

    this.mainPanel.add(this.editorScrollPane, BorderLayout.CENTER);
  }

  private void buttonExportActionPerformed(ActionEvent evt) {
    final File toSave = this.dialogProvider.msgSaveFileDialog(null, null, "note-editor",
        MmcI18n.getInstance().findBundle()
            .getString("PlainTextEditor.buttonSaveActionPerformed.saveTitle"), null,
        true, new FileFilter[] {makeFileFilter()}, MmcI18n.getInstance().findBundle()
            .getString("PlainTextEditor.buttonSaveActionPerformed.approve")); //NOI18N
    if (toSave != null) {
      try {
        final String text = this.editorPane.getText();
        FileUtils.writeStringToFile(toSave, text, UTF_8);
      } catch (final Exception ex) {
        LOGGER.error("Error during text file saving", ex);
        this.dialogProvider.msgError(this.parentSupplier.get(),
            MmcI18n.getInstance().findBundle()
                .getString("PlainTextEditor.buttonSaveActionPerformed.msgError"));
      }
    }
  }

  private void toggleButtonEncryptActionPerformed(ActionEvent evt) {
    final ResourceBundle resourceBundle = MmdI18n.getInstance().findBundle();
    final JToggleButton src = (JToggleButton) evt.getSource();
    if (src.isSelected()) {
      final PasswordPanel passwordPanel = new PasswordPanel();
      if (this.dialogProvider.msgOkCancel(this.parentSupplier.get(),
          resourceBundle.getString("PasswordPanel.dialogPassword.set.title"),
          passwordPanel)) {
        this.password = new String(passwordPanel.getPassword()).trim();
        this.hint = passwordPanel.getHint();
        if (this.password.isEmpty()) {
          src.setSelected(false);
        }
      } else {
        src.setSelected(false);
      }
    } else {
      if (this.dialogProvider
          .msgConfirmOkCancel(this.parentSupplier.get(),
              this.bundle.getString("PasswordPanel.msgResetPassword.title"),
              this.bundle.getString("PasswordPanel.msgResetPassword.msg"))) {
        this.password = null;
        this.hint = null;
      } else {
        src.setSelected(true);
      }
    }
  }

  public enum IconId {
    REDO, IMPORT, UNDO, COPY, EXPORT, BROWSE, CLEARALL, PASSWORD_ON, PASSWORD_OFF, PASTE

  }

  private enum Wrapping {

    NONE("none", "off"),
    CHAR_WRAP("char", "char"),
    WORD_WRAP("word", "word");

    private final String value;
    private final String display;

    private Wrapping(final String val, final String display) {
      this.value = val;
      this.display = display;
    }

    public static Wrapping findFor(final String text) {
      for (final Wrapping w : Wrapping.values()) {
        if (w.value.equalsIgnoreCase(text)) {
          return w;
        }
      }
      return NONE;
    }

    public String getValue() {
      return this.value;
    }

    public String getDisplay() {
      return this.display;
    }

    public Wrapping next() {
      final int index = this.ordinal() + 1;
      if (index >= Wrapping.values().length) {
        return NONE;
      } else {
        return Wrapping.values()[index];
      }
    }
  }


}

