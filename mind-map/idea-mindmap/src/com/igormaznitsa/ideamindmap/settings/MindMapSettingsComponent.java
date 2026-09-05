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
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactoryProvider;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import javax.annotation.Nullable;
import javax.swing.JComponent;
import org.jetbrains.annotations.Nls;

public class MindMapSettingsComponent implements Configurable {

  public static final String DISPLAY_NAME = "SciaReto Mind Map";
  private final MindMapDialogProvider dialogProvider = new MindMapDialogProvider(null);
  private PreferencesPanel uiPanel;

  public DialogProvider getDialogProvider() {
    return this.dialogProvider;
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
