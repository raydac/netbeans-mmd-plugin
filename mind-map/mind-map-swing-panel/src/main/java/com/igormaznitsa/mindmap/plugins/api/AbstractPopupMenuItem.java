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
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactory;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactoryProvider;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Abstract auxiliary class to implement an abstract pop-up menu item.
 *
 * @since 1.2
 */
public abstract class AbstractPopupMenuItem implements PopUpMenuItemPlugin {

  protected static final UIComponentFactory UI_COMPO_FACTORY = UIComponentFactoryProvider.findInstance();

  public AbstractPopupMenuItem() {
  }

  @Override
  public boolean equals(@Nullable final Object obj) {
    boolean result = false;
    if (obj instanceof AbstractPopupMenuItem) {
      result = this.getOrder() == ((AbstractPopupMenuItem) obj).getOrder();
    }
    return result;
  }

  @Override
  public int compareTo(@Nonnull final MindMapPlugin that) {
    if (this.getOrder() == that.getOrder()) {
      return 0;
    }
    if (this.getOrder() < that.getOrder()) {
      return -1;
    }
    return 1;
  }

  @Override
  public int hashCode() {
    return this.getClass().getName().hashCode() ^ (this.getOrder() << 7);
  }

  @Override
  public boolean isEnabled(@Nonnull final MindMapPanel panel, @Nullable final Topic topic, @Nonnull @MustNotContainNull final Topic[] selectedTopics) {
    return true;
  }

  @Override
  public boolean isCompatibleWithFullScreenMode() {
    return false;
  }
}
