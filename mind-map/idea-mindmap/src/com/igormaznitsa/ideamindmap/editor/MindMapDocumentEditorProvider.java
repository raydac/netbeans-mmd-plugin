package com.igormaznitsa.ideamindmap.editor;

import com.igormaznitsa.ideamindmap.filetype.MindMapFileType;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

public class MindMapDocumentEditorProvider implements FileEditorProvider{
    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        return virtualFile.getFileType() instanceof MindMapFileType;
    }

    @NotNull
    @Override
    public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        return new MindMapDocumentEditor(project,virtualFile);
    }

    @Override
    public void disposeEditor(@NotNull FileEditor fileEditor) {

    }

    @NotNull
    @Override
    public FileEditorState readState(@NotNull Element element, @NotNull Project project, @NotNull VirtualFile virtualFile) {
        return MindMapFileEditorState.DUMMY;
    }

    @Override
    public void writeState(@NotNull FileEditorState fileEditorState, @NotNull Project project, @NotNull Element element) {

    }

    @NotNull
    @Override
    public String getEditorTypeId() {
        return "com.igormaznitsa.ideamindmap.editor.MindMapDocumentEditor";
    }

    @NotNull
    @Override
    public FileEditorPolicy getPolicy() {
        return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
    }
}
