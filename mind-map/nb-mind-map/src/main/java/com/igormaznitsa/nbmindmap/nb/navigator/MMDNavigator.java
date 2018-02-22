/*
 * Copyright 2015-2018 Igor Maznitsa.
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
package com.igormaznitsa.nbmindmap.nb.navigator;

import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.nbmindmap.nb.editor.MMDDataObject;
import com.igormaznitsa.nbmindmap.nb.editor.MMDEditorSupport;
import com.igormaznitsa.nbmindmap.nb.swing.MindMapTreePanel;
import com.igormaznitsa.nbmindmap.nb.swing.SortedTreeModelWrapper;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.Comparator;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.windows.TopComponent;

import com.igormaznitsa.mindmap.swing.panel.utils.Utils;

@NavigatorPanel.Registration(displayName = "Mind map", mimeType = MMDDataObject.MIME)
public final class MMDNavigator extends JScrollPane implements NavigatorPanel, LookupListener, FileChangeListener, Comparator<Object> {

  private static final Logger LOGGER = LoggerFactory.getLogger(MMDNavigator.class);
  
  private static final Lookup.Template<MMDEditorSupport> MY_DATA = new Lookup.Template<MMDEditorSupport>(MMDEditorSupport.class);
  private static final long serialVersionUID = -4344090966601180253L;

  private final JTree mindMapTree;
  private SortedTreeModelWrapper treeModel;
  
  private transient Lookup.Result<? extends MMDEditorSupport> context;
  private transient MMDEditorSupport currentSupport;

  public MMDNavigator() {
    super();
    this.mindMapTree = new MindMapTreePanel(null, null, true, new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        final MMDDataObject current = (MMDDataObject) currentSupport.getDataObject();
        if (current != null) {
          final MMDEditorSupport edSupport = current.getLookup().lookup(MMDEditorSupport.class);
          if (edSupport != null) {
            edSupport.edit();
            final TreePath path = mindMapTree.getSelectionPath();
            if (path != null) {
              edSupport.focusToPosition(true, ((Topic) path.getLastPathComponent()).getPositionPath());
            }
          }
        }
      }
    }).getTree();
    this.setViewportView(this.mindMapTree);
  }

  @Override
  public String getDisplayName() {
    return java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18n/Bundle").getString("MMDNavigator.displayName");
  }

  @Override
  public String getDisplayHint() {
    return java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18n/Bundle").getString("MMDNavigator.displayHint");
  }

  @Override
  public JComponent getComponent() {
    return this;
  }

  @Override
  public void panelActivated(final Lookup context) {
    this.context = context.lookup(MY_DATA);
    this.context.addLookupListener(this);
    extractDataFromContextAndUpdate();
  }

  private void extractDataFromContextAndUpdate() {
    final Lookup.Result<? extends MMDEditorSupport> ctx = this.context;

    if (ctx == null) {
      this.currentSupport = null;
    }
    else {
      Collection<? extends MMDEditorSupport> clct = ctx.allInstances();

      if (clct.isEmpty()) {
        final TopComponent active = TopComponent.getRegistry().getActivated();
        if (active != null) {
          clct = active.getLookup().lookupAll(MMDEditorSupport.class);
        }
      }

      if (clct.isEmpty()) {
        this.currentSupport = null;
      }
      else {
        this.currentSupport = clct.iterator().next();
        if (this.currentSupport != null) {
          this.currentSupport.getDataObject().getPrimaryFile().removeFileChangeListener(this);
          this.currentSupport.getDataObject().getPrimaryFile().addFileChangeListener(this);
        }
      }
    }
    updateContent();
  }

  private String getDocumentText() {
    String result = null;
    if (this.currentSupport != null) {
      final MMDEditorSupport editor = this.currentSupport;
      result = editor.getDocumentText();
    }
    return result;
  }

  private void updateContent() {
    if (this.treeModel!=null){
      this.treeModel.dispose();
    }
    
    final String text = getDocumentText();
    if (text != null) {
      try {
        this.treeModel = new SortedTreeModelWrapper(new MindMap(null,new StringReader(text)), this);
        this.mindMapTree.setModel(this.treeModel);
        Utils.foldUnfoldTree(this.mindMapTree, true);
      }
      catch (IOException ex) {
        LOGGER.error("Can't parse mind map text", ex); //NOI18N
        this.mindMapTree.setModel(null);
      }
    }
    else {
      this.mindMapTree.setModel(null);
    }
  }

  @Override
  public void panelDeactivated() {
    if (this.context != null) {
      this.context.removeLookupListener(this);
    }

    if (this.currentSupport != null) {
      this.currentSupport.getDataObject().getPrimaryFile().removeFileChangeListener(this);
    }
    
    if(this.treeModel!=null){
      this.treeModel.dispose();
    }
    this.mindMapTree.setModel(null);
    this.currentSupport = null;
    this.context = null;
  }

  @Override
  public Lookup getLookup() {
    return null;
  }

  @Override
  public void resultChanged(final LookupEvent ev) {
    extractDataFromContextAndUpdate();
  }

  @Override
  public void fileFolderCreated(FileEvent fe) {
  }

  @Override
  public void fileDataCreated(FileEvent fe) {
  }

  @Override
  public void fileChanged(FileEvent fe) {
    SwingUtilities.invokeLater(new Runnable() {

      @Override
      public void run() {
        updateContent();
      }
    });
  }

  @Override
  public void fileDeleted(FileEvent fe) {
  }

  @Override
  public void fileRenamed(FileRenameEvent fe) {
  }

  @Override
  public void fileAttributeChanged(FileAttributeEvent fe) {
  }

  @Override
  public int compare(final Object o1, final Object o2) {
    return String.CASE_INSENSITIVE_ORDER.compare(String.valueOf(o1), String.valueOf(o2));
  }

}
