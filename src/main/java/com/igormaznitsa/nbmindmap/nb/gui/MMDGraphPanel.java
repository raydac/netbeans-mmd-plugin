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

import com.igormaznitsa.nbmindmap.nb.dataobj.MMDEditorSupport;
import java.awt.EventQueue;
import java.awt.Image;
import java.io.Serializable;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.windows.TopComponent;

public class MMDGraphPanel implements MultiViewDescription, Serializable {
  private static final long serialVersionUID = -5606801495357672062L;

  private MMDGraphEditor editor;
  private final MMDEditorSupport support;
  
  public MMDGraphPanel(final MMDEditorSupport editorSupport){
    this.support = editorSupport;
  }
  
  @Override
  public int getPersistenceType() {
    return TopComponent.PERSISTENCE_NEVER;
  }

  @Override
  public String getDisplayName() {
    return "Graph";
  }

  @Override
  public Image getIcon() {
    return ImageUtilities.loadImage("com/igormaznitsa/nbmindmap/icons/nbmm16.png");
  }

  @Override
  public HelpCtx getHelpCtx() {
    return HelpCtx.DEFAULT_HELP;
  }

  @Override
  public String preferredID() {
    return "text";
  }

  @Override
  public MultiViewElement createElement() {
    assert EventQueue.isDispatchThread();
    if (this.editor == null){
      this.editor = new MMDGraphEditor(this.support);
    }
    return this.editor;
  }
  
}
