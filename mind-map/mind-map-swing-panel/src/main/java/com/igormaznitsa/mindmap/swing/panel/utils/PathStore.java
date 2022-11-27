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

package com.igormaznitsa.mindmap.swing.panel.utils;

import com.igormaznitsa.mindmap.plugins.api.PluginContext;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Auxiliary thread safe class allows to keep some non-null file folder path associated
 * with non-null string based id. If there is not any associated folder then user.home in use.
 *
 * @since 1.4.7
 */
public class PathStore {
  private final Map<String, File> internalMap = new HashMap<>();
  private static final File USER_HOME = new File(System.getProperty("user.home"));

  public synchronized File find(final PluginContext context, final String id) {
    File result = internalMap.get(id);
    final File projectFolder = context == null ? null : context.getProjectFolder();
    if (result == null) {
      result = projectFolder == null ? USER_HOME : projectFolder;
    }
    return result;
  }

  public synchronized File put(final String id, final File file) {
    if (file == null) {
      return null;
    }

    final File folder = file.isDirectory() ? file : file.getParentFile();
    if (folder != null) {
      internalMap.put(id, folder);
    }
    return file;
  }
}
