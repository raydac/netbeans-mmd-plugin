package com.igormaznitsa.mindmap.swing.panel.utils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class IdToFileMap {
  private final Map<String, File> internalMap = new HashMap<>();
  private static final File USER_HOME = new File(System.getProperty("user.home"));

  @Nonnull
  public synchronized File find(@Nonnull final String id) {
    final File result = internalMap.get(id);
    return result == null ? USER_HOME : result;
  }

  @Nullable
  public synchronized File register(@Nonnull final String id, @Nullable File file) {
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
