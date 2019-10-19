package com.igormaznitsa.ideamindmap.lang.highlighter;

import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MMSyntaxHighlighterFactory extends SyntaxHighlighterFactory {
  @Nonnull
  @Override
  public SyntaxHighlighter getSyntaxHighlighter(@Nullable Project project, @Nullable VirtualFile virtualFile) {
    return new MMHighlighter();
  }
}
