package com.igormaznitsa.mindmap.ide.commons.editors;

import static java.util.Objects.requireNonNull;

public final class AbstractNoteEditorData {
  private final String text;
  private final String password;
  private final String hint;

  public AbstractNoteEditorData() {
    this("", null, null);
  }

  public AbstractNoteEditorData(final String text,
                                final String password,
                                final String hint) {
    this.text = requireNonNull(text);
    this.password = password;
    this.hint = hint;
  }

  public boolean isEncrypted() {
    return this.password != null && !this.password.trim().isEmpty();
  }

  public String getText() {
    return this.text;
  }

  public String getPassword() {
    return this.password;
  }

  public String getHint() {
    return this.isEncrypted() ? this.hint : null;
  }

}
