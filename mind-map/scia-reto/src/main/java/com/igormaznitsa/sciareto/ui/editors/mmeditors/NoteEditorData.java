/*
 * Copyright (C) 2015-2022 Igor A. Maznitsa
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package com.igormaznitsa.sciareto.ui.editors.mmeditors;

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

}
