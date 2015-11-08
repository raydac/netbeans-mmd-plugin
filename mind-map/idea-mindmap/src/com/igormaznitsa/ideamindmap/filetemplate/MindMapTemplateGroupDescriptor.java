package com.igormaznitsa.ideamindmap.filetemplate;

import com.igormaznitsa.ideamindmap.utils.AllIcons;
import com.intellij.ide.fileTemplates.FileTemplateDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor;

public class MindMapTemplateGroupDescriptor extends FileTemplateGroupDescriptor {
    public static class MindMapTemplateDescriptor extends FileTemplateDescriptor {
        public MindMapTemplateDescriptor() {
            super("/templates/template.mmd");
        }

        @Override
        public String getFileName() {
            return "mind_map.mmd";
        }

        @Override
        public String getDisplayName() {
            return "Mind map";
        }
    }

    public static final MindMapTemplateGroupDescriptor INSTANCE = new MindMapTemplateGroupDescriptor();

    private MindMapTemplateGroupDescriptor(){
        super("NB Mind map", AllIcons.File.MINDMAP, new MindMapTemplateDescriptor());
    }
}
