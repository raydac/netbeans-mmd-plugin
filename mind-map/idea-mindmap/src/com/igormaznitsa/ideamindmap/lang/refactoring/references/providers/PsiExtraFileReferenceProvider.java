package com.igormaznitsa.ideamindmap.lang.refactoring.references.providers;

import com.igormaznitsa.ideamindmap.lang.psi.PsiExtraFile;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.impl.PsiManagerEx;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PsiExtraFileReferenceProvider extends PsiReferenceProvider {

  @NotNull @Override public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
    PsiReference [] result = PsiReference.EMPTY_ARRAY;

    final PsiExtraFile extraFile = (PsiExtraFile)element;
    final VirtualFile targetFile = extraFile.findTargetFile();

    if (targetFile != null) {
      final TextRange range = new TextRange(0,extraFile.getTextLength());

      result = new PsiReference[]{new PsiReferenceBase<PsiExtraFile>(extraFile,true) {

        @Override public TextRange getRangeInElement() {
          return range;
        }

        @Nullable @Override public PsiElement resolve() {
          final VirtualFile theFile = this.getElement().findTargetFile();
          if (theFile == null) {
            return null;
          } else {
            return PsiManagerEx.getInstance(this.getElement().getProject()).findFile(theFile);
          }
        }

        @Override public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
          return super.handleElementRename(newElementName);
        }

        @Override public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
          return super.bindToElement(element);
        }

        @NotNull @Override public Object[] getVariants() {
          return new Object[0];
        }
      }};
    }

    return result;
  }
}
