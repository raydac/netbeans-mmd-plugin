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
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.plugins.MindMapPluginRegistry;
import com.igormaznitsa.mindmap.plugins.external.ExternalPlugins;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.Converter;
import com.intellij.util.xmlb.annotations.Transient;
import java.io.File;
import java.util.Base64;
import java.util.Objects;
import javax.annotation.Nonnull;
import com.intellij.util.xmlb.annotations.Attribute;

@State(name = "NBMindMapPlugin", storages = {
    @Storage("IdeaMindMapPlugin.xml")})
public class MindMapApplicationSettings implements ApplicationComponent, PersistentStateComponent<MindMapApplicationSettings> {

    private static final String PROPERTY = "idea.mindmap.plugin.folder";
    private static final Logger LOGGER = LoggerFactory.getLogger(MindMapApplicationSettings.class);

    @Attribute(value = "mmd_config_serialized", converter = MindMapPanelConfigSerializer.class)
    private MindMapPanelConfig editorConfig;

    @Transient
    private volatile boolean inited;
    
    public static MindMapApplicationSettings getInstance() {
        return ApplicationManager.getApplication().getComponent(MindMapApplicationSettings.class);
    }

    public MindMapApplicationSettings() {
        this.editorConfig = new MindMapPanelConfig();
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
                return MindMapPanelConfig.deserialize(Base64.getDecoder().decode(value));
            } catch (Exception ex) {
                LOGGER.warn("Detected incompatibility in config format, use default");
                return new MindMapPanelConfig();
            }
        }

        @Override
        public String toString(final MindMapPanelConfig value) {
            try {
                return Base64.getEncoder().encodeToString(value.serialize());
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

    @Override
    public void initComponent() {
        initializeComponent();
    }

    //@Override
    public void initializeComponent() {
        if (!this.inited) {
            this.inited = true;
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
    }
}
