/*
 * Copyright (C) 2015-2022 Igor A. Maznitsa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.igormaznitsa.ideamindmap.swing;

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
