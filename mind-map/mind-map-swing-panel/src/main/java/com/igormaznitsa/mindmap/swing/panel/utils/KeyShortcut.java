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

package com.igormaznitsa.mindmap.swing.panel.utils;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;
import javax.swing.KeyStroke;

public final class KeyShortcut implements Serializable {

  private static final long serialVersionUID = -4263687011484460164L;

  public static final int ALL_MODIFIERS_MASK = KeyEvent.SHIFT_MASK | KeyEvent.CTRL_MASK | KeyEvent.ALT_MASK | KeyEvent.META_MASK;
  private String id;
  private int modifiers;
  private int keyCode;

  // needed for serialization
  public KeyShortcut() {
    this.id = "";
    this.modifiers = -1;
    this.keyCode = 0;
  }

  public KeyShortcut(final String packed) {
    final String[] split = packed.split("\\*");
    this.id = split[0];
    final long code = Long.parseLong(split[1], 16);
    this.keyCode = (int) (code >>> 32);
    this.modifiers = (int) code;
  }

  /**
   * Create key shortcut only for modifiers, as fake key char will be Intgeer.MAX_VALUE.
   * @param id identifier, must not be null
   * @param modifiers modifiers mask
   * @since 1.6.2
   */
  public KeyShortcut(final String id, final int modifiers) {
    this(id, Integer.MAX_VALUE, modifiers);
  }

  public KeyShortcut(final String id, final int keyCode, final int modifiers) {
    if (id.contains("*")) {
      throw new IllegalArgumentException("ID can't contain '*'");
    }
    this.id = id;
    this.modifiers = modifiers;
    this.keyCode = keyCode;
  }

  private static int preprocessCharKeyCode(final char keyChar) {
    final int result;
    switch (keyChar) {
      case 0xA0: {
        // non-breakable space as space
        result = KeyEvent.VK_SPACE;
      }
      break;
      case '+': {
        // Character '+' is 0x2b but VK_PLUS is 0x0209
        result = KeyEvent.VK_PLUS;
      }
      break;
      case 0x0D: {
        // Decode CR as ENTER
        result = KeyEvent.VK_ENTER;
      }
      break;
      default: {
        result = keyChar;
      }
    }
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    KeyShortcut that = (KeyShortcut) o;
    return modifiers == that.modifiers && keyCode == that.keyCode &&
        Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, modifiers, keyCode);
  }

  public boolean isCtrl() {
    return (this.modifiers & KeyEvent.CTRL_MASK) != 0;
  }

  public boolean isAlt() {
    return (this.modifiers & KeyEvent.ALT_MASK) != 0;
  }

  public boolean isShift() {
    return (this.modifiers & KeyEvent.SHIFT_MASK) != 0;
  }

  public boolean isMeta() {
    return (this.modifiers & KeyEvent.META_MASK) != 0;
  }

  /**
   * Get modifiers used by the shortcut.
   *
   * @return the modifier flags
   * @see KeyEvent#CTRL_MASK
   * @see KeyEvent#SHIFT_MASK
   * @see KeyEvent#ALT_MASK
   * @see KeyEvent#META_MASK
   */
  public int getModifiers() {
    return this.modifiers;
  }

  /**
   * Get the key code used by the shortcut.
   *
   * @return the key code
   * @see KeyEvent
   */
  public int getKeyCode() {
    return this.keyCode;
  }

  public String getID() {
    return this.id;
  }

  /**
   * Match only modifiers mask for input event. No any check for key code.
   * @param event input event, can be null
   * @return true if event id not null and modifiers mask matches.
   * @since 1.6.2
   */
  public boolean matchModifiers(final InputEvent event) {
    return event != null && (this.modifiers & event.getModifiers()) == this.modifiers;
  }

  public boolean isEvent(final KeyEvent event) {
    return this.isEvent(event, ALL_MODIFIERS_MASK);
  }

  public boolean isEvent(final KeyEvent event, final int modifiersMask) {
    if (this.isModifiersOnly()) {
      return (event.getModifiers() & modifiersMask) == (this.modifiers & modifiersMask);
    } else {
      final int code =
          event.getKeyCode() == 0 ? preprocessCharKeyCode(event.getKeyChar()) :
              event.getKeyCode();
      return code == this.keyCode &&
          (event.getModifiers() & modifiersMask) == (this.modifiers & modifiersMask);
    }
  }

  public String packToString() {
    final long packed = ((long) this.keyCode << 32) | ((long) this.modifiers & 0xFFFFFFFFL);
    return this.id + '*' + Long.toHexString(packed).toUpperCase(Locale.ENGLISH);
  }

  /**
   * Check that the shortcut plays only for modifiers.
   * @return true if only modifiers processed, false otherwise.
   * @since 1.6.2
   */
  public boolean isModifiersOnly() {
    return this.keyCode == Integer.MAX_VALUE;
  }

  @Override
  public String toString() {
    final String modifierText = KeyEvent.getKeyModifiersText(this.modifiers);
    final String keyText = this.isModifiersOnly() ? "" : KeyEvent.getKeyText(this.keyCode);

    final StringBuilder builder = new StringBuilder(modifierText);

    if (builder.length() > 0 && !keyText.isEmpty()) {
      builder.append('+');
    }
    builder.append(keyText);

    return builder.toString();
  }

  public boolean doesConflictWith(final KeyStroke stroke) {
    boolean result = false;
    if (stroke != null && !this.isModifiersOnly()) {
      result = stroke.getKeyCode() == this.keyCode &&
          (this.modifiers & stroke.getModifiers()) == this.modifiers;
    }
    return result;
  }

  public String getKeyCodeName() {
    return KeyEvent.getKeyText(this.keyCode);
  }
}
