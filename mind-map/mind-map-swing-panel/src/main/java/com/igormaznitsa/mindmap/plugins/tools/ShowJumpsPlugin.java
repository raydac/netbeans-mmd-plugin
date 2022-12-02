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

package com.igormaznitsa.mindmap.plugins.tools;

import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.plugins.PopUpSection;
import com.igormaznitsa.mindmap.plugins.api.AbstractPopupMenuItem;
import com.igormaznitsa.mindmap.plugins.api.PluginContext;
import com.igormaznitsa.mindmap.swing.services.IconID;
import com.igormaznitsa.mindmap.swing.services.ImageIconServiceProvider;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;

public class ShowJumpsPlugin extends AbstractPopupMenuItem {

  private static final Icon ICO = ImageIconServiceProvider.findInstance().getIconForId(IconID.POPUP_SHOWJUMPS);

  @Override
  public JMenuItem makeMenuItem(final PluginContext context, final Topic topic) {
    JMenuItem result = UI_COMPO_FACTORY.makeCheckboxMenuItem(
        this.getResourceBundle().getString("MMDGraphEditor.makePopUp.miShowJumps"), ICO,
        context.getPanel().isShowJumps());
    result.addActionListener(
        e -> context.getPanel().setShowJumps(((JCheckBoxMenuItem) e.getSource()).isSelected()));
    return result;
  }

  @Override
  public PopUpSection getSection() {
    return PopUpSection.MANIPULATORS;
  }

  @Override
  public boolean needsTopicUnderMouse() {
    return false;
  }

  @Override
  public boolean needsSelectedTopics() {
    return false;
  }

  @Override
  public int getOrder() {
    return 4;
  }

  @Override
  public boolean isCompatibleWithFullScreenMode() {
    return true;
  }

}
