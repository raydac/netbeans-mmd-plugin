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
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

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
