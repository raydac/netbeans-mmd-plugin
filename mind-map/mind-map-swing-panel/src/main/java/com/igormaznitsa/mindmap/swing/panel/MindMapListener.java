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

import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.swing.panel.utils.KeyEventType;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;

public interface MindMapListener {
  void onMindMapModelChanged(MindMapPanel source, boolean saveToHistory);

  void onComponentElementsLayout(MindMapPanel source, Graphics2D g);

  void onMindMapModelRealigned(MindMapPanel source, Dimension coveredAreaSize);

  void onEnsureVisibilityOfTopic(MindMapPanel source, Topic topic);

  void onTopicCollapsatorClick(MindMapPanel source, Topic topic, boolean beforeAction);

  void onScaledByMouse(MindMapPanel source, Point mousePoint, double oldScale, double newScale,
                       Dimension oldSize, Dimension newSize);

  void onClickOnExtra(MindMapPanel source, int modifiers, int clicks, Topic topic, Extra<?> extra);

  void onChangedSelection(MindMapPanel source, Topic[] currentSelectedTopics);

  boolean allowedRemovingOfTopics(MindMapPanel source, Topic[] topics);

  void onNonConsumedKeyEvent(MindMapPanel source, KeyEvent event, KeyEventType type);
}
