package com.igormaznitsa.ideamindmap.lang;

import com.igormaznitsa.ideamindmap.filetype.MindMapFileType;
import com.igormaznitsa.ideamindmap.utils.IdeaUtils;
import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.lang.FileASTNode;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import org.jetbrains.annotations.NotNull;

public class MMDFile extends PsiFileBase {

  public MMDFile(final FileViewProvider fileViewProvider) {
    super(fileViewProvider, MMLanguage.INSTANCE);
  }

  @NotNull @Override public SearchScope getUseScope() {
    final Module module = ModuleUtilCore.findModuleForPsiElement(this);
    return module != null ? IdeaUtils.moduleScope(getProject(), module) : super.getUseScope();
  }

  @NotNull @Override public FileType getFileType() {
    return MindMapFileType.INSTANCE;
  }

  @Override
  public String toString() {
    final VirtualFile virtualFile = getVirtualFile();
    return "MMDFile: " + (virtualFile != null? virtualFile.getName() : "<unknown>");
  }

}
