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
package com.igormaznitsa.mindmap.plugins;

import java.awt.event.ActionListener;
import javax.annotation.Nonnull;
import com.igormaznitsa.meta.annotation.Weight;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactory;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactoryProvider;

public abstract class AbstractPopupMenuItemPlugin implements PopUpMenuItemPlugin {

  public static final int CUSTOM_PLUGIN_START = 1000;
  
  protected static final UIComponentFactory UI_COMPO_FACTORY = UIComponentFactoryProvider.findInstance();
  
  public AbstractPopupMenuItemPlugin() {
  }

  @Weight(Weight.Unit.NORMAL)
  @Override
  public void onModelSet(@Nonnull final MindMapPanel panel, @Nonnull final Topic topic){
  }
  
  @Override
  public int compareTo(@Nonnull final MindMapPlugin that) {
    if (this.getOrder() == that.getOrder()) return 0;
    if (this.getOrder()<that.getOrder()) return -1;
    return 1;
  }

}
