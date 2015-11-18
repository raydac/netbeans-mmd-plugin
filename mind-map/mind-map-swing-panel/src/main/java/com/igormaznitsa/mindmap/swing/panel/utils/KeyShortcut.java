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

import java.awt.event.KeyEvent;
import java.util.Locale;

public final class KeyShortcut {
  private final String id;
  private final int modifiers;
  private final int keyCode;
  
  private static final int ALL_MASKS = KeyEvent.SHIFT_MASK | KeyEvent.CTRL_MASK | KeyEvent.ALT_MASK | KeyEvent.META_MASK;
  
  public KeyShortcut(final String packed){
    final String [] split = packed.split("\\*");
    this.id = split[0];
    final long code = Long.parseLong(split[1], 16);
    this.keyCode = (int)(code >>> 32);
    this.modifiers = (int)code;
  }
  
  public KeyShortcut(final String id, final int keyCode, final int modifiers){
    if (id.contains("*")) throw new IllegalArgumentException("ID can't contain '*'");
    this.id = id;
    this.modifiers = modifiers;
    this.keyCode = keyCode;
  }
  
  /**
   * Get modifiers used by the shortcut.
   * @return the modifier flags
   * @see KeyEvent#CTRL_MASK
   * @see KeyEvent#SHIFT_MASK
   * @see KeyEvent#ALT_MASK
   * @see KeyEvent#META_MASK
   */
  public int getModifiers(){
    return this.modifiers;
  }
  
  /**
   * Get the key code used by the shortcut.
   * @return the key code
   * @see KeyEvent
   */
  public int getKeyCode(){
    return this.keyCode;
  }
  
  public String getID(){
    return this.id;
  }
  
  public boolean isEvent(final KeyEvent event){
    final int code = event.getKeyCode() == 0 ? event.getKeyChar() : event.getKeyCode();
    return code == this.keyCode && (event.getModifiers() & ALL_MASKS & this.modifiers) == this.modifiers;
  }
  
  @Override
  public int hashCode(){
    return this.modifiers ^ this.keyCode;
  }
  
  @Override
  public boolean equals(final Object object){
    if (object == null) return false;
    if (object == this) return true;
    if (object instanceof KeyShortcut){
      final KeyShortcut that = (KeyShortcut) object;
      return this.id.equals(that.id) && this.keyCode == that.keyCode && this.modifiers == that.modifiers;
    }
    return false;
  }
  
  public String packToString(){
    final long packed = ((long)this.keyCode<<32) | ((long)this.modifiers & 0xFFFFFFFFL);
    return this.id+'*'+Long.toHexString(packed).toUpperCase(Locale.ENGLISH);
  }
  
  @Override
  public String toString(){
    final String modifierText = KeyEvent.getKeyModifiersText(this.modifiers);
    final String keyText = KeyEvent.getKeyText(this.modifiers);
    final StringBuilder builder = new StringBuilder();
    builder.append(keyText);
    if (!modifierText.isEmpty()){
      builder.append('+').append(modifierText);
    }
        
    return builder.toString();
  }
}
