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

package com.igormaznitsa.mindmap.plugins.processors;

import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.plugins.PopUpSection;
import com.igormaznitsa.mindmap.plugins.api.AbstractFocusedTopicPlugin;
import com.igormaznitsa.mindmap.plugins.api.PluginContext;
import com.igormaznitsa.mindmap.swing.i18n.MmdI18n;
import com.igormaznitsa.mindmap.swing.panel.ui.AbstractElement;
import com.igormaznitsa.mindmap.swing.services.IconID;
import com.igormaznitsa.mindmap.swing.services.ImageIconServiceProvider;
import java.util.Objects;
import javax.swing.Icon;

public class EditTextPlugin extends AbstractFocusedTopicPlugin {

  private static final Icon ICO = ImageIconServiceProvider.findInstance().getIconForId(IconID.POPUP_EDIT_TEXT);

  @Override
  public int getOrder() {
    return 0;
  }

  @Override
  protected Icon getIcon(final PluginContext context, final Topic activeTopic) {
    return ICO;
  }

  @Override
  protected String getName(final PluginContext context, final Topic activeTopic) {
    return MmdI18n.getInstance().findBundle().getString("MMDGraphEditor.makePopUp.miEditText");
  }

  @Override
  protected void doActionForTopic(final PluginContext context, final Topic activeTopic) {
    if (activeTopic != null) {
      context.getPanel()
          .startEdit((AbstractElement) Objects.requireNonNull(activeTopic.getPayload()));
    }
  }

  @Override
  public PopUpSection getSection() {
    return PopUpSection.MAIN;
  }

  @Override
  public boolean isCompatibleWithFullScreenMode() {
    return true;
  }
}
