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

package com.igormaznitsa.mindmap.swing.panel;

import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.swing.panel.utils.KeyEventType;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import javax.annotation.Nonnull;

public interface MindMapListener {
  void onMindMapModelChanged(@Nonnull MindMapPanel source, boolean saveToHistory);

  void onComponentElementsLayouted(@Nonnull MindMapPanel source, @Nonnull Graphics2D g);

  void onMindMapModelRealigned(@Nonnull MindMapPanel source, @Nonnull Dimension coveredAreaSize);

  void onEnsureVisibilityOfTopic(@Nonnull MindMapPanel source, @Nonnull Topic topic);

  void onTopicCollapsatorClick(@Nonnull MindMapPanel source, @Nonnull Topic topic, boolean beforeAction);

  void onScaledByMouse(@Nonnull MindMapPanel source, @Nonnull Point mousePoint, double oldScale, double newScale, @Nonnull Dimension oldSize, @Nonnull Dimension newSize);

  void onClickOnExtra(@Nonnull MindMapPanel source, int modifiers, int clicks, @Nonnull Topic topic, @Nonnull Extra<?> extra);

  void onChangedSelection(@Nonnull MindMapPanel source, @Nonnull @MustNotContainNull Topic[] currentSelectedTopics);

  boolean allowedRemovingOfTopics(@Nonnull MindMapPanel source, @Nonnull @MustNotContainNull Topic[] topics);

  void onNonConsumedKeyEvent(@Nonnull MindMapPanel source, @Nonnull KeyEvent event, @Nonnull KeyEventType type);
}
