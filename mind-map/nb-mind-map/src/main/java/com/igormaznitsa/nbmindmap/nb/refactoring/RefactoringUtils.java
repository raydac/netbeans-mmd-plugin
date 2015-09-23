/*
 * Copyright 2015 Igor Maznitsa.
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
package com.igormaznitsa.nbmindmap.nb.refactoring;

import com.igormaznitsa.nbmindmap.nb.MMDDataObject;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

public enum RefactoringUtils {

  ;
    
    public static List<FileObject> findAllMindMapsInProject(final Project project) {
    final List<FileObject> result = new ArrayList<>();

    final Sources sources = ProjectUtils.getSources(project);
    final SourceGroup[] groups = sources.getSourceGroups(Sources.TYPE_GENERIC);

    for (final SourceGroup g : groups) {
      final Collection<File> files = FileUtils.listFiles(FileUtil.toFile(g.getRootFolder()), new String[]{"mmd", "MMD", "Mmd"}, true);
      for (final File f : files) {
        final FileObject fo = FileUtil.toFileObject(f);
        result.add(fo);
      }
    }
    return result;
  }

  public static boolean hasOnlyMMDNodes(final Lookup lookup){
    final Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
    boolean result = false;
    for(final Node n : nodes){
      final FileObject fo = n.getLookup().lookup(FileObject.class);
      if (fo != null){
        if (!isMMD(fo)){
            result = false;
            break;
        }else{
          result = true;
        }
      }
    }
    return result;
  }
  
  public static FileObject getMMD(final Lookup lookup){
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
  
  public static boolean isFromEditor(final Lookup lookup) {
    final EditorCookie cookie = lookup.lookup(EditorCookie.class);
    return cookie != null && cookie.getOpenedPanes() != null;
  }
  
  public static boolean isFileInOpenProject(final FileObject fo){
    final Project p = FileOwnerQuery.getOwner(fo);
    return OpenProjects.getDefault().isProjectOpen(p);
  }
  
  public static boolean isMMD(final FileObject fo){
    return fo!=null &&  MMDDataObject.MIME.equals(fo.getMIMEType());
  }

}
