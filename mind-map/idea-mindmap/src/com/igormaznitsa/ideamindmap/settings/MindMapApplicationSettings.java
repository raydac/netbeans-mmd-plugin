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

import com.igormaznitsa.ideamindmap.plugins.PrinterPlugin;
import com.igormaznitsa.meta.common.utils.GetUtils;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.plugins.MindMapPluginRegistry;
import com.igormaznitsa.mindmap.plugins.external.ExternalPlugins;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.mindmap.swing.panel.utils.RenderQuality;
import com.igormaznitsa.mindmap.swing.panel.SettingsAccessor;
import com.igormaznitsa.mindmap.swing.panel.utils.KeyShortcut;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.crypto.dsig.keyinfo.KeyValue;

@State(name = "NBMindMapPlugin", storages = {@Storage(file = "$APP_CONFIG$/nbmindmapsettings.xml")})
public class MindMapApplicationSettings implements ApplicationComponent, PersistentStateComponent<DeclaredFieldsSerializer>, DeclaredFieldsSerializer.Converter {

  private static final MindMapPanelConfig etalon = new MindMapPanelConfig();
  private static final String PROPERTY = "idea.mindmap.plugin.folder";
  private static final Logger LOGGER = LoggerFactory.getLogger(MindMapApplicationSettings.class);
  private final MindMapPanelConfig editorConfig = new MindMapPanelConfig();

  public static MindMapApplicationSettings findInstance() {
    return ApplicationManager.getApplication().getComponent(MindMapApplicationSettings.class);
  }

  public MindMapPanelConfig getConfig() {
    return this.editorConfig;
  }

  @Override
  public void initComponent() {
    MindMapPluginRegistry.getInstance().registerPlugin(new PrinterPlugin());
    final String pluginFolder = System.getProperty(PROPERTY);
    if (pluginFolder != null) {
      final File folder = new File(pluginFolder);
      if (folder.isDirectory()) {
        LOGGER.info("Loading plugins from folder : " + folder);
        new ExternalPlugins(folder).init();
      } else {
        LOGGER.error("Can't find plugin folder : " + folder);
      }
    } else {
      LOGGER.info("Property " + PROPERTY + " is not defined");
    }

  }

  @Override
  public void disposeComponent() {

  }

  @Nonnull
  @Override
  public String getComponentName() {
    return "NBMindMapApplicationSettings";
  }

  @Nullable
  @Override
  public DeclaredFieldsSerializer getState() {
    return new DeclaredFieldsSerializer(this.editorConfig, this);
  }

  @Override
  public void loadState(final DeclaredFieldsSerializer state) {
    state.fill(editorConfig, this);
  }

  public void fillBy(@Nonnull final MindMapPanelConfig mindMapPanelConfig) {
    editorConfig.makeFullCopyOf(mindMapPanelConfig, false, true);
  }

  @Nullable
  @Override
  public Object fromString(@Nonnull Class<?> fieldType, @Nullable String value) {
    if (fieldType.isAssignableFrom(Map.class)) {
      if (value == null || value.trim().isEmpty()) {
        return Collections.emptyMap();
      } else {
        try {
          final Map<String, KeyShortcut> result = new HashMap<>();
          for (final String s : value.split(",")) {
            String[] keyValue = s.split("::");
            if (keyValue.length == 2) {
              final String key = keyValue[0].trim();
              final String shortcut = keyValue[1].trim();
              result.put(key, new KeyShortcut(shortcut));
            } else {
              throw new IllegalArgumentException("Detected wrong entity pair: " + s);
            }
          }
          return result;
        }catch (Exception ex){
          LOGGER.error("Can't split key shortcuts", ex);
          return Collections.emptyMap();
        }
      }
    } if (fieldType == Color.class) {
      if (value == null) return null;
      return new Color(Integer.parseInt(value), true);
    } else if (fieldType == Font.class) {
      if (value == null) return new Font("Arial", Font.BOLD, 12);
      final String[] splitted = value.split(":");
      return new Font(splitted[0].trim(), Integer.parseInt(splitted[1].trim()), Integer.parseInt(splitted[2].trim()));
    } else if (fieldType == RenderQuality.class) {
      if (value == null) return RenderQuality.DEFAULT;
      try {
        return RenderQuality.valueOf(value);
      } catch (Exception ex) {
        return RenderQuality.DEFAULT;
      }
    } else {
      throw new Error("Unexpected field type" + fieldType);
    }
  }

  @Nonnull
  @Override
  public String asString(@Nonnull Class<?> fieldType, @Nullable Object value) {
    if (fieldType.isAssignableFrom(Map.class)) {
      if (value == null) return "";
      try {
        final StringBuilder buffer = new StringBuilder();
        final Map<String, KeyShortcut> keyMap = (Map<String, KeyShortcut>) value;
        keyMap.forEach((k, v) -> {
          if (buffer.length() > 0) buffer.append(",");
          buffer.append(k).append("::").append(v.packToString());
        });
        return buffer.toString();
      }catch (Exception ex) {
        throw new RuntimeException("Can't make key short map", ex);
      }
    } else if (fieldType == Color.class) {
      return Integer.toString(((Color) value).getRGB());
    } else if (fieldType == Font.class) {
      final Font font = (Font) value;
      return font.getFamily() + ':' + font.getStyle() + ':' + font.getSize();
    } else if (fieldType == RenderQuality.class) {
      final RenderQuality rq = (RenderQuality) value;
      return GetUtils.ensureNonNull(rq, RenderQuality.DEFAULT).name();
    } else {
      throw new Error("Unexpected field type" + fieldType);
    }
  }

  @Nullable
  @Override
  public Object provideDefaultValue(@Nonnull final String fieldName, @Nonnull final Class<?> fieldType) {
    try {
      for(final Method method : MindMapPanelConfig.class.getDeclaredMethods()) {
        if (Modifier.isPublic(method.getModifiers()) && method.getParameterCount() == 0) {
          SettingsAccessor settingsAccessor = method.getAnnotation(SettingsAccessor.class);
          if (settingsAccessor != null) {
            if (settingsAccessor.name().equals(fieldName)) {
              return method.invoke(etalon);
            }
          }
        }
      }
      throw new IllegalStateException("Can't find getter for field name: " + fieldName);
    } catch (Exception ex) {
      LOGGER.error("Error during default value extraction (" + fieldType + " " + fieldName + ')');
      throw new RuntimeException("Can't extract default value from settings", ex);
    }
  }
}
