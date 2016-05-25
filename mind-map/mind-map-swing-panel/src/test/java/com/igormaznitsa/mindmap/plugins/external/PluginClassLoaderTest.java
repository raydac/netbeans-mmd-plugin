/*
 * Copyright 2016 Igor Maznitsa.
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
package com.igormaznitsa.mindmap.plugins.external;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.File;

public class PluginClassLoaderTest {
  @Test
  public void testLoadingOfJar() throws Exception {
    final File jarFile = new File("src/test/resources/com/igormaznitsa/mindmap/plugins/external/TestPlugin.jar");
    assertTrue(jarFile.isFile());
   final PluginClassLoader loader = new PluginClassLoader(jarFile);
   assertEquals("Igor Maznitsa",loader.getAttributes(Attribute.AUTHOR));
   assertEquals("*",loader.getAttributes(Attribute.COMPATIBLE));
   assertEquals("1.0.1-SNAPSHOT",loader.getAttributes(Attribute.VERSION));
   assertEquals("It is just a test of class loader",loader.getAttributes(Attribute.REFERENCE));
   assertEquals("1.2.0",loader.getAttributes(Attribute.API));
   assertEquals("Some Test Plugin",loader.getAttributes(Attribute.TITLE));
   assertEquals("com.igormaznitsa.testmmdplugin.TestPlugin",loader.getAttributes(Attribute.PLUGINS));
  }

}
