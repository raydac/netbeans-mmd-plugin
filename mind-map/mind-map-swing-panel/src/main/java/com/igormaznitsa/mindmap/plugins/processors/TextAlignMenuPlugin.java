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

package com.igormaznitsa.mindmap.plugins.processors;

import com.igormaznitsa.mindmap.model.StandardTopicAttributes;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.plugins.PopUpSection;
import com.igormaznitsa.mindmap.plugins.api.AbstractPopupMenuItem;
import com.igormaznitsa.mindmap.plugins.api.PluginContext;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.panel.ui.TextAlign;
import com.igormaznitsa.mindmap.swing.services.IconID;
import com.igormaznitsa.mindmap.swing.services.ImageIconServiceProvider;
import java.util.Locale;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import org.apache.commons.lang3.ArrayUtils;

public class TextAlignMenuPlugin extends AbstractPopupMenuItem {

  private static final Icon ICON =
      ImageIconServiceProvider.findInstance().getIconForId(IconID.ICON_TEXT_ALIGN);
  private static final Icon ICON_CENTER =
      ImageIconServiceProvider.findInstance().getIconForId(IconID.ICON_TEXT_ALIGN_CENTER);
  private static final Icon ICON_LEFT =
      ImageIconServiceProvider.findInstance().getIconForId(IconID.ICON_TEXT_ALIGN_LEFT);
  private static final Icon ICON_RIGHT =
      ImageIconServiceProvider.findInstance().getIconForId(IconID.ICON_TEXT_ALIGN_RIGHT);
  @Override
  public JMenuItem makeMenuItem(final PluginContext context, final Topic activeTopic) {
    final JMenu result = UI_COMPO_FACTORY.makeMenu(this.getResourceBundle().getString("TextAlign.Plugin.MenuTitle"));
    result.setIcon(ICON);

    final ButtonGroup buttonGroup = UI_COMPO_FACTORY.makeButtonGroup();

    final Topic[] workTopics;
    if (activeTopic == null) {
      workTopics = context.getSelectedTopics();
    } else {
      workTopics = ArrayUtils.addFirst(context.getSelectedTopics(), activeTopic);
    }

    final TextAlign sharedTextAlign = findSharedTextAlign(workTopics);

    final JRadioButtonMenuItem menuLeft = UI_COMPO_FACTORY.makeRadioButtonMenuItem(
        this.getResourceBundle().getString("TextAlign.Plugin.MenuTitle.Left"), ICON_LEFT,
        TextAlign.LEFT == sharedTextAlign);
    final JRadioButtonMenuItem menuCenter = UI_COMPO_FACTORY.makeRadioButtonMenuItem(
        this.getResourceBundle().getString("TextAlign.Plugin.MenuTitle.Center"), ICON_CENTER,
        TextAlign.CENTER == sharedTextAlign);
    final JRadioButtonMenuItem menuRight = UI_COMPO_FACTORY.makeRadioButtonMenuItem(
        this.getResourceBundle().getString("TextAlign.Plugin.MenuTitle.Right"), ICON_RIGHT,
        TextAlign.RIGHT == sharedTextAlign);

    buttonGroup.add(menuLeft);
    buttonGroup.add(menuCenter);
    buttonGroup.add(menuRight);

    result.add(menuLeft);
    result.add(menuCenter);
    result.add(menuRight);

    menuLeft.addActionListener(e -> setAlignValue(context.getPanel(), workTopics, TextAlign.LEFT));

    menuCenter.addActionListener(
        e -> setAlignValue(context.getPanel(), workTopics, TextAlign.CENTER));

    menuRight.addActionListener(
        e -> setAlignValue(context.getPanel(), workTopics, TextAlign.RIGHT));

    return result;
  }

  private void setAlignValue(final MindMapPanel panel, final Topic[] topics,
                             final TextAlign align) {
    for (final Topic t : topics) {
      t.putAttribute(StandardTopicAttributes.MMD_TOPIC_ATTRIBUTE_TITLE_ALIGN,
          align.name().toLowerCase(Locale.ENGLISH));
    }
    panel.doNotifyModelChanged(true);
  }

  private TextAlign findSharedTextAlign(final Topic[] topics) {
    TextAlign result = null;

    for (final Topic t : topics) {
      final TextAlign topicAlign = TextAlign.findForName(
          t.getAttribute(StandardTopicAttributes.MMD_TOPIC_ATTRIBUTE_TITLE_ALIGN));
      if (result == null) {
        result = topicAlign;
      } else if (result != topicAlign) {
        return null;
      }
    }

    return result == null ? TextAlign.CENTER : result;
  }

  @Override
  public PopUpSection getSection() {
    return PopUpSection.MAIN;
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
    return 10;
  }

}
