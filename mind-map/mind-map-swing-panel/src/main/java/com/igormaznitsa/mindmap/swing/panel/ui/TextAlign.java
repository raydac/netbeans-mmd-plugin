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

package com.igormaznitsa.mindmap.swing.panel.ui;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum TextAlign {
  LEFT, RIGHT, CENTER;

  public static final List<TextAlign> VALUES = Collections.unmodifiableList(Arrays.asList(TextAlign.values()));

  public static TextAlign findForName(final String text) {
    if (text == null) {
      return CENTER;
    }
    return VALUES.stream()
        .filter(x -> x.name().equalsIgnoreCase(text))
        .findFirst()
        .orElse(CENTER);
  }
}
