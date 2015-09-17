/*
 * Copyright 2015 Igor Maznitsa.
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
package com.igormaznitsa.mindmap.swing.panel.utils;

import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.swing.panel.ui.AbstractCollapsableElement;
import com.igormaznitsa.mindmap.swing.panel.ui.AbstractElement;

public enum MindMapUtils {;
  public static final String ATTR_BORDER_COLOR = "borderColor";
  public static final String ATTR_FILL_COLOR = "fillColor";
  public static final String ATTR_TEXT_COLOR = "textColor";
  public static final String ATTR_COLLAPSED = "collapsed";

  public static boolean isHidden(final Topic topic) {
    if (topic == null) {
      return true;
    }
    final String collapsed = topic.findAttributeInAncestors(ATTR_COLLAPSED);
    return collapsed != null && Boolean.parseBoolean(collapsed);
  }

  public static Topic findFirstVisibleAncestor(final Topic topic) {
    if (topic == null) {
      return null;
    }

    final Topic[] path = topic.getPath();

    Topic lastVisible = null;

    if (path.length > 0) {
      for (final Topic t : path) {
        lastVisible = t;
        final boolean collapsed = Boolean.parseBoolean(t.getAttribute(ATTR_COLLAPSED));
        if (collapsed) {
          break;
        }
      }
    }

    return lastVisible;
  }

  public static boolean ensureVisibility(final Topic topic) {
    boolean result = false;

    Topic current = topic.getParent();
    while (current != null) {
      if (isCollapsed(current)){
        result |= setCollapsed(current, false);
      }
      current = current.getParent();
    }
    return result;
  }
  
  public static boolean isCollapsed(final Topic topic) {
    return "true".equalsIgnoreCase(topic.getAttribute(ATTR_COLLAPSED));//NOI18N
  }
  
  public static boolean setCollapsed(final Topic topic, final boolean flag) {
    return topic.setAttribute(ATTR_COLLAPSED, flag ? "true" : null);//NOI18N
  }
  
  public static void removeCollapseAttributeFromTopicsWithoutChildren(final MindMap map) {
    removeCollapseAttrIfNoChildren(map == null ? null : map.getRoot());
  }
  
  public static void removeCollapseAttrIfNoChildren(final Topic topic) {
    if (topic != null) {
      if (!topic.hasChildren()) {
        topic.setAttribute(ATTR_COLLAPSED, null);
      }
      else {
        for (final Topic t : topic.getChildren()) {
          removeCollapseAttrIfNoChildren(t);
        }
      }
    }
  }

  public static void copyColorAttributes(final Topic source, final Topic destination) {
    destination.setAttribute(ATTR_FILL_COLOR, source.getAttribute(ATTR_FILL_COLOR));
    destination.setAttribute(ATTR_BORDER_COLOR, source.getAttribute(ATTR_BORDER_COLOR));
    destination.setAttribute(ATTR_TEXT_COLOR, source.getAttribute(ATTR_TEXT_COLOR));
  }
  
}
