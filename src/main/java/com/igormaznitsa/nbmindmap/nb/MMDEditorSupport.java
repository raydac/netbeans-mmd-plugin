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

import com.igormaznitsa.nbmindmap.utils.Logger;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
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
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.text.CloneableEditor;
import org.openide.text.DataEditorSupport;

public class MMDEditorSupport extends DataEditorSupport implements OpenCookie, EditCookie, EditorCookie, ChangeListener {

  private final List<WeakReference<MMDGraphEditor>> listeners = new ArrayList<WeakReference<MMDGraphEditor>>();

  public static MMDEditorSupport create(final MMDDataObject obj) {
    return new MMDEditorSupport(obj);
  }

  private MMDEditorSupport(final MMDDataObject obj) {
    super(obj, new MMDDataEnv(obj));
  }

  public Project getProject() {
    return FileOwnerQuery.getOwner(getDataObject().getPrimaryFile());
  }

  public File getProjectDirectory(){
    File result = null;
    final Project project = getProject();
    if (project!=null){
      final FileObject projDir = project.getProjectDirectory();
      if (projDir!=null){
        return FileUtil.toFile(projDir);
      }
    }
    return result;
  }
  
  public FileObject makeRelativePathToProjectRoot(final String path) {
    final Project proj = getProject();
    if (proj == null) {
      return null;
    }
    final FileObject projFileObject = proj.getProjectDirectory();
    return projFileObject.getFileObject(path);
  }

  @Override
  protected CloneableEditor createCloneableEditor() {
    final MMDGraphEditor editor = new MMDGraphEditor(this);
    this.listeners.add(new WeakReference<MMDGraphEditor>(editor));
    return editor;
  }

  public String getDocumentText() {
    try {
      final StyledDocument doc = this.openDocument();
      return doc.getText(0, doc.getLength());
    }
    catch (Exception ex) {
      Logger.error("Can't get document text", ex);
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

  public UndoRedo.Manager getUndoRedoObject() {
    return this.getUndoRedo();
  }

  @Override
  protected boolean notifyModified() {
    boolean retValue = super.notifyModified();
    if (retValue) {
      final MMDDataObject obj = (MMDDataObject) getDataObject();
      obj.ic.add(env);
    }

    return retValue;
  }

  public boolean notifyModifiedVisual() {
    return this.notifyModified();
  }

  @Override
  protected void notifyUnmodified() {
    super.notifyUnmodified();
    MMDDataObject obj = (MMDDataObject) getDataObject();
    obj.ic.remove(env);
  }

  @Override
  public String messageHtmlName() {
    return super.messageHtmlName();
  }

  @Override
  public String messageName() {
    return super.messageName();
  }

  @Override
  public String messageToolTip() {
    return super.messageToolTip();
  }

  public void replaceDocumentText(final String text) {
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
            Logger.error("Can't replace text", ex);
          }
        }
      });
    }
    catch (Exception ex) {
      Logger.error("Can't open document to replace text", ex);
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

  private static final class MMDDataEnv extends DataEditorSupport.Env implements SaveCookie {

    private static final long serialVersionUID = 6101101548072950629L;

    private MMDDataObject dataObj;

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

    @Override
    public void save() throws IOException {
      final MMDEditorSupport ed = (MMDEditorSupport) this.findCloneableOpenSupport();
      ed.saveDocument();
    }
  }

  @Override
  public void updateTitles() {
    super.updateTitles();
  }
}
