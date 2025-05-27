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

import static java.util.Objects.requireNonNull;

import com.igormaznitsa.mindmap.model.MiscUtils;
import com.igormaznitsa.mindmap.swing.panel.utils.KeyShortcut;
import com.igormaznitsa.mindmap.swing.panel.utils.MouseButton;
import com.igormaznitsa.mindmap.swing.panel.utils.RenderQuality;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Stream;
import javax.swing.KeyStroke;
import org.apache.commons.lang3.SystemUtils;

public final class MindMapPanelConfig implements Serializable {

  public static final String KEY_ADD_CHILD_AND_START_EDIT = "addChildAndStartEdit";
  public static final String KEY_ADD_SIBLING_AND_START_EDIT = "addSiblingAndStartEdit";
  public static final String KEY_FOCUS_ROOT_OR_START_EDIT = "focusToRootOrStartEdit";
  public static final String KEY_CANCEL_EDIT = "cancelEdit";
  public static final String KEY_TOPIC_FOLD = "topicFold";
  public static final String KEY_TOPIC_FOLD_ALL = "topicFoldAll";
  public static final String KEY_TOPIC_UNFOLD = "topicUnfold";
  public static final String KEY_TOPIC_UNFOLD_ALL = "topicUnfoldAll";
  public static final String KEY_FOCUS_MOVE_UP = "moveFocusUp";
  public static final String KEY_FOCUS_MOVE_DOWN = "moveFocusDown";
  public static final String KEY_FOCUS_MOVE_LEFT = "moveFocusLeft";
  public static final String KEY_FOCUS_MOVE_RIGHT = "moveFocusRight";
  public static final String KEY_FOCUS_MOVE_UP_ADD_FOCUSED = "moveFocusUpAddFocused";
  public static final String KEY_FOCUS_MOVE_DOWN_ADD_FOCUSED = "moveFocusDownAddFocused";
  public static final String KEY_FOCUS_MOVE_LEFT_ADD_FOCUSED = "moveFocusLeftAddFocused";
  public static final String KEY_FOCUS_MOVE_RIGHT_ADD_FOCUSED = "moveFocusRightAddFocused";
  public static final String KEY_DELETE_TOPIC = "deleteSelectedTopic";
  public static final String KEY_TOPIC_TEXT_NEXT_LINE = "nextLineInTopicText";
  public static final String KEY_ZOOM_IN = "zoomIn";
  public static final String KEY_ZOOM_OUT = "zoomOut";
  public static final String KEY_ZOOM_RESET = "zoomReset";
  public static final String KEY_SHOW_POPUP = "showPopupMenu";
  public static final String KEY_BIRDSEYE_MODIFIERS = "birdsEyeModifiers";
  public static final String PREFIX_OPTIONAL_PROPERTY = "optionalProperty.";
  public static final String PREFIX_SHORTCUT = "mapShortCut.";
  private static final long serialVersionUID = -4263687011484460064L;
  private transient final List<WeakReference<MindMapConfigListener>> listeners =
      new CopyOnWriteArrayList<>();
  private final Serializable NULL_OPTIONAL_OBJECT = "some_null_object";
  private Map<String, KeyShortcut> mapShortCut;
  private Map<String, Serializable> optionalProperties;
  private int collapsatorSize = 16;
  private int textMargins = 10;
  private int otherLevelVerticalInset = 16;
  private int otherLevelHorizontalInset = 32;
  private int firstLevelVerticalInset = 32;
  private int firstLevelHorizontalInset = 48;
  private int paperMargins = 20;
  private int selectLineGap = 5;
  private int horizontalBlockGap = 5;
  private int scaleModifiers = KeyEvent.CTRL_MASK;
  private boolean drawBackground = true;
  private Color paperColor = new Color(0x617B94);
  private Color gridColor = paperColor.darker();
  private boolean showGrid = true;
  private int gridStep = 32;
  private Color rootBackgroundColor = new Color(0x031A31);
  private Color firstLevelBackgroundColor = new Color(0xB1BFCC);
  private Color otherLevelBackgroundColor = new Color(0xFDFDFD);
  private Color rootTextColor = Color.WHITE;
  private Color firstLevelTextColor = Color.BLACK;
  private Color otherLevelTextColor = Color.BLACK;
  private Color elementBorderColor = Color.BLACK;
  private Color connectorColor = Color.WHITE;
  private Color shadowColor = new Color(0x30000000, true);
  private Color collapsatorBorderColor = Color.DARK_GRAY;
  private Color collapsatorBackgroundColor = Color.WHITE;
  private Color selectLineColor = Color.ORANGE;
  private Color jumpLinkColor = Color.CYAN;
  private Color birdseyeBackground = new Color(0x9090C8FF, true);
  private Color birdseyeFront = new Color(0x90004A94, true);
  private float shadowOffset = 5.0f;
  private float elementBorderWidth = 1.0f;
  private float collapsatorBorderWidth = 1.0f;
  private float connectorWidth = 1.5f;
  private float selectLineWidth = 3.0f;
  private float jumpLinkWidth = 1.5f;
  private boolean smartTextPaste = false;
  private boolean noEscape = false;
  private Font font;
  private double scale = 1.0d;
  private boolean dropShadow = true;
  private RenderQuality renderQuality = Utils.getDefaultRenderQialityForOs();
  private MouseButton birdseyeMouseButton = MouseButton.BUTTON_3;
  private transient volatile boolean notificationEnabled = true;


  public MindMapPanelConfig(final MindMapPanelConfig cfg, final boolean copyListeners) {
    this();
    this.makeFullCopyOf(cfg, copyListeners, false);
  }

