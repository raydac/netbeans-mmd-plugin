package com.igormaznitsa.mindmap.model;

import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.meta.common.utils.Assertions;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MindMapModelEvent {
  private static final Topic[] EMPTY = new Topic[0];
  private final MindMap source;
  private final Topic[] path;

  public MindMapModelEvent(@Nonnull final MindMap source,
                           @Nullable @MustNotContainNull final Topic[] path) {
    this.source = Assertions.assertNotNull(source);
    this.path = path == null ? EMPTY : path.clone();
  }

  @Nonnull
  public MindMap getSource() {
    return this.source;
  }

  @Nonnull
  @MustNotContainNull
  public Topic[] getPath() {
    return this.path.length == 0 ? this.path : this.path.clone();
  }
}
