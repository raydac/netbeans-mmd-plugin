package com.igormaznitsa.ideamindmap.lang.refactoring.move;

import com.igormaznitsa.ideamindmap.lang.refactoring.references.providers.PsiExtraFileReference;
import com.intellij.usageView.UsageInfo;

public final class FileUsageInfo extends UsageInfo {

  private final PsiExtraFileReference theReference;

  public FileUsageInfo(final PsiExtraFileReference reference) {
    super(reference);
    this.theReference = reference;
  }

  public PsiExtraFileReference getExtraFileReference() {
    return this.theReference;
  }
}
