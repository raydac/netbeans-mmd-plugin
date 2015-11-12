package com.igormaznitsa.ideamindmap.facet;

import com.igormaznitsa.ideamindmap.utils.IdeaUtils;
import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeId;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MindMapFacet extends Facet<MindMapFacetConfiguration> {
  private static final Logger LOGGER = Logger.getInstance(MindMapFacet.class);

  public static final FacetTypeId<MindMapFacet> ID = new FacetTypeId<>("NBMindMap");

  public MindMapFacet(@NotNull FacetType facetType, @NotNull Module module,
    @NotNull String name, @NotNull MindMapFacetConfiguration configuration, Facet underlyingFacet) {
    super(facetType, module, name, configuration, underlyingFacet);
    IdeaUtils.findKnowledgeFolderForModule(module,true);
  }

  @Nullable
  public static MindMapFacet getInstance(@Nullable final Module module) {
    return module == null || module.isDisposed() ? null : FacetManager.getInstance(module).getFacetByType(ID);
  }
}
