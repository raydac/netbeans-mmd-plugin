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
