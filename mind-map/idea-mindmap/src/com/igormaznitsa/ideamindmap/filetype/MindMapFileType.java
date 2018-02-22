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
package com.igormaznitsa.ideamindmap.filetype;

import com.igormaznitsa.ideamindmap.utils.AllIcons;
import com.igormaznitsa.ideamindmap.lang.MMLanguage;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;

public class MindMapFileType extends LanguageFileType {

  public static final MindMapFileType INSTANCE = new MindMapFileType();
  @NonNls public static final String DEFAULT_EXTENSION = "mmd";

  private MindMapFileType() {
    super(MMLanguage.INSTANCE);
  }

  @Override
  public String getCharset(@Nonnull VirtualFile file, @Nonnull byte[] content) {
    return "UTF-8";
  }

  @Nonnull
  @Override
  public String getName() {
    return "NB Mind Map";
  }

  @Nonnull
  @Override
  public String getDescription() {
    return "NB Mind Map files";
  }

  @Nonnull
  @Override
  public String getDefaultExtension() {
    return DEFAULT_EXTENSION;
  }

  @Nullable
  @Override
  public Icon getIcon() {
    return AllIcons.File.MINDMAP;
  }
}
