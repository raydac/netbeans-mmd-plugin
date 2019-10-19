package com.igormaznitsa.mindmap.model;

import javax.annotation.Nonnull;

public interface MindMapModelEventListener {
  void onMindMapStructureChanged(@Nonnull MindMapModelEvent event);

  void onMindMapNodesChanged(@Nonnull MindMapModelEvent event);
}
