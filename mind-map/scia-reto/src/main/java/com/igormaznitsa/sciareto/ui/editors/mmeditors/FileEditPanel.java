/*
 * Copyright (C) 2015-2023 Igor A. Maznitsa
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

package com.igormaznitsa.sciareto.ui.editors.mmeditors;

import com.igormaznitsa.mindmap.ide.commons.editors.AbstractFileEditPanel;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactory;
import com.igormaznitsa.sciareto.ui.UiUtils;
import java.io.File;
import java.util.Objects;
import javax.swing.Icon;
import javax.swing.ImageIcon;

public class FileEditPanel extends AbstractFileEditPanel {

  public FileEditPanel(
      UIComponentFactory uiComponentFactory,
      DialogProvider dialogProvider,
      File projectFolder, DataContainer initialData) {
    super(uiComponentFactory, dialogProvider, projectFolder, initialData);
  }

  @Override
  protected Icon findIcon(final IconId id) {
    switch (id) {
      case BUTTON_CHOOSE:
        return new ImageIcon(Objects.requireNonNull(UiUtils.loadIcon("file_manager.png")));
      case LABEL_BROWSE:
        return new ImageIcon(Objects.requireNonNull(UiUtils.loadIcon("file_link.png")));
      case BUTTON_RESET:
        return new ImageIcon(Objects.requireNonNull(UiUtils.loadIcon("cross16.png")));
      default:
        return null;
    }
  }

  @Override
  protected void openFileInSystemViewer(final File file) {
    UiUtils.openInSystemViewer(file);
  }
}
