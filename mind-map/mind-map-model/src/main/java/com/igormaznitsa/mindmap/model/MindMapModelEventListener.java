package com.igormaznitsa.mindmap.model;

public interface MindMapModelEventListener {
  void onMindMapStructureChanged(MindMapModelEvent event);

  void onMindMapNodesChanged(MindMapModelEvent event);
}
