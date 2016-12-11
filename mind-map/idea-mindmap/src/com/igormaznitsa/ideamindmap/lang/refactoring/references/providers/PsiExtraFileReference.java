package com.igormaznitsa.ideamindmap.lang.refactoring.references.providers;

import com.igormaznitsa.ideamindmap.lang.psi.PsiExtraFile;
import com.igormaznitsa.ideamindmap.lang.refactoring.RefactoringUtils;
import com.igormaznitsa.mindmap.model.MMapURI;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.impl.PsiManagerEx;
import com.intellij.util.IncorrectOperationException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

public class PsiExtraFileReference extends PsiReferenceBase<PsiExtraFile> {

  private final PsiExtraFile extraFile;
  private final TextRange range;

  public PsiExtraFileReference (PsiExtraFile extraFile, TextRange range) {
    super(extraFile, true);
    this.extraFile = extraFile;
    this.range = range;
  }

  @Override public TextRange getRangeInElement() {
    return range;
  }

  @Nullable @Override public PsiElement resolve() {
    final VirtualFile theFile = this.getElement().findTargetFile();

    if (theFile == null) {
      return null;
    }
    else {
      return PsiManagerEx.getInstance(this.getElement().getProject()).findFile(theFile);
    }
  }

  public void retargetToFile(final PsiFileSystemItem file) {
    final MMapURI oldUri = extraFile.getMMapURI();
    try {
      final MMapURI newUri = RefactoringUtils.makeNewMMapUri(extraFile.getProject(),oldUri,file.getVirtualFile());
      final String packedNewMindMap = RefactoringUtils.replaceMMUriToNewFile(extraFile, oldUri, newUri);
      final PsiFile containingFile = extraFile.getContainingFile();

      final Document document = FileDocumentManager.getInstance().getDocument(containingFile.getVirtualFile());

      CommandProcessor.getInstance().executeCommand(containingFile.getProject(), new Runnable() {
        @Override
        public void run() {
          ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run() {
              final String packedMindMap = packedNewMindMap;
              document.setText(packedMindMap);
              FileDocumentManager.getInstance().saveDocument(document);
            }
          });
        }
      }, null, null, document);

      extraFile.setMMapURI(newUri);

    }
    catch (IOException ex) {
      throw new IncorrectOperationException("Can't update links in mind map",(Throwable)ex);
    }
  }



  @Override public PsiElement bindToElement(@Nonnull final PsiElement element) throws IncorrectOperationException {
    return this.extraFile;
  }

  @Nonnull @Override public Object[] getVariants() {
    return new Object[0];
  }
}
