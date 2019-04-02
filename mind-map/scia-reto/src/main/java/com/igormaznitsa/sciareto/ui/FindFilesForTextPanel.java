/* 
 * Copyright (C) 2018 Igor Maznitsa.
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
package com.igormaznitsa.sciareto.ui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.sciareto.Context;
import com.igormaznitsa.sciareto.ui.misc.FileExaminator;
import com.igormaznitsa.sciareto.ui.misc.NodeListRenderer;
import com.igormaznitsa.sciareto.ui.tree.NodeFileOrFolder;

public final class FindFilesForTextPanel extends javax.swing.JPanel {

  private static final long serialVersionUID = 8076096265342142731L;

  private static final Logger LOGGER = LoggerFactory.getLogger(FindFilesForTextPanel.class);

  private final AtomicReference<Thread> searchingThread = new AtomicReference<>();
  private final transient List<NodeFileOrFolder> foundFiles = new ArrayList<>();
  private final transient List<ListDataListener> listListeners = new ArrayList<>();

  private static final int MIN_TEXT_LENGTH = 1;

  private final NodeFileOrFolder folder;

  private static volatile String CHARSET = "UTF-8";

  private static final class TheLocale implements Comparable<TheLocale> {

    private final Locale locale;

    public TheLocale(final Locale locale) {
      this.locale = locale;
    }

    @Override
    public int hashCode() {
      return this.locale.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
      if (obj == null) {
        return false;
      }
      if (obj instanceof Locale) {
        return this.locale.equals(obj);
      } else if (obj instanceof TheLocale) {
        return this.locale.equals(((TheLocale) obj).locale);
      }
      return false;
    }

    @Override
    public String toString() {
      return this.locale.getDisplayName();
    }

    @Override
    public int compareTo(final TheLocale o) {
      if (o == this || o.locale == this.locale) {
        return 0;
      }
      return this.locale.getDisplayName().compareTo(o.locale.getDisplayName());
    }
  }

  private static volatile TheLocale LOCALE = new TheLocale(Locale.ENGLISH);
  private final Object okDialogExit;

  @SuppressWarnings("ResultOfObjectAllocationIgnored")
  public FindFilesForTextPanel(@Nonnull final Context context, @Nonnull final NodeFileOrFolder itemToFind, @Nullable final Object okDialogExit) {
    super();
    initComponents();

    this.okDialogExit = okDialogExit;

    this.folder = itemToFind;

    this.fieldText.setText("");
    this.buttonFind.setEnabled(false);

    this.fieldText.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) {
        updateStateForText();
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        updateStateForText();
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        updateStateForText();
      }
    });

    this.fieldText.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          e.consume();
          if (fieldText.getText().length() >= MIN_TEXT_LENGTH) {
            buttonFind.doClick();
          }
        }
      }

    });

    this.listOfFoundElements.setCellRenderer(new NodeListRenderer());
    this.listOfFoundElements.setModel(new ListModel<NodeFileOrFolder>() {
      @Override
      public int getSize() {
        return foundFiles.size();
      }

      @Override
      @Nonnull
      public NodeFileOrFolder getElementAt(final int index) {
        return foundFiles.get(index);
      }

      @Override
      public void addListDataListener(@Nonnull final ListDataListener l) {
        listListeners.add(l);
      }

      @Override
      public void removeListDataListener(@Nonnull final ListDataListener l) {
        listListeners.remove(l);
      }

    });

    final ComboBoxModel<String> charsets = new DefaultComboBoxModel<>(Charset.availableCharsets().keySet().toArray(new String[0]));
    this.comboCharsets.setModel(charsets);
    this.comboCharsets.setSelectedItem(CHARSET);
    this.comboCharsets.revalidate();

    final Locale[] allLocales = Locale.getAvailableLocales();
    final TheLocale[] theLocales = new TheLocale[allLocales.length];
    for (int i = 0; i < allLocales.length; i++) {
      theLocales[i] = new TheLocale(allLocales[i]);
    }
    Arrays.sort(theLocales);

    final ComboBoxModel<TheLocale> locales = new DefaultComboBoxModel<>(theLocales);
    this.comboLocale.setModel(locales);
    this.comboLocale.setSelectedItem(LOCALE);

    this.comboCharsets.addActionListener((ActionEvent e) -> {
      CHARSET = comboCharsets.getSelectedItem().toString();
    });

    this.comboLocale.addActionListener((ActionEvent e) -> {
      LOCALE = (TheLocale) comboLocale.getSelectedItem();
    });

    this.comboLocale.revalidate();

    new Focuser(this.fieldText);
    UiUtils.makeOwningDialogResizable(this);

    revalidate();
    doLayout();
  }

  private void updateStateForText() {
    final String text = this.fieldText.getText();
    if (text.length() >= MIN_TEXT_LENGTH) {
      this.buttonFind.setEnabled(true);
    } else {
      this.buttonFind.setEnabled(false);
    }
  }

  @Nullable
  public NodeFileOrFolder getSelected() {
    return this.listOfFoundElements.getSelectedValue();
  }

  public void dispose() {
    final Thread thread = this.searchingThread.getAndSet(null);
    if (thread != null) {
      thread.interrupt();
    }
  }

  private void addFileIntoList(@Nonnull final NodeFileOrFolder file) {
    SwingUtilities.invokeLater(() -> {
      final boolean first = foundFiles.isEmpty();

      foundFiles.add(file);
      listListeners.forEach((l) -> {
        l.intervalAdded(new ListDataEvent(listOfFoundElements, ListDataEvent.INTERVAL_ADDED, foundFiles.size() - 1, foundFiles.size() - 1));
      });

      if (first) {
        listOfFoundElements.setSelectedIndex(0);
      }
    });
  }

  private void startSearchThread(@Nonnull @MustNotContainNull final List<NodeFileOrFolder> scope, @Nonnull final byte[] dataToFindVariant1, @Nonnull final byte[] dataToFindVariant2) {
    int size = 0;
    size = scope.stream().map((p) -> p.size()).reduce(size, Integer::sum);

    final byte[] fileOpBuffer = new byte[1024 * 1024];

    final Runnable runnable = new Runnable() {
      int value = 0;

      private void processFile(final NodeFileOrFolder file) {
        value++;
        final File f = file.makeFileForNode();

        try {
          if (new FileExaminator(f).doesContainData(fileOpBuffer, dataToFindVariant1, dataToFindVariant2)) {
            addFileIntoList(file);
          }
        } catch (Exception ex) {
          LOGGER.error("Error during text search in '" + f + '\'', ex);
        }

        if (!Thread.currentThread().isInterrupted()) {
          safeSetProgressValue(value);
        }
      }

      private void processFolder(final NodeFileOrFolder folder) {
        value++;
        for (final NodeFileOrFolder f : folder) {
          if (f.isLeaf()) {
            processFile(f);
          } else {
            processFolder(f);
          }
        }
        if (!Thread.currentThread().isInterrupted()) {
          safeSetProgressValue(value);
        }
      }

      @Override
      public void run() {
        for (final NodeFileOrFolder p : scope) {
          for (final NodeFileOrFolder f : p) {
            if (Thread.currentThread().isInterrupted()) {
              return;
            }
            if (f.isLeaf()) {
              processFile(f);
            } else {
              processFolder(f);
            }
          }
        }
        safeSetProgressValue(Integer.MAX_VALUE);
        SwingUtilities.invokeLater(() -> {
          buttonFind.setEnabled(true);
          fieldText.setEnabled(true);
          comboCharsets.setEnabled(true);
          comboLocale.setEnabled(true);
          if (foundFiles.isEmpty()) {
            fieldText.requestFocus();
          } else {
            listOfFoundElements.requestFocus();
          }
        });
      }
    };

    final Thread thread = new Thread(runnable, "SciaRetoSearchUsage"); //NOI18N
    thread.setDaemon(true);

    final Thread oldThread = this.searchingThread.getAndSet(thread);
    if (oldThread != null) {
      oldThread.interrupt();
      try {
        oldThread.join(1000L);
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
        LOGGER.error("Exception during waiting of search thread interruption", ex); //NOI18N
      }
    }

    this.progressBarSearch.setMinimum(0);
    this.progressBarSearch.setMaximum(size);
    this.progressBarSearch.setValue(0);

    thread.start();

  }

  private void safeSetProgressValue(final int value) {
    SwingUtilities.invokeLater(() -> {
      if (value == Integer.MAX_VALUE) {
        progressBarSearch.setEnabled(false);
        progressBarSearch.setIndeterminate(false);
        progressBarSearch.setValue(0);
      } else if (value < 0) {
        progressBarSearch.setEnabled(true);
        progressBarSearch.setIndeterminate(true);
      } else {
        progressBarSearch.setEnabled(true);
        progressBarSearch.setIndeterminate(false);
        progressBarSearch.setValue(value);
      }
    });
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    jPanel1 = new javax.swing.JPanel();
    jLabel1 = new javax.swing.JLabel();
    fieldText = new javax.swing.JTextField();
    buttonFind = new javax.swing.JButton();
    jPanel3 = new javax.swing.JPanel();
    filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
    jLabel2 = new javax.swing.JLabel();
    comboCharsets = new javax.swing.JComboBox();
    jLabel3 = new javax.swing.JLabel();
    comboLocale = new javax.swing.JComboBox<>();
    filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(16, 0), new java.awt.Dimension(16, 0), new java.awt.Dimension(16, 32767));
    jPanel2 = new javax.swing.JPanel();
    progressBarSearch = new javax.swing.JProgressBar();
    jScrollPane1 = new javax.swing.JScrollPane();
    listOfFoundElements = new javax.swing.JList<>();

    setLayout(new java.awt.BorderLayout());

    jPanel1.setLayout(new java.awt.BorderLayout());

    jLabel1.setText("Text to search: ");
    jPanel1.add(jLabel1, java.awt.BorderLayout.WEST);

    fieldText.setText("jTextField1");
    fieldText.setToolTipText("Press 'Enter' or the 'Find' button for search");
    jPanel1.add(fieldText, java.awt.BorderLayout.CENTER);

    buttonFind.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/find16.png"))); // NOI18N
    buttonFind.setText("Find");
    buttonFind.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        buttonFindActionPerformed(evt);
      }
    });
    jPanel1.add(buttonFind, java.awt.BorderLayout.LINE_END);

    jPanel3.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 1, 3, 1));
    jPanel3.setLayout(new java.awt.GridBagLayout());
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 5;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.weightx = 1000.0;
    jPanel3.add(filler1, gridBagConstraints);

    jLabel2.setText("Charset: ");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    jPanel3.add(jLabel2, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    jPanel3.add(comboCharsets, gridBagConstraints);

    jLabel3.setText("Locale:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 0;
    jPanel3.add(jLabel3, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 4;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.weighty = 100.0;
    jPanel3.add(comboLocale, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 0;
    jPanel3.add(filler2, gridBagConstraints);

    jPanel1.add(jPanel3, java.awt.BorderLayout.PAGE_END);

    add(jPanel1, java.awt.BorderLayout.PAGE_START);

    jPanel2.setLayout(new java.awt.BorderLayout());
    jPanel2.add(progressBarSearch, java.awt.BorderLayout.NORTH);

    listOfFoundElements.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    listOfFoundElements.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
      public void mouseMoved(java.awt.event.MouseEvent evt) {
        listOfFoundElementsMouseMoved(evt);
      }
    });
    listOfFoundElements.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        listOfFoundElementsMouseClicked(evt);
      }
    });
    jScrollPane1.setViewportView(listOfFoundElements);

    jPanel2.add(jScrollPane1, java.awt.BorderLayout.CENTER);

    add(jPanel2, java.awt.BorderLayout.CENTER);
  }// </editor-fold>//GEN-END:initComponents

  private void buttonFindActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonFindActionPerformed
    this.fieldText.setEnabled(false);
    this.buttonFind.setEnabled(false);
    this.comboCharsets.setEnabled(false);
    this.comboLocale.setEnabled(false);

    this.listOfFoundElements.clearSelection();
    this.foundFiles.clear();
    this.listOfFoundElements.revalidate();
    this.listOfFoundElements.repaint();

    final List<NodeFileOrFolder> folders = new ArrayList<>();
    folders.add(this.folder);

    final Locale selectedLocale = ((TheLocale) this.comboLocale.getSelectedItem()).locale;

    try {
      final byte[] str1 = this.fieldText.getText().toLowerCase(selectedLocale).getBytes(this.comboCharsets.getSelectedItem().toString());
      final byte[] str2 = this.fieldText.getText().toUpperCase(selectedLocale).getBytes(this.comboCharsets.getSelectedItem().toString());
      LOGGER.info("Start find byte patterns: " + SystemUtils.toString(str1) + ", " + SystemUtils.toString(str2));
      startSearchThread(folders, str1, str2);
    } catch (UnsupportedEncodingException ex) {
      JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
    }
  }//GEN-LAST:event_buttonFindActionPerformed

  private void listOfFoundElementsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listOfFoundElementsMouseClicked
    if (evt.getClickCount() > 1 && !evt.isPopupTrigger() && this.listOfFoundElements.getSelectedIndex() >= 0) {
      UiUtils.closeCurrentDialogWithResult(this, this.okDialogExit);
    }
  }//GEN-LAST:event_listOfFoundElementsMouseClicked

  private void listOfFoundElementsMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listOfFoundElementsMouseMoved
    final ListModel model = this.listOfFoundElements.getModel();
    final int index = this.listOfFoundElements.locationToIndex(evt.getPoint());
    if (index < 0) {
      this.listOfFoundElements.setToolTipText(null);
    } else {
      final File file = ((NodeFileOrFolder) model.getElementAt(index)).makeFileForNode();
      this.listOfFoundElements.setToolTipText(file == null ? null : file.getAbsolutePath());
    }
  }//GEN-LAST:event_listOfFoundElementsMouseMoved


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton buttonFind;
  private javax.swing.JComboBox comboCharsets;
  private javax.swing.JComboBox<TheLocale> comboLocale;
  private javax.swing.JTextField fieldText;
  private javax.swing.Box.Filler filler1;
  private javax.swing.Box.Filler filler2;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JLabel jLabel3;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JPanel jPanel2;
  private javax.swing.JPanel jPanel3;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JList<NodeFileOrFolder> listOfFoundElements;
  private javax.swing.JProgressBar progressBarSearch;
  // End of variables declaration//GEN-END:variables
}
