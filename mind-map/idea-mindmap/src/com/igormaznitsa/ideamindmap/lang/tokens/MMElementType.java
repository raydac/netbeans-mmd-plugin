package com.igormaznitsa.ideamindmap.lang.tokens;

import com.igormaznitsa.ideamindmap.lang.MMLanguage;
import com.intellij.psi.tree.IElementType;
import javax.annotation.Nonnull;
import org.jetbrains.annotations.NonNls;

public class MMElementType extends IElementType {
  public MMElementType(@Nonnull @NonNls String debugName) {
    super(debugName, MMLanguage.INSTANCE);
  }
}
