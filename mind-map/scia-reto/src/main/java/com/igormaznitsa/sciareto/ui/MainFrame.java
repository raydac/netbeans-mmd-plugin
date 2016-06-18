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
package com.igormaznitsa.sciareto.ui;

import com.igormaznitsa.sciareto.ui.tabs.MainTabPane;
import com.igormaznitsa.sciareto.ui.tabs.TabTitle;
import com.igormaznitsa.sciareto.preferences.PreferencesPanel;
import com.igormaznitsa.sciareto.ui.tree.ExplorerTree;
import com.igormaznitsa.sciareto.ui.misc.AboutPanel;
import com.igormaznitsa.sciareto.ui.editors.PictureViewer;
import com.igormaznitsa.sciareto.ui.editors.TextEditor;
import com.igormaznitsa.sciareto.ui.editors.MMDEditor;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.filechooser.FileView;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.sciareto.Context;
import com.igormaznitsa.sciareto.Main;
import com.igormaznitsa.sciareto.preferences.FileHistoryManager;
import com.igormaznitsa.sciareto.ui.tree.NodeProject;

public final class MainFrame extends javax.swing.JFrame implements Context {

  private static final long serialVersionUID = 3798040833406256900L;

  private static final Logger LOGGER = LoggerFactory.getLogger(MainFrame.class);

  private final MainTabPane tabPane;
  private final ExplorerTree explorerTree;

