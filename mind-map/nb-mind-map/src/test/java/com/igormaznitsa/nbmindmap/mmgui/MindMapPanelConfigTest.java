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
package com.igormaznitsa.nbmindmap.mmgui;

import com.igormaznitsa.mindmap.swing.panel.MindMapConfigListener;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.Assert.*;
import org.junit.Test;

public class MindMapPanelConfigTest {
  
  @Test
  public void testMakeFullCopyOf(){
    final MindMapPanelConfig dst = new MindMapPanelConfig();
    final MindMapPanelConfig src = new MindMapPanelConfig();
    
    assertTrue(dst.isDropShadow());
    assertTrue(dst.isDrawBackground());
    
    src.setDrawBackground(false);
    src.setDropShadow(false);

    final AtomicInteger callCounter = new AtomicInteger();
    
    final MindMapConfigListener lstnr = new MindMapConfigListener() {
      @Override
      public void onConfigurationPropertyChanged(MindMapPanelConfig changedConfig) {
        callCounter.incrementAndGet();
      }
    };
    
    src.addConfigurationListener(lstnr);
    
    
    dst.makeFullCopyOf(src, true, true);
    
    assertFalse(dst.isDropShadow());
    assertFalse(dst.isDrawBackground());
    assertEquals(1,callCounter.get());
  }
  
}