  public MindMapPanelConfig() {
    this.mapShortCut = new HashMap<>();
    this.optionalProperties = new HashMap<>();
    this.font =
        findDefaultFont(Font.PLAIN, 18, Font.MONOSPACED, new String[] {"JetBrains Mono SemiBold"});
    if (SystemUtils.IS_OS_MAC) {
      // key map for MAC
      this.mapShortCut.put(KEY_ADD_CHILD_AND_START_EDIT,
          new KeyShortcut(KEY_ADD_CHILD_AND_START_EDIT, KeyEvent.VK_TAB, 0));
      this.mapShortCut.put(KEY_ADD_SIBLING_AND_START_EDIT,
          new KeyShortcut(KEY_ADD_SIBLING_AND_START_EDIT, KeyEvent.VK_ENTER, 0));
      this.mapShortCut
          .put(KEY_CANCEL_EDIT, new KeyShortcut(KEY_CANCEL_EDIT, KeyEvent.VK_ESCAPE, 0));
      this.mapShortCut.put(KEY_TOPIC_FOLD, new KeyShortcut(KEY_TOPIC_FOLD, KeyEvent.VK_MINUS, 0));
      this.mapShortCut.put(KEY_TOPIC_FOLD_ALL,
          new KeyShortcut(KEY_TOPIC_FOLD_ALL, KeyEvent.VK_MINUS, KeyEvent.ALT_MASK));
      this.mapShortCut
          .put(KEY_TOPIC_UNFOLD, new KeyShortcut(KEY_TOPIC_UNFOLD, KeyEvent.VK_EQUALS, 0));
      this.mapShortCut.put(KEY_TOPIC_UNFOLD_ALL,
          new KeyShortcut(KEY_TOPIC_UNFOLD_ALL, KeyEvent.VK_EQUALS, KeyEvent.ALT_MASK));
      this.mapShortCut.put(KEY_FOCUS_ROOT_OR_START_EDIT,
          new KeyShortcut(KEY_FOCUS_ROOT_OR_START_EDIT, KeyEvent.VK_SPACE, KeyEvent.ALT_MASK));
      this.mapShortCut
          .put(KEY_FOCUS_MOVE_DOWN, new KeyShortcut(KEY_FOCUS_MOVE_DOWN, KeyEvent.VK_DOWN, 0));
      this.mapShortCut
          .put(KEY_FOCUS_MOVE_UP, new KeyShortcut(KEY_FOCUS_MOVE_UP, KeyEvent.VK_UP, 0));
      this.mapShortCut
          .put(KEY_FOCUS_MOVE_LEFT, new KeyShortcut(KEY_FOCUS_MOVE_LEFT, KeyEvent.VK_LEFT, 0));
      this.mapShortCut
          .put(KEY_FOCUS_MOVE_RIGHT, new KeyShortcut(KEY_FOCUS_MOVE_RIGHT, KeyEvent.VK_RIGHT, 0));
      this.mapShortCut.put(KEY_FOCUS_MOVE_DOWN_ADD_FOCUSED,
          new KeyShortcut(KEY_FOCUS_MOVE_DOWN_ADD_FOCUSED, KeyEvent.VK_DOWN, KeyEvent.SHIFT_MASK));
      this.mapShortCut.put(KEY_FOCUS_MOVE_UP_ADD_FOCUSED,
          new KeyShortcut(KEY_FOCUS_MOVE_UP_ADD_FOCUSED, KeyEvent.VK_UP, KeyEvent.SHIFT_MASK));
      this.mapShortCut.put(KEY_FOCUS_MOVE_LEFT_ADD_FOCUSED,
          new KeyShortcut(KEY_FOCUS_MOVE_LEFT_ADD_FOCUSED, KeyEvent.VK_LEFT, KeyEvent.SHIFT_MASK));
      this.mapShortCut.put(KEY_FOCUS_MOVE_RIGHT_ADD_FOCUSED,
          new KeyShortcut(KEY_FOCUS_MOVE_RIGHT_ADD_FOCUSED, KeyEvent.VK_RIGHT,
              KeyEvent.SHIFT_MASK));
      this.mapShortCut
          .put(KEY_DELETE_TOPIC, new KeyShortcut(KEY_DELETE_TOPIC, KeyEvent.VK_DELETE, 0));
      this.mapShortCut.put(KEY_TOPIC_TEXT_NEXT_LINE,
          new KeyShortcut(KEY_TOPIC_TEXT_NEXT_LINE, KeyEvent.VK_ENTER, KeyEvent.SHIFT_MASK));
      this.mapShortCut
          .put(KEY_ZOOM_IN, new KeyShortcut(KEY_ZOOM_IN, KeyEvent.VK_EQUALS, KeyEvent.CTRL_MASK));
      this.mapShortCut
          .put(KEY_ZOOM_OUT, new KeyShortcut(KEY_ZOOM_OUT, KeyEvent.VK_MINUS, KeyEvent.CTRL_MASK));
      this.mapShortCut
          .put(KEY_ZOOM_RESET, new KeyShortcut(KEY_ZOOM_RESET, KeyEvent.VK_0, KeyEvent.CTRL_MASK));
      this.mapShortCut.put(KEY_SHOW_POPUP, new KeyShortcut(KEY_SHOW_POPUP, KeyEvent.VK_SPACE,
          KeyEvent.CTRL_MASK | KeyEvent.ALT_MASK));
      this.mapShortCut.put(KEY_BIRDSEYE_MODIFIERS, new KeyShortcut(KEY_BIRDSEYE_MODIFIERS,
          KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK));
    } else {
      // key map for Linux and Windows
      this.mapShortCut.put(KEY_ADD_CHILD_AND_START_EDIT,
          new KeyShortcut(KEY_ADD_CHILD_AND_START_EDIT, KeyEvent.VK_TAB, 0));
      this.mapShortCut.put(KEY_ADD_SIBLING_AND_START_EDIT,
          new KeyShortcut(KEY_ADD_SIBLING_AND_START_EDIT, KeyEvent.VK_ENTER, 0));
      this.mapShortCut
          .put(KEY_CANCEL_EDIT, new KeyShortcut(KEY_CANCEL_EDIT, KeyEvent.VK_ESCAPE, 0));
      this.mapShortCut.put(KEY_TOPIC_FOLD, new KeyShortcut(KEY_TOPIC_FOLD, KeyEvent.VK_MINUS, 0));
      this.mapShortCut.put(KEY_TOPIC_FOLD_ALL,
          new KeyShortcut(KEY_TOPIC_FOLD_ALL, KeyEvent.VK_MINUS, KeyEvent.ALT_MASK));
      this.mapShortCut
          .put(KEY_TOPIC_UNFOLD, new KeyShortcut(KEY_TOPIC_UNFOLD, KeyEvent.VK_EQUALS, 0));
      this.mapShortCut.put(KEY_TOPIC_UNFOLD_ALL,
          new KeyShortcut(KEY_TOPIC_UNFOLD_ALL, KeyEvent.VK_EQUALS, KeyEvent.ALT_MASK));
      this.mapShortCut.put(KEY_FOCUS_ROOT_OR_START_EDIT,
          new KeyShortcut(KEY_FOCUS_ROOT_OR_START_EDIT, KeyEvent.VK_SPACE, KeyEvent.CTRL_MASK));
      this.mapShortCut
          .put(KEY_FOCUS_MOVE_DOWN, new KeyShortcut(KEY_FOCUS_MOVE_DOWN, KeyEvent.VK_DOWN, 0));
      this.mapShortCut
          .put(KEY_FOCUS_MOVE_UP, new KeyShortcut(KEY_FOCUS_MOVE_UP, KeyEvent.VK_UP, 0));
      this.mapShortCut
          .put(KEY_FOCUS_MOVE_LEFT, new KeyShortcut(KEY_FOCUS_MOVE_LEFT, KeyEvent.VK_LEFT, 0));
      this.mapShortCut
          .put(KEY_FOCUS_MOVE_RIGHT, new KeyShortcut(KEY_FOCUS_MOVE_RIGHT, KeyEvent.VK_RIGHT, 0));
      this.mapShortCut.put(KEY_FOCUS_MOVE_DOWN_ADD_FOCUSED,
          new KeyShortcut(KEY_FOCUS_MOVE_DOWN_ADD_FOCUSED, KeyEvent.VK_DOWN, KeyEvent.SHIFT_MASK));
      this.mapShortCut.put(KEY_FOCUS_MOVE_UP_ADD_FOCUSED,
          new KeyShortcut(KEY_FOCUS_MOVE_UP_ADD_FOCUSED, KeyEvent.VK_UP, KeyEvent.SHIFT_MASK));
      this.mapShortCut.put(KEY_FOCUS_MOVE_LEFT_ADD_FOCUSED,
          new KeyShortcut(KEY_FOCUS_MOVE_LEFT_ADD_FOCUSED, KeyEvent.VK_LEFT, KeyEvent.SHIFT_MASK));
      this.mapShortCut.put(KEY_FOCUS_MOVE_RIGHT_ADD_FOCUSED,
          new KeyShortcut(KEY_FOCUS_MOVE_RIGHT_ADD_FOCUSED, KeyEvent.VK_RIGHT,
              KeyEvent.SHIFT_MASK));
      this.mapShortCut
          .put(KEY_DELETE_TOPIC, new KeyShortcut(KEY_DELETE_TOPIC, KeyEvent.VK_DELETE, 0));
      this.mapShortCut.put(KEY_TOPIC_TEXT_NEXT_LINE,
          new KeyShortcut(KEY_TOPIC_TEXT_NEXT_LINE, KeyEvent.VK_ENTER, KeyEvent.SHIFT_MASK));
      this.mapShortCut
          .put(KEY_ZOOM_IN, new KeyShortcut(KEY_ZOOM_IN, KeyEvent.VK_EQUALS, KeyEvent.CTRL_MASK));
      this.mapShortCut
          .put(KEY_ZOOM_OUT, new KeyShortcut(KEY_ZOOM_OUT, KeyEvent.VK_MINUS, KeyEvent.CTRL_MASK));
      this.mapShortCut
          .put(KEY_ZOOM_RESET, new KeyShortcut(KEY_ZOOM_RESET, KeyEvent.VK_0, KeyEvent.CTRL_MASK));
      this.mapShortCut.put(KEY_SHOW_POPUP, new KeyShortcut(KEY_SHOW_POPUP, KeyEvent.VK_SPACE,
          KeyEvent.CTRL_MASK | KeyEvent.ALT_MASK));
      this.mapShortCut.put(KEY_BIRDSEYE_MODIFIERS, new KeyShortcut(KEY_BIRDSEYE_MODIFIERS,
          KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK));
    }
  }

