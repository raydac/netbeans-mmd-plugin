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
package com.igormaznitsa.nbmindmap.nb.refactoring;

import com.igormaznitsa.nbmindmap.nb.editor.MMDDataObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.nbmindmap.nb.refactoring.elements.AbstractPlugin;

public final class RefactoringUtils {

  private RefactoringUtils() {
  }

  @Nonnull
  public static Collection<FileObject> findAllMindMapsInFolder(@Nullable final NonRecursiveFolder folder, @Nullable final AbstractPlugin plugin) {
    final FileObject folderFile = folder == null ? null : folder.getFolder();

    if (folderFile == null) {
      return Collections.<FileObject>emptyList();
    } else {
      final List<FileObject> result = new ArrayList<FileObject>();
      final Enumeration<? extends FileObject> e = folderFile.getChildren(true);

      while (e.hasMoreElements()) {
        if (plugin != null && plugin.isCanceled()) {
          break;
        }

        final FileObject nxt = e.nextElement();
        if (nxt.isData() && nxt.hasExt("mmd")) {
          result.add(nxt);
        }
      }

      return result;
    }
  }

  @Nonnull
  @MustNotContainNull
  public static List<FileObject> findAllMindMapsInProject(@Nonnull final Project project, @Nullable final AbstractPlugin plugin) {
    final List<FileObject> result = new ArrayList<FileObject>();

    final Sources sources = ProjectUtils.getSources(project);
    final SourceGroup[] groups = sources.getSourceGroups(Sources.TYPE_GENERIC);

    for (final SourceGroup g : groups) {
      if (plugin != null && plugin.isCanceled()) {
        return result;
      }
      final FileObject gobject = g.getRootFolder();
      if (gobject != null) {
        final Enumeration<? extends FileObject> e = gobject.getChildren(true);
        while (e.hasMoreElements()) {
          if (plugin != null && plugin.isCanceled()) {
            return result;
          }
          final FileObject nxt = e.nextElement();
          if (nxt.isData() && nxt.hasExt("mmd")) {
            result.add(nxt);
          }
        }
      }
    }
    return result;
  }

  public static boolean hasOnlyMMDNodes(final Lookup lookup) {
    final Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
    boolean result = false;
    for (final Node n : nodes) {
      final FileObject fo = n.getLookup().lookup(FileObject.class);
      if (fo != null) {
        if (!isMMD(fo)) {
          result = false;
          break;
        } else {
          result = true;
        }
      }
    }
    return result;
  }

  public static FileObject getMMD(final Lookup lookup) {
    final Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
    FileObject result = null;
    for (final Node n : nodes) {
      final FileObject fo = n.getLookup().lookup(FileObject.class);
      if (fo != null) {
        if (isMMD(fo)) {
          result = fo;
          break;
        }
      }
    }
    return result;
  }

  public static FileObject[] getMMDs(final Lookup lookup) {
    final Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
    final List<FileObject> result = new ArrayList<FileObject>();
    for (final Node n : nodes) {
      final FileObject fo = n.getLookup().lookup(FileObject.class);
      if (fo != null) {
        if (isMMD(fo)) {
          result.add(fo);
        }
      }
    }
    return result.toArray(new FileObject[result.size()]);
  }

  public static boolean isFromEditor(final Lookup lookup) {
    final EditorCookie cookie = lookup.lookup(EditorCookie.class);
    return cookie != null && cookie.getOpenedPanes() != null;
  }

  public static boolean isFileInOpenProject(final FileObject fo) {
    final Project p = FileOwnerQuery.getOwner(fo);
    return OpenProjects.getDefault().isProjectOpen(p);
  }

  public static boolean isMMD(final FileObject fo) {
    return fo != null && MMDDataObject.MMD_EXT.equalsIgnoreCase(fo.getExt());
  }

}
