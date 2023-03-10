package com.igormaznitsa.sciareto.ui.tabs;

import javax.annotation.Nonnull;
import javax.swing.JMenuItem;

public interface TabExporter {
  @Nonnull
  JMenuItem makeMenuItem();

  @Nonnull
  String getTitle();
}
