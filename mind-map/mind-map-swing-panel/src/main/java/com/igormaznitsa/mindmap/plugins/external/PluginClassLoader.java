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

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.igormaznitsa.meta.common.utils.Assertions;

public class PluginClassLoader extends URLClassLoader {
  
  private final JarURLConnection connection;
  private final File pluginFile;
  private final Map<String,String> attributes;
  
  public PluginClassLoader(@Nonnull final File pluginFile) throws IOException {
    super(new URL[]{Assertions.assertNotNull(pluginFile).toURI().toURL()});
    this.pluginFile = pluginFile;
    this.connection = (JarURLConnection) new URL("jar", "", pluginFile.toURI() + "!/").openConnection();
    
    final Manifest manifest = this.connection.getManifest();
    Map<String,String> detectedAttributes = null;
    
    if (manifest != null) {
      final Attributes pluginAttributes = manifest.getEntries().get("nb-mindmap-plugin");
      if (pluginAttributes!=null){
        detectedAttributes = new HashMap<String, String>();
        for(final Object key : pluginAttributes.keySet()){
          final String keyAsText = key.toString();
          detectedAttributes.put(keyAsText, pluginAttributes.getValue(keyAsText));
        }
      }
    }
    if (detectedAttributes == null) throw new IllegalArgumentException("File is not a NB mind map plugin");
    this.attributes = Collections.unmodifiableMap(detectedAttributes);
  }
  
  @Nullable
  public String getAttributes(@Nonnull final Attribute attr){
    return this.attributes.get(attr.getAttrName());
  }
  
  @Nonnull
  public File getFile(){
    return this.pluginFile;
  }
  
}
