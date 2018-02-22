/*
 * Copyright 2015-2018 Igor Maznitsa.
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
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

import com.igormaznitsa.meta.common.utils.Assertions;

public class RenameUI extends AbstractMMDRefactoringUI {

  private final RenameRefactoring refactoring;
  private RenamePanel panel;
  private final String name;
  private final FileObject  file;
  private final Lookup lookup;
  
  public RenameUI(final Lookup lookup, final FileObject file){
    this.file = file;
    this.lookup = lookup;
    this.name = file.getName();
    this.refactoring = new RenameRefactoring(Lookups.fixed(file, this));
  }
  
  @Override
  public String getName() {
    return String.format(BUNDLE.getString("RenameUI.getName"),this.name);
  }

  @Override
  public String getDescription() {
    return String.format(BUNDLE.getString("RenameUI.getDescription"),this.name);
  }

  @Override
  public boolean isQuery() {
    return false;
  }

  @Override
  public CustomRefactoringPanel getPanel(final ChangeListener parent) {
    if (this.panel == null){
      this.panel = new RenamePanel(this.file.getName(), this.lookup, parent);
    }
    return this.panel;
  }

  @Override
  public Problem setParameters() {
    this.refactoring.setNewName(Assertions.assertNotNull(this.panel).getNewName());
    return this.refactoring.checkParameters();
  }

  @Override
  public Problem checkParameters() {
    this.refactoring.setNewName(Assertions.assertNotNull(this.panel).getNewName());
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
