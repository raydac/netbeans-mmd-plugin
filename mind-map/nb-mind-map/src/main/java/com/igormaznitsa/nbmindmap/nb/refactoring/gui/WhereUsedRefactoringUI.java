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
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

public class WhereUsedRefactoringUI extends AbstractMMDRefactoringUI {

  private final WhereUsedQuery query;
  private WhereUsedPanel panel;
  private final String name;
  private final String nameWithExt;
  private final FileObject fileObj;
  private final Lookup lookup;
  
  public WhereUsedRefactoringUI(final Lookup lookup, final FileObject mmd){
    this.fileObj = mmd;
    this.lookup = lookup;
    
    this.name = mmd.getName();
    this.nameWithExt = mmd.getNameExt();
    this.query = new WhereUsedQuery(Lookups.fixed(mmd, this));
  }
  
  @Override
  public String getName() {
    return String.format(BUNDLE.getString("WhereUsedUI.getName"),this.nameWithExt);
  }

  @Override
  public String getDescription() {
    return String.format(BUNDLE.getString("WhereUsedUI.getDescription"),this.nameWithExt);
  }

  @Override
  public boolean isQuery() {
    return true;
  }

  @Override
  public CustomRefactoringPanel getPanel(final ChangeListener parent) {
    if (this.panel == null){
      this.panel = new WhereUsedPanel(lookup,this.name, false, parent);
    }
    return this.panel;
  }

  @Override
  public Problem setParameters() {
    this.query.putValue(WhereUsedQuery.SEARCH_IN_COMMENTS, this.panel.isSearchInComments());
    return this.query.checkParameters();
  }

  @Override
  public Problem checkParameters() {
    return null;
  }

  @Override
  public boolean hasParameters() {
    return true;
  }

  @Override
  public AbstractRefactoring getRefactoring() {
    return this.query;
  }

  @Override
  public HelpCtx getHelpCtx() {
    return null;
  }
  
}
