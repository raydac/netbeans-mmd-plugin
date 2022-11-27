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

package com.igormaznitsa.ideamindmap.swing;

import com.igormaznitsa.ideamindmap.editor.MindMapDocumentEditor;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditor;
import javax.annotation.Nonnull;

public class FindInMindMapAction extends AnAction {

  private static final Logger LOGGER = LoggerFactory.getLogger(FindInMindMapAction.class);

  @Override
  public void actionPerformed(@Nonnull AnActionEvent anActionEvent) {
    final FileEditor fileEditor = (FileEditor) anActionEvent.getDataContext().getData("fileEditor");
    if (fileEditor instanceof MindMapDocumentEditor) {
      ((MindMapDocumentEditor) fileEditor).activateTextSearchPanel();
    }
  }

  @Override
  public void update(@Nonnull final AnActionEvent anActionEvent) {
    final FileEditor fileEditor = (FileEditor) anActionEvent.getDataContext().getData("fileEditor");
    anActionEvent.getPresentation().setEnabled(fileEditor instanceof MindMapDocumentEditor);
  }
}
