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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyDouble;
import static org.mockito.Mockito.anyFloat;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.igormaznitsa.mindmap.swing.panel.utils.KeyShortcut;
import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class MindMapPanelConfigTest {

  @Test
  public void testHasDifferenceInParameters_NoDifference() {
    final MindMapPanelConfig one = new MindMapPanelConfig();
    final MindMapPanelConfig two = new MindMapPanelConfig();

    assertFalse(one.hasDifferenceInParameters(two));
  }

  @Test
  public void testHasDifferenceInParameters_DifferenceInBoolean() {
    final MindMapPanelConfig one = new MindMapPanelConfig();
    final MindMapPanelConfig two = new MindMapPanelConfig();

    one.setDropShadow(!two.isDropShadow());
    assertTrue(one.hasDifferenceInParameters(two));
  }

  @Test
  public void testHasDifferenceInParameters_DifferenceInKeyShortcuts() {
    final MindMapPanelConfig one = new MindMapPanelConfig();
    final MindMapPanelConfig two = new MindMapPanelConfig();

    one.setKeyShortCut(
        new KeyShortcut(MindMapPanelConfig.KEY_ADD_CHILD_AND_START_EDIT, Integer.MAX_VALUE,
            Integer.MIN_VALUE));
    assertTrue(one.hasDifferenceInParameters(two));
  }

  @Test
  public void testHasDifferenceInParameters_DifferenceInFloat() {
    final MindMapPanelConfig one = new MindMapPanelConfig();
    final MindMapPanelConfig two = new MindMapPanelConfig();

    one.setCollapsatorBorderWidth(two.getCollapsatorBorderWidth() + 0.001f);
    assertTrue(one.hasDifferenceInParameters(two));
  }

  @Test
  public void testHasDifferenceInParameters_DifferenceInInt() {
    final MindMapPanelConfig one = new MindMapPanelConfig();
    final MindMapPanelConfig two = new MindMapPanelConfig();

    one.setCollapsatorSize(two.getCollapsatorSize() + 1);
    assertTrue(one.hasDifferenceInParameters(two));
  }

  @Test
  public void testOptionalProperties_SetGet() {
    final MindMapPanelConfig config = new MindMapPanelConfig();

    assertNull(config.getOptionalProperty("string", null));
    assertNull(config.getOptionalProperty("number", null));
    assertNull(config.getOptionalProperty("enum", null));
    assertTrue(config.getOptionalProperties().isEmpty());

    config.setOptionalProperty("string", "Hello");
    config.setOptionalProperty("number", 123);
    config.setOptionalProperty("enum", TestEnum.ONE);
    assertEquals(3, config.getOptionalProperties().size());

    assertEquals("Hello", config.getOptionalProperty("string", null));
    assertEquals(TestEnum.ONE, config.<TestEnum>getOptionalProperty("enum", null));
    assertEquals(Integer.valueOf(123), config.<Integer>getOptionalProperty("number", null));
  }

  @Test
  public void testOptionalProperties_hasDifferenceInParameters() {
    final MindMapPanelConfig one = new MindMapPanelConfig();
    final MindMapPanelConfig two = new MindMapPanelConfig();

    assertFalse(one.hasDifferenceInParameters(two));

    one.setOptionalProperty("string", "Hello");
    one.setOptionalProperty("number", 123);
    one.setOptionalProperty("enum", TestEnum.ONE);

    assertTrue(one.hasDifferenceInParameters(two));

    two.setOptionalProperty("string", "Hello");
    two.setOptionalProperty("number", 123);
    two.setOptionalProperty("enum", TestEnum.ONE);

    assertFalse(one.hasDifferenceInParameters(two));

    two.setOptionalProperty("enum", TestEnum.TWO);
    assertTrue(one.hasDifferenceInParameters(two));
  }

  @Test
  public void testHasDifferenceInParameters_DifferenceInColor() {
    final MindMapPanelConfig one = new MindMapPanelConfig();
    final MindMapPanelConfig two = new MindMapPanelConfig();

    one.setCollapsatorBackgroundColor(
        new Color(two.getCollapsatorBackgroundColor().getRGB() ^ 0xFFFFFFFF));
    assertTrue(one.hasDifferenceInParameters(two));
  }

  @Test
  public void testHasDifferenceInParameters_DifferenceInFont() {
    final MindMapPanelConfig one = new MindMapPanelConfig();
    final MindMapPanelConfig two = new MindMapPanelConfig();

    one.setFont(two.getFont().deriveFont(17.0f));
    assertTrue(one.hasDifferenceInParameters(two));
  }

  @Test
  public void testHasDifferenceInParameters_DifferenceInDouble() {
    final MindMapPanelConfig one = new MindMapPanelConfig();
    final MindMapPanelConfig two = new MindMapPanelConfig();

    one.setScale(two.getScale() + 0.00001d);
    assertTrue(one.hasDifferenceInParameters(two));
  }

  @Test
  public void testSaveRestoreState() {
    final Map<String, Object> storage = new HashMap<>();
    final Preferences prefs = mock(Preferences.class);

    doAnswer(invocation -> {
      final String key = invocation.getArgument(0);
      final String value = invocation.getArgument(1);
      storage.put(key, value);
      return null;
    }).when(prefs).put(anyString(), anyString());

    doAnswer((final InvocationOnMock invocation) -> {
      final String key = invocation.getArgument(0);
      final Integer value = invocation.getArgument(1);
      storage.put(key, value);
      return null;
    }).when(prefs).putInt(anyString(), anyInt());

    doAnswer((final InvocationOnMock invocation) -> {
      final String key = invocation.getArgument(0);
      final Boolean value = invocation.getArgument(1);
      storage.put(key, value);
      return null;
    }).when(prefs).putBoolean(anyString(), anyBoolean());

    doAnswer((final InvocationOnMock invocation) -> {
      final String key = invocation.getArgument(0);
      final Float value = invocation.getArgument(1);
      storage.put(key, value);
      return null;
    }).when(prefs).putFloat(anyString(), anyFloat());

    doAnswer((final InvocationOnMock invocation) -> {
      final String key = invocation.getArgument(0);
      final Double value = invocation.getArgument(1);
      storage.put(key, value);
      return null;
    }).when(prefs).putDouble(anyString(), anyDouble());

    when(prefs.get(anyString(), any())).thenAnswer((InvocationOnMock invocation) -> {
      final String key = invocation.getArgument(0);
      final String def = invocation.getArgument(1);
      return storage.containsKey(key) ? storage.get(key) : def;
    });

    when(prefs.getBoolean(anyString(), anyBoolean())).thenAnswer((InvocationOnMock invocation) -> {
      final String key = invocation.getArgument(0);
      final Boolean def = invocation.getArgument(1);
      return storage.containsKey(key) ? (Boolean) storage.get(key) : def;
    });

    when(prefs.getInt(anyString(), anyInt())).thenAnswer((InvocationOnMock invocation) -> {
      final String key = invocation.getArgument(0);
      final Integer def = invocation.getArgument(1);
      return storage.containsKey(key) ? (Integer) storage.get(key) : def;
    });

    when(prefs.getFloat(anyString(), anyFloat())).thenAnswer((InvocationOnMock invocation) -> {
      final String key = invocation.getArgument(0);
      final Float def = invocation.getArgument(1);
      return storage.containsKey(key) ? (Float) storage.get(key) : def;
    });

    when(prefs.getDouble(anyString(), anyDouble())).thenAnswer((InvocationOnMock invocation) -> {
      final String key = invocation.getArgument(0);
      final Double def = invocation.getArgument(1);
      return storage.containsKey(key) ? (Double) storage.get(key) : def;
    });

    try {
      when(prefs.keys()).thenAnswer(
          (final InvocationOnMock invocation) -> storage.keySet().toArray(new String[0]));
    } catch (Exception ex) {
      fail("Unexpected exception");
    }

    final MindMapPanelConfig config = new MindMapPanelConfig();

    config.setScale(100.5d);
    config.setGridColor(Color.orange);
    config.setShowGrid(false);
    config.setOptionalProperty("one", TestEnum.ONE);
    config.setOptionalProperty("two", TestEnum.TWO);
    config.setOptionalProperty("three", 112);
    config.setFont(new Font("Helvetica", Font.ITALIC, 36));

    config.setKeyShortCut(new KeyShortcut("testShortCut", 1234, 5678));

    config.saveTo(prefs);
    assertFalse(storage.isEmpty());

    final MindMapPanelConfig newConfig = new MindMapPanelConfig();

    newConfig.loadFrom(prefs);

    assertEquals(TestEnum.ONE, newConfig.getOptionalProperty("one", null));
    assertEquals(TestEnum.TWO, newConfig.getOptionalProperty("two", null));
    assertEquals(Integer.valueOf(112), newConfig.getOptionalProperty("three", null));

    assertFalse(newConfig.isShowGrid());
    assertEquals(Color.orange, newConfig.getGridColor());
    assertEquals(new Font("Helvetica", Font.ITALIC, 36), newConfig.getFont());
    assertEquals(100.5d, newConfig.getScale(), 0.0d);

    final KeyShortcut shortCut = newConfig.getKeyShortCut("testShortCut");
    assertNotNull(shortCut);
    assertEquals("testShortCut", shortCut.getID());
    assertEquals(1234, shortCut.getKeyCode());
    assertEquals(5678, shortCut.getModifiers());

    storage.clear();

    newConfig.loadFrom(prefs);

    final MindMapPanelConfig etalon = new MindMapPanelConfig();

    assertEquals(etalon.isShowGrid(), newConfig.isShowGrid());
    assertEquals(etalon.getGridColor(), newConfig.getGridColor());
    assertEquals(etalon.getFont(), newConfig.getFont());
    assertEquals(etalon.getScale(), newConfig.getScale(), 0.0d);
    assertNull(newConfig.getKeyShortCut("testShortCut"));
    assertNotNull(newConfig.getKeyShortCut(MindMapPanelConfig.KEY_ADD_CHILD_AND_START_EDIT));
  }

  private enum TestEnum {
    ONE, TWO
  }

}
