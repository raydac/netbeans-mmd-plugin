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

package com.igormaznitsa.ideamindmap.print;

import com.igormaznitsa.ideamindmap.utils.IdeaUtils;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.print.MMDPrintPanel;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import java.awt.Window;
import javax.annotation.Nonnull;
import javax.swing.SwingUtilities;

public class IdeaMMDPrintPanelAdaptor implements MMDPrintPanel.Adaptor {

  private static final Logger LOGGER = LoggerFactory.getLogger(IdeaMMDPrintPanelAdaptor.class);

  private final Project project;

  public IdeaMMDPrintPanelAdaptor(@Nonnull final Project project) {
    this.project = project;
  }

  @Override
  public void startBackgroundTask(@Nonnull final MMDPrintPanel source, @Nonnull final String taskName, @Nonnull final Runnable task) {
    final Task.Backgroundable backgroundTask = new Task.Backgroundable(this.project, taskName) {
      @Override
      public void run(@Nonnull final ProgressIndicator indicator) {
        try {
          indicator.setIndeterminate(true);
          task.run();
          IdeaUtils.showPopup(String.format("%s has been sent to the printer", taskName), MessageType.INFO);
        } catch (Exception ex) {
          LOGGER.error("Print error", ex);
          IdeaUtils.showPopup("Print error! See the log!", MessageType.ERROR);
        } finally {
          indicator.stop();
        }
      }
    };
    ProgressManager.getInstance().run(backgroundTask);
  }

  @Override
  public boolean isDarkTheme(@Nonnull final MMDPrintPanel source) {
    return IdeaUtils.isDarkTheme();
  }

  @Override
  public void onPrintTaskStarted(@Nonnull final MMDPrintPanel source) {
    SwingUtilities.invokeLater(() -> {
      final Window wnd = SwingUtilities.windowForComponent(source);
      if (wnd != null) {
        wnd.dispose();
      }
    });
  }

}
