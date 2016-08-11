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
package com.igormaznitsa.sciareto.ui.tree;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.DropMode;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ToolTipManager;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.meta.common.utils.Assertions;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import com.igormaznitsa.sciareto.Context;
import com.igormaznitsa.sciareto.Main;
import com.igormaznitsa.sciareto.ui.DialogProviderManager;
import com.igormaznitsa.sciareto.ui.UiUtils;

public final class ExplorerTree extends JScrollPane {

  private static final long serialVersionUID = 3894835807758698784L;
  private static final Logger LOGGER = LoggerFactory.getLogger(ExplorerTree.class);

  private final DnDTree projectTree;
  private final Context context;

  public ExplorerTree(@Nonnull final Context context) {
    super();
    this.projectTree = new DnDTree();
    this.context = context;
    this.projectTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    this.projectTree.setDropMode(DropMode.ON);

    this.projectTree.setEditable(true);

    ToolTipManager.sharedInstance().registerComponent(this.projectTree);

    this.projectTree.setCellRenderer(new TreeCellRenderer());
    this.projectTree.setModel(new NodeProjectGroup(context, "."));
    this.projectTree.setRootVisible(false);
    this.setViewportView(this.projectTree);

    this.projectTree.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(@Nonnull final KeyEvent e) {
        if (!e.isConsumed() && e.getModifiers() == 0 && e.getKeyCode() == KeyEvent.VK_ENTER){
          final TreePath selectedPath = projectTree.getSelectionPath();
          if (selectedPath != null){
            final NodeFileOrFolder node = (NodeFileOrFolder) selectedPath.getLastPathComponent();
            if (node != null){
              if (node.isLeaf()){
              final File file = node.makeFileForNode();
              if (file != null && !context.openFileAsTab(file)) {
                UiUtils.openInSystemViewer(file);
              }
              }else{
                projectTree.expandPath(selectedPath);
              }
            }
          }
        }
      }
      
    });
    
    this.projectTree.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(@Nonnull final MouseEvent e) {
        if (e.getClickCount() > 1) {
          final int selRow = projectTree.getRowForLocation(e.getX(), e.getY());
          final TreePath selPath = projectTree.getPathForLocation(e.getX(), e.getY());
          if (selRow >= 0) {
            final NodeFileOrFolder node = (NodeFileOrFolder) selPath.getLastPathComponent();
            if (node != null && node.isLeaf()) {
              final File file = node.makeFileForNode();
              if (file != null && !context.openFileAsTab(file)) {
                UiUtils.openInSystemViewer(file);
              }
            }
          }
        }
      }

      @Override
      public void mouseReleased(@Nonnull final MouseEvent e) {
        if (e.isPopupTrigger()) {
          processPopup(e);
        }
      }

      @Override
      public void mousePressed(@Nonnull final MouseEvent e) {
        if (e.isPopupTrigger()) {
          processPopup(e);
        }
      }

      private void processPopup(@Nonnull final MouseEvent e) {
        final TreePath selPath = projectTree.getPathForLocation(e.getX(), e.getY());
        if (selPath != null) {
          projectTree.setSelectionPath(selPath);
          final Object last = selPath.getLastPathComponent();
          if (last instanceof NodeFileOrFolder) {
            makePopupMenu((NodeFileOrFolder) last).show(e.getComponent(), e.getX(), e.getY());
          }
        }
      }

    });
  }

  @Nonnull
  @MustNotContainNull
  public List<NodeFileOrFolder> findForNamePattern(@Nullable final Pattern namePattern) {
    return getCurrentGroup().findForNamePattern(namePattern);
  }
  
  @Nonnull
  @MustNotContainNull
  public List<NodeFileOrFolder> findNodesForFile(@Nonnull final File file) {
    return getCurrentGroup().findRelatedNodes(file, new ArrayList<NodeFileOrFolder>());
  }

  public void сloseProject(@Nonnull final NodeProject tree) {
    ((NodeProjectGroup) this.projectTree.getModel()).removeProject(tree);
    this.context.onCloseProject(tree);
  }

  public void focusToFileItem(@Nonnull final File file) {
    final NodeProjectGroup group = getCurrentGroup();
    final TreePath pathToFile = group.findPathToFile(file);
    if (pathToFile != null) {
      this.projectTree.setSelectionPath(pathToFile);
      this.projectTree.scrollPathToVisible(pathToFile);
    }
  }

  public void unfoldProject(@Nonnull final NodeProject node) {
    Utils.safeSwingCall(new Runnable() {
      @Override
      public void run() {
        projectTree.expandPath(new TreePath(new Object[]{getCurrentGroup(), node}));
      }
    });
  }

  @Nonnull
  private JPopupMenu makePopupMenu(@Nonnull final NodeFileOrFolder node) {
    final JPopupMenu result = new JPopupMenu();

    if (!node.isLeaf()) {
      final JMenu makeNew = new JMenu("New...");

      JMenuItem item = new JMenuItem("Folder");
      item.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(@Nonnull final ActionEvent e) {
          addChildTo(node, null);
        }
      });
      makeNew.add(item);

      item = new JMenuItem("Mind map");
      item.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(@Nonnull final ActionEvent e) {
          addChildTo(node, "mmd");
        }
      });
      makeNew.add(item);

      item = new JMenuItem("Text");
      item.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(@Nonnull final ActionEvent e) {
          addChildTo(node, "txt");
        }
      });
      makeNew.add(item);

      result.add(makeNew);
    }

    if (!node.isProjectKnowledgeFolder()) {
      final JMenuItem rename = new JMenuItem("Rename");
      rename.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(@Nonnull final ActionEvent e) {
          projectTree.startEditingAtPath(node.makeTreePath());
        }
      });
      result.add(rename);
    }

    if (node instanceof NodeProject) {
      final JMenuItem close = new JMenuItem("Close");
      close.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(@Nonnull final ActionEvent e) {
          сloseProject((NodeProject) node);
        }
      });
      result.add(close);
      final JMenuItem refresh = new JMenuItem("Reload");
      refresh.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(@Nonnull final ActionEvent e) {
          getCurrentGroup().refreshProjectFolder((NodeProject) node);
        }
      });
      result.add(refresh);
    }

    final JMenuItem delete = new JMenuItem("Delete");
    delete.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(@Nonnull final ActionEvent e) {
        if (DialogProviderManager.getInstance().getDialogProvider().msgConfirmYesNo("Delete", "Do you really want to delete \"" + node + "\"?")) {
          context.deleteTreeNode(node);
        }
      }
    });
    result.add(delete);

    final JMenuItem openInSystem = new JMenuItem("Open in System");
    openInSystem.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        final File file = node.makeFileForNode();
        if (file!= null && file.exists()) {
          UiUtils.openInSystemViewer(file);
        }
      }
    });
    result.add(openInSystem);

    return result;
  }

  private void addChildTo(@Nonnull final NodeFileOrFolder folder, @Nullable final String extension) {
    String fileName = JOptionPane.showInputDialog(Main.getApplicationFrame(), extension == null ? "Folder name" : "File name", extension == null ? "New folder" : "New " + extension.toUpperCase(Locale.ENGLISH) + " file", JOptionPane.QUESTION_MESSAGE);
    if (fileName != null) {
      fileName = fileName.trim();
      if (NodeProjectGroup.FILE_NAME.matcher(fileName).matches()) {
        if (extension != null) {
          final String providedExtension = FilenameUtils.getExtension(fileName);
          if (!extension.equalsIgnoreCase(providedExtension)) {
            fileName += '.' + extension;
          }
        }
        final File file = new File(folder.makeFileForNode(), fileName);
        if (file.exists()) {
          DialogProviderManager.getInstance().getDialogProvider().msgError("File '" + fileName + "' already exists!");
          return;
        }

        boolean ok = false;

        if (extension == null) {
          if (!file.mkdirs()) {
            LOGGER.error("Can't create folder");
            DialogProviderManager.getInstance().getDialogProvider().msgError("Can't create folder '" + fileName + "'!");
          } else {
            ok = true;
          }
        } else {
          switch (extension) {
            case "mmd": {
              final MindMap model = new MindMap(null, true);
              final Topic root = model.getRoot();
              if (root != null) {
                root.setText("Root");
              }
              try {
                FileUtils.write(file, model.write(new StringWriter()).toString(), "UTF-8");
                ok = true;
              } catch (IOException ex) {
                LOGGER.error("Can't create MMD file", ex);
                DialogProviderManager.getInstance().getDialogProvider().msgError("Can't create mind map '" + fileName + "'!");
              }
            }
            break;
            case "txt": {
              try {
                FileUtils.write(file, "", "UTF-8");
                ok = true;
              } catch (IOException ex) {
                LOGGER.error("Can't create TXT file", ex);
                DialogProviderManager.getInstance().getDialogProvider().msgError("Can't create txt file '" + fileName + "'!");
              }
            }
            break;
            default:
              throw new Error("Unexpected extension : " + extension);
          }
        }

        if (ok) {
          getCurrentGroup().addChild(folder, file);
          context.openFileAsTab(file);
          context.focusInTree(file);
        }
      } else {
        DialogProviderManager.getInstance().getDialogProvider().msgError("Illegal file name!");
      }
    }
  }

  public boolean deleteNode(@Nonnull final NodeFileOrFolder node) {
    return getCurrentGroup().fireNotificationThatNodeDeleted(node);
  }

  @Nonnull
  public NodeProjectGroup getCurrentGroup() {
    return (NodeProjectGroup) this.projectTree.getModel();
  }

  public void setModel(@Nonnull final NodeProjectGroup model, final boolean expandFirst) {
    this.projectTree.setModel(Assertions.assertNotNull(model));
    if (expandFirst && model.getChildCount() > 0) {
      this.projectTree.expandPath(new TreePath(new Object[]{model, model.getChildAt(0)}));
    }
  }

}
