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

package com.igormaznitsa.mindmap.plugins.api;

import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;

/**
 * Plug-in to provide visual representation of attributes.
 *
 * @since 1.2
 */
public interface VisualAttributePlugin extends AttributePlugin {
  /**
   * Get renderable object represents the attribute.
   *
   * @param config the configuration of context were it will be rendered
   * @param topic  the topic
   * @return object to render the attribute, null if it is not shown
   * @since 1.3.0
   */
  Renderable getScaledImage(MindMapPanelConfig config, Topic topic);

  /**
   * Process click on image represents the attribute,
   *
   * @param context             the plugin context
   * @param topic               the topic
   * @param activeGroupModifier true if any modifier to work with topic group (like SHIFT or CTRL) is active
   * @param clickCount          detected number of mouse clicks
   * @return true if the map was changed for the operation, false otherwise
   * @since 1.4.7
   */
  boolean onClick(PluginContext context, Topic topic, boolean activeGroupModifier, int clickCount);

  /**
   * Get tool-tip for image represents the attribute.
   *
   * @param context the plugin context
   * @param topic   the topic
   * @return text to be shown as tool-tip or null if nothing
   */
  String getToolTip(PluginContext context, Topic topic);

  /**
   * Is the visual attribute clickable one.
   *
   * @param context the plugin context
   * @param topic   the topic
   * @return true if the attribute is clickable one, false otherwise
   */
  boolean isClickable(PluginContext context, Topic topic);
}
