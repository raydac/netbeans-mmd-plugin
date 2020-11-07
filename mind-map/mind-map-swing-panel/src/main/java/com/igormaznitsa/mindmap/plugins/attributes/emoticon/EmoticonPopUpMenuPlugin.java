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

import static com.igormaznitsa.mindmap.swing.panel.utils.Utils.safeObjectEquals;


import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.plugins.PopUpSection;
import com.igormaznitsa.mindmap.plugins.api.AbstractPopupMenuItem;
import com.igormaznitsa.mindmap.plugins.api.PluginContext;
import com.igormaznitsa.mindmap.swing.services.IconID;
import com.igormaznitsa.mindmap.swing.services.ImageIconServiceProvider;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;

public class EmoticonPopUpMenuPlugin extends AbstractPopupMenuItem {

  private static final ResourceBundle BUNDLE = java.util.ResourceBundle.getBundle("com/igormaznitsa/mindmap/swing/panel/Bundle");
  private static final Icon ICON = ImageIconServiceProvider.findInstance().getIconForId(IconID.ICON_EMOTICONS);

  @Override
  @Nullable
  public JMenuItem makeMenuItem(@Nonnull final PluginContext context, @Nullable final Topic activeTopic) {
    final JMenuItem result = UI_COMPO_FACTORY.makeMenuItem(BUNDLE.getString("Emoticons.MenuTitle"), ICON);
    result.setToolTipText(BUNDLE.getString("Emoticons.MenuTooltip"));
    result.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(@Nonnull final ActionEvent e) {
        final IconPanel iconPanel = new IconPanel(context);
        final JScrollPane scrollPane = UI_COMPO_FACTORY.makeScrollPane();
        scrollPane.getVerticalScrollBar().setUnitIncrement(32);
        scrollPane.getVerticalScrollBar().setBlockIncrement(96);
        scrollPane.setPreferredSize(new Dimension(512, 400));
        scrollPane.setViewportView(iconPanel);
        if (context.getDialogProvider().msgOkCancel(null, BUNDLE.getString("Emoticons.DialogTitle"), scrollPane)) {
          final String emoticonName = iconPanel.getSelectedName();
          if (emoticonName != null) {
            final boolean changed;
            if ("empty".equals(emoticonName)) {
              changed = setAttribute(null, context, activeTopic);
            } else {
              changed = setAttribute(emoticonName, context, activeTopic);
            }
            if (changed) {
              context.getPanel().doNotifyModelChanged(true);
            }
          }
        }
      }
    });
    return result;
  }

  private boolean setAttribute(@Nullable final String value, @Nonnull final PluginContext context, @Nullable final Topic activeTopic) {
    boolean changed = false;
    if (activeTopic != null) {
      final String old = activeTopic.getAttribute(EmoticonVisualAttributePlugin.ATTR_KEY);
      if (!safeObjectEquals(old, value)) {
        activeTopic.setAttribute(EmoticonVisualAttributePlugin.ATTR_KEY, value);
        changed = true;
      }
    }
    for (final Topic t : context.getSelectedTopics()) {
      final String old = t.getAttribute(EmoticonVisualAttributePlugin.ATTR_KEY);
      if (!safeObjectEquals(old, value)) {
        t.setAttribute(EmoticonVisualAttributePlugin.ATTR_KEY, value);
        changed = true;
      }
    }
    return changed;
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
