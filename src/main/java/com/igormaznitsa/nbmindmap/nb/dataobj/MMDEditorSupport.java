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
package com.igormaznitsa.nbmindmap.nb.dataobj;

import com.igormaznitsa.nbmindmap.nb.gui.MMDTextPanel;
import com.igormaznitsa.nbmindmap.nb.gui.MMDGraphPanel;
import com.igormaznitsa.nbmindmap.utils.Logger;
import java.io.IOException;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.StyledDocument;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.editor.GuardedDocument;
import org.openide.awt.UndoRedo;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.DataEditorSupport;

public class MMDEditorSupport extends DataEditorSupport implements OpenCookie, EditCookie, EditorCookie {

  private volatile MultiViewDescription[] lastGeneratedDescriptions;
  
  public static MMDEditorSupport create(final MMDDataObject obj) {
    return new MMDEditorSupport(obj);
  }

  private MMDEditorSupport(MMDDataObject obj) {
    super(obj, new MMDDataEnv(obj));
  }

  public Project getProject(){
    return FileOwnerQuery.getOwner(getDataObject().getPrimaryFile());
  }
  
  public FileObject makeRelativeForProject(final String path){
    final Project proj = getProject();
    if (proj == null) return null;
    final FileObject projFileObject = proj.getProjectDirectory();
    return projFileObject.getFileObject(path);
  }

  @Override
  protected boolean close(final boolean ask) {
    final boolean result = super.close(ask);
    if (result){
      this.lastGeneratedDescriptions = null;
    }
    return result;
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
  public boolean notifyModified() {
    boolean retValue;

    retValue = super.notifyModified();
    if (retValue) {
      MMDDataObject obj = (MMDDataObject) getDataObject();
      obj.ic.add(env);
    }

    return retValue;
  }

  @Override
  protected boolean asynchronousOpen() {
    return false;
  }

  @Override
  protected UndoRedo.Manager createUndoRedoManager() {
    final UndoRedo.Manager result = super.createUndoRedoManager();

    result.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            final MultiViewDescription[] desc = lastGeneratedDescriptions;
            if (desc!=null){
              for(final MultiViewDescription d : desc){
                if (d instanceof MMDGraphPanel){
                  ((MMDGraphPanel)d).updateView();
                }
              }
            }
          }
        });
      }
    });

    return result;
  }

  public UndoRedo.Manager getUndoRedoObject() {
    return this.getUndoRedo();
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

  @Override
  protected Pane createPane() {
    final MMDGraphPanel graphPanel = new MMDGraphPanel(this);
    final MMDTextPanel textPanel = new MMDTextPanel(this);
    
    final MultiViewDescription[] descriptions = {
      graphPanel,
      textPanel
    };

    this.lastGeneratedDescriptions = descriptions;
    
    return (CloneableEditorSupport.Pane) MultiViewFactory.createCloneableMultiView(descriptions, descriptions[0]);
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

  private static final class MMDDataEnv extends DataEditorSupport.Env implements SaveCookie {

    private static final long serialVersionUID = 6101101548072950629L;

    public MMDDataEnv(final MMDDataObject obj) {
      super(obj);
    }

    @Override
    protected FileObject getFile() {
      return super.getDataObject().getPrimaryFile();
    }

    @Override
    protected FileLock takeLock() throws IOException {
      return ((MMDDataObject) super.getDataObject()).getPrimaryEntry().takeLock();
    }

    @Override
    public void save() throws IOException {
      final MMDEditorSupport ed = (MMDEditorSupport) this.findCloneableOpenSupport();
      ed.saveDocument();
    }

  }

}
