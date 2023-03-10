package com.igormaznitsa.sciareto.ui.tabs;

import javax.annotation.Nonnull;

public interface TabImporter {
    void execute(@Nonnull final TabProvider tabProvider);
    @Nonnull
    String getTitle();
}
