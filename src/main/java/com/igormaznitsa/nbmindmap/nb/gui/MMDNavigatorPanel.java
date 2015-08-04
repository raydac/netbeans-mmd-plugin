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
package com.igormaznitsa.nbmindmap.nb.gui;

import com.igormaznitsa.nbmindmap.model.MindMap;
import com.igormaznitsa.nbmindmap.nb.dataobj.MMDDataObject;
import com.igormaznitsa.nbmindmap.nb.dataobj.MMDEditorSupport;
import com.igormaznitsa.nbmindmap.utils.Logger;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.api.actions.Openable;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

@NavigatorPanel.Registration(displayName = "Mind map", mimeType = MMDDataObject.MIME)
public class MMDNavigatorPanel extends JScrollPane implements NavigatorPanel, LookupListener {

  private static final Lookup.Template<? extends MMDDataObject> MY_DATA = new Lookup.Template<MMDDataObject>(MMDDataObject.class);
  private static final long serialVersionUID = -4344090966601180253L;

  private final JTree mindMapTree;

  private Lookup.Result<? extends MMDDataObject> curContext;
  private MMDDataObject content;

  public MMDNavigatorPanel() {
    super();
    this.mindMapTree = new JTree();
    this.setViewportView(this.mindMapTree);

    this.mindMapTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    
    this.mindMapTree.addMouseListener(new MouseAdapter() {

      @Override
      public void mouseClicked(final MouseEvent e) {
        if (!e.isPopupTrigger() && e.getClickCount()>1){
          final MMDDataObject current = content.getLookup().lookup(MMDDataObject.class);
          if (current != null) {
            final Openable openable = current.getLookup().lookup(Openable.class);
            if (openable != null) {
              openable.open();
            }
          }
        }
      }
    });
    
  }

  @Override
  public String getDisplayName() {
    final MMDDataObject mmddo = content;
    return mmddo == null ? "NONE" : mmddo.getName();
  }

  @Override
  public String getDisplayHint() {
    return "Current mind map object in tree view";
  }

  @Override
  public JComponent getComponent() {
    return this;
  }

  @Override
  public void panelActivated(final Lookup context) {
    this.curContext = context.lookup(MY_DATA);
    this.curContext.addLookupListener(this);
    extractDataFromContextAndUpdate();
  }

  private void extractDataFromContextAndUpdate() {
    final Lookup.Result<? extends MMDDataObject> ctx = this.curContext;
    if (ctx == null) {
      this.content = null;
    }
    else {
      final Collection<? extends MMDDataObject> clct = ctx.allInstances();
      if (clct == null || clct.isEmpty()) {
        this.content = null;
      }
      else {
        this.content = clct.iterator().next();
      }
    }
    updateContent();
  }

  private String getDocumentText(){
    String result = null;
    if (this.content != null){
      final MMDEditorSupport editor = this.content.getLookup().lookup(MMDEditorSupport.class);
      if (editor!=null){
        result = editor.getDocumentText();
      }
    }
    return result;
  }
  
  private void updateContent() {
     final String text = getDocumentText();
    if (text != null) {
      try {
        this.mindMapTree.setModel(new MindMap(new StringReader(text)));
      }
      catch (IOException ex) {
        Logger.error("Can't parse mind map text", ex);
        this.mindMapTree.setModel(null);
      }
    }
    else {
      this.mindMapTree.setModel(null);
    }
  }

  @Override
  public void panelDeactivated() {
    if (this.curContext != null) {
      this.curContext.removeLookupListener(this);
    }
    this.mindMapTree.setModel(null);
    this.content = null;
    this.curContext = null;
  }

  @Override
  public Lookup getLookup() {
    return null;
  }

  @Override
  public void resultChanged(final LookupEvent ev) {
    extractDataFromContextAndUpdate();
  }

}
