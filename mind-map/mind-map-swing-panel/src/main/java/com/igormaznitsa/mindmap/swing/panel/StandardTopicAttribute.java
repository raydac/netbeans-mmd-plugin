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

package com.igormaznitsa.mindmap.swing.panel;

import com.igormaznitsa.mindmap.model.Topic;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public enum StandardTopicAttribute {
  ATTR_BORDER_COLOR("borderColor"),
  ATTR_FILL_COLOR("fillColor"),
  ATTR_TEXT_COLOR("textColor"),
  ATTR_LEFTSIDE("leftSide"),
  ATTR_COLLAPSED("collapsed");

  private final String textName;

  private StandardTopicAttribute(@Nonnull final String textName) {
    this.textName = textName;
  }

  @Nullable
  public static StandardTopicAttribute findForText(@Nullable final String text) {
    for (final StandardTopicAttribute s : values()) {
      if (s.getText().equals(text)) {
        return s;
      }
    }
    return null;
  }

  public static boolean doesContainOnlyStandardAttributes(@Nonnull final Topic topic) {
    final Map<String, String> attrs = topic.getAttributes();
    for (final String k : attrs.keySet()) {
      if (findForText(k) == null) {
        return false;
      }
    }
    return true;
  }

  @Nonnull
  public String getText() {
    return this.textName;
  }
}
