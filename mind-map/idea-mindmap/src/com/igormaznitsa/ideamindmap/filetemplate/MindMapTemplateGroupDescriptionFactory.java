package com.igormaznitsa.ideamindmap.filetemplate;

import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory;

public class MindMapTemplateGroupDescriptionFactory implements FileTemplateGroupDescriptorFactory {

    @Override
    public FileTemplateGroupDescriptor getFileTemplatesDescriptor() {
        return MindMapTemplateGroupDescriptor.INSTANCE;
    }
}
