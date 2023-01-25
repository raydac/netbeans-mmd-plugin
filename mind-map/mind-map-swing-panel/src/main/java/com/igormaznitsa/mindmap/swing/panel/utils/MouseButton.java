/*
 * Copyright (C) 2015-2023 Igor A. Maznitsa
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

package com.igormaznitsa.mindmap.swing.panel.utils;

import java.awt.event.MouseEvent;

public enum MouseButton {
  BUTTON_1,
  BUTTON_2,
  BUTTON_3;

  public boolean match(final MouseEvent event) {
    switch (this) {
      case BUTTON_1: return event.getButton() == MouseEvent.BUTTON1;
      case BUTTON_2: return event.getButton() == MouseEvent.BUTTON2;
      case BUTTON_3: return event.getButton() == MouseEvent.BUTTON3;
      default: throw new Error("Unexpected error");
    }
  }
}
