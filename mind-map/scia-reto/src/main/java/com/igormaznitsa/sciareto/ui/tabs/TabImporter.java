package com.igormaznitsa.sciareto.ui.tabs;

import javax.annotation.Nonnull;
import javax.swing.JMenuItem;

public interface TabImporter {
  @Nonnull
  JMenuItem makeMenuItem();

  @Nonnull
  String getTitle();
}
