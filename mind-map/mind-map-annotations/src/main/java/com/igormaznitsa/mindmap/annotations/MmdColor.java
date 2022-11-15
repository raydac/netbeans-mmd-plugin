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
