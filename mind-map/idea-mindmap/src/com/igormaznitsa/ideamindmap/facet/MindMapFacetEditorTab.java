package com.igormaznitsa.ideamindmap.facet;

import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;

public class MindMapFacetEditorTab extends FacetEditorTab {

  private final MindMapFacetConfiguration config;

  private MindMapFacetPanel panel;

  public MindMapFacetEditorTab(final MindMapFacetConfiguration config){
    this.config = config;
  }

  @NotNull @Override public JComponent createComponent() {
    if (this.panel == null){
      this.panel = new MindMapFacetPanel(this);
    }
    return this.panel.getPanel();
  }

  @Override public boolean isModified() {
    return this.panel == null ? false : this.panel.isChanged();
  }

  @Override public void apply() throws ConfigurationException {
    if (this.panel!=null){
      this.panel.save();
    }
  }

  @Override public void reset() {
    if (this.panel!=null){
      this.panel.reset();
    }
  }

  @Override public void disposeUIResources() {

  }

  public MindMapFacetConfiguration getConfiguration(){
    return this.config;
  }

  @Nls @Override public String getDisplayName() {
    return "NB Mind Map";
  }
}
