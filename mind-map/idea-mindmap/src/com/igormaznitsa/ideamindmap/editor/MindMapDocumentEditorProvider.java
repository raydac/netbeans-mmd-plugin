/*
 * Copyright 2015 Igor Maznitsa.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.igormaznitsa.ideamindmap.editor;

import com.igormaznitsa.ideamindmap.filetype.MindMapFileType;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

public class MindMapDocumentEditorProvider implements FileEditorProvider, DumbAware {
  @Override
  public boolean accept(@NotNull Project project, @NotNull VirtualFile virtualFile) {
    return virtualFile.getFileType() instanceof MindMapFileType;
  }

  @NotNull
  @Override
  public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile virtualFile) {
    return new MindMapDocumentEditor(project, virtualFile);
  }

  @Override
  public void disposeEditor(@NotNull FileEditor fileEditor) {

  }

  @NotNull
  @Override
  public FileEditorState readState(@NotNull Element element, @NotNull Project project, @NotNull VirtualFile virtualFile) {
    return MindMapFileEditorState.DUMMY;
  }

  @Override
  public void writeState(@NotNull FileEditorState fileEditorState, @NotNull Project project, @NotNull Element element) {

  }

  @NotNull
  @Override
  public String getEditorTypeId() {
    return "com.igormaznitsa.ideamindmap.editor.MindMapDocumentEditor";
  }

  @NotNull
  @Override
  public FileEditorPolicy getPolicy() {
    return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
  }
}
