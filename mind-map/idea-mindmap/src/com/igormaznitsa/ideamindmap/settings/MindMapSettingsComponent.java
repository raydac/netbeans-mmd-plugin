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
package com.igormaznitsa.ideamindmap.settings;

import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurableProvider;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.JComponent;

public class MindMapSettingsComponent extends ConfigurableProvider implements Configurable,ApplicationComponent{

  private static final Logger LOGGER = LoggerFactory.getLogger(MindMapSettingsComponent.class);
  public static final String ID = "idea.mind.map.settings";
  public static final String COMPONENT_NAME = "NBMindMapSettingsComponent";
  public static final String DISPLAY_NAME = "NB Mind Map";

  private static MindMapSettingsComponent instance;

  private MindMapSettingsPanel uiPanel;

  public MindMapSettingsComponent getInstance(){
    if (instance == null){
      instance = new MindMapSettingsComponent();
    }

    return instance;
  }

  @Override public void initComponent() {
    getInstance();
  }

  @Override public void disposeComponent() {
    this.uiPanel = null;
  }

  @Nls @Override public String getDisplayName() {
    return DISPLAY_NAME;
  }

  @Nullable @Override public String getHelpTopic() {
    return null;
  }

  @Nullable @Override public Configurable createConfigurable() {
    return getInstance();
  }

  @Nonnull @Override public String getComponentName() {
    return COMPONENT_NAME;
  }

  @Nullable @Override public JComponent createComponent() {
    if (this.uiPanel == null){
      this.uiPanel = new MindMapSettingsPanel(this);
    }
    this.uiPanel.reset(MindMapApplicationSettings.findInstance().getConfig());
    return this.uiPanel.getPanel();
  }

  @Override public boolean isModified() {
    return this.uiPanel != null && this.uiPanel.isModified();
  }

  @Override public void apply() throws ConfigurationException {
    if (this.uiPanel!=null){
      MindMapApplicationSettings.findInstance().fillBy(this.uiPanel.makeConfig());
    }
  }

  @Override public void reset() {
    if (this.uiPanel!=null){
      this.uiPanel.reset(MindMapApplicationSettings.findInstance().getConfig());
    }
  }

  @Override public void disposeUIResources() {
    this.uiPanel = null;
  }

}
