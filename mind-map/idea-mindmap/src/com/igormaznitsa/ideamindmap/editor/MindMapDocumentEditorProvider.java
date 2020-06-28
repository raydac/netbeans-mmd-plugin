/*
 * Copyright 2015-2018 Igor Maznitsa.
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
import com.igormaznitsa.ideamindmap.lang.MMLanguage;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.diagnostic.Logger;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.jdom.Element;

public class MindMapDocumentEditorProvider implements FileEditorProvider, DumbAware {

  private static final Logger LOGGER = Logger.getInstance(MindMapDocumentEditorProvider.class);

  /**
   * Keep calls through reflection to save compatibility with early versions of IDE.
   *
   * @param virtualFile virtual file to check
   * @return true if mind map file, false otherwise or if error
   */
  private static boolean isScratchFileType(@Nullable final VirtualFile virtualFile) {
    if (virtualFile == null) {
      return false;
    }
    try {
      final Class<?> klazz = Class.forName("com.intellij.ide.scratch.ScratchFileService");
      final Method methodInstance = klazz.getMethod("getInstance");
      final Method methodGetScratchesMapping = klazz.getMethod("getScratchesMapping");
      final Object mapping = methodGetScratchesMapping.invoke(methodInstance.invoke( null));
      final Class<?> perFileMappingsClass = Class.forName("com.intellij.lang.PerFileMappings");
      final Method methodGetMappings = perFileMappingsClass.getMethod("getMapping", VirtualFile.class);
      return methodGetMappings.invoke(mapping, virtualFile) instanceof MMLanguage;
    } catch (ClassNotFoundException ex) {
      return false;
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
      LOGGER.error("Can't find or invoke expected method, may be some unexpected changes in Scratch API!", ex);
      return false;
    }
  }

  @Override
  public boolean accept(@Nonnull Project project, @Nonnull VirtualFile virtualFile) {
    return virtualFile.getFileType() instanceof MindMapFileType || isScratchFileType(virtualFile);
  }

  @Nonnull
  @Override
  public FileEditor createEditor(@Nonnull Project project, @Nonnull VirtualFile virtualFile) {
    return new MindMapDocumentEditor(project, virtualFile);
  }

  @Override
  public void disposeEditor(@Nonnull FileEditor fileEditor) {

  }

  @Nonnull
  @Override
  public FileEditorState readState(@Nonnull Element element, @Nonnull Project project, @Nonnull VirtualFile virtualFile) {
    return MindMapFileEditorState.DUMMY;
  }

  @Override
  public void writeState(@Nonnull FileEditorState fileEditorState, @Nonnull Project project, @Nonnull Element element) {

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