  private static boolean isPreferDefaultFont() {
    final String hostLang = Locale.getDefault().getLanguage();
    return !(Locale.ENGLISH.getLanguage().equalsIgnoreCase(hostLang)
        || Locale.GERMAN.getLanguage().equalsIgnoreCase(hostLang)
        || Locale.FRENCH.getLanguage().equalsIgnoreCase(hostLang)
        || "ru".equalsIgnoreCase(hostLang)
        || "ua".equalsIgnoreCase(hostLang)
        || "es".equalsIgnoreCase(hostLang));
  }

  public static Font findDefaultFont(final int style, final int size,
                                     final String defaultFontFamily,
                                     final String[] fontNameStartsWith) {
    if (isPreferDefaultFont()) {
      return new Font(defaultFontFamily, style, size);
    } else {
      return Stream.of(
              GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames())
          .filter(x -> {
            for (final String s : fontNameStartsWith) {
              if (x.startsWith(s)) {
                return true;
              }
            }
            return false;
          })
          .map(x -> new Font(x, style, size))
          .findFirst()
          .orElse(new Font(defaultFontFamily, style, size));
    }
  }

  public static MindMapPanelConfig deserialize(final byte[] value) throws IOException {
    try {
      final ObjectInputStream stream =
          new ObjectInputStream(new ByteArrayInputStream(value));
      return (MindMapPanelConfig) stream.readObject();
    } catch (ClassNotFoundException ex) {
      throw new IOException("Can;t find class during deserialization", ex);
    }
  }

