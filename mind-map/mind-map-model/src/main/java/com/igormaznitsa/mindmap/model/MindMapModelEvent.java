package com.igormaznitsa.mindmap.model;

import static java.util.Objects.requireNonNull;

public class MindMapModelEvent {
  private static final Topic[] EMPTY = new Topic[0];
  private final MindMap source;
  private final Topic[] path;

  public MindMapModelEvent(final MindMap source,
                           final Topic[] path) {
    this.source = requireNonNull(source);
    this.path = path == null ? EMPTY : path.clone();
  }

  public MindMap getSource() {
    return this.source;
  }

  public Topic[] getPath() {
    return this.path.length == 0 ? this.path : this.path.clone();
  }
}
