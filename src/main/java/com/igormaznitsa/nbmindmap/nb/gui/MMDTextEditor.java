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
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.text.Document;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.openide.text.CloneableEditor;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.windows.TopComponent;

public class MMDTextEditor extends CloneableEditor implements MultiViewElement, Runnable {

  private static final long serialVersionUID = -8776707243607267446L;

  private JComponent toolbar;
  private MultiViewElementCallback callback;

  public MMDTextEditor() {

  }

  public MMDTextEditor(final MMDEditorSupport support) {
    super(support);
  }

  @Override
  public JComponent getVisualRepresentation() {
    return this;
  }

  @Override
  public void componentActivated() {
    super.componentActivated();
  }

  @Override
  public void componentClosed() {
    super.componentClosed();
  }

  @Override
  public void componentOpened() {
    super.componentOpened();
  }

  @Override
  public void componentShowing() {
    super.componentShowing();
  }

  @Override
  public void componentDeactivated() {
    super.componentDeactivated();
  }

  @Override
  public void componentHidden() {
    super.componentHidden();
  }

  @Override
  public JComponent getToolbarRepresentation() {
    if (this.toolbar == null) {
      final JEditorPane pane = this.pane;
      if (pane != null) {
        Document doc = pane.getDocument();
        if (doc instanceof NbDocument.CustomToolbar) {
          this.toolbar = ((NbDocument.CustomToolbar) doc).createToolbar(pane);
        }
      }
      if (this.toolbar == null) {
        this.toolbar = new JPanel();
      }
    }
    return this.toolbar;
  }

  @Override
  public void setMultiViewCallback(final MultiViewElementCallback cllback) {
    this.callback = cllback;
    updateName();
  }

  @Override
  public CloseOperationState canCloseElement() {
    return CloseOperationState.STATE_OK;
  }

  @Override
  public int getPersistenceType() {
    return PERSISTENCE_NEVER;
  }

  @Override
  public void updateName() {
    Mutex.EVENT.readAccess(this);
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
    super.updateName();
    tc.setName(this.getName());
    tc.setDisplayName(this.getDisplayName());
    tc.setHtmlDisplayName(this.getHtmlDisplayName());
  }

  @Override
  public Lookup getLookup() {
    return ((MMDDataObject) ((MMDEditorSupport) cloneableEditorSupport()).getDataObject()).getNodeDelegate().getLookup();
  }
}
