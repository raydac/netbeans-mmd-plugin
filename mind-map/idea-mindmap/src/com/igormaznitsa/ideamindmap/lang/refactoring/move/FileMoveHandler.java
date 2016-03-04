package com.igormaznitsa.ideamindmap.lang.refactoring.move;

import com.igormaznitsa.ideamindmap.lang.refactoring.references.providers.PsiExtraFileReference;
import com.igormaznitsa.ideamindmap.utils.IdeaUtils;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.PsiFileReferenceHelper;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.refactoring.move.moveFilesOrDirectories.MoveFileHandler;
import com.intellij.usageView.UsageInfo;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.Processor;
import com.intellij.util.Query;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FileMoveHandler extends MoveFileHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileMoveHandler.class);

  @Override public boolean canProcessElement(final PsiFile element) {
    return true;
  }

  @Override public void prepareMovedFile(PsiFile file, PsiDirectory moveDestination, Map<PsiElement, PsiElement> oldToNewMap) {
    oldToNewMap.put(file, file);
  }

  @Nullable @Override public List<UsageInfo> findUsages(final PsiFile psiFile, final PsiDirectory newParent, final boolean searchInComments, final boolean searchInNonJavaFiles) {
    Query<PsiReference> search = ReferencesSearch.search(psiFile);
    final List<PsiExtraFileReference> extraFileRefs = new ArrayList<PsiExtraFileReference>();
    search.forEach(new Processor<PsiReference>() {
      @Override public boolean process(PsiReference psiReference) {
        if (psiReference instanceof PsiExtraFileReference){
          extraFileRefs.add((PsiExtraFileReference)psiReference);
        }
        return true;
      }
    });

    if (extraFileRefs.isEmpty()){
      return null;
    }else{
      final List<UsageInfo> result = new ArrayList<UsageInfo>(extraFileRefs.size());
      for(final PsiExtraFileReference e : extraFileRefs){
        result.add(new FileUsageInfo(e));
      }
      return result;
    }
  }

  @Override public void retargetUsages(final List<UsageInfo> usageInfos, Map<PsiElement, PsiElement> oldToNewMap) {
    final PsiFile newFile = (PsiFile) oldToNewMap.values().iterator().next();

    for(final UsageInfo i : usageInfos){
      final PsiExtraFileReference reference = ((FileUsageInfo) i).getExtraFileReference();
      reference.retargetToFile(newFile);
    }
  }

  @Override public void updateMovedFile(PsiFile file) throws IncorrectOperationException {
  }
}
