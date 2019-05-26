/*
 * Copyright 2015-2018 Igor Maznitsa.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.igormaznitsa.ideamindmap.filetemplate;

import com.igormaznitsa.ideamindmap.utils.AllIcons;
import com.intellij.ide.fileTemplates.FileTemplateDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory;

public class MindMapTemplateGroupDescriptionFactory implements FileTemplateGroupDescriptorFactory {
  public static final String MINDMAP_EMPTY_MAP = "Empty Mind Map.mmd";

  @Override
  public FileTemplateGroupDescriptor getFileTemplatesDescriptor() {
    final FileTemplateGroupDescriptor group = new FileTemplateGroupDescriptor("IDEA Mind Map",AllIcons.Logo.MINDMAP);
    group.addTemplate(new FileTemplateDescriptor(MINDMAP_EMPTY_MAP, AllIcons.File.MINDMAP));
    return group;
  }
}
