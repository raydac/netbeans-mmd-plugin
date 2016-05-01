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
package com.igormaznitsa.mindmap.plugins.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.plugins.AbstractPopupMenuItemPlugin;
import com.igormaznitsa.mindmap.plugins.MindMapPopUpItemCustomProcessor;
import com.igormaznitsa.mindmap.plugins.PopUpSection;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.panel.Texts;
import com.igormaznitsa.mindmap.swing.services.IconID;
import com.igormaznitsa.mindmap.swing.services.ImageIconServiceProvider;

public class ChangeColorPlugin extends AbstractPopupMenuItemPlugin {

  private static final Icon ICO = ImageIconServiceProvider.findInstance().getIconForId(IconID.POPUP_CHANGECOLOR);

  
  @Override
  @Nullable
  public JMenuItem getPluginMenuItem(@Nonnull final MindMapPanel panel, @Nonnull final DialogProvider dialogProvider, @Nonnull final PopUpSection section, @Nullable final Topic topic, @Nonnull @MustNotContainNull final Topic[] selectedTopics, @Nullable final MindMapPopUpItemCustomProcessor customProcessor) {
    JMenuItem result = null;
    if (section == PopUpSection.TOOLS && (topic != null || selectedTopics.length>0)){
      result = UI_COMPO_FACTORY.makeMenuItem(
          selectedTopics.length > 0 ? 
          Texts.getString("MMDGraphEditor.makePopUp.miColorsForSelected"):
          Texts.getString("MMDGraphEditor.makePopUp.miColorsForTopic"), ICO);

      final ChangeColorPlugin theInstance = this;
      result.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(@Nonnull final ActionEvent e) {
          if (customProcessor!=null){
            customProcessor.doJobInsteadOfPlugin(theInstance, panel, dialogProvider, section, topic, selectedTopics);
          }
        }
      });
    }
    return result;
  }

  @Override
  public int getOrder() {
    return 4;
  }

}
