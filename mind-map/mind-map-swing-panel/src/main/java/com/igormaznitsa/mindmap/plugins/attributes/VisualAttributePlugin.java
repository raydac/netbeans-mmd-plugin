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
package com.igormaznitsa.mindmap.plugins.attributes;

import java.awt.Image;
import java.awt.event.MouseEvent;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;

public interface VisualAttributePlugin extends AttributePlugin {
  @Nullable
  Image getScaledImage(@Nonnull MindMapPanel panel, @Nonnull Topic topic, double scale);
  void onMouseEvent(@Nonnull MindMapPanel panel, @Nonnull Topic topic, @Nonnull MouseEvent event);
}
