/*
 * Copyright 2015 Igor Maznitsa.
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
package com.igormaznitsa.ideamindmap.facet;

import com.igormaznitsa.ideamindmap.utils.AllIcons;
import com.intellij.facet.Facet;
import com.intellij.facet.FacetType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

public class MindMapFacetType extends FacetType<MindMapFacet,MindMapFacetConfiguration> {

  public static final MindMapFacetType INSTANCE = new MindMapFacetType();

  private MindMapFacetType(){
    super(MindMapFacet.ID,"NBMindMap","NB Mind Map");
  }

  @Override public MindMapFacetConfiguration createDefaultConfiguration() {
    return new MindMapFacetConfiguration();
  }

  @Override public MindMapFacet createFacet(@NotNull final Module module, final String name, @NotNull final MindMapFacetConfiguration configuration, @Nullable final Facet underlyingFacet) {
    return new MindMapFacet(this,module,name,configuration,underlyingFacet);
  }

  @Override public boolean isSuitableModuleType(ModuleType moduleType) {
    return moduleType!=null;
  }

  @Nullable @Override public Icon getIcon() {
    return AllIcons.Logo.MINDMAP;
  }
}
