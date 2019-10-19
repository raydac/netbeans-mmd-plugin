package com.igormaznitsa.ideamindmap.lang.refactoring.references.manipulators;

import com.igormaznitsa.ideamindmap.lang.psi.PsiExtraFile;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.AbstractElementManipulator;
import com.intellij.util.IncorrectOperationException;
import javax.annotation.Nonnull;

public class PsiExtraFileManipulator extends AbstractElementManipulator<PsiExtraFile> {

  @Override
  public PsiExtraFile handleContentChange(@Nonnull PsiExtraFile element, @Nonnull TextRange range, String newContent) throws IncorrectOperationException {
    return element;
  }

  @Nonnull
  @Override
  public TextRange getRangeInElement(@Nonnull PsiExtraFile element) {
    return new TextRange(5, element.getText().lastIndexOf('<'));
  }
}
