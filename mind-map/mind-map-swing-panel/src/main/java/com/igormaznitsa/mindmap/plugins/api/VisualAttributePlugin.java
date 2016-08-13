/*
 * Copyright 2016 Igor Maznitsa.
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;

/**
 * Plug-in to provide visual representation of attributes.
 * 
 * @since 1.2
 */
public interface VisualAttributePlugin extends AttributePlugin {
  /**
   * Get renderable object represents the attribute.
   * @param config the configuration of context were it will be rendered
   * @param topic the topic
   * @return object to render the attribute, null if it is not shown
   * @since 1.3.0
   */
  @Nullable
  Renderable getScaledImage(@Nonnull MindMapPanelConfig config, @Nonnull Topic topic);
  /**
   * Process click on image represents the attribute, 
   * @param panel the panel
   * @param topic the topic 
   * @param clickCount detected number of mouse clicks
   * @return true if the map was changed for the operation, false otherwise
   */
  boolean onClick(@Nonnull MindMapPanel panel, @Nonnull Topic topic, int clickCount);
  /**
   * Get tool-tip for image represents the attribute.
   * @param panel the panel
   * @param topic the topic
   * @return text to be shown as tool-tip or null if nothing
   */
  @Nullable
  String getToolTip(@Nonnull MindMapPanel panel, @Nonnull Topic topic);
  /**
   * Is the visual attribute clickable one.
   * @param panel the panel
   * @param topic the topic
   * @return true if the attribute is clickable one, false otherwise
   */
  boolean isClickable(@Nonnull MindMapPanel panel, @Nonnull Topic topic);
}
