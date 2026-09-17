/*
 * Copyright (C) 2015-2026 Igor A. Maznitsa
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

package com.igormaznitsa.ideamindmap.lang.structure;

import static java.util.Objects.requireNonNull;

import com.igormaznitsa.ideamindmap.editor.MindMapDocumentEditor;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.lang.PsiStructureViewFactory;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class MindMapStructureViewFactory implements PsiStructureViewFactory {

  @Nullable
  public static StructureViewBuilder forEditor(@Nonnull final MindMapDocumentEditor editor) {
    requireNonNull(editor, "editor must not be null");
    final PsiFile psiFile = PsiManager.getInstance(editor.getProject()).findFile(editor.getFile());
    return psiFile == null ? null : new MindMapStructureViewBuilder(psiFile, editor);
  }

  @Nullable
  static MindMapDocumentEditor findVisualEditor(@Nonnull final PsiFile psiFile) {
    final VirtualFile virtualFile = psiFile.getVirtualFile();
    if (virtualFile == null) {
      return null;
    }
    return Arrays.stream(FileEditorManager.getInstance(psiFile.getProject()).getEditors(virtualFile))
        .filter(MindMapDocumentEditor.class::isInstance)
        .map(MindMapDocumentEditor.class::cast)
        .findFirst()
        .orElse(null);
  }

  @Nullable
  @Override
  public StructureViewBuilder getStructureViewBuilder(@Nonnull final PsiFile psiFile) {
    return new MindMapStructureViewBuilder(psiFile, findVisualEditor(psiFile));
  }
}
