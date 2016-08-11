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
package com.igormaznitsa.sciareto.ui.misc;

import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.apache.commons.io.FilenameUtils;
import com.igormaznitsa.sciareto.Context;
import com.igormaznitsa.sciareto.ui.Focuser;
import com.igormaznitsa.sciareto.ui.Icons;
import com.igormaznitsa.sciareto.ui.MainFrame;
import com.igormaznitsa.sciareto.ui.editors.PictureViewer;
import com.igormaznitsa.sciareto.ui.tree.ExplorerTree;
import com.igormaznitsa.sciareto.ui.tree.NodeFileOrFolder;
import com.igormaznitsa.sciareto.ui.tree.NodeProject;
import com.igormaznitsa.sciareto.ui.tree.TreeCellRenderer;

public class GoToFilePanel extends javax.swing.JPanel {

  private static final long serialVersionUID = 6372355072139143322L;

  private final ExplorerTree tree;

  private final List<NodeFileOrFolder> foundNodeList = new ArrayList<>();
  private final List<ListDataListener> listeners = new ArrayList<>();
  
  private static class ListRenderer extends DefaultListCellRenderer {
    
    private static final long serialVersionUID = 3875614392486198647L;

    public ListRenderer() {
      super();
    }

    @Nonnull
    private static String makeTextForNode(@Nonnull final NodeFileOrFolder node){
        final NodeProject project = node.findProject();
        if (project==null){
          return node.toString();
        } else {
          final String projectName = project.toString();
          return node.toString()+" (found in "+projectName+')';
        }
    }
    
    @Override
    @Nonnull
    public Component getListCellRendererComponent(@Nonnull final JList<?> list, @Nonnull final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
      super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

      final NodeFileOrFolder node = (NodeFileOrFolder) value;
      
      final String ext = FilenameUtils.getExtension(node.toString()).toLowerCase(Locale.ENGLISH); 
      if (node instanceof NodeProject || !node.isLeaf()){
        this.setIcon(TreeCellRenderer.DEFAULT_FOLDER_CLOSED);
      } else if (ext.equals("mmd")) {
        this.setIcon(Icons.DOCUMENT.getIcon());
      } else if (PictureViewer.SUPPORTED_FORMATS.contains(ext)) {
        this.setIcon(TreeCellRenderer.ICON_IMAGE);
      } else {
        this.setIcon(TreeCellRenderer.DEFAULT_FILE);
      }

      this.setText(makeTextForNode(node));
      
      return this;
    }
    
  }
  
  public GoToFilePanel(@Nonnull final ExplorerTree tree) {
    this.tree = tree;
    
    initComponents();
    
    this.listFoundFiles.setCellRenderer(new ListRenderer());
    
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
    
    this.listFoundFiles.setModel(new ListModel<NodeFileOrFolder>(){
      
      
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
    for(final ListDataListener l : this.listeners){
      l.contentsChanged(new ListDataEvent(this.listFoundFiles.getModel(), ListDataEvent.CONTENTS_CHANGED, 0, this.foundNodeList.size()));
    }
    if (!this.foundNodeList.isEmpty()){
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
          buffer.append(".*");
          break;
        default: {
          if (Character.isWhitespace(c) || Character.isISOControl(c)) {
            buffer.append("\\s");
          } else {
            final String code = Integer.toHexString(c).toUpperCase(Locale.ENGLISH);
            buffer.append("\\u").append("0000",0,4-code.length()).append(code);
          }
        }
        break;
      }
    }
    buffer.append(".*");
    return Pattern.compile(buffer.toString(), WIDTH);
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

    labelFilenameMask.setText("File Name prefix (wildcards: ? and *)");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
    add(labelFilenameMask, gridBagConstraints);
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
    if (index<0){
      this.listFoundFiles.setToolTipText(null);
    } else {
      final File file = ((NodeFileOrFolder) model.getElementAt(index)).makeFileForNode();
      this.listFoundFiles.setToolTipText(file == null ? null : file.getAbsolutePath());
    }
  }//GEN-LAST:event_listFoundFilesMouseMoved


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JLabel labelFilenameMask;
  private javax.swing.JList<NodeFileOrFolder> listFoundFiles;
  private javax.swing.JTextField textFieldMask;
  // End of variables declaration//GEN-END:variables
}
