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
package com.igormaznitsa.ideamindmap.settings;

import com.igormaznitsa.ideamindmap.plugins.PrinterPlugin;
import com.igormaznitsa.meta.common.utils.GetUtils;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.plugins.MindMapPluginRegistry;
import com.igormaznitsa.mindmap.plugins.external.ExternalPlugins;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.mindmap.swing.panel.utils.RenderQuality;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.io.File;
import java.lang.reflect.Field;

@State(name = "NBMindMapPlugin", storages = {@Storage(id = "nbmindmap", file = "$APP_CONFIG$/nbmindmapsettings.xml")})
public class MindMapApplicationSettings implements ApplicationComponent, PersistentStateComponent<DeclaredFieldsSerializer>, DeclaredFieldsSerializer.Converter {

    private static final MindMapPanelConfig etalon = new MindMapPanelConfig();
    private final MindMapPanelConfig editorConfig = new MindMapPanelConfig();
    private static final String PROPERTY = "nbmmd.plugin.folder";

    private static final Logger LOGGER = LoggerFactory.getLogger(MindMapApplicationSettings.class);

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

    public static MindMapApplicationSettings findInstance() {
        return ApplicationManager.getApplication().getComponent(MindMapApplicationSettings.class);
    }

    @Nullable
    @Override
    public Object fromString(@Nonnull Class<?> fieldType, @Nonnull String value) {
        if (fieldType == Color.class) {
            return new Color(Integer.parseInt(value), true);
        } else if (fieldType == Font.class) {
            final String[] splitted = value.split(":");
            return new Font(splitted[0], Integer.parseInt(splitted[1]), Integer.parseInt(splitted[2]));
        } else if (fieldType == RenderQuality.class) {
            try {
                return RenderQuality.valueOf(value);
            } catch (Exception ex) {
                return RenderQuality.DEFAULT;
            }
        } else
            throw new Error("Unexpected field type" + fieldType);
    }

    @Nonnull
    @Override
    public String asString(@Nonnull Class<?> fieldType, @Nonnull Object value) {
        if (fieldType == Color.class) {
            return Integer.toString(((Color) value).getRGB());
        } else if (fieldType == Font.class) {
            final Font font = (Font) value;
            return font.getFamily() + ':' + Integer.toString(font.getStyle()) + ':' + Integer.toString(font.getSize());
        } else if (fieldType == RenderQuality.class) {
            final RenderQuality rq = (RenderQuality) value;
            return GetUtils.ensureNonNull(rq, RenderQuality.DEFAULT).name();
        } else
            throw new Error("Unexpected field type" + fieldType);
    }

    @Nullable
    @Override
    public Object provideDefaultValue(@Nonnull final String fieldName, @Nonnull final Class<?> fieldType) {
        try {
            final Field field = MindMapPanelConfig.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(etalon);
        } catch (Exception ex) {
            LOGGER.error("Error during default value extraction (" + fieldType + " " + fieldName + ')');
            throw new RuntimeException("Can't extract default value from settings", ex);
        }
    }
}
