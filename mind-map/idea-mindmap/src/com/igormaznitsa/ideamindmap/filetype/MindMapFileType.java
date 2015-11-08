package com.igormaznitsa.ideamindmap.filetype;

import com.igormaznitsa.ideamindmap.utils.AllIcons;
import com.igormaznitsa.ideamindmap.lang.MindMapLanguage;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class MindMapFileType  extends LanguageFileType {

    public static final MindMapFileType INSTANCE = new MindMapFileType();
    @NonNls public static final String DEFAULT_EXTENSION = "mmd";
    @NonNls public static final String DOT_DEFAULT_EXTENSION = '.'+DEFAULT_EXTENSION;


    private MindMapFileType() {super(MindMapLanguage.INSTANCE);};

    @NotNull
    @Override
    public String getName() {
        return "NB Mind map";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "NB Mind map files";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return DEFAULT_EXTENSION;
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return AllIcons.File.MINDMAP;
    }
}
