package com.igormaznitsa.sciareto.ui.tabs;

import javax.annotation.Nonnull;

public interface TabExporter {
    void execute(@Nonnull final TabProvider tabProvider);
    @Nonnull
    String getTitle();
}
