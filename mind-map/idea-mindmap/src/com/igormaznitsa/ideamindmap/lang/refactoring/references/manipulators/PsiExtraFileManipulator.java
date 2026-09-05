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

import static com.igormaznitsa.mindmap.model.logger.LoggerFactory.getLogger;

import com.igormaznitsa.ideamindmap.lang.psi.PsiExtraFile;
import com.igormaznitsa.mindmap.model.MMapURI;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.AbstractElementManipulator;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import java.net.URISyntaxException;
import javax.annotation.Nonnull;

public class PsiExtraFileManipulator extends AbstractElementManipulator<PsiExtraFile> {
  private static final Logger LOGGER = getLogger(PsiExtraFileManipulator.class);
  private static final int PRE_TAG_LENGTH = 5;

  @Override
  public PsiExtraFile handleContentChange(
      @Nonnull final PsiExtraFile element,
      @Nonnull final TextRange range,
      final String newContent) throws IncorrectOperationException {
    final String oldText = element.getText();
    if (range.getStartOffset() < 0 || range.getEndOffset() > oldText.length()) {
      return element;
    }

    final PsiFile containingFile = element.getContainingFile();
    if (containingFile == null || containingFile.getVirtualFile() == null) {
      return element;
    }

    final Document document = FileDocumentManager.getInstance().getDocument(containingFile.getVirtualFile());
    if (document == null) {
      return element;
    }

    final int start = element.getTextRange().getStartOffset() + range.getStartOffset();
    final int end = element.getTextRange().getStartOffset() + range.getEndOffset();
    document.replaceString(start, end, newContent);

    try {
      element.setMMapURI(new MMapURI(newContent.trim()));
    } catch (URISyntaxException ex) {
      LOGGER.error("Can't apply renamed file URI: " + newContent, ex);
      throw new IncorrectOperationException("Can't apply renamed file URI", (Throwable) ex);
    }

    return element;
  }

  @Nonnull
  @Override
  public TextRange getRangeInElement(@Nonnull final PsiExtraFile element) {
    final String text = element.getText();
    final int end = text.lastIndexOf('<');
    if (end <= PRE_TAG_LENGTH || PRE_TAG_LENGTH >= text.length()) {
      return new TextRange(0, text.length());
    }
    return new TextRange(PRE_TAG_LENGTH, end);
  }
}
