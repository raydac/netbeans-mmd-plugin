package com.igormaznitsa.mindmap.model.annotations;

/**
 * Color constants to be used for generated MMD topics.
 *
 * @see MmdTopic#colorFill()
 * @see MmdTopic#colorText()
 * @see MmdTopic#colorBorder()
 * @since 1.5.3
 */
public enum MmdColor {
  DEFAULT(""),
  BLACK("#000000"),
  SILVER("#C0C0C0"),
  GRAY("#808080"),
  WHITE("#FFFFFF"),
  MAROON("#800000"),
  RED("#FF0000"),
  PURPLE("#800080"),
  FUCHSIA("#FF00FF"),
  GREEN("#008000"),
  LIME("#00FF00"),
  OLIVE("#808000"),
  YELLOW("#FFFF00"),
  NAVY("#000080"),
  BLUE("#0000FF"),
  TEAL("#008080"),
  AQUA("#00FFFF");

  private final String htmlColor;

  MmdColor(final String htmlColor) {
    this.htmlColor = htmlColor;
  }

  public String getHtmlColor() {
    return this.htmlColor;
  }
}