  public boolean isKeyEvent(final String id, final KeyEvent event,
                            final int modifiersMask) {
    final KeyShortcut shortCut = this.mapShortCut.get(id);
    return shortCut != null && shortCut.isEvent(event, modifiersMask);
  }

  /**
   * Check that modifiers match input event.
   *
   * @param id    identifier of shortcut
   * @param event event, can be null
   * @return true if event is not null and modifiers match
   * @since 1.6.2
   */
  public boolean isModifiers(final String id, final InputEvent event) {
    final KeyShortcut shortCut = this.mapShortCut.get(id);
    return shortCut != null && shortCut.matchModifiers(event);
  }

  public boolean isKeyEvent(final String id, final KeyEvent event) {
    return this.isKeyEvent(id, event, KeyShortcut.ALL_MODIFIERS_MASK);
  }

  @SuppressWarnings("unchecked")
  public <T extends Serializable> T getOptionalProperty(final String id, final T defaultValue) {
    synchronized (this.optionalProperties) {
      final Serializable result = this.optionalProperties.get(id);
      return (T) (result == null || result == NULL_OPTIONAL_OBJECT ? defaultValue : result);
    }
  }

  public void setOptionalProperty(final String id, final Serializable value) {
    synchronized (this.optionalProperties) {
      this.optionalProperties.put(id, value == null ? NULL_OPTIONAL_OBJECT : value);
    }
  }

  public KeyShortcut getKeyShortCut(final String id) {
    return this.mapShortCut.get(id);
  }

  public void setKeyShortCut(final KeyShortcut shortCut) {
    this.mapShortCut.put(shortCut.getID(), shortCut);
  }

  @SettingsAccessor(name = "optionalProperties")
  public Map<String, Serializable> getOptionalProperties() {
    return new HashMap<>(this.optionalProperties);
  }

  @SettingsAccessor(name = "optionalProperties")
  public void setOptionalProperties(final Map<String, Serializable> properties) {
    synchronized (this.optionalProperties) {
      this.optionalProperties.clear();
      this.optionalProperties.putAll(properties);
    }
  }

  @SettingsAccessor(name = "mapShortCut")
  public Map<String, KeyShortcut> getKeyShortcutMap() {
    return new HashMap<>(this.mapShortCut);
  }

  @SettingsAccessor(name = "mapShortCut")
  public void setKeyShortcutMap(final Map<String, KeyShortcut> map) {
    this.mapShortCut.clear();
    this.mapShortCut.putAll(map);
  }

  public boolean hasDifferenceInParameters(final MindMapPanelConfig etalon) {
    for (final Field f : MindMapPanelConfig.class.getDeclaredFields()) {
      if ((f.getModifiers() & (Modifier.STATIC | Modifier.TRANSIENT | Modifier.FINAL)) != 0) {
        continue;
      }
      try {
        final Object thisValue = f.get(this);
        final Object thatValue = f.get(etalon);

        if (thisValue == null && thatValue == null) {
          continue;
        }
        if (thisValue == null || !thisValue.equals(thatValue)) {
          return true;
        }

      } catch (IllegalAccessException ex) {
        throw new Error("IllegalAccessException [" + f.getName() + ']', ex);
      } catch (IllegalArgumentException ex) {
        throw new Error("IllegalArgumentException [" + f.getName() + ']', ex);
      }
    }

    return !this.mapShortCut.equals(etalon.mapShortCut)
        || !this.optionalProperties.equals(etalon.optionalProperties);
  }

