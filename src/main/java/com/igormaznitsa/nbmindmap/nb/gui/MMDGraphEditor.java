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

import com.igormaznitsa.nbmindmap.nb.dataobj.MMDDataObject;
import com.igormaznitsa.nbmindmap.nb.dataobj.MMDEditorSupport;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;

public class MMDGraphEditor extends CloneableTopComponent implements MultiViewElement, MultiViewDescription, Runnable {

  private static final long serialVersionUID = -8776707243607267446L;

  private JComponent toolbar;
  private MultiViewElementCallback callback;
  private MMDEditorSupport editorSupport;

  public MMDGraphEditor() {
    super();
  }

  public MMDGraphEditor(final MMDEditorSupport support) {
    super();
    this.editorSupport = support;
  }

  @Override
  public JComponent getVisualRepresentation() {
    return this;
  }

  @Override
  public void componentActivated() {
  }

  @Override
  public void componentClosed() {
  }

  @Override
  public void componentOpened() {
  }

  @Override
  public void componentShowing() {
  }

  @Override
  public void componentDeactivated() {
  }

  @Override
  public void componentHidden() {
  }

  @Override
  public String getDisplayName() {
    return "Graph";
  }
  
  @Override
  public JComponent getToolbarRepresentation() {
    if (this.toolbar == null) {
        this.toolbar = new JPanel();
    }
    return this.toolbar;
  }

  @Override
  public String preferredID() {
    return "MMDGraphEditor";
  }

  @Override
  public void setMultiViewCallback(final MultiViewElementCallback cllback) {
    this.callback = cllback;
    updateName();
  }

  public void updateName() {
    final MMDEditorSupport ces = this.editorSupport;

    if (ces != null) {
      Mutex.EVENT.writeAccess(
              new Runnable() {
                @Override
                public void run() {
                  String name = ces.messageHtmlName();
                  setHtmlDisplayName(name);
                  name = ces.messageName();
                  setDisplayName(name);
                  setName(name); // XXX compatibility

                  setToolTipText(ces.messageToolTip());
                }
              }
      );
    }
  }

  @Override
  public CloseOperationState canCloseElement() {
    return CloseOperationState.STATE_OK;
  }

  @Override
  public void run() {
    final MultiViewElementCallback c = this.callback;
    if (c == null) {
      return;
    }
    TopComponent tc = c.getTopComponent();
    if (tc == null) {
      return;
    }
    updateName();
    tc.setName(this.getName());
    tc.setDisplayName(this.getDisplayName());
    tc.setHtmlDisplayName(this.getHtmlDisplayName());
  }

  @Override
  public Lookup getLookup() {
    return ((MMDDataObject) (this.editorSupport).getDataObject()).getNodeDelegate().getLookup();
  }

  @Override
  public MultiViewElement createElement() {
    return this;
  }
}
