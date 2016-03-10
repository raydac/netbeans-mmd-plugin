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
package com.igormaznitsa.ideamindmap.view;

import com.igormaznitsa.ideamindmap.view.nodes.KnowledgeViewProjectNode;
import com.intellij.ide.projectView.TreeStructureProvider;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.ide.util.treeView.AbstractTreeStructureBase;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class KnowledgeViewPanelTreeStructure extends AbstractTreeStructureBase implements ViewSettings, TreeStructureProvider, DumbAware {

  private List<TreeStructureProvider> myProviders;

  private final AbstractTreeNode myRoot;

  public KnowledgeViewPanelTreeStructure(final Project project) {
    super(project);
    myRoot = createRoot(project, this);
  }

  protected AbstractTreeNode createRoot(final Project project, final ViewSettings settings) {
    return new KnowledgeViewProjectNode(project, settings);
  }

  @Override
  public List<TreeStructureProvider> getProviders() {
    if (myProviders == null) {
      final TreeStructureProvider[] providers = new TreeStructureProvider[]{this};
      myProviders = Arrays.asList(providers);
    }
    return myProviders;
  }

  public void setProviders(TreeStructureProvider... treeStructureProviders) {
    myProviders = treeStructureProviders == null ? null : Arrays.asList(treeStructureProviders);
  }

  @Override public Object getRootElement() {
    return this.myRoot;
  }

  @Override public void commit() {
    PsiDocumentManager.getInstance(myProject).commitAllDocuments();
  }

  @Override public boolean hasSomethingToCommit() {
    return !myProject.isDisposed()
      && PsiDocumentManager.getInstance(myProject).hasUncommitedDocuments();
  }

  @Override public boolean isShowMembers() {
    return false;
  }

  @Override public boolean isStructureView() {
    return false;
  }

  @Override public boolean isShowModules() {
    return false;
  }

  @Override public boolean isFlattenPackages() {
    return false;
  }

  @Override public boolean isAbbreviatePackageNames() {
    return false;
  }

  @Override public boolean isHideEmptyMiddlePackages() {
    return false;
  }

  @Override public boolean isShowLibraryContents() {
    return false;
  }

  @Nonnull @Override public Collection<AbstractTreeNode> modify(@Nonnull final AbstractTreeNode parent, @Nonnull final Collection<AbstractTreeNode> children, final ViewSettings settings) {
    return children;
  }

  @Nullable @Override public Object getData(Collection<AbstractTreeNode> selected, String dataName) {
    return null;
  }
}