  public MainFrame() {
    initComponents();
    setIconImage(UiUtils.loadImage("logo256x256.png"));

    this.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(@Nonnull final WindowEvent e) {
        if (doClosing()) {
          dispose();
        }
      }
    });

    this.tabPane = new MainTabPane(this);

    this.explorerTree = new ExplorerTree(this);

    final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    splitPane.setOneTouchExpandable(true);
    splitPane.setDividerLocation(250);
    splitPane.setResizeWeight(0.0d);
    splitPane.setLeftComponent(this.explorerTree);
    splitPane.setRightComponent(this.tabPane);

    add(splitPane, BorderLayout.CENTER);

    this.menuOpenRecentProject.addMenuListener(new MenuListener() {
      @Override
      public void menuSelected(MenuEvent e) {
        final File[] lastOpenedProjects = FileHistoryManager.getInstance().getLastOpenedProjects();
        if (lastOpenedProjects.length > 0) {
          for (final File folder : lastOpenedProjects) {
            final JMenuItem item = new JMenuItem(folder.getName());
            item.setToolTipText(folder.getAbsolutePath());
            item.addActionListener(new ActionListener() {
              @Override
              public void actionPerformed(ActionEvent e) {
                openProject(folder, false);
              }
            });
            menuOpenRecentProject.add(item);
          }
        }
      }

      @Override
      public void menuDeselected(MenuEvent e) {
        menuOpenRecentProject.removeAll();
      }

      @Override
      public void menuCanceled(MenuEvent e) {
      }
    });

    this.menuOpenRecentFile.addMenuListener(new MenuListener() {
      @Override
      public void menuSelected(MenuEvent e) {
        final File[] lastOpenedFiles = FileHistoryManager.getInstance().getLastOpenedFiles();
        if (lastOpenedFiles.length > 0) {
          for (final File file : lastOpenedFiles) {
            final JMenuItem item = new JMenuItem(file.getName());
            item.setToolTipText(file.getAbsolutePath());
            item.addActionListener(new ActionListener() {
              @Override
              public void actionPerformed(ActionEvent e) {
                openFileAsTab(file);
              }
            });
            menuOpenRecentFile.add(item);
          }
        }
      }

      @Override
      public void menuDeselected(MenuEvent e) {
        menuOpenRecentFile.removeAll();
      }

      @Override
      public void menuCanceled(MenuEvent e) {
      }
    });

    restoreState();
  }

  private boolean doClosing() {
    boolean hasUnsaved = false;
    for (final TabTitle t : tabPane) {
      hasUnsaved |= t.isChanged();
    }

    if (hasUnsaved) {
      if (!DialogProviderManager.getInstance().getDialogProvider().msgConfirmOkCancel("Detected non-saved docuemtns", "Detected unsaved documents! Close application?")) {
        return false;
      }
    }

    saveState();
    return true;
  }

  private void restoreState() {
    try {
      final File[] activeProjects = FileHistoryManager.getInstance().getActiveProjects();
      for (final File f : activeProjects) {
        if (f.isDirectory()) {
          openProject(f, true);
        }
      }
      final File[] activeFiles = FileHistoryManager.getInstance().getActiveFiles();
      for (final File f : activeFiles) {
        if (f.isFile()) {
          openFileAsTab(f);
        }
      }
    } catch (IOException ex) {
      LOGGER.error("Can't restore state", ex);
    }
  }

  private void saveState() {
    try {
      final List<File> files = new ArrayList<File>();
      for (final NodeProject p : this.explorerTree.getCurrentGroup()) {
        final File f = p.getFile();
        if (f.isDirectory()) {
          files.add(f);
        }
      }
      FileHistoryManager.getInstance().saveActiveProjects(files.toArray(new File[files.size()]));
      files.clear();

      for (final TabTitle p : this.tabPane) {
        final File f = p.getAssociatedFile();
        if (f.isFile()) {
          files.add(f);
        }
      }
      FileHistoryManager.getInstance().saveActiveFiles(files.toArray(new File[files.size()]));
    } catch (IOException ex) {
      LOGGER.error("Can't save state", ex);
    }
  }

  @Override
  public boolean openFileAsTab(@Nonnull final File file) {
    boolean result = false;
    if (file.isFile()) {
      if (this.tabPane.focusToFile(file)) {
        result = true;
      } else {
        final String ext = FilenameUtils.getExtension(file.getName()).toLowerCase(Locale.ENGLISH);
        if (ext.equals("mmd")) {
          try {
            final MMDEditor panel = new MMDEditor(this, file);
            this.tabPane.createTab(panel);
            result = true;
          } catch (IOException ex) {
            LOGGER.error("Can't load mind map", ex);
          }
        } else if (PictureViewer.SUPPORTED_FORMATS.contains(ext)) {
          try {
            final PictureViewer panel = new PictureViewer(this, file);
            this.tabPane.createTab(panel);
            result = true;
          } catch (IOException ex) {
            LOGGER.error("Can't load file as image", ex);
          }
        } else {
          if (file.length() >= (2L * 1024L * 1024L) && !DialogProviderManager.getInstance().getDialogProvider().msgConfirmYesNo("Very big file", "It is a very big file! Are you sure to open it?")) {
            return true;
          }

          try {
            final TextEditor panel = new TextEditor(this, file);
            this.tabPane.createTab(panel);
            result = true;
          } catch (IOException ex) {
            LOGGER.error("Can't load file as text", ex);
          }
        }
      }
    }
    if (result) {
      try {
        FileHistoryManager.getInstance().registerOpenedFile(file);
      } catch (IOException x) {
        LOGGER.error("Can't register last opened file", x);
      }
    }
    return result;
  }

  @Override
  public void focusInTree(@Nonnull final TabTitle title) {
    this.explorerTree.focusToFileItem(title.getAssociatedFile());
  }

  @Override
  public void focusInTree(@Nonnull final File file) {
    this.explorerTree.focusToFileItem(file);
  }

  @Override
  public void closeTab(@Nonnull final TabTitle... titles) {
    for (final TabTitle t : titles) {
      this.tabPane.removeTab(t);
    }
  }

  @Override
  @Nullable
  public NodeProject findProjectForFile(@Nonnull final File file) {
    return this.explorerTree.getCurrentGroup().findProjectForFile(file);
  }

  @Override
  public void notifyReloadConfig() {
    for (final TabTitle t : this.tabPane) {
      final JComponent editor = t.getProvider().getMainComponent();
      if (editor instanceof MMDEditor) {
        ((MMDEditor) editor).refreshConfig();
      }
    }
  }

  @Override
  public void onCloseProject(@Nonnull final NodeProject project) {
    final File projectFolder = project.getFile();
    if (projectFolder != null) {
      for (final TabTitle t : this.tabPane) {
        if (t.belongFolder(projectFolder)) {
          t.doSafeClose();
        }
      }
    }
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form
   * Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    jMenuBar1 = new javax.swing.JMenuBar();
    menuFile = new javax.swing.JMenu();
    menuNewProject = new javax.swing.JMenuItem();
    menuNewFile = new javax.swing.JMenuItem();
    jSeparator2 = new javax.swing.JPopupMenu.Separator();
    menuOpenProject = new javax.swing.JMenuItem();
    menuOpenRecentProject = new javax.swing.JMenu();
    menuOpenFile = new javax.swing.JMenuItem();
    menuOpenRecentFile = new javax.swing.JMenu();
    jSeparator3 = new javax.swing.JPopupMenu.Separator();
    menSave = new javax.swing.JMenuItem();
    menuSaveAs = new javax.swing.JMenuItem();
    menuSaveAll = new javax.swing.JMenuItem();
    jSeparator1 = new javax.swing.JPopupMenu.Separator();
    menuExit = new javax.swing.JMenuItem();
    menuEdit = new javax.swing.JMenu();
    menuPreferences = new javax.swing.JMenuItem();
    menuHelp = new javax.swing.JMenu();
    menuAbout = new javax.swing.JMenuItem();

    setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
    setLocationByPlatform(true);

    menuFile.setText("File");

    menuNewProject.setText("New Project");
    menuNewProject.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        menuNewProjectActionPerformed(evt);
      }
    });
    menuFile.add(menuNewProject);

    menuNewFile.setText("New File");
    menuNewFile.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        menuNewFileActionPerformed(evt);
      }
    });
    menuFile.add(menuNewFile);
    menuFile.add(jSeparator2);

    menuOpenProject.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
    menuOpenProject.setText("Open Project");
    menuOpenProject.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        menuOpenProjectActionPerformed(evt);
      }
    });
    menuFile.add(menuOpenProject);

    menuOpenRecentProject.setText("Open Recent Project");
    menuFile.add(menuOpenRecentProject);

    menuOpenFile.setText("Open File");
    menuOpenFile.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        menuOpenFileActionPerformed(evt);
      }
    });
    menuFile.add(menuOpenFile);

    menuOpenRecentFile.setText("Open Recent File");
    menuFile.add(menuOpenRecentFile);
    menuFile.add(jSeparator3);

    menSave.setText("Save");
    menSave.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        menSaveActionPerformed(evt);
      }
    });
    menuFile.add(menSave);

    menuSaveAs.setText("Save As");
    menuSaveAs.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        menuSaveAsActionPerformed(evt);
      }
    });
    menuFile.add(menuSaveAs);

    menuSaveAll.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
    menuSaveAll.setText("Save All");
    menuSaveAll.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        menuSaveAllActionPerformed(evt);
      }
    });
    menuFile.add(menuSaveAll);
    menuFile.add(jSeparator1);

    menuExit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_MASK));
    menuExit.setText("Exit");
    menuExit.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        menuExitActionPerformed(evt);
      }
    });
    menuFile.add(menuExit);

    jMenuBar1.add(menuFile);

    menuEdit.setText("Edit");

    menuPreferences.setText("Preferences");
    menuPreferences.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        menuPreferencesActionPerformed(evt);
      }
    });
    menuEdit.add(menuPreferences);

    jMenuBar1.add(menuEdit);

    menuHelp.setText("Help");

    menuAbout.setText("About");
    menuAbout.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        menuAboutActionPerformed(evt);
      }
    });
    menuHelp.add(menuAbout);

    jMenuBar1.add(menuHelp);

    setJMenuBar(jMenuBar1);

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void menuAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuAboutActionPerformed
    JOptionPane.showMessageDialog(Main.getApplicationFrame(), new AboutPanel(), "About", JOptionPane.PLAIN_MESSAGE);
  }//GEN-LAST:event_menuAboutActionPerformed

  private void menuOpenProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuOpenProjectActionPerformed
    final JFileChooser fileChooser = new JFileChooser();
    fileChooser.setFileView(new FileView() {
      private Icon KNOWLEDGE_FOLDER_ICO = null;

      @Override
      public Icon getIcon(final File f) {
        if (f.isDirectory()) {
          final File knowledge = new File(f, ".projectKnowledge");
          if (knowledge.isDirectory()) {
            if (KNOWLEDGE_FOLDER_ICO == null) {
              final Icon icon = UIManager.getIcon("FileView.directoryIcon");
              if (icon != null) {
                KNOWLEDGE_FOLDER_ICO = new ImageIcon(UiUtils.makeBadged(UiUtils.iconToImage(fileChooser, icon), Icons.MMDBADGE.getIcon().getImage()));
              }
            }
            return KNOWLEDGE_FOLDER_ICO;
          } else {
            return super.getIcon(f);
          }
        } else if (f.isFile() && f.getName().toLowerCase(Locale.ENGLISH).endsWith(".mmd")) {
          return Icons.DOCUMENT.getIcon();
        } else {
          return super.getIcon(f);
        }
      }
    });
    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    fileChooser.setMultiSelectionEnabled(false);
    fileChooser.setDialogTitle("Open project folder");

    if (fileChooser.showOpenDialog(Main.getApplicationFrame()) == JFileChooser.APPROVE_OPTION) {
      openProject(fileChooser.getSelectedFile(), false);
    }
  }//GEN-LAST:event_menuOpenProjectActionPerformed

  @Override
  public boolean openProject(@Nonnull final File folder, final boolean enforceSeparatedProject) {
    boolean result = false;
    if (folder.isDirectory()) {
      final NodeProject alreadyOpened = findProjectForFile(folder);
      if (alreadyOpened == null || enforceSeparatedProject) {
        this.explorerTree.getCurrentGroup().addProjectFolder(folder);
        try {
          FileHistoryManager.getInstance().registerOpenedProject(folder);
        } catch (IOException ex) {
          LOGGER.error("Can't register last opened project", ex);
        }
      } else {
        this.focusInTree(folder);
      }
      result = true;
    } else {
      LOGGER.error("Can't find folder : " + folder);
      DialogProviderManager.getInstance().getDialogProvider().msgError("Can't find project folder!");
    }
    return result;
  }

  @Override
  public void editPreferences() {
    final PreferencesPanel configPanel = new PreferencesPanel(this);
    configPanel.load();
    if (DialogProviderManager.getInstance().getDialogProvider().msgOkCancel("Preferences", configPanel)) {
      configPanel.save();
    }
  }

  private void menuSaveAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuSaveAllActionPerformed
    for (final TabTitle t : this.tabPane) {
      t.save();
    }
  }//GEN-LAST:event_menuSaveAllActionPerformed

  private void menuPreferencesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuPreferencesActionPerformed
    editPreferences();
  }//GEN-LAST:event_menuPreferencesActionPerformed

  private void menuOpenFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuOpenFileActionPerformed
    final File file = DialogProviderManager.getInstance().getDialogProvider().msgOpenFileDialog("open-file", "Open file", null, true, MMDEditor.MMD_FILE_FILTER, "Open");
    if (file != null) {
      if (openFileAsTab(file)) {
        try {
          FileHistoryManager.getInstance().registerOpenedProject(file);
        } catch (IOException ex) {
          LOGGER.error("Can't register last opened file", ex);
        }
      }
    }
  }//GEN-LAST:event_menuOpenFileActionPerformed

  private void menSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menSaveActionPerformed
    final int index = this.tabPane.getSelectedIndex();
    if (index >= 0) {
      ((TabTitle) this.tabPane.getTabComponentAt(index)).save();
    }
  }//GEN-LAST:event_menSaveActionPerformed

  private void menuSaveAsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuSaveAsActionPerformed
    final int index = this.tabPane.getSelectedIndex();
    if (index >= 0) {
      ((TabTitle) this.tabPane.getTabComponentAt(index)).saveAs();
    }
  }//GEN-LAST:event_menuSaveAsActionPerformed

  private void menuExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuExitActionPerformed
    if (doClosing()) {
      dispose();
    }
  }//GEN-LAST:event_menuExitActionPerformed

  private void menuNewProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuNewProjectActionPerformed
    final JFileChooser folder = new JFileChooser();
    folder.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    folder.setDialogTitle("Create project folder");
    folder.setApproveButtonText("Create");
    folder.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    if (folder.showOpenDialog(Main.getApplicationFrame()) == JFileChooser.APPROVE_OPTION) {
      final File file = folder.getSelectedFile();
      if (file.exists()) {
        DialogProviderManager.getInstance().getDialogProvider().msgError("File '" + file.getName() + "' already exists!");
      } else if (file.mkdirs()) {
        final File knowledgeFolder = new File(file,".projectKnowledge");
        knowledgeFolder.mkdirs();
        if (openProject(file, true)) {
          this.focusInTree(file);
        }
      } else {
        LOGGER.error("Can't create folder : " + file);
        DialogProviderManager.getInstance().getDialogProvider().msgError("Can't create folder: " + file);
      }
    }
  }//GEN-LAST:event_menuNewProjectActionPerformed

  private void menuNewFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuNewFileActionPerformed
    final TabTitle current = this.tabPane.getCurrentTitle();
    if (current != null) {
      final NodeProject project = findProjectForFile(current.getAssociatedFile());
      final File folder = project == null ? null : project.getFile();
      createMindMapFile(folder);
    } else {
      final File created = createMindMapFile(null);
      if (created!=null && openFileAsTab(created)){
        
      }
    }
  }//GEN-LAST:event_menuNewFileActionPerformed

  @Nullable
  @Override
  public File createMindMapFile(@Nullable final File folder) {
    final JFileChooser chooser = new JFileChooser(folder);
    chooser.setDialogTitle("Create new Mind Map");
    chooser.setFileFilter(MMDEditor.MMD_FILE_FILTER);
    chooser.setMultiSelectionEnabled(false);
    chooser.setApproveButtonText("Create");
    
    File result = null;
    
    if (chooser.showSaveDialog(Main.getApplicationFrame()) == JFileChooser.APPROVE_OPTION) {
      File file = chooser.getSelectedFile();
      if (!file.getName().endsWith(".mmd")) {
        file = new File(file.getAbsolutePath()+".mmd");
      }

      if (file.exists()) {
        DialogProviderManager.getInstance().getDialogProvider().msgError("File '" + file + "' already exists!");
      } else {
        try {
          final MindMap mindMap = new MindMap(null, true);
          final String text = mindMap.write(new StringWriter()).toString();
          FileUtils.write(file, text, "UTF-8");
          result = file;
        } catch (IOException ex) {
          DialogProviderManager.getInstance().getDialogProvider().msgError("Can't save mind map into file '" + file.getName() + "'");
        }
      }
    }
    return result;
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JMenuBar jMenuBar1;
  private javax.swing.JPopupMenu.Separator jSeparator1;
  private javax.swing.JPopupMenu.Separator jSeparator2;
  private javax.swing.JPopupMenu.Separator jSeparator3;
  private javax.swing.JMenuItem menSave;
  private javax.swing.JMenuItem menuAbout;
  private javax.swing.JMenu menuEdit;
  private javax.swing.JMenuItem menuExit;
  private javax.swing.JMenu menuFile;
  private javax.swing.JMenu menuHelp;
  private javax.swing.JMenuItem menuNewFile;
  private javax.swing.JMenuItem menuNewProject;
  private javax.swing.JMenuItem menuOpenFile;
  private javax.swing.JMenuItem menuOpenProject;
  private javax.swing.JMenu menuOpenRecentFile;
  private javax.swing.JMenu menuOpenRecentProject;
  private javax.swing.JMenuItem menuPreferences;
  private javax.swing.JMenuItem menuSaveAll;
  private javax.swing.JMenuItem menuSaveAs;
  // End of variables declaration//GEN-END:variables
}
