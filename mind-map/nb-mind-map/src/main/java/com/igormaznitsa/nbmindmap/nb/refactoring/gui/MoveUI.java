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
package com.igormaznitsa.nbmindmap.nb.refactoring.gui;

import javax.swing.event.ChangeListener;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

public class MoveUI extends AbstractRefactoringUI {

  private final Lookup lookup;
  private final FileObject [] files;
  private final MoveRefactoring refactoring;
  private MovePanel panel;
  
  public MoveUI(final Lookup lookup, final FileObject [] files) {
    this.files = files;
    this.lookup = lookup;
    this.refactoring = new MoveRefactoring(Lookups.fixed((Object[])files));
  }
  
  @Override
  public String getName() {
    return "Name";
  }

  @Override
  public String getDescription() {
    return "Description";
  }

  @Override
  public boolean isQuery() {
    return false;
  }

  @Override
  public CustomRefactoringPanel getPanel(final ChangeListener parent) {
    if (this.panel == null){
      this.panel = new MovePanel(this.lookup, this.files, parent);
    }
    return this.panel;
  }

  @Override
  public Problem setParameters() {
    final FileObject fo = this.panel.getTarget();
    if (fo != null) {
      this.refactoring.setTarget(Lookups.fixed(fo.toURL()));
    }
    else {
      this.refactoring.setTarget(Lookup.EMPTY);
    }
    return this.refactoring.checkParameters();
  }

  @Override
  public Problem checkParameters() {
    final FileObject fo = this.panel.getTarget();
    if (fo!=null){
      this.refactoring.setTarget(Lookups.fixed(fo.toURL()));
    }else{
      this.refactoring.setTarget(Lookup.EMPTY);
    }
    return this.refactoring.checkParameters();
  }

  @Override
  public boolean hasParameters() {
    return true;
  }

  @Override
  public AbstractRefactoring getRefactoring() {
    return this.refactoring;
  }

  @Override
  public HelpCtx getHelpCtx() {
    return null;
  }
  
}
