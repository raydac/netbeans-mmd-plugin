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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.sciareto.Context;
import com.igormaznitsa.sciareto.ui.misc.NodeListRenderer;
import com.igormaznitsa.sciareto.ui.tree.NodeFileOrFolder;
import com.igormaznitsa.sciareto.ui.tree.NodeProject;

public class FindUsagesPanel extends javax.swing.JPanel {

  private static final long serialVersionUID = -2670972411220199031L;

  private final AtomicReference<Thread> searchingThread = new AtomicReference<>();

  private static final Logger LOGGER = LoggerFactory.getLogger(FindUsagesPanel.class);

  private final transient List<NodeFileOrFolder> foundFiles = new ArrayList<>();
  private final transient  List<ListDataListener> listListeners = new ArrayList<>();

  private final String fullNormalizedPath;

  private final boolean findEverywhere;
  
  public FindUsagesPanel(@Nonnull final Context context, @Nonnull final NodeFileOrFolder itemToFind, final boolean findEverywhere) {
    initComponents();

    this.findEverywhere = findEverywhere;
    
    final File asfile = itemToFind.makeFileForNode();

    if (asfile == null) {
      LOGGER.error("Can't get file for node " + itemToFind); //NOI18N
      throw new IllegalArgumentException("Can't get file for node"); //NOI18N
    }

    this.fullNormalizedPath = FilenameUtils.normalize(asfile.getAbsolutePath());

    this.textFieldSearchPath.setText(this.fullNormalizedPath);
    this.textFieldSearchPath.setEnabled(false);

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

    final List<NodeProject> scope = new ArrayList<>();

    for (final NodeFileOrFolder p : context.getCurrentGroup()) {
      scope.add((NodeProject) p);
    }

    UiUtils.makeOwningDialogResizable(this);
    startSearchThread(scope, itemToFind);
  }

  @Nullable
  public NodeFileOrFolder getSelected() {
    return this.listOfFoundElements.getSelectedValue();
  }

  private void addFileIntoList(@Nonnull final NodeFileOrFolder file) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        final boolean first = foundFiles.isEmpty();
        
        foundFiles.add(file);
        for (final ListDataListener l : listListeners) {
          l.intervalAdded(new ListDataEvent(listOfFoundElements, ListDataEvent.INTERVAL_ADDED, foundFiles.size() - 1, foundFiles.size() - 1));
        }
        
        if (first){
          listOfFoundElements.setSelectedIndex(0);
        }
        
      }
    });
  }

  private void startSearchThread(@Nonnull @MustNotContainNull final List<NodeProject> scope, @Nonnull final NodeFileOrFolder itemToFind) {
    int size = 0;
    for (final NodeProject p : scope) {
      size += p.size();
    }

    final File nodeFileToSearch = itemToFind.makeFileForNode();

    if (nodeFileToSearch == null) {
      safeSetProgressValue(Integer.MAX_VALUE);
    } else {

      final Runnable runnable = new Runnable() {

        int value = 0;

        private void processFile(final NodeFileOrFolder file) {
          value++;

          final File f = file.makeFileForNode();
          final NodeProject project = file.findProject();
          if (project != null) {
            final String extension = FilenameUtils.getExtension(f.getName()).toLowerCase(Locale.ENGLISH);
            if ("mmd".equals(extension)) { //NOI18N
              Reader reader = null;
              try {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8")); //NOI18N
                final MindMap map = new MindMap(null, reader);
                if (!MapUtils.findTopicsRelatedToFile(project.getFolder(), nodeFileToSearch, map).isEmpty()) {
                  addFileIntoList(file);
                }
              } catch (Exception ex) {
                LOGGER.error("Can't parse map", ex); //NOI18N
              } finally {
                IOUtils.closeQuietly(reader);
              }
            } else if (findEverywhere){
              try {
                final LineIterator lineIterator = org.apache.commons.io.FileUtils.lineIterator(f, "UTF-8"); //NOI18N
                try {
                  while (lineIterator.hasNext()) {
                    if (Thread.currentThread().isInterrupted()) {
                      return;
                    }
                    final String lineFromFile = lineIterator.nextLine();
                    if (lineFromFile.contains(fullNormalizedPath)) {
                      addFileIntoList(file);
                      break;
                    }
                  }
                } finally {
                  LineIterator.closeQuietly(lineIterator);
                }
              } catch (Exception ex) {
                LOGGER.error("Error during text search in file : " + f); //NOI18N
              }
            }
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
          for (final NodeProject p : scope) {
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
          LOGGER.error("Exception during waiting of search thread interruption", ex); //NOI18N
        }
      }

      this.progressBarSearch.setMinimum(0);
      this.progressBarSearch.setMaximum(size);
      this.progressBarSearch.setValue(0);

      thread.start();
    }
  }

  private void safeSetProgressValue(final int value) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        if (value == Integer.MAX_VALUE) {
          progressBarSearch.setEnabled(false);
          progressBarSearch.setIndeterminate(false);
          progressBarSearch.setValue(progressBarSearch.getMaximum());
        } else if (value < 0) {
          progressBarSearch.setEnabled(true);
          progressBarSearch.setIndeterminate(true);
        } else {
          progressBarSearch.setEnabled(true);
          progressBarSearch.setIndeterminate(false);
          progressBarSearch.setValue(value);
        }
      }
    });
  }

  public void dispose() {
    final Thread thread = this.searchingThread.getAndSet(null);
    if (thread != null) {
      thread.interrupt();
    }
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form
   * Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    jLabel1 = new javax.swing.JLabel();
    textFieldSearchPath = new javax.swing.JTextField();
    jScrollPane1 = new javax.swing.JScrollPane();
    listOfFoundElements = new javax.swing.JList<>();
    jPanel1 = new javax.swing.JPanel();
    progressBarSearch = new javax.swing.JProgressBar();

    setLayout(new java.awt.GridBagLayout());

    jLabel1.setText("Search usage of :");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    add(jLabel1, gridBagConstraints);

    textFieldSearchPath.setEditable(false);
    textFieldSearchPath.setText("jTextField1");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.ipadx = 236;
    gridBagConstraints.weightx = 1000.0;
    add(textFieldSearchPath, gridBagConstraints);

    listOfFoundElements.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    jScrollPane1.setViewportView(listOfFoundElements);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.ipadx = 354;
    gridBagConstraints.ipady = 229;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1000.0;
    add(jScrollPane1, gridBagConstraints);

    jPanel1.setLayout(new java.awt.BorderLayout());
    jPanel1.add(progressBarSearch, java.awt.BorderLayout.CENTER);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    add(jPanel1, gridBagConstraints);
  }// </editor-fold>//GEN-END:initComponents


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JLabel jLabel1;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JList<NodeFileOrFolder> listOfFoundElements;
  private javax.swing.JProgressBar progressBarSearch;
  private javax.swing.JTextField textFieldSearchPath;
  // End of variables declaration//GEN-END:variables
}
