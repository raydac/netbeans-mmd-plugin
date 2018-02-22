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
package com.igormaznitsa.mindmap.plugins;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;
import com.igormaznitsa.mindmap.plugins.api.AbstractExporter;

public class MindMapPluginRegistryTest {
  
  @Test
  public void testInitializationAndCaching() {
    final MindMapPluginRegistry registry = MindMapPluginRegistry.getInstance();
    final List<AbstractExporter> exportPlugins1 = registry.findFor(AbstractExporter.class);
    final List<AbstractExporter> exportPlugins2 = registry.findFor(AbstractExporter.class);
    assertFalse(exportPlugins1.isEmpty());
    assertFalse(exportPlugins2.isEmpty());
    assertSame(exportPlugins1,exportPlugins2);
    try{
      exportPlugins1.set(0,null);
      fail("Must be immutable list");
    }catch(UnsupportedOperationException ex){
      // ignore
    }
  }
  
}
