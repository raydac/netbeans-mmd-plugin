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

package com.igormaznitsa.ideamindmap.view;

import static java.util.Objects.requireNonNull;

import com.igormaznitsa.ideamindmap.utils.AllIcons;
import com.intellij.ide.SelectInContext;
import com.intellij.ide.SelectInTarget;
import com.intellij.ide.projectView.impl.AbstractProjectViewPane;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.openapi.vfs.VirtualFile;
import javax.annotation.Nonnull;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class KnowledgeViewPane extends AbstractProjectViewPane {
  @NonNls
  public static final String ID = "NBKnowledgePane";

  public KnowledgeViewPane(@Nonnull Project project) {
    super(project);
  }

  @Override
  public String getTitle() {
    return "Knowledge folders";
  }

  @Override
  public Icon getIcon() {
    return requireNonNull(AllIcons.Logo.MINDMAP);
  }

  @Nonnull
  @Override
  public String getId() {
    return ID;
  }

  @Override
  public int getWeight() {
    return 51175;
  }

  @Override
  public JComponent createComponent() {
    return new JLabel("Not implemented yet");
  }

  @Override
  public ActionCallback updateFromRoot(boolean b) {
    return ActionCallback.DONE;
  }

  @Override
  public void select(Object o, VirtualFile virtualFile, boolean b) {

  }

  @Override
  public @NotNull SelectInTarget createSelectInTarget() {
    return new SelectInTarget() {
      @Override
      public boolean canSelect(SelectInContext selectInContext) {
        return false;
      }

      @Override
      public void selectIn(SelectInContext selectInContext, boolean b) {

      }
    };
  }
}
