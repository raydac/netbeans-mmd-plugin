/*
 * Copyright 2019 Igor Maznitsa.
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

package com.igormaznitsa.mindmap.plugins.api;

import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.mindmap.model.MindMapController;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Interface describes context where executed or activated plug-in.
 *
 * @since 1.4.7
 */
public interface PluginContext {
  @Nonnull
  MindMapPanelConfig getPanelConfig();

  @Nonnull
  MindMapController getController();

  @Nonnull
  MindMapPanel getPanel();

  @Nonnull
  DialogProvider getDialogProvider();

  @Nullable
  @MustNotContainNull
  Topic[] getSelectedTopics();

  void processPluginActivation(@Nonnull ExternallyExecutedPlugin plugin, @Nullable Topic activeTopic);
}