  public Preferences saveTo(final Preferences prefs) {
    if (prefs != null) {
      final String prefix = MindMapPanelConfig.class.getSimpleName();

      for (final Field f : MindMapPanelConfig.class.getDeclaredFields()) {
        if ((f.getModifiers() & (Modifier.STATIC | Modifier.TRANSIENT | Modifier.FINAL)) != 0) {
          continue;
        }

        final Class<?> fieldClass = f.getType();
        final String fieldName = prefix + '.' + f.getName();

        try {
          if (fieldClass == boolean.class) {
            prefs.putBoolean(fieldName, f.getBoolean(this));
          } else if (fieldClass == int.class) {
            prefs.putInt(fieldName, f.getInt(this));
          } else if (fieldClass == float.class) {
            prefs.putFloat(fieldName, f.getFloat(this));
          } else if (fieldClass == double.class) {
            prefs.putDouble(fieldName, f.getDouble(this));
          } else if (fieldClass == Font.class) {
            final Font theFont = (Font) f.get(this);
            prefs.put(fieldName + ".name", theFont.getName());
            prefs.putInt(fieldName + ".size", theFont.getSize());
            prefs.putInt(fieldName + ".style", theFont.getStyle());
          } else if (fieldClass == Color.class) {
            prefs.putInt(fieldName, ((Color) f.get(this)).getRGB());
          } else if (fieldClass == String.class) {
            prefs.put(fieldName, (String) f.get(this));
          } else if (fieldClass == RenderQuality.class) {
            prefs.put(fieldName, ((RenderQuality) f.get(this)).name());
          } else if (fieldClass == MouseButton.class) {
            prefs.put(fieldName, ((MouseButton) f.get(this)).name());
          } else if (fieldClass == Map.class) {
            if (f.getName().equals("mapShortCut")) {
              for (final Map.Entry<String, KeyShortcut> e : this.mapShortCut.entrySet()) {
                prefs.put(PREFIX_SHORTCUT + e.getValue().getID(), e.getValue().packToString());
              }
            } else {
              synchronized (this.optionalProperties) {
                for (final Map.Entry<String, Serializable> e : this.optionalProperties.entrySet()) {
                  final String key = PREFIX_OPTIONAL_PROPERTY + e.getKey();
                  if (e.getValue() == NULL_OPTIONAL_OBJECT) {
                    prefs.remove(key);
                  } else {
                    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    try {
                      final ObjectOutputStream objectOutputStream =
                          new ObjectOutputStream(outputStream);
                      objectOutputStream.writeObject(e.getValue());
                      objectOutputStream.close();
                      final String encodedValue =
                          Base64.getEncoder().encodeToString(outputStream.toByteArray());
                      prefs.put(key, encodedValue);
                    } catch (final IOException ex) {
                      throw new RuntimeException("Error during write optional property: " + e, ex);
                    }
                  }
                }
                this.optionalProperties.values().removeIf(x -> x == NULL_OPTIONAL_OBJECT);
              }
            }
          } else {
            throw new Error("Unexpected field type " + fieldClass.getName());
          }
        } catch (IllegalAccessException ex) {
          throw new Error("IllegalAccessException [" + fieldClass.getName() + ']', ex);
        } catch (IllegalArgumentException ex) {
          throw new Error("IllegalArgumentException [" + fieldClass.getName() + ']', ex);
        }
      }
    }
    return prefs;
  }

  public Preferences loadFrom(final Preferences prefs) {
    if (prefs != null) {
      final String prefix = MindMapPanelConfig.class.getSimpleName();

      final MindMapPanelConfig etalon = new MindMapPanelConfig();

      for (final Field f : MindMapPanelConfig.class.getDeclaredFields()) {
        if ((f.getModifiers() & (Modifier.STATIC | Modifier.TRANSIENT | Modifier.FINAL)) != 0) {
          continue;
        }

        final Class<?> fieldClass = f.getType();
        final String fieldName = prefix + '.' + f.getName();

        try {
          if (fieldClass == boolean.class) {
            f.setBoolean(this, prefs.getBoolean(fieldName, f.getBoolean(etalon)));
          } else if (fieldClass == int.class) {
            f.setInt(this, prefs.getInt(fieldName, f.getInt(etalon)));
          } else if (fieldClass == float.class) {
            f.setFloat(this, prefs.getFloat(fieldName, f.getFloat(etalon)));
          } else if (fieldClass == double.class) {
            f.setDouble(this, prefs.getDouble(fieldName, f.getDouble(etalon)));
          } else if (fieldClass == Font.class) {
            final Font etalonFont = etalon.getFont();

            final String fontName = prefs.get(fieldName + ".name", etalonFont.getName());
            final int fontSize = prefs.getInt(fieldName + ".size", etalonFont.getSize());
            final int fontStyle = prefs.getInt(fieldName + ".style", etalonFont.getStyle());

            f.set(this, new Font(fontName, fontStyle, fontSize));
          } else if (fieldClass == Color.class) {
            final int argb = prefs.getInt(fieldName, ((Color) f.get(etalon)).getRGB());
            f.set(this, new Color(argb, true));
          } else if (fieldClass == String.class) {
            f.set(this, prefs.get(fieldName, (String) f.get(etalon)));
          } else if (fieldClass == RenderQuality.class) {
            final String name = prefs.get(fieldName, ((RenderQuality) f.get(etalon)).name());
            f.set(this, RenderQuality.valueOf(name));
          } else if (fieldClass == MouseButton.class) {
            final String name = prefs.get(fieldName, ((MouseButton) f.get(etalon)).name());
            f.set(this, MouseButton.valueOf(name));
          } else if (fieldClass == Map.class) {
            if (f.getName().equals("mapShortCut")) {
              this.mapShortCut.clear();
              this.mapShortCut.putAll(etalon.mapShortCut);
              try {
                for (final String k : prefs.keys()) {
                  if (k.startsWith(PREFIX_SHORTCUT)) {
//            final int dotIndex = k.indexOf('.');
//            final String id = k.substring(dotIndex + 1);
                    final String packedValue = prefs.get(k, "");
                    if (packedValue.isEmpty()) {
                      throw new Error("Unexpected situation, short cut value is empty [" + k + ']');
                    }
                    final KeyShortcut unpacked = new KeyShortcut(packedValue);
                    this.mapShortCut.put(unpacked.getID(), unpacked);
                  }
                }
              } catch (BackingStoreException ex) {
                throw new Error("Can't get list of keys from storage", ex);
              }
            } else {
              this.optionalProperties.clear();
              this.optionalProperties.putAll(etalon.optionalProperties);

              try {
                for (final String k : prefs.keys()) {
                  if (k.startsWith(PREFIX_OPTIONAL_PROPERTY)) {
                    final String propertyName = k.substring(k.indexOf('.') + 1);
                    final String value = prefs.get(k, null);
                    if (value == null) {
                      throw new Error("Unexpected situation, property value is null [" + k + ']');
                    }
                    try {
                      final Serializable readValue =
                          (Serializable) new ObjectInputStream(
                              new ByteArrayInputStream(
                                  Base64.getDecoder().decode(value))).readObject();
                      if (readValue != null) {
                        this.optionalProperties.put(propertyName, readValue);
                      }
                    } catch (ClassNotFoundException | IOException ex) {
                      // ignore error to save possibility to load config
                    }
                  }
                }
              } catch (BackingStoreException ex) {
                throw new Error("Can't get list of keys from storage", ex);
              }
            }
          } else {
            throw new Error("Unexpected field type " + fieldClass.getName());
          }
        } catch (IllegalAccessException ex) {
          throw new Error("IllegalAccessException [" + fieldClass.getName() + ']', ex);
        } catch (IllegalArgumentException ex) {
          throw new Error("IllegalArgumentException [" + fieldClass.getName() + ']', ex);
        }
      }
    }
    return prefs;
  }

