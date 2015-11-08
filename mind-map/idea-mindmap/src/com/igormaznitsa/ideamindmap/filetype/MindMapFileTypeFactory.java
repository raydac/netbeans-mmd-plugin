package com.igormaznitsa.ideamindmap.filetype;

import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import org.jetbrains.annotations.NotNull;

public class MindMapFileTypeFactory extends FileTypeFactory {
    @Override
    public void createFileTypes(@NotNull FileTypeConsumer fileTypeConsumer) {
        fileTypeConsumer.consume(MindMapFileType.INSTANCE, MindMapFileType.DEFAULT_EXTENSION);
    }
}
