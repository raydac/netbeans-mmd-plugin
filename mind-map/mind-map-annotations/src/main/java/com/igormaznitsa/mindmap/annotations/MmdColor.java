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

package com.igormaznitsa.mindmap.annotations;

import java.util.NoSuchElementException;

/**
 * Color constants to be used for generated MMD topics.
 *
 * @see MmdTopic#colorFill()
 * @see MmdTopic#colorText()
 * @see MmdTopic#colorBorder()
 */
public enum MmdColor {
  /**
   * Default color
   */
  DEFAULT(""),
  /**
   * Black
   */
  BLACK("#000000"),
  /**
   * Silver
   */
  SILVER("#C0C0C0"),
  /**
   * Gray
   */
  GRAY("#808080"),
  /**
   * White
   */
  WHITE("#FFFFFF"),
  /**
   * Maroon
   */
  MAROON("#800000"),
  /**
   * Red
   */
  RED("#FF0000"),
  /**
   * Purple
   */
  PURPLE("#800080"),
  /**
   * Fuchsia
   */
  FUCHSIA("#FF00FF"),
  /**
   * Green
   */
  GREEN("#008000"),
  /**
   * Lime
   */
  LIME("#00FF00"),
  /**
   * Olive
   */
  OLIVE("#808000"),
  /**
   * Yellow
   */
  YELLOW("#FFFF00"),
  /**
   * Navy
   */
  NAVY("#000080"),
  /**
   * Blue
   */
  BLUE("#0000FF"),
  /**
   * Teal
   */
  TEAL("#008080"),
  /**
   * Aqua
   */
  AQUA("#00FFFF"),
  /**
   * Orange
   */
  ORANGE("#FFA500"),
  /**
   * Dark orange
   */
  DARK_ORANG("#FF8C00"),
  /**
   * Coral
   */
  CORAL("#FF7F50"),
  /**
   * Tomato
   */
  TOMATO("#FF6347");

  private final String htmlColor;

  MmdColor(final String htmlColor) {
    this.htmlColor = htmlColor;
  }

  public String getHtmlColor() {
    return this.htmlColor;
  }

  public static MmdColor findForHtmlColor(final String htmlColor) {
    if (htmlColor == null || htmlColor.isEmpty()) {
      return DEFAULT;
    }
    for (final MmdColor color : MmdColor.values()) {
      if (color.htmlColor.equalsIgnoreCase(htmlColor)) {
        return color;
      }
    }
    throw new NoSuchElementException("There is no color enum value for " + htmlColor);
  }
}
