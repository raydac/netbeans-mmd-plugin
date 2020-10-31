package com.igormaznitsa.nbmindmap.nb.swing;

import com.igormaznitsa.meta.common.utils.Assertions;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class NoteEditorData {

    private final String text;
    private final String password;
    private final String hint;

    public NoteEditorData() {
        this("", null, null);
    }

    public NoteEditorData(@Nonnull final String text, @Nullable final String password,
        @Nullable final String hint) {
        this.text = Assertions.assertNotNull(text);
        this.password = password;
        this.hint = hint;
    }

    public boolean isEncrypted() {
        return this.password != null && !this.password.trim().isEmpty();
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
    public String getHint() {
        return this.isEncrypted() ? this.hint : null;
    }

    @Nonnull
    @Override
    public String toString() {
        return "NoteEditorData(" + this.text + ", " + this.password + ", " + this.hint + ')';
    }

}
