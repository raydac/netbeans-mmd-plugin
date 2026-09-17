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

package com.igormaznitsa.ideamindmap.editor;

import com.igormaznitsa.ideamindmap.filetype.MindMapFileType;
import com.igormaznitsa.ideamindmap.lang.MMLanguage;
import com.intellij.ide.scratch.ScratchFileService;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.jdom.Element;

public class MindMapDocumentEditorProvider implements FileEditorProvider, DumbAware {

  private static boolean isScratchMappedToMindMap(@Nullable final VirtualFile virtualFile) {
    return virtualFile != null
        && ScratchFileService.getInstance().getScratchesMapping().getMapping(virtualFile) instanceof MMLanguage;
  }

  @Override
  public boolean accept(@Nonnull final Project project, @Nonnull final VirtualFile virtualFile) {
    return virtualFile.getFileType() instanceof MindMapFileType || isScratchMappedToMindMap(virtualFile);
  }

  @Nonnull
  @Override
  public FileEditor createEditor(@Nonnull final Project project, @Nonnull final VirtualFile virtualFile) {
    return new MindMapDocumentEditor(project, virtualFile);
  }

  @Nonnull
  @Override
  public FileEditorState readState(@Nonnull final Element element, @Nonnull final Project project, @Nonnull final VirtualFile virtualFile) {
    return MindMapFileEditorState.DUMMY;
  }

  @Nonnull
  @Override
  public String getEditorTypeId() {
    return "com.igormaznitsa.ideamindmap.editor.MindMapDocumentEditor";
  }

  @Nonnull
  @Override
  public FileEditorPolicy getPolicy() {
    return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
  }

}
