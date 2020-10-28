package com.igormaznitsa.sciareto.ui.editors.mmeditors;

import com.igormaznitsa.meta.common.utils.Assertions;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class NoteEditorData {
    private final String text;
    private final String password;
    private final String tip;

    public NoteEditorData() {
        this("", null, null);
    }

    public NoteEditorData(@Nonnull final String text, @Nullable final String password,
                          @Nullable final String tip) {
        this.text = Assertions.assertNotNull(text);
        this.password = password;
        this.tip = tip;
    }

    @Nonnull
    public String getText() {
        return this.text;
    }

    @Nullable
    public String getPassword() {
        return this.password;
    }

    @Nullable
    public String getTip() {
        return this.tip;
    }

}
