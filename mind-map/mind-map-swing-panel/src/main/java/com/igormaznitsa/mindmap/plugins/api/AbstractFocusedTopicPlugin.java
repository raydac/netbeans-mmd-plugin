/*
 * Copyright 2015-2018 Igor Maznitsa.
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

import com.igormaznitsa.mindmap.model.Topic;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.Icon;
import javax.swing.JMenuItem;

/**
 * Auxiliary class to create plug-ins working with selected topic.
 *
 * @since 1.2
 */
public abstract class AbstractFocusedTopicPlugin extends AbstractPopupMenuItem {

  @Override
  @Nullable
  public JMenuItem makeMenuItem(
      @Nonnull final PluginContext context,
      @Nullable final Topic activeTopic,
      @Nullable final CustomJob customProcessor) {

    final JMenuItem result = UI_COMPO_FACTORY.makeMenuItem(getName(context, activeTopic), getIcon(context, activeTopic));

    result.setToolTipText(getReference());

    final AbstractFocusedTopicPlugin theInstance = this;

    result.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(@Nonnull final ActionEvent e) {
        if (customProcessor == null) {
          doActionForTopic(context, activeTopic);
        } else {
          customProcessor.doJob(context, theInstance);
        }
      }
    });
    return result;
  }

  @Override
  public boolean needsTopicUnderMouse() {
    return true;
  }

  @Override
  public boolean needsSelectedTopics() {
    return false;
  }

  @Nonnull
  protected abstract Icon getIcon(@Nonnull PluginContext context, @Nullable Topic activeTopic);

  @Nonnull
  protected abstract String getName(@Nonnull PluginContext context, @Nullable Topic activeTopic);

  @Nullable
  protected String getReference() {
    return null;
  }

  @Override
  public boolean isEnabled(@Nonnull final PluginContext context, @Nullable final Topic activeTopic) {
    return context.getSelectedTopics().length == 1 || (context.getSelectedTopics().length == 0 && activeTopic != null);
  }

  protected abstract void doActionForTopic(@Nonnull PluginContext context, @Nullable Topic actionTopic);

}
