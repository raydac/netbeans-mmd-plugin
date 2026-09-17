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

import static java.util.Objects.requireNonNull;

import com.igormaznitsa.ideamindmap.editor.MindMapDocumentEditor;
import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.swing.panel.MindMapListener;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.panel.utils.KeyEventType;
import com.intellij.ide.structureView.FileEditorPositionListener;
import com.intellij.ide.structureView.ModelListener;
import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.Filter;
import com.intellij.ide.util.treeView.smartTree.Grouper;
import com.intellij.ide.util.treeView.smartTree.Sorter;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiTreeChangeAdapter;
import com.intellij.psi.PsiTreeChangeEvent;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

final class MindMapStructureViewModel
    implements StructureViewModel, StructureViewModel.ElementInfoProvider, StructureViewModel.ExpandInfoProvider {

  private static final int[] ROOT_PATH = {0};

  private final PsiFile psiFile;
  private final MindMapDocumentEditor editor;
  private final MindMapStructureViewElement rootElement;
  private final StructureSyncListener syncListener;
  private final List<FileEditorPositionListener> editorPositionListeners = new CopyOnWriteArrayList<>();
  private final List<ModelListener> modelListeners = new CopyOnWriteArrayList<>();

  MindMapStructureViewModel(
      @Nonnull final PsiFile psiFile,
      @Nullable final MindMapDocumentEditor editor
  ) {
    this.psiFile = requireNonNull(psiFile, "psiFile must not be null");
    this.editor = editor;
    this.rootElement = new MindMapStructureViewElement(this, ROOT_PATH);
    this.syncListener = new StructureSyncListener();
    this.attachListeners();
  }

  @Nonnull
  @Override
  public StructureViewTreeElement getRoot() {
    return this.rootElement;
  }

  @Nullable
  @Override
  public Object getCurrentEditorElement() {
    final Topic selected = this.selectedTopic();
    return selected == null ? null : new TopicPathKey(selected.getPositionPath());
  }

  @Override
  public void addEditorPositionListener(@Nonnull final FileEditorPositionListener listener) {
    this.editorPositionListeners.add(requireNonNull(listener, "listener must not be null"));
  }

  @Override
  public void removeEditorPositionListener(@Nonnull final FileEditorPositionListener listener) {
    this.editorPositionListeners.remove(requireNonNull(listener, "listener must not be null"));
  }

  @Override
  public void addModelListener(@Nonnull final ModelListener modelListener) {
    this.modelListeners.add(requireNonNull(modelListener, "modelListener must not be null"));
  }

  @Override
  public void removeModelListener(@Nonnull final ModelListener modelListener) {
    this.modelListeners.remove(requireNonNull(modelListener, "modelListener must not be null"));
  }

  @Nonnull
  @Override
  public Grouper[] getGroupers() {
    return Grouper.EMPTY_ARRAY;
  }

  @Nonnull
  @Override
  public Sorter[] getSorters() {
    return new Sorter[] {Sorter.ALPHA_SORTER};
  }

  @Nonnull
  @Override
  public Filter[] getFilters() {
    return Filter.EMPTY_ARRAY;
  }

  @Override
  public boolean shouldEnterElement(final Object element) {
    return false;
  }

  @Override
  public boolean isAlwaysShowsPlus(@Nonnull final StructureViewTreeElement element) {
    return false;
  }

  @Override
  public boolean isAlwaysLeaf(@Nonnull final StructureViewTreeElement element) {
    return element instanceof MindMapStructureViewElement topicElement
        && topicElement.isLeafTopic();
  }

  @Override
  public boolean isAutoExpand(@Nonnull final StructureViewTreeElement element) {
    return element instanceof MindMapStructureViewElement topicElement
        && topicElement.isShallowTopic();
  }

  @Override
  public boolean isSmartExpand() {
    return true;
  }

  @Override
  public void dispose() {
    this.detachPanelListener();
    PsiManager.getInstance(this.psiFile.getProject()).removePsiTreeChangeListener(this.syncListener);
  }

  @Nullable
  MindMap currentMap() {
    if (this.hasLivePanel()) {
      return this.editor.getMindMapPanel().getModel();
    }
    return this.parsePsiMap();
  }

  @Nonnull
  PsiFile psiFile() {
    return this.psiFile;
  }

  @Nullable
  MindMapDocumentEditor openVisualEditor() {
    if (this.hasLivePanel()) {
      return this.editor;
    }
    if (this.psiFile.getVirtualFile() == null) {
      return null;
    }
    FileEditorManager.getInstance(this.psiFile.getProject())
        .openFile(this.psiFile.getVirtualFile(), true);
    return MindMapStructureViewFactory.findVisualEditor(this.psiFile);
  }

  private boolean hasLivePanel() {
    return this.editor != null && !this.editor.getMindMapPanel().isDisposed();
  }

  @Nullable
  private Topic selectedTopic() {
    if (!this.hasLivePanel()) {
      return null;
    }
    final Topic[] selected = this.editor.getMindMapPanel().getSelectedTopics();
    return selected.length == 0 ? null : selected[0];
  }

  @Nullable
  private MindMap parsePsiMap() {
    try {
      return new MindMap(new StringReader(this.psiFile.getText()));
    } catch (final IOException | RuntimeException ex) {
      return null;
    }
  }

  private void attachListeners() {
    PsiManager.getInstance(this.psiFile.getProject())
        .addPsiTreeChangeListener(this.syncListener, this);
    if (this.hasLivePanel()) {
      this.editor.getMindMapPanel().addMindMapListener(this.syncListener);
    }
  }

  private void detachPanelListener() {
    if (!this.hasLivePanel()) {
      return;
    }
    this.editor.getMindMapPanel().removeMindMapListener(this.syncListener);
  }

  private void notifyModelChanged() {
    this.modelListeners.forEach(ModelListener::onModelChanged);
  }

  private void notifyEditorPositionChanged() {
    this.editorPositionListeners.forEach(FileEditorPositionListener::onCurrentElementChanged);
  }

  private final class StructureSyncListener extends PsiTreeChangeAdapter implements MindMapListener {

    private void notifyIfSameFile(@Nullable final PsiFile file) {
      if (MindMapStructureViewModel.this.psiFile.equals(file)) {
        MindMapStructureViewModel.this.notifyModelChanged();
      }
    }

    @Override
    public void childrenChanged(@Nonnull final PsiTreeChangeEvent event) {
      this.notifyIfSameFile(event.getFile());
    }

    @Override
    public void childAdded(@Nonnull final PsiTreeChangeEvent event) {
      this.notifyIfSameFile(event.getFile());
    }

    @Override
    public void childRemoved(@Nonnull final PsiTreeChangeEvent event) {
      this.notifyIfSameFile(event.getFile());
    }

    @Override
    public void childReplaced(@Nonnull final PsiTreeChangeEvent event) {
      this.notifyIfSameFile(event.getFile());
    }

    @Override
    public void childMoved(@Nonnull final PsiTreeChangeEvent event) {
      this.notifyIfSameFile(event.getFile());
    }

    @Override
    public void onMindMapModelChanged(final MindMapPanel source, final boolean saveToHistory) {
      MindMapStructureViewModel.this.notifyModelChanged();
    }

    @Override
    public void onChangedSelection(final MindMapPanel source, final Topic[] currentSelectedTopics) {
      MindMapStructureViewModel.this.notifyEditorPositionChanged();
    }

    @Override
    public void onComponentElementsLayout(final MindMapPanel source, final Graphics2D g) {
    }

    @Override
    public void onMindMapModelRealigned(final MindMapPanel source, final Dimension coveredAreaSize) {
    }

    @Override
    public void onEnsureVisibilityOfTopic(final MindMapPanel source, final Topic topic) {
    }

    @Override
    public void onTopicCollapsatorClick(
        final MindMapPanel source,
        final Topic topic,
        final boolean beforeAction
    ) {
    }

    @Override
    public void onScaledByMouse(
        final MindMapPanel source,
        final Point mousePoint,
        final double oldScale,
        final double newScale,
        final Dimension oldSize,
        final Dimension newSize
    ) {
    }

    @Override
    public void onClickOnExtra(
        final MindMapPanel source,
        final int modifiers,
        final int clicks,
        final Topic topic,
        final Extra<?> extra
    ) {
    }

    @Override
    public boolean allowedRemovingOfTopics(final MindMapPanel source, final Topic[] topics) {
      return true;
    }

    @Override
    public void onNonConsumedKeyEvent(
        final MindMapPanel source,
        final KeyEvent event,
        final KeyEventType type
    ) {
    }

    @Override
    public void onQuickNoteEvent(final MindMapPanel source, final boolean activate) {
    }
  }
}
