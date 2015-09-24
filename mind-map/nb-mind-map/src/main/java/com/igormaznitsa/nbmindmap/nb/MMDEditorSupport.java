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
package com.igormaznitsa.nbmindmap.nb;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.StyledDocument;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.editor.GuardedDocument;
import org.openide.awt.UndoRedo;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.text.CloneableEditor;
import org.openide.text.DataEditorSupport;
import org.openide.windows.CloneableTopComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MMDEditorSupport extends DataEditorSupport implements OpenCookie, EditCookie, EditorCookie, EditorCookie.Observable, ChangeListener {

  private final List<WeakReference<MMDGraphEditor>> listeners = new ArrayList<>();

  private static final Logger logger = LoggerFactory.getLogger(MMDEditorSupport.class);

  public MMDEditorSupport(final MMDDataObject obj) {
    super(obj, new MMDDataEnv(obj));
    setMIMEType(MMDDataObject.MIME);
  }

  public Project getProject() {
    return FileOwnerQuery.getOwner(getDataObject().getPrimaryFile());
  }

  public File getProjectDirectory() {
    File result = null;
    final Project project = getProject();
    if (project != null) {
      final FileObject projDir = project.getProjectDirectory();
      if (projDir != null) {
        return FileUtil.toFile(projDir);
      }
    }
    return result;
  }

  @Override
  protected CloneableEditor createCloneableEditor() {
    final MMDGraphEditor editor = new MMDGraphEditor(this);
    this.listeners.add(new WeakReference<>(editor));
    return editor;
  }

  public String getDocumentText() {
    if (this.getDataObject().isValid()) {
      try {
        final StyledDocument doc = this.openDocument();
        return doc.getText(0, doc.getLength());
      }
      catch (Exception ex) {
        logger.error("Can't get document text", ex); //NOI18N
        return null;
      }
    }
    else {
      logger.warn("DataObject " + this.getDataObject() + " is not valid");
      return null;
    }
  }

  @Override
  protected boolean asynchronousOpen() {
    return false;
  }

  @Override
  protected UndoRedo.Manager createUndoRedoManager() {
    final UndoRedo.Manager result = super.createUndoRedoManager();
    result.addChangeListener(this);
    return result;
  }

  public void onEditorActivated() {
  }

  public org.openide.awt.UndoRedo getUndoRedoObject() {
    return super.getUndoRedo();
  }

  @Override
  protected String messageHtmlName() {
    return super.messageHtmlName();
  }

  @Override
  protected String messageName() {
    return super.messageName();
  }

  @Override
  protected String messageToolTip() {
    return super.messageToolTip();
  }

  public synchronized void replaceDocumentText(final String text) {
    try {
      final GuardedDocument doc = (GuardedDocument) this.openDocument();
      doc.runAtomic(new Runnable() {
        @Override
        public void run() {
          try {
            doc.remove(0, doc.getLength());
            doc.insertString(0, text, null);
          }
          catch (Exception ex) {
            logger.error("Can't replace text", ex); //NOI18N
          }
        }
      });
    }
    catch (Exception ex) {
      logger.error("Can't open document to replace text", ex); //NOI18N
    }

  }

  @Override
  public void stateChanged(final ChangeEvent e) {
    for (final WeakReference<MMDGraphEditor> g : this.listeners) {
      final MMDGraphEditor ge = g.get();
      if (ge != null) {
        ge.updateView();
      }
    }
  }

  public void focusToPosition(final boolean enforceVisibilityOfTopic, final int[] positionPath) {
    final Enumeration<CloneableTopComponent> editors = this.allEditors.getComponents();
    while (editors.hasMoreElements()) {
      final MMDGraphEditor editor = (MMDGraphEditor) editors.nextElement();
      editor.focusToPath(enforceVisibilityOfTopic, positionPath);
    }
  }

  private static final class MMDDataEnv extends DataEditorSupport.Env {

    private static final long serialVersionUID = 6101101548072950629L;

    private final MMDDataObject dataObj;

    public MMDDataEnv(final MMDDataObject obj) {
      super(obj);
      this.dataObj = obj;
    }

    @Override
    protected FileObject getFile() {
      return this.dataObj.getPrimaryFile();
    }

    @Override
    protected FileLock takeLock() throws IOException {
      return this.dataObj.getPrimaryEntry().takeLock();
    }
  }

  @Override
  public void updateTitles() {
    super.updateTitles();
  }
}
