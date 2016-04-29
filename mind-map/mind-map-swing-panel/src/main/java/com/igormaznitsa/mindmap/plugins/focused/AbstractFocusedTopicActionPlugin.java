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
package com.igormaznitsa.mindmap.plugins.focused;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.plugins.AbstractPopupMenuItemPlugin;
import com.igormaznitsa.mindmap.plugins.MindMapPopUpItemCustomProcessor;
import com.igormaznitsa.mindmap.plugins.PopUpSection;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;

abstract class AbstractFocusedTopicActionPlugin extends AbstractPopupMenuItemPlugin {

  @Override
  @Nullable
  public JMenuItem getPluginMenuItem(
      @Nonnull final MindMapPanel panel,
      @Nonnull final DialogProvider dialogProvider,
      @Nonnull final PopUpSection section,
      @Nullable final Topic actionTopic,
      @Nonnull @MustNotContainNull final Topic[] selectedTopics,
      @Nullable final MindMapPopUpItemCustomProcessor customProcessor) {
    JMenuItem result = null;
    if (section == getPopUpSection() && isAllowed(panel, actionTopic, selectedTopics)) {
      result = UI_COMPO_FACTORY.makeMenuItem(getName(panel, actionTopic, selectedTopics), getIcon(panel, actionTopic, selectedTopics));
      result.setToolTipText(getReference());

      final AbstractFocusedTopicActionPlugin theInstance = this;

      result.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(@Nonnull final ActionEvent e) {
          if (customProcessor == null) {
            doActionForTopic(panel, dialogProvider, actionTopic, selectedTopics);
          } else {
            customProcessor.doJobInsteadOfPlugin(theInstance, panel, dialogProvider, section, actionTopic, selectedTopics);
          }
        }
      });
    }
    return result;
  }

  public boolean isAllowed(@Nonnull final MindMapPanel panel, @Nullable final Topic actionTopic, @Nonnull @MustNotContainNull final Topic[] selectedTopics) {
    return actionTopic != null;
  }

  @Nonnull
  protected abstract PopUpSection getPopUpSection();
  
  @Nonnull
  protected abstract ImageIcon getIcon(@Nonnull MindMapPanel panel, @Nullable Topic actionTopic, @Nonnull @MustNotContainNull Topic[] selectedTopics);

  @Nonnull
  protected abstract String getName(@Nonnull MindMapPanel panel, @Nullable Topic actionTopic, @Nonnull @MustNotContainNull Topic[] selectedTopics);

  @Nonnull
  protected String getReference() {
    return null;
  }

  protected abstract void doActionForTopic(@Nonnull MindMapPanel panel, @Nonnull DialogProvider dialogProvider, @Nullable Topic actionTopic, @Nonnull @MustNotContainNull Topic[] selectedTopics);

}
