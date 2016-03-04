package com.igormaznitsa.ideamindmap.lang.refactoring.references.providers;

import com.igormaznitsa.ideamindmap.lang.psi.PsiExtraFile;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class PsiExtraFileReferenceProvider extends PsiReferenceProvider {

  @NotNull @Override public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
    PsiReference[] result = PsiReference.EMPTY_ARRAY;

    final PsiExtraFile extraFile = (PsiExtraFile) element;
    final VirtualFile targetFile = extraFile.findTargetFile();

    if (targetFile != null) {
      final TextRange range = new TextRange(0, extraFile.getTextLength());

      result = new PsiReference[] { new PsiExtraFileReference(extraFile, range) };
    }

    return result;
  }

}
