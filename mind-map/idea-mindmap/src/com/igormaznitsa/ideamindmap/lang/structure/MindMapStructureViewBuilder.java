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
import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

final class MindMapStructureViewBuilder extends TreeBasedStructureViewBuilder {

  private final PsiFile psiFile;
  private final MindMapDocumentEditor editor;

  MindMapStructureViewBuilder(
      @Nonnull final PsiFile psiFile,
      @Nullable final MindMapDocumentEditor editor
  ) {
    this.psiFile = requireNonNull(psiFile, "psiFile must not be null");
    this.editor = editor;
  }

  @Nonnull
  @Override
  public StructureViewModel createStructureViewModel(@Nullable final Editor unused) {
    return new MindMapStructureViewModel(this.psiFile, this.editor);
  }

  @Override
  public boolean isRootNodeShown() {
    return true;
  }
}
