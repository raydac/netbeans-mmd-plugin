package com.igormaznitsa.mindmap.model;

import static java.util.Objects.requireNonNull;

/**
 * Object contains mind map model event.
 */
public class MindMapModelEvent {
  private static final Topic[] EMPTY = new Topic[0];
  private final MindMap source;
  private final Topic[] path;

  /**
   * Constructor.
   *
   * @param source source mind map must not be null
   * @param path   path to changed topic, can be null
   */
  public MindMapModelEvent(
      final MindMap source,
      final Topic[] path
  ) {
    this.source = requireNonNull(source);
    this.path = path == null ? EMPTY : path.clone();
  }

  /**
   * Source mind map
   *
   * @return source mind map, must not be null
   */
  public MindMap getSource() {
    return this.source;
  }

  /**
   * Path to changed topic.
   *
   * @return array of topics as path to the changed one, must not be null
   */
  public Topic[] getPath() {
    return this.path;
  }
}
