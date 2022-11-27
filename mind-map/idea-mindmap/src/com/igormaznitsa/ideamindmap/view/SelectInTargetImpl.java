/*
 * Copyright (C) 2015-2022 Igor A. Maznitsa
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

import com.intellij.ide.SelectInContext;
import com.intellij.ide.SelectInTarget;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import javax.annotation.Nonnull;
import org.jetbrains.annotations.Nullable;

public class SelectInTargetImpl implements SelectInTarget {
  private final Project myProject;

  public SelectInTargetImpl(@Nonnull final Project project) {
    this.myProject = project;
  }

  @Override
  public boolean canSelect(@Nonnull final SelectInContext context) {
    if (this.myProject.isDisposed()) {
      return false;
    } else {
      VirtualFile virtualFile = context.getVirtualFile();
      if (!virtualFile.isValid()) {
        return false;
      } else {
        final Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
        Object psiFile;
        if (document != null) {
          psiFile = PsiDocumentManager.getInstance(this.myProject).getPsiFile(document);
        } else if (context.getSelectorInFile() instanceof PsiFile) {
          psiFile = context.getSelectorInFile();
        } else if (virtualFile.isDirectory()) {
          psiFile = PsiManager.getInstance(this.myProject).findDirectory(virtualFile);
        } else {
          psiFile = PsiManager.getInstance(this.myProject).findFile(virtualFile);
        }
        return psiFile != null;
      }
    }
  }

  @Override
  public void selectIn(@Nonnull final SelectInContext context, final boolean b) {
  }

  @Nullable
  @Override
  public String getToolWindowId() {
    return KnowledgeViewPane.ID;
  }

  @Nullable
  @Override
  public String getMinorViewId() {
    return KnowledgeViewPane.ID;
  }

  @Override
  public float getWeight() {
    return 4;
  }
}
