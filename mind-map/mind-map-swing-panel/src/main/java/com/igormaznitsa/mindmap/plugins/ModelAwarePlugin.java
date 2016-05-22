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
package com.igormaznitsa.mindmap.plugins;

import javax.annotation.Nonnull;
import com.igormaznitsa.meta.annotation.Weight;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;

public interface ModelAwarePlugin extends MindMapPlugin {
  @Weight(Weight.Unit.NORMAL)
  void onDeleteTopic(@Nonnull final MindMapPanel panel, @Nonnull final Topic topic);
  void onCreateTopic(@Nonnull final MindMapPanel panel, @Nonnull Topic parent, @Nonnull final Topic newTopic);
}
