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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.Locale;

public final class KeyShortcut {

  public static final int ALL_MODIFIERS_MASK = KeyEvent.SHIFT_MASK | KeyEvent.CTRL_MASK | KeyEvent.ALT_MASK | KeyEvent.META_MASK;
  private final String id;
  private final int modifiers;
  private final int keyCode;

  public KeyShortcut(@Nonnull final String packed) {
    final String[] split = packed.split("\\*");
    this.id = split[0];
    final long code = Long.parseLong(split[1], 16);
    this.keyCode = (int) (code >>> 32);
    this.modifiers = (int) code;
  }

  public KeyShortcut(@Nonnull final String id, final int keyCode, final int modifiers) {
    if (id.contains("*")) {
      throw new IllegalArgumentException("ID can't contain '*'");
    }
    this.id = id;
    this.modifiers = modifiers;
    this.keyCode = keyCode;
  }

  private static int preprocessCharKeyCode(final int modifiers, char keyChar) {
    final int result;
    switch (keyChar) {
      case 0xA0: {
        // non-breakable space as space
        result = KeyEvent.VK_SPACE;
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

  @Nonnull
  public String getID() {
    return this.id;
  }

  public boolean isEvent(@Nonnull final KeyEvent event) {
    return this.isEvent(event, ALL_MODIFIERS_MASK);
  }

  public boolean isEvent(@Nonnull final KeyEvent event, final int modifiersPlayingRole) {
    final int code = event.getKeyCode() == 0 ? preprocessCharKeyCode(event.getModifiers(), event.getKeyChar()) : event.getKeyCode();
    return code == this.keyCode && (event.getModifiers() & modifiersPlayingRole) == (this.modifiers & modifiersPlayingRole);
  }

  @Override
  public int hashCode() {
    return this.modifiers ^ this.keyCode;
  }

  @Override
  public boolean equals(@Nullable final Object object) {
    if (object == this) {
      return true;
    }
    if (object instanceof KeyShortcut) {
      final KeyShortcut that = (KeyShortcut) object;
      return this.id.equals(that.id) && this.keyCode == that.keyCode && this.modifiers == that.modifiers;
    }
    return false;
  }

  @Nonnull
  public String packToString() {
    final long packed = ((long) this.keyCode << 32) | ((long) this.modifiers & 0xFFFFFFFFL);
    return this.id + '*' + Long.toHexString(packed).toUpperCase(Locale.ENGLISH);
  }

  @Override
  @Nonnull
  public String toString() {
    final String modifierText = KeyEvent.getKeyModifiersText(this.modifiers);
    final String keyText = KeyEvent.getKeyText(this.keyCode);

    final StringBuilder builder = new StringBuilder(modifierText);

    if (builder.length() > 0 && !keyText.isEmpty()) {
      builder.append('+');
    }
    builder.append(keyText);

    return builder.toString();
  }

  public boolean doesConflictWith(@Nullable final KeyStroke stroke) {
    boolean result = false;
    if (stroke != null) {
      result = stroke.getKeyCode() == this.keyCode && (this.modifiers & stroke.getModifiers()) == this.modifiers;
    }
    return result;
  }

  @Nonnull
  public String getKeyCodeName() {
    return KeyEvent.getKeyText(this.keyCode);
  }
}
