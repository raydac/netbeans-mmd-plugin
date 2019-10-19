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

package com.igormaznitsa.ideamindmap.view.nodes;

import com.igormaznitsa.ideamindmap.utils.AllIcons;
import com.igormaznitsa.ideamindmap.utils.IdeaUtils;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.ModuleGroup;
import com.intellij.ide.projectView.impl.nodes.AbstractProjectNode;
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.Icon;

public class KnowledgeViewProjectNode extends AbstractProjectNode {
  public KnowledgeViewProjectNode(final Project project, final ViewSettings viewSettings) {
    super(project, project, viewSettings);
  }

  @Override
  protected AbstractTreeNode createModuleGroup(final Module module)
      throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    return null;
  }

  @Override
  protected AbstractTreeNode createModuleGroupNode(final ModuleGroup moduleGroup)
      throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    return null;
  }

  @Nonnull
  @Override
  public Collection<? extends AbstractTreeNode> getChildren() {
    final ArrayList<AbstractTreeNode> result = new ArrayList<AbstractTreeNode>();
    final PsiManager psiManager = PsiManager.getInstance(getProject());

    for (final Module m : ModuleManager.getInstance(myProject).getModules()) {
      final VirtualFile knowledgeFolder = IdeaUtils.findKnowledgeFolderForModule(m, false);
      if (knowledgeFolder != null) {

        final String moduleName = m.getName();

        final PsiDirectory dir = psiManager.findDirectory(knowledgeFolder);
        final PsiDirectoryNode node = new PsiDirectoryNode(myProject, dir, getSettings()) {

          protected Icon patchIcon(final Icon original, final VirtualFile file) {
            return AllIcons.File.FOLDER;
          }

          @Override
          public String getTitle() {
            return moduleName;
          }

          @Override
          public String toString() {
            return moduleName;
          }

          @Override
          public boolean isFQNameShown() {
            return false;
          }

          @Override
          public VirtualFile getVirtualFile() {
            return knowledgeFolder;
          }

          @Nullable
          @Override
          protected String calcTooltip() {
            return "The Knowledge folder for " + m.getName();
          }

          @Override
          protected boolean shouldShowModuleName() {
            return false;
          }

        };
        result.add(node);
      }
    }
    return result;
  }
}