  public void makeAtomicChange(final Runnable runnable) {
    this.notificationEnabled = false;
    try {
      runnable.run();
    } finally {
      this.notificationEnabled = true;
      this.notifyCfgListenersAboutChange();
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.optionalProperties, this.mapShortCut);
  }

  @Override
  public boolean equals(final Object object) {
    if (object == null) {
      return false;
    }
    if (object == this) {
      return true;
    }
    return object instanceof MindMapPanelConfig &&
        !this.hasDifferenceInParameters((MindMapPanelConfig) object);
  }

  public float safeScaleFloatValue(final float value, final float minimal) {
    final float result = (float) (this.scale * (double) value);
    return Float.compare(result, minimal) >= 0 ? result : minimal;
  }

  private static Font cloneFont(final Font font) {
    return new Font(font.getName(), font.getStyle(), font.getSize());
  }
  public void makeFullCopyOf(final MindMapPanelConfig src, final boolean copyListeners,
                             final boolean makeNotification) {
    if (src != null) {
      for (final Field f : MindMapPanelConfig.class.getDeclaredFields()) {
        if (f.getName().equals("listeners")) {
          if (copyListeners) {
            this.listeners.clear();
            this.listeners.addAll(src.listeners);
          }
        } else if ((f.getModifiers() & (Modifier.STATIC | Modifier.FINAL)) == 0 &&
            f.getType() != Map.class) {
          try {
            Object value = f.get(src);
            if (value instanceof Font) {
              final Font font = (Font)value;
              value = cloneFont(font); // prevent possible bugs in deserialization of font object in IDE like IDEA
            }
            f.set(this, value);
          } catch (Exception ex) {
            throw new Error("Unexpected state during cloning field " + f, ex);
          }
        }
      }

      this.mapShortCut.clear();
      this.mapShortCut.putAll(src.mapShortCut);

      this.optionalProperties.clear();
      this.optionalProperties.putAll(src.optionalProperties);

      if (makeNotification) {
        this.notifyCfgListenersAboutChange();
      }
    }
  }

  public void addConfigurationListener(final MindMapConfigListener l) {
    this.listeners.add(new WeakReference<>(requireNonNull(l)));
  }

  public void removeConfigurationListener(final MindMapConfigListener l) {
    this.listeners.removeIf(x -> {
      final MindMapConfigListener listener = x.get();
      return listener == null || listener.equals(l);
    });
  }

  public boolean isShortcutConflict(final KeyStroke keyStroke) {
    boolean result = false;
    if (keyStroke != null) {
      for (final KeyShortcut s : this.mapShortCut.values()) {
        if (s.doesConflictWith(keyStroke)) {
          result = true;
          break;
        }
      }
    }
    return result;
  }

  private void notifyCfgListenersAboutChange() {
    if (this.notificationEnabled) {
      this.listeners.stream().map(Reference::get)
          .filter(Objects::nonNull)
          .forEach(x -> x.onConfigurationPropertyChanged(this));
    }
    this.listeners.removeIf(x -> x.get() == null);
  }

  public byte[] serialize() throws IOException {
    final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    final ObjectOutputStream objectStream = new ObjectOutputStream(outStream);
    objectStream.writeObject(this);
    objectStream.close();
    return outStream.toByteArray();
  }

  public boolean isKeyEventDetected(final KeyEvent event,
                                    final int effectiveModifiers,
                                    final String... shortCutIDs) {
    for (final String k : shortCutIDs) {
      final KeyShortcut shortCut = this.mapShortCut.get(k);
      if (shortCut != null && shortCut.isEvent(event, effectiveModifiers)) {
        return true;
      }
    }
    return false;
  }

  public boolean isKeyEventDetected(final KeyEvent event,
                                    final String... shortCutIDs) {
    for (final String k : shortCutIDs) {
      final KeyShortcut shortCut = this.mapShortCut.get(k);
      if (shortCut != null && shortCut.isEvent(event, KeyShortcut.ALL_MODIFIERS_MASK)) {
        return true;
      }
    }
    return false;
  }

  @SettingsAccessor(name = "horizontalBlockGap")
  public int getHorizontalBlockGap() {
    return this.horizontalBlockGap;
  }

  @SettingsAccessor(name = "horizontalBlockGap")
  public void setHorizontalBlockGap(final int gap) {
    this.horizontalBlockGap = gap;
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "selectLineWidth")
  public float getSelectLineWidth() {
    return this.selectLineWidth;
  }

