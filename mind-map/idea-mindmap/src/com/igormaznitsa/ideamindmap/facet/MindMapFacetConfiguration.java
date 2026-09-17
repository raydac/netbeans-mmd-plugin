/*
 * Copyright (C) 2015-2022 Igor A. Maznitsa
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

package com.igormaznitsa.ideamindmap.facet;

import com.intellij.facet.FacetConfiguration;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.openapi.components.PersistentStateComponent;
import org.jetbrains.annotations.NotNull;

public class MindMapFacetConfiguration implements FacetConfiguration, PersistentStateComponent<MindMapFacetConfiguration.State> {

  private static final String KEY_USE_INSIDE_BROWSER = "useInsideBrowser";
  private static final String KEY_USE_PROJECT_BASE_FOLDER_AS_ROOT = "useProjectBaseFolderAsRoot";
  private static final String KEY_MAKE_RELATIVE_PATH = "makeRelativePath";
  private static final String KEY_TRIM_TOPIC_TEXT_BEFORE_SET = "trimTopicText";
  private static final String KEY_COPY_COLOR_INFO_FROM_PARENT = "copyParentColorInfoInNew";
  private static final String KEY_UNFOLD_COLLAPSED_TOPIC_DROP_TARGET = "unfoldCollapsedTopicInDrop";
  private static final String KEY_DISABLE_PROJECT_KNOWLEDGE_AUTOCTREATION = "disableAutocreateProjectKnowledgeFolder";
  private final InMemoryPreferenceNode preferences = new InMemoryPreferenceNode();

  public static final class State {
    public boolean useInsideBrowser;
    public boolean useProjectBaseFolderAsRoot;
    public boolean makeRelativePath = true;
    public boolean trimTopicText;
    public boolean copyParentColorInfoInNew = true;
    public boolean unfoldCollapsedTopicInDrop = true;
    public boolean disableAutocreateProjectKnowledgeFolder = true;
  }

  @Override
  public FacetEditorTab[] createEditorTabs(final FacetEditorContext editorContext, final FacetValidatorsManager validatorsManager) {
    return new FacetEditorTab[] {new MindMapFacetEditorTab(this)};
  }

  @Override
  public State getState() {
    final State state = new State();
    state.useInsideBrowser = this.isUseInsideBrowser();
    state.useProjectBaseFolderAsRoot = this.isUseProjectBaseFolderAsRoot();
    state.makeRelativePath = this.isMakeRelativePath();
    state.trimTopicText = this.isTrimTopicTextBeforeSet();
    state.copyParentColorInfoInNew = this.isCopyColorInformationFromParent();
    state.unfoldCollapsedTopicInDrop = this.isUnfoldTopicWhenItIsDropTarget();
    state.disableAutocreateProjectKnowledgeFolder = this.isDisableAutoCreateProjectKnowledgeFolder();
    return state;
  }

  @Override
  public void loadState(@NotNull final State state) {
    this.setUseInsideBrowser(state.useInsideBrowser);
    this.setUseProjectBaseFolderAsRoot(state.useProjectBaseFolderAsRoot);
    this.setMakeRelativePath(state.makeRelativePath);
    this.setTrimTopicTextBeforeSet(state.trimTopicText);
    this.setCopyColorInformationFromParent(state.copyParentColorInfoInNew);
    this.setUnfoldTopicWhenItIsDropTarget(state.unfoldCollapsedTopicInDrop);
    this.setDisableAutoCreateProjectKnowledgeFolder(state.disableAutocreateProjectKnowledgeFolder);
  }

  public boolean isTrimTopicTextBeforeSet() {
    return this.preferences.getBoolean(KEY_TRIM_TOPIC_TEXT_BEFORE_SET, false);
  }

  public void setTrimTopicTextBeforeSet(final boolean flag) {
    this.preferences.putBoolean(KEY_TRIM_TOPIC_TEXT_BEFORE_SET, flag);
  }

  public boolean isUseProjectBaseFolderAsRoot() {
    return this.preferences.getBoolean(KEY_USE_PROJECT_BASE_FOLDER_AS_ROOT, false);
  }

  public void setUseProjectBaseFolderAsRoot(final boolean flag) {
    this.preferences.putBoolean(KEY_USE_PROJECT_BASE_FOLDER_AS_ROOT, flag);
  }

  public boolean isUseInsideBrowser() {
    return this.preferences.getBoolean(KEY_USE_INSIDE_BROWSER, false);
  }

  public void setUseInsideBrowser(final boolean flag) {
    this.preferences.putBoolean(KEY_USE_INSIDE_BROWSER, flag);
  }

  public boolean isCopyColorInformationFromParent() {
    return this.preferences.getBoolean(KEY_COPY_COLOR_INFO_FROM_PARENT, true);
  }

  public void setCopyColorInformationFromParent(final boolean flag) {
    this.preferences.putBoolean(KEY_COPY_COLOR_INFO_FROM_PARENT, flag);
  }

  public boolean isUnfoldTopicWhenItIsDropTarget() {
    return this.preferences.getBoolean(KEY_UNFOLD_COLLAPSED_TOPIC_DROP_TARGET, true);
  }

  public void setUnfoldTopicWhenItIsDropTarget(final boolean flag) {
    this.preferences.putBoolean(KEY_UNFOLD_COLLAPSED_TOPIC_DROP_TARGET, flag);
  }

  public boolean isMakeRelativePath() {
    return this.preferences.getBoolean(KEY_MAKE_RELATIVE_PATH, true);
  }

  public void setMakeRelativePath(final boolean flag) {
    this.preferences.putBoolean(KEY_MAKE_RELATIVE_PATH, flag);
  }

  public boolean isDisableAutoCreateProjectKnowledgeFolder() {
    return this.preferences.getBoolean(KEY_DISABLE_PROJECT_KNOWLEDGE_AUTOCTREATION, true);
  }

  public void setDisableAutoCreateProjectKnowledgeFolder(final boolean flag) {
    this.preferences.putBoolean(KEY_DISABLE_PROJECT_KNOWLEDGE_AUTOCTREATION, flag);
  }

}
