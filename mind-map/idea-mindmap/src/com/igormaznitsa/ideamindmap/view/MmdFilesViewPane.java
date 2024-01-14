/*
 * Copyright (C) 2015-2024 Igor A. Maznitsa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.igormaznitsa.ideamindmap.view;

import static java.util.Objects.requireNonNull;

import com.igormaznitsa.ideamindmap.utils.AllIcons;
import com.intellij.ide.SelectInTarget;
import com.intellij.ide.impl.ProjectViewSelectInTarget;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.ProjectAbstractTreeStructureBase;
import com.intellij.ide.projectView.impl.ProjectTreeStructure;
import com.intellij.ide.projectView.impl.ProjectViewPane;
import com.intellij.ide.projectView.impl.ProjectViewTree;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VirtualFile;
import javax.annotation.Nonnull;
import javax.swing.Icon;
import javax.swing.tree.DefaultTreeModel;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class MmdFilesViewPane extends ProjectViewPane {
  @NonNls
  public static final String ID = "NBKnowledgePane";

  public MmdFilesViewPane(@Nonnull Project project) {
    super(project);
  }

  @Override
  @NotNull
  public String getTitle() {
    return "MMD Files";
  }

  @Override
  @NotNull
  public Icon getIcon() {
    return requireNonNull(AllIcons.Logo.MINDMAP);
  }

  @Nonnull
  @Override
  public String getId() {
    return ID;
  }

  @Override
  public int getWeight() {
    return 51175;
  }

  @Override
  public @NotNull SelectInTarget createSelectInTarget() {
    return new ProjectViewSelectInTarget(myProject) {

      @Override
      public String toString() {
        return ID;
      }

      @Override
      public String getMinorViewId() {
        return ID;
      }

      @Override
      public float getWeight() {
        return 10;
      }
    };
  }

  @NotNull
  @Override
  protected ProjectAbstractTreeStructureBase createStructure() {
    return new ProjectTreeStructure(myProject, ID) {
      @Override
      protected MmdFileNode createRoot(@NotNull Project project, @NotNull
      ViewSettings settings) {
        return new MmdFileNode(project, settings, getProjectDir(project), MmdFilesViewPane.this);
      }

      @NotNull
      private VirtualFile getProjectDir(Project project) {
        VirtualFile guessedProjectDir = ProjectUtil.guessProjectDir(project);
        if (guessedProjectDir == null) {
          throw new IllegalStateException("Could not get project directory");
        }
        return guessedProjectDir;
      }

      // Children will be searched in async mode
      @Override
      public boolean isToBuildChildrenInBackground(@NotNull Object element) {
        return true;
      }
    };
  }

  @NotNull
  @Override
  protected ProjectViewTree createTree(@NotNull DefaultTreeModel model) {
    return new ProjectViewTree(model) {
      @Override
      public boolean isRootVisible() {
        return true;
      }
    };
  }
}
