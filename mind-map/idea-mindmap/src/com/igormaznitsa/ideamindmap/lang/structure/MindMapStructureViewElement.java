/*
 * Copyright (C) 2015-2026 Igor A. Maznitsa
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

package com.igormaznitsa.ideamindmap.lang.structure;

import static com.igormaznitsa.mindmap.swing.panel.utils.Utils.getFirstLine;
import static com.igormaznitsa.mindmap.swing.panel.utils.Utils.makeShortTextVersion;
import static java.util.Objects.requireNonNull;

import com.igormaznitsa.ideamindmap.editor.MindMapDocumentEditor;
import com.igormaznitsa.ideamindmap.utils.AllIcons;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.Topic;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import java.util.Locale;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.Icon;
import javax.swing.JComponent;

final class MindMapStructureViewElement
    implements StructureViewTreeElement, SortableTreeElement, ItemPresentation {

  private static final int TITLE_LIMIT = 80;

  private final MindMapStructureViewModel model;
  private final int[] positionPath;

  MindMapStructureViewElement(
      @Nonnull final MindMapStructureViewModel model,
      @Nonnull final int[] positionPath
  ) {
    this.model = requireNonNull(model, "model must not be null");
    this.positionPath = requireNonNull(positionPath, "positionPath must not be null").clone();
  }

  @Nonnull
  @Override
  public Object getValue() {
    return new TopicPathKey(this.positionPath);
  }

  @Override
  public void navigate(final boolean requestFocus) {
    final MindMapDocumentEditor visualEditor = this.model.openVisualEditor();
    if (visualEditor == null || visualEditor.getMindMapPanel().isDisposed()) {
      return;
    }
    final Topic liveTopic = visualEditor.getMindMapPanel().getModel().findAtPosition(this.positionPath);
    if (liveTopic == null) {
      return;
    }
    visualEditor.getMindMapPanel().focusTo(liveTopic);
    visualEditor.topicToCentre(liveTopic);
    if (requestFocus) {
      final JComponent focusTarget = visualEditor.getPreferredFocusedComponent();
      if (focusTarget != null) {
        focusTarget.requestFocusInWindow();
      }
    }
  }

  @Override
  public boolean canNavigate() {
    return this.resolveTopic() != null;
  }

  @Override
  public boolean canNavigateToSource() {
    return this.canNavigate();
  }

  @Nonnull
  @Override
  public ItemPresentation getPresentation() {
    return this;
  }

  @Nonnull
  @Override
  public TreeElement[] getChildren() {
    final Topic topic = this.resolveTopic();
    if (topic == null) {
      return TreeElement.EMPTY_ARRAY;
    }
    return topic.getChildren().stream()
        .map(child -> new MindMapStructureViewElement(this.model, child.getPositionPath()))
        .toArray(TreeElement[]::new);
  }

  @Nonnull
  @Override
  public String getAlphaSortKey() {
    final String text = this.getPresentableText();
    return text == null ? "" : text.toLowerCase(Locale.ROOT);
  }

  @Nullable
  @Override
  public String getPresentableText() {
    final Topic topic = this.resolveTopic();
    if (topic == null) {
      return this.model.psiFile().getName();
    }
    final String title = getFirstLine(topic.getText()).strip();
    return title.isEmpty() ? "<empty>" : makeShortTextVersion(title, TITLE_LIMIT);
  }

  @Nullable
  @Override
  public String getLocationString() {
    final Topic topic = this.resolveTopic();
    if (topic == null || topic.isExtrasEmpty()) {
      return null;
    }
    return topic.getExtras().keySet().stream()
        .map(type -> type.name().toLowerCase(Locale.ROOT))
        .collect(Collectors.joining(", "));
  }

  @Nullable
  @Override
  public Icon getIcon(final boolean unused) {
    final Topic topic = this.resolveTopic();
    if (topic == null) {
      return AllIcons.File.MINDMAP;
    }
    switch (topic.getTopicLevel()) {
      case 0:
        return AllIcons.Tree.DOCUMENT;
      case 1:
        return AllIcons.Tree.BLUEBALL;
      default:
        return AllIcons.Tree.GOLDBALL;
    }
  }

  boolean isLeafTopic() {
    final Topic topic = this.resolveTopic();
    return topic == null || topic.getChildren().isEmpty();
  }

  boolean isShallowTopic() {
    return this.positionPath.length <= 2;
  }

  @Nullable
  private Topic resolveTopic() {
    final MindMap map = this.model.currentMap();
    return map == null ? null : map.findAtPosition(this.positionPath);
  }
}
