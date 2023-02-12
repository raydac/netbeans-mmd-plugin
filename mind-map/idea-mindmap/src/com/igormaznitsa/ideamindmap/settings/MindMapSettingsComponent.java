/*
 * Copyright (C) 2015-2022 Igor A. Maznitsa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.igormaznitsa.ideamindmap.settings;

import com.igormaznitsa.ideamindmap.editor.MindMapDialogProvider;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactoryProvider;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurableProvider;
import com.intellij.openapi.options.ConfigurationException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.JComponent;
import org.jetbrains.annotations.Nls;

public class MindMapSettingsComponent extends ConfigurableProvider implements Configurable, ApplicationComponent {

  public static final String ID = "idea.mind.map.settings";
  public static final String COMPONENT_NAME = "NBMindMapSettingsComponent";
  public static final String DISPLAY_NAME = "IDEA Mind Map";
  private static final Logger LOGGER = LoggerFactory.getLogger(MindMapSettingsComponent.class);
  private static MindMapSettingsComponent instance;
  private final MindMapDialogProvider dialogProvider = new MindMapDialogProvider(null);
  private PreferencesPanel uiPanel;

  public MindMapSettingsComponent getInstance() {
    if (instance == null) {
      instance = new MindMapSettingsComponent();
    }
    return instance;
  }

  public DialogProvider getDialogProvider() {
    return dialogProvider;
  }

  @Override
  public void initComponent() {
    getInstance();
  }

  @Override
  public void disposeComponent() {
    this.uiPanel = null;
  }

  @Nls
  @Override
  public String getDisplayName() {
    return DISPLAY_NAME;
  }

  @Nullable
  @Override
  public String getHelpTopic() {
    return null;
  }

  @Nullable
  @Override
  public Configurable createConfigurable() {
    return this.getInstance();
  }

  @Nonnull
  @Override
  public String getComponentName() {
    return COMPONENT_NAME;
  }

  @Nullable
  @Override
  public JComponent createComponent() {
    if (this.uiPanel == null) {
      this.uiPanel = new PreferencesPanel(this, UIComponentFactoryProvider.findInstance(), this.dialogProvider);
    }
    this.uiPanel.load(MindMapApplicationSettings.getInstance().getConfig());
    return this.uiPanel.getPanel();
  }

  @Override
  public boolean isModified() {
    return this.uiPanel != null && this.uiPanel.checkChanges();
  }

  @Override
  public void apply() throws ConfigurationException {
    if (this.uiPanel != null) {
        MindMapApplicationSettings.getInstance().loadState(MindMapApplicationSettings.from(this.uiPanel.save(true)));
    }
  }

  @Override
  public void reset() {
    if (this.uiPanel != null) {
      this.uiPanel.load(MindMapApplicationSettings.getInstance().getConfig());
    }
  }

  @Override
  public void disposeUIResources() {
    this.uiPanel = null;
  }

}
