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

package com.igormaznitsa.mindmap.plugins.attributes.emoticon;

import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.plugins.PopUpSection;
import com.igormaznitsa.mindmap.plugins.api.AbstractPopupMenuItem;
import com.igormaznitsa.mindmap.plugins.api.CustomJob;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.services.IconID;
import com.igormaznitsa.mindmap.swing.services.ImageIconServiceProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

public class EmoticonPopUpMenuPlugin extends AbstractPopupMenuItem {

  private static final ResourceBundle BUNDLE = java.util.ResourceBundle.getBundle("com/igormaznitsa/mindmap/swing/panel/Bundle");
  private static final Icon ICON = ImageIconServiceProvider.findInstance().getIconForId(IconID.ICON_EMOTICONS);

  @Override
  @Nullable
  public JMenuItem makeMenuItem(@Nonnull final MindMapPanel panel, @Nonnull final DialogProvider dialogProvider, @Nullable final Topic topic, @Nonnull @MustNotContainNull final Topic[] selectedTopics, @Nullable final CustomJob customProcessor) {
    final JMenuItem result = new JMenuItem(BUNDLE.getString("Emoticons.MenuTitle"), ICON);
    result.setToolTipText(BUNDLE.getString("Emoticons.MenuTooltip"));
    result.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(@Nonnull final ActionEvent e) {
        final IconPanel iconPanel = new IconPanel();
        final JScrollPane scrollPane = UI_COMPO_FACTORY.makeScrollPane();
        scrollPane.setPreferredSize(new Dimension(512, 400));
        scrollPane.setViewportView(iconPanel);
        if (dialogProvider.msgOkCancel(null, BUNDLE.getString("Emoticons.DialogTitle"), scrollPane)) {
          final String emoticonName = iconPanel.getSelectedName();
          if (emoticonName != null) {
            if ("empty".equals(emoticonName)) {
              setAttribute(null, topic, selectedTopics);
            } else {
              setAttribute(emoticonName, topic, selectedTopics);
            }
          }
          panel.notifyModelChanged();
        }
      }
    });
    return result;
  }

  private void setAttribute(@Nullable final String value, @Nullable final Topic topic, @Nonnull @MustNotContainNull final Topic[] topics) {
    if (topic != null) {
      topic.setAttribute(EmoticonVisualAttributePlugin.ATTR_KEY, value);
    }
    for (final Topic t : topics) {
      t.setAttribute(EmoticonVisualAttributePlugin.ATTR_KEY, value);
    }
  }

  @Override
  @Nonnull
  public PopUpSection getSection() {
    return PopUpSection.EXTRAS;
  }

  @Override
  public boolean needsTopicUnderMouse() {
    return true;
  }

  @Override
  public boolean needsSelectedTopics() {
    return true;
  }

  @Override
  public int getOrder() {
    return CUSTOM_PLUGIN_START - 1;
  }

}
