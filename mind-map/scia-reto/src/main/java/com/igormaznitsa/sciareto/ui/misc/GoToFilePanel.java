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
package com.igormaznitsa.sciareto.ui.misc;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.ListModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import com.igormaznitsa.mindmap.swing.panel.utils.Focuser;
import com.igormaznitsa.sciareto.ui.UiUtils;
import com.igormaznitsa.sciareto.ui.tree.ExplorerTree;
import com.igormaznitsa.sciareto.ui.tree.NodeFileOrFolder;

public class GoToFilePanel extends javax.swing.JPanel implements Comparator<NodeFileOrFolder> {

  private static final long serialVersionUID = 6372355072139143322L;

  private final ExplorerTree tree;

  private final transient List<NodeFileOrFolder> foundNodeList = new ArrayList<>();
  private final transient List<ListDataListener> listeners = new ArrayList<>();

  private final Object dialogOkObject;
  
  @Override
  public int compare(@Nonnull final NodeFileOrFolder o1, @Nonnull final NodeFileOrFolder o2) {
    return o1.toString().compareTo(o2.toString());
  }

  @SuppressWarnings("ResultOfObjectAllocationIgnored")
  public GoToFilePanel(@Nonnull final ExplorerTree tree, @Nullable final Object dialogOkObject) {
    super();
    this.tree = tree;
    this.dialogOkObject = dialogOkObject;
    initComponents();

    this.listFoundFiles.setCellRenderer(new NodeListRenderer());

    final Dimension dim = new Dimension(512, 400);
    setPreferredSize(dim);
    setMinimumSize(dim);
    setMaximumSize(dim);

    new Focuser(this.textFieldMask);
    this.textFieldMask.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(@Nonnull final DocumentEvent e) {
        processEnteredPattern();
      }

      @Override
      public void removeUpdate(@Nonnull final DocumentEvent e) {
        processEnteredPattern();
      }

      @Override
      public void changedUpdate(@Nonnull final DocumentEvent e) {
        processEnteredPattern();
      }
    });

    this.listFoundFiles.setModel(new ListModel<NodeFileOrFolder>() {

      @Override
      public int getSize() {
        return foundNodeList.size();
      }

      @Override
      @Nonnull
      public NodeFileOrFolder getElementAt(final int index) {
        return foundNodeList.get(index);
      }

      @Override
      public void addListDataListener(@Nonnull final ListDataListener l) {
        listeners.add(l);
      }

      @Override
      public void removeListDataListener(@Nonnull final ListDataListener l) {
        listeners.remove(l);
      }

    });
  }

  @Nullable
  public NodeFileOrFolder getSelected() {
    return this.listFoundFiles.getSelectedValue();
  }

  private void processEnteredPattern() {
    this.foundNodeList.clear();
    this.foundNodeList.addAll(this.tree.findForNamePattern(makePattern(this.textFieldMask.getText())));
    for (final ListDataListener l : this.listeners) {
      l.contentsChanged(new ListDataEvent(this.listFoundFiles.getModel(), ListDataEvent.CONTENTS_CHANGED, 0, this.foundNodeList.size()));
    }

    Collections.sort(this.foundNodeList, this);

    if (!this.foundNodeList.isEmpty()) {
      this.listFoundFiles.setSelectedIndex(0);
      this.listFoundFiles.ensureIndexIsVisible(0);
    }
  }

  @Nullable
  private Pattern makePattern(@Nonnull final String text) {
    if (text.isEmpty()) {
      return null;
    }
    final StringBuilder buffer = new StringBuilder();
    for (final char c : text.toCharArray()) {
      switch (c) {
        case '?':
          buffer.append('.');
          break;
        case '*':
          buffer.append(".*"); //NOI18N
          break;
        default: {
          if (Character.isWhitespace(c) || Character.isISOControl(c)) {
            buffer.append("\\s"); //NOI18N
          } else {
            final String code = Integer.toHexString(c).toUpperCase(Locale.ENGLISH);
            buffer.append("\\u").append("0000", 0, 4 - code.length()).append(code); //NOI18N
          }
        }
        break;
      }
    }
    buffer.append(".*"); //NOI18N
    return Pattern.compile(buffer.toString(), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form
   * Editor.
   */
  @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        labelFilenameMask = new javax.swing.JLabel();
        textFieldMask = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        listFoundFiles = new javax.swing.JList<>();

        setLayout(new java.awt.GridBagLayout());

        labelFilenameMask.setText(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("panelGoToFilePanel.labelFileName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        add(labelFilenameMask, gridBagConstraints);

        textFieldMask.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                textFieldMaskKeyPressed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1000.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        add(textFieldMask, gridBagConstraints);

        listFoundFiles.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        listFoundFiles.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                listFoundFilesMouseMoved(evt);
            }
        });
        listFoundFiles.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                listFoundFilesMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(listFoundFiles);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1000.0;
        gridBagConstraints.weighty = 1000.0;
        add(jScrollPane1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

  private void listFoundFilesMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listFoundFilesMouseMoved
    final ListModel model = this.listFoundFiles.getModel();
    final int index = this.listFoundFiles.locationToIndex(evt.getPoint());
    if (index < 0) {
      this.listFoundFiles.setToolTipText(null);
    } else {
      final File file = ((NodeFileOrFolder) model.getElementAt(index)).makeFileForNode();
      this.listFoundFiles.setToolTipText(file == null ? null : file.getAbsolutePath());
    }
  }//GEN-LAST:event_listFoundFilesMouseMoved

  private void textFieldMaskKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textFieldMaskKeyPressed
    int selectedIndex = this.listFoundFiles.getSelectedIndex();
    boolean processed = false;
    if (!evt.isConsumed() && evt.getModifiers() == 0) {
      switch (evt.getKeyCode()) {
        case KeyEvent.VK_UP: {
          processed = true;
          evt.consume();
          if (selectedIndex >= 0) {
            selectedIndex--;
          }
        }
        break;
        case KeyEvent.VK_DOWN: {
          processed = true;
          evt.consume();
          if (selectedIndex >= 0) {
            selectedIndex++;
          }
        }
        break;
      }

      if (processed && !this.foundNodeList.isEmpty()) {
        if (selectedIndex < 0) {
          selectedIndex = this.foundNodeList.size() - 1;
        } else if (selectedIndex >= this.foundNodeList.size()) {
          selectedIndex = 0;
        }
        this.listFoundFiles.setSelectedIndex(selectedIndex);
        this.listFoundFiles.ensureIndexIsVisible(selectedIndex);
      }

    }
  }//GEN-LAST:event_textFieldMaskKeyPressed

  private void listFoundFilesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listFoundFilesMouseClicked
    if (evt.getClickCount()>1 && !evt.isPopupTrigger() && this.listFoundFiles.getSelectedIndex()>=0) {
      UiUtils.closeCurrentDialogWithResult(this, this.dialogOkObject);
    }
  }//GEN-LAST:event_listFoundFilesMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel labelFilenameMask;
    private javax.swing.JList<NodeFileOrFolder> listFoundFiles;
    private javax.swing.JTextField textFieldMask;
    // End of variables declaration//GEN-END:variables
}
