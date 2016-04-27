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

import java.awt.Image;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.JPopupMenu;
import com.igormaznitsa.meta.annotation.Weight;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;

public abstract class AbstractPlugin implements Comparable<AbstractPlugin> {

  public AbstractPlugin() {
  }

  @Weight(Weight.Unit.LIGHT)
  public void doPrepareMenu(@Nonnull final MindMapPanel panel, @Nonnull final JPopupMenu menu, @Nullable final Topic topic) {
  }

  @Weight(Weight.Unit.NORMAL)
  public void doActivate(@Nonnull final MindMapPanel panel, @Nonnull final Topic topic) {
  }

  @Nullable
  @Weight(Weight.Unit.LIGHT)
  public Image doDraw(@Nonnull final MindMapPanel panel, @Nonnull final Topic topic){
    return null;
  }

  @Weight(Weight.Unit.NORMAL)
  public void onModelSet(@Nonnull final MindMapPanel panel, @Nonnull final Topic topic){
  }
  
  public int getOrder(){
    return 0;
  }

  @Override
  public int compareTo(@Nonnull final AbstractPlugin that) {
    if (this.getOrder() == that.getOrder()) return 0;
    if (this.getOrder()<that.getOrder()) return -1;
    return 1;
  }
}
