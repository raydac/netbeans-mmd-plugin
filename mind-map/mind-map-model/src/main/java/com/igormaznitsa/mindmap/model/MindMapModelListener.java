package com.igormaznitsa.mindmap.model;

public interface MindMapModelListener {
  void onMindMapStructureChanged(MindMapModelEvent event);

  void onMindMapNodesChanged(MindMapModelEvent event);
}
