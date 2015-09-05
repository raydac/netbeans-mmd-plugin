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

import java.awt.Image;
import java.beans.BeanInfo;
import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

@MIMEResolver.ExtensionRegistration(displayName = "#MMDDataObject.extensionDisplayName", mimeType = MMDDataObject.MIME, extension = {MMDDataObject.MMD_EXT})
@DataObject.Registration(iconBase = "com/igormaznitsa/nbmindmap/icons/logo/logo16.png", displayName = "#MMDDataObject.displayName", mimeType = MMDDataObject.MIME)
public class MMDDataObject extends MultiDataObject {

  private static final long serialVersionUID = -833567211826863321L;

  public static final String MIME = "text/x-nbmmd+markdown"; //NOI18N
  public static final String MMD_EXT = "mmd"; //NOI18N

  private static final Image NODE_ICON_16x16 = ImageUtilities.loadImage("com/igormaznitsa/nbmindmap/icons/logo/logo16.png"); //NOI18N
  private static final Image NODE_ICON_32x32 = ImageUtilities.loadImage("com/igormaznitsa/nbmindmap/icons/logo/logo32.png"); //NOI18N

  final InstanceContent ic;
  private final AbstractLookup lookup;
  
  public MMDDataObject(final FileObject pf, final MultiFileLoader loader) throws DataObjectExistsException, IOException {
    super(pf, loader);
    registerEditor(MIME, true);

    this.ic = new InstanceContent();
    this.lookup = new AbstractLookup(ic);
    ic.add(MMDEditorSupport.create(this));
    ic.add(this);
  }

  @Override
  protected Node createNodeDelegate() {
    return new DataNode(this, Children.LEAF, this.lookup) {
      @Override
      public Image getIcon(final int type) {
        switch (type) {
          case BeanInfo.ICON_COLOR_32x32:
          case BeanInfo.ICON_MONO_32x32:
            return NODE_ICON_32x32;
          default:
            return NODE_ICON_16x16;
        }
      }
    };
  }

  @Override
  protected int associateLookup() {
    return 1;
  }

  @Override
  public Lookup getLookup() {
    return this.lookup;
  }

  @Override
  public <T extends Node.Cookie> T getCookie(final Class<T> type) {
    return lookup.lookup(type);
  }

}
