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

import java.io.IOException;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.Environment;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

@MIMEResolver.ExtensionRegistration(displayName = "#MMDDataObject.extensionDisplayName", mimeType = MMDDataObject.MIME, extension = {MMDDataObject.MMD_EXT})
@DataObject.Registration(iconBase = "com/igormaznitsa/nbmindmap/icons/logo/logo16.png", displayName = "#MMDDataObject.displayName", mimeType = MMDDataObject.MIME)
public class MMDDataObject extends MultiDataObject implements CookieSet.Factory {

  private static final long serialVersionUID = -833567211826863321L;

  public static final String MIME = "text/x-nbmmd+plain"; //NOI18N
  public static final String MMD_EXT = "mmd"; //NOI18N

  private MMDEditorSupport editorSupport;

  private final SaveCookie saveCookie = new SaveCookie() {
    @Override
    public void save() throws IOException {
      getEditorSupport().saveDocument();
      setModified(false);
    }
  };
  
  public MMDDataObject(final FileObject pf, final MultiFileLoader loader) throws DataObjectExistsException, IOException {
    super(pf, loader);
    getCookieSet().add(MMDEditorSupport.class, this);
  }

  private synchronized MMDEditorSupport getEditorSupport() {
    if (this.editorSupport == null) {
      this.editorSupport = new MMDEditorSupport(this);
    }
    return this.editorSupport;
  }

  public Project findProject(){
    Project result = null;
    final FileObject primary = this.getPrimaryFile();
    if (primary!=null){
      result = FileOwnerQuery.getOwner(primary);
    }
    return result;
  }
  
  public void firePrimaryFileChanged(){
    super.firePropertyChange(PROP_PRIMARY_FILE, getPrimaryFile(), getPrimaryFile());
  }
  
  @Override
  public <T extends Node.Cookie> T createCookie(Class<T> klass) {
    if (klass.isAssignableFrom(MMDEditorSupport.class)) {
      return klass.cast(getEditorSupport());
    }
    return null;
  }

  @Override
  protected Node createNodeDelegate() {
    final Lookup env = Environment.find(this);
    Node result = env == null ? null : env.lookup(Node.class);
    if (result == null){
      result = new MMDDataNode(this, getLookup());
    }
    return result;
  }

  @Override
  public void setModified(final boolean modif) {
    super.setModified(modif);
    if (modif){
      if (this.getCookie(SaveCookie.class) == null){
          getCookieSet().add(this.saveCookie);
      }
    }else{
      if (this.saveCookie.equals(getCookie(SaveCookie.class))){
        getCookieSet().remove(this.saveCookie);
      }
    }
  }

  
  @Override
  public Lookup getLookup() {
    return this.getCookieSet().getLookup();
  }
}
