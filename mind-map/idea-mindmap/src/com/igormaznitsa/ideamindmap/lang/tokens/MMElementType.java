package com.igormaznitsa.ideamindmap.lang.tokens;

import com.igormaznitsa.ideamindmap.lang.MMLanguage;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class MMElementType extends IElementType {
  public MMElementType(@NotNull @NonNls String debugName) {
    super(debugName, MMLanguage.INSTANCE);
  }
}
