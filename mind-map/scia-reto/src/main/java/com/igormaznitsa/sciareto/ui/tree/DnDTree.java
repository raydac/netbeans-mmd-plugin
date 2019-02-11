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
package com.igormaznitsa.sciareto.ui.tree;

import com.igormaznitsa.sciareto.preferences.PrefUtils;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

public final class DnDTree extends JTree implements DragSourceListener, DropTargetListener, DragGestureListener {

  private static final long serialVersionUID = -4915750239120689053L;

  private boolean dragAcceptableType = false;

  public DnDTree() {
    super();
    this.setDragEnabled(true);
    this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    this.setDropMode(DropMode.ON_OR_INSERT);

    final DragSource dragSource = DragSource.getDefaultDragSource();
    dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, this);

    final DropTarget dropTarget = new DropTarget(this, this);

    this.setTransferHandler(new TransferHandler() {

      private static final long serialVersionUID = 3109256773218160485L;

      @Override
      public boolean canImport(@Nonnull final TransferHandler.TransferSupport support) {
        return false;
//        return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
      }

      @Override
      protected Transferable createTransferable(@Nonnull final JComponent c) {
        final JTree tree = (JTree) c;
        final TreePath selected = tree.getSelectionPath();
        final NodeFileOrFolder item = (NodeFileOrFolder) selected.getLastPathComponent();
        return new FileTransferable(Arrays.asList(item.makeFileForNode()));
      }

      @Override
      public boolean importData(@Nonnull final TransferHandler.TransferSupport support) {
        return canImport(support);
      }
    });
  }

  @Override
  @Nullable
  public String getToolTipText(@Nonnull final MouseEvent evt) {
    if (getRowForLocation(evt.getX(), evt.getY()) == -1) {
      return null;
    }
    final TreePath curPath = getPathForLocation(evt.getX(), evt.getY());
    final Object lastElement = curPath.getLastPathComponent();
    if (lastElement instanceof NodeFileOrFolder) {
      final NodeFileOrFolder nodeFileOrFolder = (NodeFileOrFolder) lastElement;
      final File file = nodeFileOrFolder.makeFileForNode();
      return file == null ? null
              : (nodeFileOrFolder.hasNoAccess() ? "[NO ACCESS]" : "")
              + (nodeFileOrFolder.hasNoAccess() ? "[READ ONLY]" : "")
              + file.getAbsolutePath();
    } else {
      return null;
    }
  }

  public void focusToFirstElement() {
    final TreeModel model = this.getModel();
    final Object root = model.getRoot();
    if (root != null) {
      final Object firstChild = model.getChildCount(root) > 0 ? model.getChild(root, 0) : null;
      if (firstChild != null) {
        this.setSelectionPath(new TreePath(new Object[]{root, firstChild}));
      }
    }
  }

  @Override
  public void dragEnter(@Nonnull final DragSourceDragEvent dsde) {
  }

  @Override
  public void dragOver(@Nonnull final DragSourceDragEvent dsde) {
  }

  @Override
  public void dropActionChanged(@Nonnull final DragSourceDragEvent dsde) {
  }

  @Override
  public void dragExit(@Nonnull final DragSourceEvent dse) {
  }

  @Override
  public void dragDropEnd(@Nonnull final DragSourceDropEvent dtde) {
  }

  @Override
  public void dragEnter(@Nonnull final DropTargetDragEvent dtde) {
    dtde.rejectDrag();
//    
//    this.dragAcceptableType = checkDragType(dtde);
//    if (!this.dragAcceptableType) {
//      dtde.rejectDrag();
//    }
  }

  @Override
  public void dragOver(@Nonnull final DropTargetDragEvent dtde) {
    if (!this.dragAcceptableType) {
      dtde.rejectDrag();
    }
  }

  @Override
  public void dropActionChanged(@Nonnull DropTargetDragEvent dtde) {
  }

  @Override
  public void dragExit(@Nonnull final DropTargetEvent dte) {
  }

  @Override
  public void drop(@Nonnull final DropTargetDropEvent dtde) {
    if (this.dragAcceptableType) {

      final Point dragPoint = dtde.getLocation();

      final TreePath path = getPathForLocation(dragPoint.x, dragPoint.y);

      if (path != null) {
        final Object dropTargetNode = path.getLastPathComponent();
        if (dropTargetNode instanceof NodeFileOrFolder) {
          final NodeFileOrFolder node = (NodeFileOrFolder) dropTargetNode;
          if (!node.isLeaf()) {
            //TODO processing of file drag in tree
            System.out.println("Not implemented yet!"); //NOI18N
          } else {
            dtde.rejectDrop();
          }
        }
      }

      repaint();
    }
  }

  protected static boolean checkDragType(@Nonnull final DropTargetDragEvent dtde) {
    boolean result = false;
    for (final DataFlavor flavor : dtde.getCurrentDataFlavors()) {
      final Class dataClass = flavor.getRepresentationClass();
      if (FileTransferable.class.isAssignableFrom(dataClass) || flavor.isFlavorJavaFileListType()) {
        result = true;
        break;
      }
    }
    return result;
  }

  @Override
  public void dragGestureRecognized(@Nonnull final DragGestureEvent dragGestureEvent) {
    final JTree tree = (JTree) dragGestureEvent.getComponent();
    final TreePath path = tree.getSelectionPath();
    if (path != null) {
      final Object selection = path.getLastPathComponent();
      if (selection instanceof NodeFileOrFolder) {
        FileTransferable node = new FileTransferable(Arrays.asList(((NodeFileOrFolder) selection).makeFileForNode()));
        dragGestureEvent.startDrag(DragSource.DefaultCopyDrop, node, this);
      }
    }
  }

  @Nullable
  public TreePath findTreePathToFolderContains(@Nonnull final File file) {
    final File folder;
    folder = file.getParentFile();

    if (folder == null) {
      return null;
    }

    final NodeProjectGroup model = (NodeProjectGroup) this.getModel();

    TreePath result = null;
    for (final NodeFileOrFolder p : model) {
      result = p.findPathToFile(folder);
      if (result != null) {
        break;
      }
    }
    return result;
  }

  @Nullable
  public TreePath tryCreatePathInTreeToFile(@Nonnull final File file) throws IOException {
    TreePath path = null;

    final List<String> folders = new ArrayList<>();

    final NodeProjectGroup model = (NodeProjectGroup) this.getModel();

    File theFolder = file.getParentFile();

    while (theFolder != null) {
      path = model.findPathToFile(theFolder);
      if (path == null) {
        if (theFolder.isDirectory()) {
          folders.add(0, theFolder.getName());
        }
        theFolder = theFolder.getParentFile();
      } else {
        break;
      }
    }

    if (path != null) {
      NodeFileOrFolder parent = (NodeFileOrFolder) path.getLastPathComponent();
      for (final String name : folders) {
        final File folder = new File(parent.makeFileForNode(), name);
        if (folder.isDirectory() || folder.mkdir()) {
          parent = model.addChild(parent, PrefUtils.isShowHiddenFilesAndFolders(), folder);
        } else {
          throw new IOException("Can't find or create folder: " + folder);
        }
      }
      path = parent.makeTreePath();
    }

    return path;
  }

  @Nullable
  public File cloneFile(@Nonnull final TreePath path) throws IOException {
    final NodeFileOrFolder node = (NodeFileOrFolder) path.getLastPathComponent();
    final File baseFile = node.makeFileForNode();
    if (baseFile == null) {
      return null;
    }

    final File folder = baseFile.getParentFile();

    String name = node.name;
    final String extension = FilenameUtils.getExtension(baseFile.getName());
    final String baseName = FilenameUtils.getBaseName(baseFile.getName());
    File newFile = null;
    for (int i = 1; i < Integer.MAX_VALUE; i++) {
      newFile = new File(folder, baseName + "_copy" + i + (extension.isEmpty() ? "" : '.' + extension));
      if (!newFile.exists()) {
        break;
      }
    }

    FileUtils.copyFile(baseFile, newFile);

    return newFile;
  }

}
