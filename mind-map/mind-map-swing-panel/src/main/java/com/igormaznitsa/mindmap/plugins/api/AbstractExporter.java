/*
 * Copyright 2016 Igor Maznitsa.
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
package com.igormaznitsa.mindmap.plugins.api;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import com.igormaznitsa.meta.annotation.MayContainNull;
import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.plugins.PopUpSection;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.panel.Texts;

/**
 * Abstract auxiliary class automates way to implement an abstract exporter.
 * @since 1.2
 */
public abstract class AbstractExporter extends AbstractPopupMenuItem implements HasMnemonic {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractExporter.class);

  @Override
  @Nullable
  public JMenuItem makeMenuItem(
      @Nonnull final MindMapPanel panel,
      @Nonnull final DialogProvider dialogProvider,
      @Nullable final Topic actionTopic,
      @Nonnull @MayContainNull final Topic[] selectedTopics,
      @Nullable final CustomJob processor) {
    final JMenuItem result = UI_COMPO_FACTORY.makeMenuItem(getName(panel, actionTopic, selectedTopics), getIcon(panel, actionTopic, selectedTopics));
    result.setToolTipText(getReference(panel, actionTopic, selectedTopics));

    final AbstractPopupMenuItem theInstance = this;

    result.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(@Nonnull final ActionEvent e) {
        try {
          if (processor == null) {
            final JComponent options = makeOptions();
            if (options != null && !dialogProvider.msgOkCancel(null,getName(panel, actionTopic, selectedTopics), options)) {
              return;
            }
            doExport(panel, options, null);
          } else {
            processor.doJob(theInstance, panel, dialogProvider, actionTopic, selectedTopics);
          }
        } catch (Exception ex) {
          LOGGER.error("Error during map export", ex); //NOI18N
          dialogProvider.msgError(null,Texts.getString("MMDGraphEditor.makePopUp.errMsgCantExport"));
        }
      }
    });
    return result;
  }

  @Override
  @Nonnull
  public PopUpSection getSection() {
    return PopUpSection.EXPORT;
  }

  @Override
  public boolean needsTopicUnderMouse() {
    return false;
  }

  @Override
  public boolean needsSelectedTopics() {
    return false;
  }

  @Nullable
  public JComponent makeOptions() {
    return null;
  }

  @Nullable
  public String getMnemonic() {
    return null;
  }

  public abstract void doExport(@Nonnull final MindMapPanel panel, @Nullable final JComponent options, @Nullable final OutputStream out) throws IOException;

  @Nonnull
  public abstract String getName(@Nonnull final MindMapPanel panel, @Nullable Topic actionTopic, @Nonnull @MustNotContainNull Topic[] selectedTopics);

  @Nonnull
  public abstract String getReference(@Nonnull final MindMapPanel panel, @Nullable Topic actionTopic, @Nonnull @MustNotContainNull Topic[] selectedTopics);

  @Nonnull
  public abstract Icon getIcon(@Nonnull final MindMapPanel panel, @Nullable Topic actionTopic, @Nonnull @MustNotContainNull Topic[] selectedTopics);

}
