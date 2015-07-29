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

import com.igormaznitsa.nbmindmap.nb.gui.MMDGraphEditor;
import java.io.IOException;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.DataEditorSupport;


public class MMDEditorSupport extends DataEditorSupport implements OpenCookie, EditCookie, EditorCookie {
  
  final MultiViewDescription [] descriptions = {
    new MMDGraphEditor(this),
    new MMDTextView(this)
  };
  
  public static MMDEditorSupport create(final MMDDataObject obj){
    return new MMDEditorSupport(obj);
  }
  
  private MMDEditorSupport(MMDDataObject obj){
    super(obj, new MMDDataEnv(obj));
  }

  @Override
  protected boolean notifyModified() {
    boolean retValue;
    
    retValue = super.notifyModified();
    if (retValue){
      MMDDataObject obj = (MMDDataObject)getDataObject();
      obj.ic.add(env);
    }
    
    return retValue;
  }

  @Override
  protected boolean asynchronousOpen() {
    return false;
  }

  @Override
  protected void notifyUnmodified() {
    super.notifyUnmodified();
    MMDDataObject obj = (MMDDataObject)getDataObject();
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
    return (CloneableEditorSupport.Pane)MultiViewFactory.createCloneableMultiView(this.descriptions, this.descriptions[0]);
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
