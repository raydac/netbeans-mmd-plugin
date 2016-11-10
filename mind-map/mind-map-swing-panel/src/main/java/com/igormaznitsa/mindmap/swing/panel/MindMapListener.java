/*
 * Copyright 2015 Igor Maznitsa.
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

import java.awt.Dimension;

import javax.annotation.Nonnull;

import com.igormaznitsa.meta.annotation.MustNotContainNull;

public interface MindMapListener {
  void onMindMapModelChanged(@Nonnull MindMapPanel source);
  void onMindMapModelRealigned(@Nonnull MindMapPanel source, @Nonnull Dimension coveredAreaSize);
  void onEnsureVisibilityOfTopic(@Nonnull MindMapPanel source, @Nonnull Topic topic);
  void onTopicCollapsatorClick(@Nonnull MindMapPanel source, @Nonnull Topic topic, boolean beforeAction);
  void onClickOnExtra(@Nonnull MindMapPanel source,int clicks, @Nonnull Topic topic, @Nonnull Extra<?> extra);
  void onChangedSelection(@Nonnull MindMapPanel source, @Nonnull @MustNotContainNull Topic [] currentSelectedTopics);
  boolean allowedRemovingOfTopics(@Nonnull MindMapPanel source, @Nonnull @MustNotContainNull Topic [] topics);
}
