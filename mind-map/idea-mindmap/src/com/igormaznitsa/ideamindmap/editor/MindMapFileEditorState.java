package com.igormaznitsa.ideamindmap.editor;

import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;

public class MindMapFileEditorState implements FileEditorState {

  public static final MindMapFileEditorState DUMMY = new MindMapFileEditorState();

  @Override
  public boolean canBeMergedWith(FileEditorState fileEditorState, FileEditorStateLevel fileEditorStateLevel) {
    return false;
  }
}
