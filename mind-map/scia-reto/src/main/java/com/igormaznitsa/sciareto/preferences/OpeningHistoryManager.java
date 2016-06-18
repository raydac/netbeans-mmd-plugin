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
package com.igormaznitsa.sciareto.preferences;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterInputStream;
import java.util.zip.DeflaterOutputStream;
import javax.annotation.Nonnull;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FilenameUtils;
import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.meta.common.utils.IOUtils;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;

public class OpeningHistoryManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(OpeningHistoryManager.class);

  private static final OpeningHistoryManager INSTANCE = new OpeningHistoryManager();

  private static final String LAST_OPENED_PROJECTS = "last.projects.opened";
  private static final String LAST_OPENED_FILES = "last.files.opened";

  private final List<File> lastOpenedProjects = new ArrayList<>();
  private final List<File> lastOpenedFiles = new ArrayList<>();

  private static final int MAX_PROJECTS = 10;
  private static final int MAX_FILES = 10;

  private OpeningHistoryManager() {
    final String projectsStr = PreferencesManager.getInstance().getPreferences().get(LAST_OPENED_PROJECTS, null);
    final String filesStr = PreferencesManager.getInstance().getPreferences().get(LAST_OPENED_FILES, null);
    try {
      final String[] folders = projectsStr == null ? new String[0] : decodeString(projectsStr).split("\\" + File.pathSeparatorChar);
      final String[] files = filesStr == null ? new String[0] : decodeString(filesStr).split("\\" + File.pathSeparatorChar);
      fillList(folders, this.lastOpenedProjects);
      fillList(files, this.lastOpenedFiles);
    } catch (Exception ex) {
      throw new Error("Can't init module", ex);
    }
  }

  private String encodeString(@Nonnull final String str) throws IOException {
    return Base64.encodeBase64String(IOUtils.packData(str.getBytes("UTF-8")));
  }

  private String decodeString(@Nonnull final String str) throws IOException {
    return new String(IOUtils.unpackData(Base64.decodeBase64(str)), "UTF-8");
  }

  private static String packToString(final List<File> list) {
    final StringBuilder result = new StringBuilder();
    for (final File f : list) {
      if (result.length() > 0) {
        result.append(File.pathSeparator);
      }
      result.append(FilenameUtils.normalize(f.getAbsolutePath()));
    }
    return result.toString();
  }

  private static void fillList(final String[] paths, final List<File> list) {
    list.clear();
    for (final String s : paths) {
      if (s != null && !s.trim().isEmpty()) {
        list.add(new File(s));
      }
    }
  }

  private void flush() {
    try {
      PreferencesManager.getInstance().getPreferences().flush();
    } catch (BackingStoreException ex) {
      LOGGER.error("Can't flush preferences", ex);
    }
  }

  public void registerOpenedProject(@Nonnull final File folder) throws IOException {
    if (folder.isDirectory()) {
      synchronized (this.lastOpenedProjects) {
        this.lastOpenedProjects.remove(folder);
        this.lastOpenedProjects.add(0, folder);
        while (this.lastOpenedProjects.size() > MAX_PROJECTS) {
          this.lastOpenedProjects.remove(this.lastOpenedProjects.size() - 1);
        }

        PreferencesManager.getInstance().getPreferences().put(LAST_OPENED_PROJECTS, encodeString(packToString(this.lastOpenedProjects)));
        flush();
      }
    }
  }

  public void registerOpenedFile(@Nonnull final File file) throws IOException {
    if (file.isFile()) {
      synchronized (this.lastOpenedFiles) {
        this.lastOpenedFiles.remove(file);
        this.lastOpenedFiles.add(0, file);
        while (this.lastOpenedFiles.size() > MAX_FILES) {
          this.lastOpenedFiles.remove(this.lastOpenedFiles.size() - 1);
        }
        PreferencesManager.getInstance().getPreferences().put(LAST_OPENED_FILES, encodeString(packToString(this.lastOpenedFiles)));
        flush();
      }
    }
  }

  @Nonnull
  @MustNotContainNull
  public File[] getLastOpenedProjects() {
    synchronized (this.lastOpenedProjects) {
      return this.lastOpenedProjects.toArray(new File[this.lastOpenedProjects.size()]);
    }
  }

  @Nonnull
  @MustNotContainNull
  public File[] getLastOpenedFiles() {
    synchronized (this.lastOpenedFiles) {
      return this.lastOpenedFiles.toArray(new File[this.lastOpenedFiles.size()]);
    }
  }

  @Nonnull
  public static OpeningHistoryManager getInstance() {
    return INSTANCE;
  }

}
