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
package com.igormaznitsa.ideamindmap.facet;

import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;

import javax.annotation.Nonnull;
import javax.swing.JComponent;

public class MindMapFacetEditorTab extends FacetEditorTab {

  private final MindMapFacetConfiguration config;

  private MindMapFacetPanel panel;

  public MindMapFacetEditorTab(final MindMapFacetConfiguration config){
    this.config = config;
  }

  @Nonnull @Override public JComponent createComponent() {
    if (this.panel == null){
      this.panel = new MindMapFacetPanel(this);
    }
    return this.panel.getPanel();
  }

  @Override public boolean isModified() {
    return this.panel != null && this.panel.isChanged();
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
