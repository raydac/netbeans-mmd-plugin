package com.igormaznitsa.ideamindmap.filetemplate;

import com.igormaznitsa.ideamindmap.utils.AllIcons;
import com.intellij.ide.fileTemplates.FileTemplateDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory;

public class MindMapTemplateGroupDescriptionFactory implements FileTemplateGroupDescriptorFactory {
  public static final String MINDMAP_EMPTY_MAP = "Mind Map empty.mmd.ft";

  @Override
  public FileTemplateGroupDescriptor getFileTemplatesDescriptor() {
    final FileTemplateGroupDescriptor group = new FileTemplateGroupDescriptor("NB Mind Map",AllIcons.Logo.MINDMAP);
    group.addTemplate(new FileTemplateDescriptor(MINDMAP_EMPTY_MAP, AllIcons.File.MINDMAP));
    return group;
  }
}
