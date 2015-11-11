package com.igormaznitsa.ideamindmap.filetype;

import com.igormaznitsa.ideamindmap.utils.AllIcons;
import com.igormaznitsa.ideamindmap.lang.MindMapLanguage;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class MindMapFileType extends LanguageFileType {

  public static final MindMapFileType INSTANCE = new MindMapFileType();
  @NonNls public static final String DEFAULT_EXTENSION = "mmd";

  private MindMapFileType() {
    super(MindMapLanguage.INSTANCE);
  }

  @Override
  public String getCharset(@NotNull VirtualFile file, @NotNull byte[] content) {
    return "UTF-8";
  }

  @NotNull
  @Override
  public String getName() {
    return "NBMindMap";
  }

  @NotNull
  @Override
  public String getDescription() {
    return "NB Mind Map files";
  }

  @NotNull
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