  @SettingsAccessor(name = "selectLineWidth")
  public void setSelectLineWidth(final float f) {
    this.selectLineWidth = f;
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "jumpLinkWidth")
  public float getJumpLinkWidth() {
    return this.jumpLinkWidth;
  }

  @SettingsAccessor(name = "jumpLinkWidth")
  public void setJumpLinkWidth(final float f) {
    this.jumpLinkWidth = f;
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "jumpLinkColor")
  public Color getJumpLinkColor() {
    return this.jumpLinkColor;
  }

  @SettingsAccessor(name = "jumpLinkColor")
  public void setJumpLinkColor(final Color color) {
    this.jumpLinkColor = color;
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "birdseyeBackground")
  public Color getBirdseyeBackground() {
    return this.birdseyeBackground;
  }

  @SettingsAccessor(name = "birdseyeBackground")
  public void setBirdseyeBackground(final Color color) {
    this.birdseyeBackground = color;
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "birdseyeFront")
  public Color getBirdseyeFront() {
    return this.birdseyeFront;
  }

  @SettingsAccessor(name = "birdseyeFront")
  public void setBirdseyeFront(final Color color) {
    this.birdseyeFront = color;
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "scaleModifiers")
  public int getScaleModifiers() {
    return this.scaleModifiers;
  }

  @SettingsAccessor(name = "scaleModifiers")
  public void setScaleModifiers(final int value) {
    this.scaleModifiers = value;
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "selectLineColor")
  public Color getSelectLineColor() {
    return this.selectLineColor;
  }

  @SettingsAccessor(name = "selectLineColor")
  public void setSelectLineColor(final Color color) {
    this.selectLineColor = color;
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "paperMargins")
  public int getPaperMargins() {
    return this.paperMargins;
  }

  @SettingsAccessor(name = "paperMargins")
  public void setPaperMargins(final int size) {
    this.paperMargins = size;
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "smartTextPaste")
  public boolean isSmartTextPaste() {
    return this.smartTextPaste;
  }

  @SettingsAccessor(name = "smartTextPaste")
  public void setSmartTextPaste(final boolean flag) {
    this.smartTextPaste = flag;
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "noEscape")
  public boolean isNoEscape() {
    return this.noEscape;
  }

  @SettingsAccessor(name = "noEscape")
  public void setNoEscape(final boolean flag) {
    this.noEscape = flag;
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "drawBackground")
  public boolean isDrawBackground() {
    return this.drawBackground;
  }

  @SettingsAccessor(name = "drawBackground")
  public void setDrawBackground(final boolean flag) {
    this.drawBackground = flag;
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "otherLevelVerticalInset")
  public int getOtherLevelVerticalInset() {
    return this.otherLevelVerticalInset;
  }

  @SettingsAccessor(name = "otherLevelVerticalInset")
  public void setOtherLevelVerticalInset(final int value) {
    this.otherLevelVerticalInset = value;
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "otherLevelHorizontalInset")
  public int getOtherLevelHorizontalInset() {
    return this.otherLevelHorizontalInset;
  }

  @SettingsAccessor(name = "otherLevelHorizontalInset")
  public void setOtherLevelHorizontalInset(final int value) {
    this.otherLevelHorizontalInset = value;
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "firstLevelVerticalInset")
  public int getFirstLevelVerticalInset() {
    return this.firstLevelVerticalInset;
  }

  @SettingsAccessor(name = "firstLevelVerticalInset")
  public void setFirstLevelVerticalInset(final int value) {
    this.firstLevelVerticalInset = value;
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "firstLevelHorizontalInset")
  public int getFirstLevelHorizontalInset() {
    return this.firstLevelHorizontalInset;
  }

  @SettingsAccessor(name = "firstLevelHorizontalInset")
  public void setFirstLevelHorizontalInset(final int value) {
    this.firstLevelHorizontalInset = value;
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "paperColor")
  public Color getPaperColor() {
    return this.paperColor;
  }

  @SettingsAccessor(name = "paperColor")
  public void setPaperColor(final Color color) {
    this.paperColor = requireNonNull(color);
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "gridColor")
  public Color getGridColor() {
    return this.gridColor;
  }

  @SettingsAccessor(name = "gridColor")
  public void setGridColor(final Color color) {
    this.gridColor = color;
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "showGrid")
  public boolean isShowGrid() {
    return this.showGrid;
  }

  @SettingsAccessor(name = "showGrid")
  public void setShowGrid(final boolean flag) {
    this.showGrid = flag;
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "gridStep")
  public int getGridStep() {
    return this.gridStep;
  }

  @SettingsAccessor(name = "gridStep")
  public void setGridStep(final int step) {
    this.gridStep = step;
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "rootBackgroundColor")
  public Color getRootBackgroundColor() {
    return this.rootBackgroundColor;
  }

  @SettingsAccessor(name = "rootBackgroundColor")
  public void setRootBackgroundColor(final Color color) {
    this.rootBackgroundColor = requireNonNull(color);
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "firstLevelBackgroundColor")
  public Color getFirstLevelBackgroundColor() {
    return this.firstLevelBackgroundColor;
  }

  @SettingsAccessor(name = "firstLevelBackgroundColor")
  public void setFirstLevelBackgroundColor(final Color color) {
    this.firstLevelBackgroundColor = color;
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "otherLevelBackgroundColor")
  public Color getOtherLevelBackgroundColor() {
    return this.otherLevelBackgroundColor;
  }

