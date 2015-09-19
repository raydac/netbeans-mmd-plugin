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
package com.igormaznitsa.nbmindmap.nb.refactoring.elements;

import com.igormaznitsa.mindmap.model.MMapURI;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.nbmindmap.nb.refactoring.RefactoringUtils;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractPlugin<T extends AbstractRefactoring> implements RefactoringPlugin {

  protected static final Logger logger = LoggerFactory.getLogger(AbstractPlugin.class);

  protected final T refactoring;

  private final Map<FileObject, List<FileObject>> cache = new HashMap<>();

  public AbstractPlugin(final T refactoring) {
    this.refactoring = refactoring;
  }

  protected List<FileObject> allMapsInProject(final Project project) {
    if (project == null) {
      return Collections.<FileObject>emptyList();
    }
    final FileObject projectFolder = project.getProjectDirectory();

    List<FileObject> result = this.cache.get(projectFolder);
    if (result == null) {
      result = RefactoringUtils.findAllMindMapsInProject(project);
      this.cache.put(projectFolder, result);
    }
    return result;
  }

  @Override
  public Problem prepare(final RefactoringElementsBag session) {
    Problem result = null;

    for (final FileObject fileObject : this.refactoring.getRefactoringSource().lookupAll(FileObject.class)) {
      if (result != null) break;
      
      final Project project = FileOwnerQuery.getOwner(fileObject);
      result = processFileObject(project, 0, session, fileObject);
    }

    return result;
  }

  private Problem processFileObject(final Project project, int level, final RefactoringElementsBag session, final FileObject fileObject) {
    final Project theProject;
    if (project == null) {
      theProject = FileOwnerQuery.getOwner(fileObject);
    }
    else {
      theProject = project;
    }
    final File projectFolder = FileUtil.toFile(theProject.getProjectDirectory());

    Problem result = processFile(theProject, level, projectFolder, session, fileObject);
    level++;
    if (fileObject.isFolder()) {
      for (final FileObject fo : fileObject.getChildren()) {
        if (result != null) {
          break;
        }

        if (fo.isFolder()) {
          result = processFileObject(theProject, level, session, fo);
        }
        else {
          result = processFile(theProject, level, projectFolder, session, fo);
        }
      }
    }

    return result;
  }

  protected abstract Problem processFile(Project project, int level, File projectFolder, RefactoringElementsBag session, FileObject fileObject);

  protected boolean doesMindMapContainFileLink(final Project project, final FileObject mindMap, final MMapURI fileToCheck) throws IOException {
    final FileObject baseFolder = project.getProjectDirectory();
    try {
      final MindMap parsedMap = new MindMap(new StringReader(mindMap.asText("UTF-8")));
      return parsedMap.doesContainFileLink(FileUtil.toFile(baseFolder), fileToCheck);
    }
    catch (IllegalArgumentException ex) {
      // not mind map
      return false;
    }
  }
  
}
