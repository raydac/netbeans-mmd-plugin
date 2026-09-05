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

import static com.igormaznitsa.mindmap.model.logger.LoggerFactory.getLogger;
import static java.util.Base64.getDecoder;
import static java.util.Base64.getEncoder;

import com.igormaznitsa.ideamindmap.plugins.PrinterPlugin;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.plugins.MindMapPluginRegistry;
import com.igormaznitsa.mindmap.plugins.external.ExternalPlugins;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.Converter;
import com.intellij.util.xmlb.annotations.Attribute;
import java.io.File;
import javax.annotation.Nonnull;

@State(name = "NBMindMapPlugin", storages = {
    @Storage("IdeaMindMapPlugin.xml")})
public class MindMapApplicationSettings implements PersistentStateComponent<MindMapApplicationSettings> {

    private static final String PROPERTY = "idea.mindmap.plugin.folder";
    private static final Logger LOGGER = getLogger(MindMapApplicationSettings.class);

    @Attribute(value = "mmd_config_serialized", converter = MindMapPanelConfigSerializer.class)
    private MindMapPanelConfig editorConfig;

    private static volatile boolean pluginsInited;

    public static MindMapApplicationSettings getInstance() {
        return ApplicationManager.getApplication().getService(MindMapApplicationSettings.class);
    }

    public MindMapApplicationSettings() {
        this.editorConfig = new MindMapPanelConfig();
        initializePlugins();
    }

    public static MindMapApplicationSettings from(final MindMapPanelConfig config) {
        MindMapApplicationSettings result = new MindMapApplicationSettings();
        result.editorConfig = config;
        return result;
    }

    public static class MindMapPanelConfigSerializer extends Converter<MindMapPanelConfig> {

        @Override
        public MindMapPanelConfig fromString(final String value) {
            try {
                return new MindMapPanelConfig(MindMapPanelConfig.deserialize(getDecoder().decode(value)), false);
            } catch (Exception ex) {
                LOGGER.warn("Detected incompatibility in config format, use default");
                return new MindMapPanelConfig();
            }
        }

        @Override
        public String toString(final MindMapPanelConfig value) {
            try {
                return getEncoder().encodeToString(value.serialize());
            } catch (Exception ex) {
                LOGGER.error("Can't serialize configuration for error", ex);
                throw new RuntimeException("Error during configuration serialization", ex);
            }
        }
    }

    public MindMapPanelConfig getConfig() {
        return this.editorConfig;
    }

    @Nonnull
    @Override
    public MindMapApplicationSettings getState() {
        return this;
    }

    @Override
    public void loadState(final MindMapApplicationSettings state) {
        this.editorConfig.makeFullCopyOf(state.editorConfig, false, true);
    }

    private static void initializePlugins() {
        if (pluginsInited) {
            return;
        }

        pluginsInited = true;
        MindMapPluginRegistry.getInstance().registerPlugin(new PrinterPlugin());
        final String pluginFolder = System.getProperty(PROPERTY);
        if (pluginFolder == null) {
            LOGGER.info("Property " + PROPERTY + " is not defined");
            return;
        }

        final File folder = new File(pluginFolder);
        if (folder.isDirectory()) {
            LOGGER.info("Loading plugins from folder : " + folder);
            new ExternalPlugins(folder).init();
        } else {
            LOGGER.error("Can't find plugin folder : " + folder);
        }
    }
}
