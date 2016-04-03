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
package com.igormaznitsa.nbmindmap.nb.refactoring.gui;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.refactoring.api.ui.ExplorerContext;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

public final class MovePanel extends javax.swing.JPanel implements CustomRefactoringPanel {

  public static final ResourceBundle BUNDLE = ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18n/Bundle");
  private static final long serialVersionUID = -5276978458284047575L;

  private final AtomicBoolean initialized = new AtomicBoolean();

  private final Lookup lookup;
  private final FileObject[] files;
  private final ChangeListener parent;
  private final Map<Project, String[]> cachedFolders = new HashMap<Project, String[]>();

  public MovePanel(final Lookup lookup, final FileObject[] files, final ChangeListener parent) {
    initComponents();
    this.lookup = lookup;
    this.files = files;
    this.parent = parent;

    this.comboProjects.setRenderer(new ProjectCellRenderer());
    this.comboProjects.removeAllItems();
    this.comboFolders.setRenderer(new FolderCellRenderer());
    this.comboFolders.removeAllItems();
  }

  @Override
  public void initialize() {
    if (this.initialized.compareAndSet(false, true)) {
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          initValues();
        }
      });
    }
  }

  @Override
  public Component getComponent() {
    return this;
  }

  private void initValues() {
    final String text;
    if (this.files.length > 1) {
      text = String.format(BUNDLE.getString("MovePanel.multiFileText"), Integer.toString(this.files.length));
    }
    else {
      text = String.format(BUNDLE.getString("MovePanel.singleFileText"), this.files[0].getName());
    }
    this.labelMessage.setText(text);

    final List<Project> projects = new ArrayList<Project>();
    for (final Project p : OpenProjects.getDefault().getOpenProjects()) {
      projects.add(p);
    }
    final ComboBoxModel<Project> projectModel = new DefaultComboBoxModel<Project>(projects.toArray(new Project[projects.size()]));

    final ItemListener listener = new ItemListener() {
      @Override
      public void itemStateChanged(final ItemEvent e) {
        if (comboProjects.equals(e.getSource())) {
          updateFolders();
          parent.stateChanged(null);
        }
        else if (comboFolders.equals(e.getSource())) {
          parent.stateChanged(null);
        }
      }
    };

    this.comboProjects.addItemListener(listener);
    this.comboFolders.addItemListener(listener);

    this.comboProjects.setModel(projectModel);
    this.comboProjects.setSelectedItem(FileOwnerQuery.getOwner(this.files[0]));
    updateFolders();

    final ExplorerContext explorerContext = this.lookup.lookup(ExplorerContext.class);
    if (explorerContext != null) {
      final Node targetNode = explorerContext.getTargetNode();
      if (targetNode != null) {
        final DataObject dobj = targetNode.getLookup().lookup(DataObject.class);
        if (dobj != null) {
          final FileObject fo = dobj.getPrimaryFile();
          if (fo != null && fo.isValid() && fo.isFolder()) {
            final Project proj = FileOwnerQuery.getOwner(fo);
            if (proj != null) {
              this.comboProjects.setSelectedItem(proj);
              this.comboFolders.setSelectedItem(FileUtil.getRelativePath(proj.getProjectDirectory(), fo));
            }
          }
        }
      }
    }
  }

  public FileObject getTarget() {
    final Project project = (Project) this.comboProjects.getSelectedItem();
    final String folder = (String) this.comboFolders.getSelectedItem();

    if (project == null || folder == null) {
      return null;
    }
    else {
      return project.getProjectDirectory().getFileObject(folder);
    }
  }

  private void updateFolders() {
    final Project project = (Project) this.comboProjects.getSelectedItem();
    if (project == null) {
      this.comboFolders.setModel(new DefaultComboBoxModel());
    }
    else {
      String[] foldersForProject = this.cachedFolders.get(project);

      if (foldersForProject == null) {
        foldersForProject = collectAllFoldersForProject(project);
        this.cachedFolders.put(project, foldersForProject);
      }

      final ComboBoxModel<String> folderModel = new DefaultComboBoxModel<String>(foldersForProject);
      this.comboFolders.setModel(folderModel);
      if (folderModel.getSize() > 0) {
        this.comboFolders.setSelectedIndex(0);
      }
    }
  }

  private String[] collectAllFoldersForProject(final Project project) {
    final List<String> result = new ArrayList<String>();
    getFolders(project.getProjectDirectory(), project.getProjectDirectory(), result);
    return result.toArray(new String[result.size()]);
  }

  private void getFolders(final FileObject root, final FileObject folder, final List<String> result) {
    for (final FileObject c : folder.getChildren()) {
      if (c.isFolder()) {
        result.add(FileUtil.getRelativePath(root, c));
        getFolders(root, c, result);
      }
    }
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    labelProject = new javax.swing.JLabel();
    comboProjects = new javax.swing.JComboBox();
    labelFolder = new javax.swing.JLabel();
    comboFolders = new javax.swing.JComboBox();
    labelMessage = new javax.swing.JLabel();

    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18n/Bundle"); // NOI18N
    org.openide.awt.Mnemonics.setLocalizedText(labelProject, bundle.getString("MovePanel.labelProject.text")); // NOI18N

    comboProjects.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

    org.openide.awt.Mnemonics.setLocalizedText(labelFolder, bundle.getString("MovePanel.labelFolder.text")); // NOI18N

    comboFolders.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

    labelMessage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/igormaznitsa/nbmindmap/icons/logo/logo16.png"))); // NOI18N
    org.openide.awt.Mnemonics.setLocalizedText(labelMessage, "..."); // NOI18N

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(labelMessage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addGroup(layout.createSequentialGroup()
            .addGap(3, 3, 3)
            .addComponent(labelFolder)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(comboFolders, 0, 319, Short.MAX_VALUE))
          .addGroup(layout.createSequentialGroup()
            .addComponent(labelProject)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(comboProjects, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(labelMessage)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(comboProjects, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(labelProject))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(comboFolders, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(labelFolder))
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
  }// </editor-fold>//GEN-END:initComponents


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JComboBox comboFolders;
  private javax.swing.JComboBox comboProjects;
  private javax.swing.JLabel labelFolder;
  private javax.swing.JLabel labelMessage;
  private javax.swing.JLabel labelProject;
  // End of variables declaration//GEN-END:variables

}
