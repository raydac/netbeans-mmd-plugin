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
import javax.lang.model.SourceVersion;
import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.meta.common.utils.Assertions;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.commons.version.Version;
import com.igormaznitsa.commons.version.VersionValidator;

public class PluginClassLoader extends URLClassLoader {

  private final JarURLConnection connection;
  private final File pluginFile;
  private final Map<String, String> attributes;

  private static final Logger LOGGER = LoggerFactory.getLogger(PluginClassLoader.class);

  private final Version apiVersion;
  private VersionValidator compatibilityValidator;

  public PluginClassLoader(@Nonnull final File pluginFile) throws IOException {
    super(new URL[]{Assertions.assertNotNull(pluginFile).toURI().toURL()},PluginClassLoader.class.getClassLoader());
    this.pluginFile = pluginFile;
    this.connection = (JarURLConnection) new URL("jar", "", pluginFile.toURI() + "!/").openConnection();

    final Manifest manifest = this.connection.getManifest();
    Map<String, String> detectedAttributes = null;

    if (manifest != null) {
      final Attributes pluginAttributes = manifest.getEntries().get("nb-mindmap-plugin");
      if (pluginAttributes != null) {
        detectedAttributes = new HashMap<String, String>();
        for (final Object key : pluginAttributes.keySet()) {
          final String keyAsText = key.toString();
          detectedAttributes.put(keyAsText, pluginAttributes.getValue(keyAsText));
        }
      }
    }
    if (detectedAttributes == null) {
      throw new IllegalArgumentException("File is not a NB mind map plugin");
    }
    this.attributes = Collections.unmodifiableMap(detectedAttributes);
    this.apiVersion = new Version(this.attributes.get(Attribute.API.getAttrName()));
  }

  @Nonnull
  @MustNotContainNull
  public String[] extractPluginClassNames() {
    final String classNameList = this.getAttributes(Attribute.PLUGINS);
    String[] result;
    if (classNameList == null) {
      result = new String[0];
    } else {
      final String[] splitted = classNameList.split("\\,");
      result = new String[splitted.length];
      for (int i = 0; i < splitted.length; i++) {
        final String str = splitted[i].trim();
        if (SourceVersion.isName(str)) {
          result[i] = str;
        } else {
          LOGGER.error("Detected illegal plugin class name " + str + " at " + this.pluginFile);
        }
      }
    }
    return result;
  }

  @Nullable
  public Version extractVersion() {
    final String version = getAttributes(Attribute.VERSION);
    return version == null ? null : new Version(version);
  }

  public boolean isCompatibleWithIde(@Nonnull final Version ideVersion) {
    final String compatible = this.getAttributes(Attribute.COMPATIBLE);
    if (compatible == null) {
      return false;
    }
    if (compatible.trim().equals("*")) {
      return true;
    }
    if (this.compatibilityValidator == null) {
      this.compatibilityValidator = new VersionValidator(compatible);
    }
    return this.compatibilityValidator.isValid(ideVersion);
  }

  @Nonnull
  public Version getApiVersion() {
    return this.apiVersion;
  }

  @Nullable
  public String getAttributes(@Nonnull final Attribute attr) {
    return this.attributes.get(attr.getAttrName());
  }

  @Nonnull
  public File getFile() {
    return this.pluginFile;
  }

}
