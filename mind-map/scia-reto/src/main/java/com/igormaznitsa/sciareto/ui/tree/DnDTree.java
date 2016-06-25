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
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public final class DnDTree extends JTree implements DragSourceListener, DropTargetListener, DragGestureListener {

  private static final long serialVersionUID = -4915750239120689053L;

  private boolean dragAcceptableType;
  
  public DnDTree(){
    super();
    this.setDragEnabled(true);
    this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    this.setDropMode(DropMode.ON_OR_INSERT);
    
    final DragSource dragSource = DragSource.getDefaultDragSource();
    dragSource.createDefaultDragGestureRecognizer(this,DnDConstants.ACTION_MOVE, this);
    
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
      final File file = ((NodeFileOrFolder) lastElement).makeFileForNode();
      return file == null ? null : file.getAbsolutePath();
    } else {
      return null;
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
    if (!this.dragAcceptableType){
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
           //TODO
           System.out.println("Moved file");
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
      if (selection instanceof NodeFileOrFolder){
        FileTransferable node = new FileTransferable(Arrays.asList(((NodeFileOrFolder)selection).makeFileForNode()));
        dragGestureEvent.startDrag(DragSource.DefaultCopyDrop, node, this);
      } 
    }
  }
  
}
