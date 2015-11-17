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
package com.igormaznitsa.mindmap.swing.panel;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class MindMapPanelConfigTest {

  @Test
  public void testHasDifferenceInParameters_NoDifference () {
    final MindMapPanelConfig one = new MindMapPanelConfig();
    final MindMapPanelConfig two = new MindMapPanelConfig();

    assertFalse(one.hasDifferenceInParameters(two));
  }

  @Test
  public void testHasDifferenceInParameters_DifferenceInBoolean(){
    final MindMapPanelConfig one = new MindMapPanelConfig();
    final MindMapPanelConfig two = new MindMapPanelConfig();
    
    one.setDropShadow(!two.isDropShadow());
    assertTrue(one.hasDifferenceInParameters(two));
  }
  
  @Test
  public void testHasDifferenceInParameters_DifferenceInFloat(){
    final MindMapPanelConfig one = new MindMapPanelConfig();
    final MindMapPanelConfig two = new MindMapPanelConfig();
    
    one.setCollapsatorBorderWidth(two.getCollapsatorBorderWidth()+0.001f);
    assertTrue(one.hasDifferenceInParameters(two));
  }
  
  @Test
  public void testHasDifferenceInParameters_DifferenceInInt(){
    final MindMapPanelConfig one = new MindMapPanelConfig();
    final MindMapPanelConfig two = new MindMapPanelConfig();
    
    one.setCollapsatorSize(two.getCollapsatorSize()+1);
    assertTrue(one.hasDifferenceInParameters(two));
  }
  
  @Test
  public void testHasDifferenceInParameters_DifferenceInColor(){
    final MindMapPanelConfig one = new MindMapPanelConfig();
    final MindMapPanelConfig two = new MindMapPanelConfig();
    
    one.setCollapsatorBackgroundColor(new Color(two.getCollapsatorBackgroundColor().getRGB()^0xFFFFFFFF));
    assertTrue(one.hasDifferenceInParameters(two));
  }
  
  
  @Test
  public void testHasDifferenceInParameters_DifferenceInFont(){
    final MindMapPanelConfig one = new MindMapPanelConfig();
    final MindMapPanelConfig two = new MindMapPanelConfig();
    
    one.setFont(two.getFont().deriveFont(17));
    assertTrue(one.hasDifferenceInParameters(two));
  }
  
  @Test
  public void testHasDifferenceInParameters_DifferenceInDouble(){
    final MindMapPanelConfig one = new MindMapPanelConfig();
    final MindMapPanelConfig two = new MindMapPanelConfig();
    
    one.setScale(two.getScale()+0.00001d);
    assertTrue(one.hasDifferenceInParameters(two));
  }
  
  @Test
  public void testSaveRestoreState(){
    final Map<String, Object> storage = new HashMap<String, Object>();
    final Preferences prefs = mock(Preferences.class);
     
    doAnswer(new Answer(){
      @Override
      public Object answer(final InvocationOnMock invocation) throws Throwable {
        final String key = invocation.getArgumentAt(0, String.class);
        final String value = invocation.getArgumentAt(1, String.class);
        storage.put(key,value);
        return null;
      }
    }).when(prefs).put(anyString(), anyString());
    
    doAnswer(new Answer(){
      @Override
      public Object answer(final InvocationOnMock invocation) throws Throwable {
        final String key = invocation.getArgumentAt(0, String.class);
        final Integer value = invocation.getArgumentAt(1, Integer.class);
        storage.put(key,value);
        return null;
      }
    }).when(prefs).putInt(anyString(), anyInt());
    
    doAnswer(new Answer(){
      @Override
      public Object answer(final InvocationOnMock invocation) throws Throwable {
        final String key = invocation.getArgumentAt(0, String.class);
        final Boolean value = invocation.getArgumentAt(1, Boolean.class);
        storage.put(key,value);
        return null;
      }
    }).when(prefs).putBoolean(anyString(), anyBoolean());
    
    doAnswer(new Answer(){
      @Override
      public Object answer(final InvocationOnMock invocation) throws Throwable {
        final String key = invocation.getArgumentAt(0, String.class);
        final Float value = invocation.getArgumentAt(1, Float.class);
        storage.put(key,value);
        return null;
      }
    }).when(prefs).putFloat(anyString(), anyFloat());
    
    doAnswer(new Answer(){
      @Override
      public Object answer(final InvocationOnMock invocation) throws Throwable {
        final String key = invocation.getArgumentAt(0, String.class);
        final Double value = invocation.getArgumentAt(1, Double.class);
        storage.put(key,value);
        return null;
      }
    }).when(prefs).putDouble(anyString(), anyDouble());

    when(prefs.get(anyString(), anyString())).thenAnswer(new Answer<String>(){
      @Override
      public String  answer(InvocationOnMock invocation) throws Throwable {
        final String key = invocation.getArgumentAt(0, String.class);
        final String def = invocation.getArgumentAt(1, String.class);
        return storage.containsKey(key) ? (String)storage.get(key) : def; 
      }
    });
    
    when(prefs.getBoolean(anyString(), anyBoolean())).thenAnswer(new Answer<Boolean>(){
      @Override
      public Boolean  answer(InvocationOnMock invocation) throws Throwable {
        final String key = invocation.getArgumentAt(0, String.class);
        final Boolean def = invocation.getArgumentAt(1, Boolean.class);
        return storage.containsKey(key) ? (Boolean)storage.get(key) : def; 
      }
    });
    
    when(prefs.getInt(anyString(), anyInt())).thenAnswer(new Answer<Integer>(){
      @Override
      public Integer  answer(InvocationOnMock invocation) throws Throwable {
        final String key = invocation.getArgumentAt(0, String.class);
        final Integer def = invocation.getArgumentAt(1, Integer.class);
        return storage.containsKey(key) ? (Integer)storage.get(key) : def; 
      }
    });
    
    when(prefs.getFloat(anyString(), anyFloat())).thenAnswer(new Answer<Float>(){
      @Override
      public Float  answer(InvocationOnMock invocation) throws Throwable {
        final String key = invocation.getArgumentAt(0, String.class);
        final Float def = invocation.getArgumentAt(1, Float.class);
        return storage.containsKey(key) ? (Float)storage.get(key) : def; 
      }
    });
    
    when(prefs.getDouble(anyString(), anyDouble())).thenAnswer(new Answer<Double>(){
      @Override
      public Double answer(InvocationOnMock invocation) throws Throwable {
        final String key = invocation.getArgumentAt(0, String.class);
        final Double def = invocation.getArgumentAt(1, Double.class);
        return storage.containsKey(key) ? (Double)storage.get(key) : def; 
      }
    });
    
    final MindMapPanelConfig config = new MindMapPanelConfig();
    
    config.setScale(100.5d);
    config.setGridColor(Color.orange);
    config.setShowGrid(false);
    config.setFont(new Font("Helvetica",Font.ITALIC,36));
    
    config.saveTo(prefs);
    assertFalse(storage.isEmpty());

    final MindMapPanelConfig newConfig = new MindMapPanelConfig();
    
    newConfig.loadFrom(prefs);

    assertFalse(newConfig.isShowGrid());
    assertEquals(Color.orange, newConfig.getGridColor());
    assertEquals(new Font("Helvetica", Font.ITALIC, 36), newConfig.getFont());
    assertEquals(100.5d, newConfig.getScale(),0.0d);
    
    storage.clear();
    
    newConfig.loadFrom(prefs);

    final MindMapPanelConfig etalon = new MindMapPanelConfig();
    
    assertEquals(etalon.isShowGrid(), newConfig.isShowGrid());
    assertEquals(etalon.getGridColor(), newConfig.getGridColor());
    assertEquals(etalon.getFont(),  newConfig.getFont());
    assertEquals(etalon.getScale(), newConfig.getScale(), 0.0d);
  }
      
  
}
