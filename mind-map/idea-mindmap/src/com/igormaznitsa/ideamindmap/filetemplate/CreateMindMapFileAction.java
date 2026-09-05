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

package com.igormaznitsa.ideamindmap.filetemplate;

import com.igormaznitsa.ideamindmap.filetype.MindMapFileType;
import com.igormaznitsa.ideamindmap.utils.AllIcons;
import com.intellij.ide.actions.CreateFileFromTemplateAction;
import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CreateMindMapFileAction extends CreateFileFromTemplateAction {
  public static final String TEMPLATE_NAME = "SciaReto Mind Map";

  private static final String DEFAULT_MINDMAP_TEXT =
      "[Scia Reto](https://sciareto.org) mind map   \n"
          + "> __version__=`1.1`,showJumps=`true`\n"
          + "---\n"
          + "\n"
          + "# The Root Topic\n"
          + "> mmd.emoticon=`tree`\n";

  public CreateMindMapFileAction() {
    super("SciaReto Mind Map", "Create a new SciaReto Mind Map file", AllIcons.File.MINDMAP);
  }

  @Override
  protected void buildDialog(
      @Nonnull final Project project,
      @Nonnull final PsiDirectory directory,
      @Nonnull final CreateFileFromTemplateDialog.Builder builder) {
    builder.setTitle("New SciaReto Mind Map")
        .addKind("SciaReto Mind Map", AllIcons.File.MINDMAP, TEMPLATE_NAME);
  }

  @Override
  protected String getActionName(
      @Nonnull final PsiDirectory directory,
      @Nonnull final String newName,
      final String templateName) {
    return "Create SciaReto Mind Map: " + newName;
  }

  @Override
  protected PsiFile createFile(
      @Nonnull final String name,
      @Nonnull final String templateName,
      @Nonnull final PsiDirectory directory) {
    final FileTemplate template = this.resolveTemplate(directory.getProject(), templateName);
    return template != null
        ? this.createFileFromTemplate(name, template, directory)
        : this.createDefaultMindMapFile(name, directory);
  }

  @Nullable
  private FileTemplate resolveTemplate(@Nonnull final Project project, @Nullable final String templateName) {
    if (templateName == null || templateName.isEmpty()) {
      return null;
    }

    final FileTemplateManager manager = FileTemplateManager.getInstance(project);
    return Stream.of(
            manager.findInternalTemplate(templateName),
            manager.getTemplate(templateName))
        .filter(Objects::nonNull)
        .findFirst()
        .orElseGet(() -> this.findNamedTemplate(manager, templateName));
  }

  @Nullable
  private FileTemplate findNamedTemplate(
      @Nonnull final FileTemplateManager manager,
      @Nonnull final String templateName) {
    return Stream.concat(
            Arrays.stream(manager.getAllTemplates()),
            Arrays.stream(manager.getAllJ2eeTemplates()))
        .filter(template -> templateName.equals(template.getName()))
        .findFirst()
        .orElse(null);
  }

  @Nonnull
  private PsiFile createDefaultMindMapFile(@Nonnull final String name, @Nonnull final PsiDirectory directory) {
    final String fileName = this.fileNameWithExtension(name);
    final PsiFile file = PsiFileFactory.getInstance(directory.getProject())
        .createFileFromText(fileName, MindMapFileType.INSTANCE, DEFAULT_MINDMAP_TEXT);
    return (PsiFile) directory.add(file);
  }

  @Nonnull
  private String fileNameWithExtension(@Nonnull final String name) {
    final String extension = "." + MindMapFileType.DEFAULT_EXTENSION;
    return name.endsWith(extension) ? name : name + extension;
  }
}
