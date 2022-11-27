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

package com.igormaznitsa.mindmap.swing.panel;

import com.igormaznitsa.mindmap.model.StandardTopicAttributes;
import com.igormaznitsa.mindmap.model.Topic;
import java.util.Map;

public enum StandardTopicAttribute implements StandardTopicAttributes {
  ATTR_BORDER_COLOR(MMD_TOPIC_ATTRIBUTE_COLOR_BORDER),
  ATTR_FILL_COLOR(MMD_TOPIC_ATTRIBUTE_COLOR_FILL),
  ATTR_TEXT_COLOR(MMD_TOPIC_ATTRIBUTE_COLOR_TEXT),
  ATTR_LEFTSIDE(MMD_TOPIC_ATTRIBUTE_SIDE_LEFT),
  ATTR_COLLAPSED(MMD_TOPIC_ATTRIBUTE_COLLAPSED);

  private final String textName;

  StandardTopicAttribute(final String textName) {
    this.textName = textName;
  }

  public static StandardTopicAttribute findForText(final String text) {
    for (final StandardTopicAttribute s : values()) {
      if (s.getText().equals(text)) {
        return s;
      }
    }
    return null;
  }

  public static boolean doesContainOnlyStandardAttributes(final Topic topic) {
    final Map<String, String> attrs = topic.getAttributes();
    for (final String k : attrs.keySet()) {
      if (findForText(k) == null) {
        return false;
      }
    }
    return true;
  }

  public String getText() {
    return this.textName;
  }
}
