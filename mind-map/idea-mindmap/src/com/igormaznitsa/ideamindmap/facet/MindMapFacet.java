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

import com.igormaznitsa.ideamindmap.utils.IdeaUtils;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeId;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MindMapFacet extends Facet<MindMapFacetConfiguration> {
  private static final Logger LOGGER = LoggerFactory.getLogger(MindMapFacet.class);

  public static final FacetTypeId<MindMapFacet> ID = new FacetTypeId<MindMapFacet>("NBMindMap");

  public MindMapFacet(@NotNull FacetType facetType, @NotNull Module module,
    @NotNull String name, @NotNull MindMapFacetConfiguration configuration, Facet underlyingFacet) {
    super(facetType, module, name, configuration, underlyingFacet);
    IdeaUtils.findKnowledgeFolderForModule(module,!this.getConfiguration().isDisableAutoCreateProjectKnowledgeFolder());
  }

  @Nullable
  public static MindMapFacet getInstance(@Nullable final Module module) {
    return module == null || module.isDisposed() ? null : FacetManager.getInstance(module).getFacetByType(ID);
  }
}
