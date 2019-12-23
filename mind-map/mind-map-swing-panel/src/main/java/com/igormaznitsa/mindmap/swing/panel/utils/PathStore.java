package com.igormaznitsa.mindmap.swing.panel.utils;

import com.igormaznitsa.mindmap.plugins.api.PluginContext;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Auxiliary thread safe class allows to keep some non-null file folder path associated
 * with non-null string based id. If there is not any associated folder then user.home in use.
 *
 * @since 1.4.7
 */
public class PathStore {
  private final Map<String, File> internalMap = new HashMap<>();
  private static final File USER_HOME = new File(System.getProperty("user.home"));

  @Nonnull
  public synchronized File find(@Nullable final PluginContext context, @Nonnull final String id) {
    File result = internalMap.get(id);
    final File projectFolder = context == null ? null : context.getProjectFolder();
    if (result == null) {
      result = projectFolder == null ? USER_HOME : projectFolder;
    }
    return result;
  }

  @Nullable
  public synchronized File put(@Nonnull final String id, @Nullable File file) {
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
