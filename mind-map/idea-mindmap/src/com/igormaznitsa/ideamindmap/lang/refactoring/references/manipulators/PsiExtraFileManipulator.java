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
