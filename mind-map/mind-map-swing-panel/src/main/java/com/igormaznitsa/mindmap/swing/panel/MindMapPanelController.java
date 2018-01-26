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

import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.swing.panel.ui.AbstractElement;
import com.igormaznitsa.mindmap.swing.panel.ui.ElementPart;

import java.awt.Point;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.JPopupMenu;

public interface MindMapPanelController {

  boolean isUnfoldCollapsedTopicDropTarget(@Nonnull MindMapPanel source);

  boolean isCopyColorInfoFromParentToNewChildAllowed(@Nonnull MindMapPanel source);

  boolean isTrimTopicTextBeforeSet(@Nonnull MindMapPanel source);
  
  boolean isSelectionAllowed(@Nonnull MindMapPanel source);

  boolean isElementDragAllowed(@Nonnull MindMapPanel source);

  boolean isMouseMoveProcessingAllowed(@Nonnull MindMapPanel source);

  boolean isMouseWheelProcessingAllowed(@Nonnull MindMapPanel source);

  boolean isMouseClickProcessingAllowed(@Nonnull MindMapPanel source);

  @Nonnull
  MindMapPanelConfig provideConfigForMindMapPanel(@Nonnull MindMapPanel source);

  @Nullable
  JPopupMenu makePopUpForMindMapPanel(@Nonnull MindMapPanel source, @Nonnull Point point, @Nullable AbstractElement elementUnderMouse, @Nullable ElementPart elementPartUnderMouse);

  @Nonnull
  DialogProvider getDialogProvider(@Nonnull MindMapPanel source);

  boolean processDropTopicToAnotherTopic(@Nonnull MindMapPanel source, @Nonnull Point dropPoint, @Nonnull Topic draggedTopic, @Nullable Topic destinationTopic);
}
