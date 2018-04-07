/* 
 * Copyright (C) 2018 Igor Maznitsa.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package com.igormaznitsa.sciareto.preferences;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.meta.common.utils.IOUtils;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;

public final class FileHistoryManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileHistoryManager.class);

  private static final FileHistoryManager INSTANCE = new FileHistoryManager();

  private static final String LAST_OPENED_PROJECTS = "last.projects.opened"; //NOI18N
  private static final String LAST_OPENED_FILES = "last.files.opened"; //NOI18N
  private static final String ACTIVE_PROJECTS = "projects.active"; //NOI18N
  private static final String ACTIVE_FILES = "files.active"; //NOI18N

  private final List<File> lastOpenedProjects = new ArrayList<>();
  private final List<File> lastOpenedFiles = new ArrayList<>();

  private static final int MAX_PROJECTS = 10;
  private static final int MAX_FILES = 10;

  private FileHistoryManager() {
    final String projectsStr = PreferencesManager.getInstance().getPreferences().get(LAST_OPENED_PROJECTS, null);
    final String filesStr = PreferencesManager.getInstance().getPreferences().get(LAST_OPENED_FILES, null);
    try {
      final String[] folders = projectsStr == null ? new String[0] : decodeString(projectsStr).split("\\" + File.pathSeparatorChar); //NOI18N
      final String[] files = filesStr == null ? new String[0] : decodeString(filesStr).split("\\" + File.pathSeparatorChar); //NOI18N
      fillList(folders, this.lastOpenedProjects);
      fillList(files, this.lastOpenedFiles);
    } catch (Exception ex) {
      throw new Error("Can't init module", ex); //NOI18N
    }
  }

  @Nonnull
  @MustNotContainNull
  public File [] getActiveProjects() throws IOException {
    final String activeProjectsStr = PreferencesManager.getInstance().getPreferences().get(ACTIVE_PROJECTS, null);
    final File [] result;
    if (activeProjectsStr == null){
      result = new File[0];
    } else {
      final List<File> list = new ArrayList<>();
      fillList(decodeString(activeProjectsStr).split("\\"+File.pathSeparatorChar), list); //NOI18N
      result = list.toArray(new File[list.size()]);
    }
    return result;
  }
  
  @Nonnull
  @MustNotContainNull
  public File [] getActiveFiles() throws IOException{
    final String activeProjectsStr = PreferencesManager.getInstance().getPreferences().get(ACTIVE_FILES, null);
    final File [] result;
    if (activeProjectsStr == null){
      result = new File[0];
    } else {
      final List<File> list = new ArrayList<>();
      fillList(decodeString(activeProjectsStr).split("\\"+File.pathSeparatorChar), list); //NOI18N
      result = list.toArray(new File[list.size()]);
    }
    return result;
  }

  public synchronized void saveActiveProjects(@Nonnull @MustNotContainNull final File [] projectFolders) throws IOException{
    PreferencesManager.getInstance().getPreferences().put(ACTIVE_PROJECTS, encodeString(packToString(projectFolders)));
    PreferencesManager.getInstance().flush();
  }
  
  public synchronized void saveActiveFiles(@Nonnull @MustNotContainNull final File [] files) throws IOException{
    PreferencesManager.getInstance().getPreferences().put(ACTIVE_FILES, encodeString(packToString(files)));
    PreferencesManager.getInstance().flush();
  }
  
  @Nonnull
  private String encodeString(@Nonnull final String str) throws IOException {
    return Base64.encodeBase64String(IOUtils.packData(str.getBytes("UTF-8"))); //NOI18N
  }

  @Nonnull
  private String decodeString(@Nonnull final String str) throws IOException {
    return new String(IOUtils.unpackData(Base64.decodeBase64(str)), "UTF-8"); //NOI18N
  }

  @Nonnull
  private static String packToString(@Nonnull @MustNotContainNull final List<File> files) {
    return packToString(files.toArray(new File[files.size()]));
  }  
  
  @Nonnull
  private static String packToString(@Nonnull @MustNotContainNull final File[] files) {
    final StringBuilder result = new StringBuilder();
    for (final File f : files) {
      if (result.length() > 0) {
        result.append(File.pathSeparatorChar);
      }
      result.append(FilenameUtils.normalize(f.getAbsolutePath()));
    }
    return result.toString();
  }

  private static void fillList(@Nonnull @MustNotContainNull final String[] paths, @Nonnull @MustNotContainNull final List<File> list) {
    list.clear();
    for (final String s : paths) {
      if (StringUtils.isNotBlank(s)) {
        list.add(new File(s));
      }
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
        PreferencesManager.getInstance().flush();
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
        PreferencesManager.getInstance().flush();
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
  public static FileHistoryManager getInstance() {
    return INSTANCE;
  }

}
