package com.igormaznitsa.ideamindmap.lang.refactoring.references.manipulators;

import com.igormaznitsa.ideamindmap.lang.psi.PsiExtraFile;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.AbstractElementManipulator;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

public class PsiExtraFileManipulator extends AbstractElementManipulator<PsiExtraFile> {

  @Override public PsiExtraFile handleContentChange(@NotNull PsiExtraFile element, @NotNull TextRange range, String newContent) throws IncorrectOperationException {
    return element;
  }

  @NotNull @Override public TextRange getRangeInElement(@NotNull PsiExtraFile element) {
    return new TextRange(5,element.getText().lastIndexOf('<'));
  }
}
