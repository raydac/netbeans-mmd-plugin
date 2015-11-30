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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;

public class KnowledgeViewProjectNode extends AbstractProjectNode {
  public KnowledgeViewProjectNode(final Project project, final ViewSettings viewSettings) {
    super(project, project, viewSettings);
  }

  @Override protected AbstractTreeNode createModuleGroup(final Module module)
    throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    return null;
  }

  @Override protected AbstractTreeNode createModuleGroupNode(final ModuleGroup moduleGroup)
    throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    return null;
  }

  @NotNull @Override public Collection<? extends AbstractTreeNode> getChildren() {
    final ArrayList<AbstractTreeNode> result = new ArrayList<AbstractTreeNode>();
    final PsiManager psiManager = PsiManager.getInstance(getProject());

    for (final Module m : ModuleManager.getInstance(myProject).getModules()) {
      final VirtualFile knowledgeFolder = IdeaUtils.findKnowledgeFolderForModule(m, false);
      if (knowledgeFolder != null) {

        final String moduleName = m.getName();

        final PsiDirectory dir = psiManager.findDirectory(knowledgeFolder);
        final PsiDirectoryNode node = new PsiDirectoryNode(myProject, dir, getSettings()) {

          @Override protected Icon patchIcon(Icon original, VirtualFile file) {
            return AllIcons.File.FOLDER;
          }

          @Override public String getTitle() {
            return moduleName;
          }

          @Override public String toString() {
            return moduleName;
          }

          @Override public boolean isFQNameShown() {
            return false;
          }

          @Override public VirtualFile getVirtualFile() {
            return knowledgeFolder;
          }

          @Nullable @Override protected String calcTooltip() {
            return "The Knowledge folder for " + m.getName();
          }

          @Override protected boolean shouldShowModuleName() {
            return false;
          }

          @Override protected boolean shouldShowSourcesRoot() {
            return false;
          }
        };
        result.add(node);
      }
    }
    return result;
  }
}
