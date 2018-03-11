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

import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The Interface allows to define a custom processing for a menu pop-up item plug-in to replace its standard behavior.
 *
 * @see PopUpMenuItemPlugin
 * @since 1.2
 */
public interface CustomJob {
  /**
   * Business logic for the job.
   *
   * @param plugin         the plug-in calling the job.
   * @param panel          the panel
   * @param dialogProvider the dialog provider
   * @param topic          topic for the job
   * @param selectedTopics currently selected topics
   */
  void doJob(
      @Nonnull PopUpMenuItemPlugin plugin,
      @Nonnull MindMapPanel panel,
      @Nonnull DialogProvider dialogProvider,
      @Nullable Topic topic,
      @Nullable @MustNotContainNull Topic[] selectedTopics);

}
