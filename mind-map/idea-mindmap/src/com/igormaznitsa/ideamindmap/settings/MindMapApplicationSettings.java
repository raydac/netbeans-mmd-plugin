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

import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.awt.Font;

@State(name = "NBMindMapPlugin", storages = { @Storage(id = "nbmindmap", file = "$APP_CONFIG$/nbmindmapsettings.xml") })
public class MindMapApplicationSettings implements ApplicationComponent, PersistentStateComponent<DeclaredFieldsSerializer>, DeclaredFieldsSerializer.Converter {

  private final MindMapPanelConfig editorConfig = new MindMapPanelConfig();

  public MindMapPanelConfig getConfig() {
    return this.editorConfig;
  }

  @Override public void initComponent() {

  }

  @Override public void disposeComponent() {

  }

  @NotNull @Override public String getComponentName() {
    return "NBMindMapApplicationSettings";
  }

  @Nullable @Override public DeclaredFieldsSerializer getState() {
    return new DeclaredFieldsSerializer(this.editorConfig,this);
  }

  @Override public void loadState(final DeclaredFieldsSerializer state) {
    state.fill(editorConfig,this);
  }

  public void fillBy(@NotNull final MindMapPanelConfig mindMapPanelConfig) {
    editorConfig.makeFullCopyOf(mindMapPanelConfig, false, true);
  }

  public static MindMapApplicationSettings findInstance() {
    return ApplicationManager.getApplication().getComponent(MindMapApplicationSettings.class);
  }

  @Nullable @Override public Object fromString(@NotNull Class<?> fieldType, @NotNull String value) {
    if (fieldType == Color.class) {
      return new Color(Integer.parseInt(value), true);
    }
    else if (fieldType == Font.class) {
      final String[] splitted = value.split(":");
      return new Font(splitted[0], Integer.parseInt(splitted[1]), Integer.parseInt(splitted[2]));
    }
    else
      throw new Error("Unexpected field type" + fieldType);
  }

  @NotNull @Override public String asString(@NotNull Class<?> fieldType, @NotNull Object value) {
    if (fieldType == Color.class) {
      return Integer.toString(((Color) value).getRGB());
    }
    else if (fieldType == Font.class) {
      final Font font = (Font) value;
      return font.getFamily() + ':' + Integer.toString(font.getStyle()) + ':' + Integer.toString(font.getSize());
    }
    else
      throw new Error("Unexpected field type" + fieldType);
  }

  @Nullable @Override public Object provideDefaultValue(@NotNull String fieldName, @NotNull Class<?> fieldType) {
      throw new Error("Unexpected call to provide default value for field " + fieldType);
  }
}