  @SettingsAccessor(name = "otherLevelBackgroundColor")
  public void setOtherLevelBackgroundColor(final Color color) {
    this.otherLevelBackgroundColor = color;
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "rootTextColor")
  public Color getRootTextColor() {
    return this.rootTextColor;
  }

  @SettingsAccessor(name = "rootTextColor")
  public void setRootTextColor(final Color color) {
    this.rootTextColor = requireNonNull(color);
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "firstLevelTextColor")
  public Color getFirstLevelTextColor() {
    return this.firstLevelTextColor;
  }

  @SettingsAccessor(name = "firstLevelTextColor")
  public void setFirstLevelTextColor(final Color color) {
    this.firstLevelTextColor = requireNonNull(color);
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "otherLevelTextColor")
  public Color getOtherLevelTextColor() {
    return this.otherLevelTextColor;
  }

  @SettingsAccessor(name = "otherLevelTextColor")
  public void setOtherLevelTextColor(final Color color) {
    this.otherLevelTextColor = requireNonNull(color);
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "elementBorderColor")
  public Color getElementBorderColor() {
    return this.elementBorderColor;
  }

  @SettingsAccessor(name = "elementBorderColor")
  public void setElementBorderColor(final Color color) {
    this.elementBorderColor = requireNonNull(color);
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "connectorColor")
  public Color getConnectorColor() {
    return this.connectorColor;
  }

  @SettingsAccessor(name = "connectorColor")
  public void setConnectorColor(final Color color) {
    this.connectorColor = requireNonNull(color);
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "shadowColor")
  public Color getShadowColor() {
    return this.shadowColor;
  }

  @SettingsAccessor(name = "shadowColor")
  public void setShadowColor(final Color color) {
    this.shadowColor = requireNonNull(color);
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "collapsatorBorderColor")
  public Color getCollapsatorBorderColor() {
    return this.collapsatorBorderColor;
  }

  @SettingsAccessor(name = "collapsatorBorderColor")
  public void setCollapsatorBorderColor(final Color color) {
    this.collapsatorBorderColor = requireNonNull(color);
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "collapsatorBackgroundColor")
  public Color getCollapsatorBackgroundColor() {
    return this.collapsatorBackgroundColor;
  }

  @SettingsAccessor(name = "collapsatorBackgroundColor")
  public void setCollapsatorBackgroundColor(final Color color) {
    this.collapsatorBackgroundColor = requireNonNull(color);
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "elementBorderWidth")
  public float getElementBorderWidth() {
    return this.elementBorderWidth;
  }

  @SettingsAccessor(name = "elementBorderWidth")
  public void setElementBorderWidth(final float value) {
    this.elementBorderWidth = value;
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "collapsatorBorderWidth")
  public float getCollapsatorBorderWidth() {
    return this.collapsatorBorderWidth;
  }

  @SettingsAccessor(name = "collapsatorBorderWidth")
  public void setCollapsatorBorderWidth(final float width) {
    this.collapsatorBorderWidth = width;
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "shadowOffset")
  public float getShadowOffset() {
    return this.shadowOffset;
  }

  @SettingsAccessor(name = "shadowOffset")
  public void setShadowOffset(final float value) {
    this.shadowOffset = value;
  }

  @SettingsAccessor(name = "connectorWidth")
  public float getConnectorWidth() {
    return this.connectorWidth;
  }

  @SettingsAccessor(name = "connectorWidth")
  public void setConnectorWidth(final float value) {
    this.connectorWidth = value;
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "font")
  public Font getFont() {
    return this.font;
  }

  @SettingsAccessor(name = "font")
  public void setFont(final Font f) {
    this.font = requireNonNull(f);
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "scale")
  public double getScale() {
    return this.scale;
  }

  @SettingsAccessor(name = "scale")
  public void setScale(final double value) {
    this.scale = Math.max(0.01d, value);
    notifyCfgListenersAboutChange();
  }

  public void setScaleWithoutListenerNotification(final double value) {
    this.scale = Math.max(0.01d, value);
  }

  @SettingsAccessor(name = "dropShadow")
  public boolean isDropShadow() {
    return this.dropShadow;
  }

  @SettingsAccessor(name = "dropShadow")
  public void setDropShadow(final boolean value) {
    this.dropShadow = value;
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "renderQuality")
  public RenderQuality getRenderQuality() {
    return this.renderQuality;
  }

  @SettingsAccessor(name = "renderQuality")
  public void setRenderQuality(final RenderQuality value) {
    this.renderQuality = MiscUtils.ensureNotNull(value, Utils.getDefaultRenderQialityForOs());
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "birdseyeMouseButton")
  public MouseButton getBirdseyeMouseButton() {
    return this.birdseyeMouseButton;
  }

  @SettingsAccessor(name = "birdseyeMouseButton")
  public void setBirdseyeMouseButton(final MouseButton value) {
    this.birdseyeMouseButton = MiscUtils.ensureNotNull(value, MouseButton.BUTTON_3);
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "collapsatorSize")
  public int getCollapsatorSize() {
    return this.collapsatorSize;
  }

  @SettingsAccessor(name = "collapsatorSize")
  public void setCollapsatorSize(final int size) {
    this.collapsatorSize = size;
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "textMargins")
  public int getTextMargins() {
    return this.textMargins;
  }

  @SettingsAccessor(name = "textMargins")
  public void setTextMargins(final int value) {
    this.textMargins = value;
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "selectLineGap")
  public int getSelectLineGap() {
    return this.selectLineGap;
  }

  @SettingsAccessor(name = "selectLineGap")
  public void setSelectLineGap(final int value) {
    this.selectLineGap = value;
    notifyCfgListenersAboutChange();
  }

}
