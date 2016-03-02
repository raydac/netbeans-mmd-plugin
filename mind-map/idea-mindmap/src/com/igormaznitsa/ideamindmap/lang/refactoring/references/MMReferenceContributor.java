package com.igormaznitsa.ideamindmap.lang.refactoring.references;

import com.igormaznitsa.ideamindmap.lang.psi.PsiExtraFile;
import com.igormaznitsa.ideamindmap.lang.refactoring.references.providers.PsiExtraFileReferenceProvider;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import org.jetbrains.annotations.NotNull;

public class MMReferenceContributor extends PsiReferenceContributor {

  static final Logger logger = LoggerFactory.getLogger(MMReferenceContributor.class);

  @Override public void registerReferenceProviders(@NotNull final PsiReferenceRegistrar registrar) {
    registrar.registerReferenceProvider(PlatformPatterns.psiElement(PsiExtraFile.class),new PsiExtraFileReferenceProvider());
  }
}
