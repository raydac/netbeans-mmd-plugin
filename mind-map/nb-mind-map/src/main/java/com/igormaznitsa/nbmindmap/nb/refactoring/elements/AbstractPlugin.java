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
package com.igormaznitsa.nbmindmap.nb.refactoring.elements;

import com.igormaznitsa.mindmap.model.MMapURI;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.nbmindmap.nb.refactoring.RefactoringUtils;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.api.Scope;
import org.netbeans.modules.refactoring.spi.ProgressProviderAdapter;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

public abstract class AbstractPlugin<T extends AbstractRefactoring> extends ProgressProviderAdapter implements RefactoringPlugin {

  protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractPlugin.class);
  protected final T refactoring;
  protected static final ResourceBundle BUNDLE = ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18n/Bundle");
  private final Map<FileObject, Collection<FileObject>> cache = new HashMap<FileObject, Collection<FileObject>>();

  private final List<RefactoringElementImplementation> elements = new ArrayList<RefactoringElementImplementation>();

  private final AtomicBoolean canceled = new AtomicBoolean(false);

  public AbstractPlugin(final T refactoring) {
    super();
    this.refactoring = refactoring;
  }

  protected void addElement(final RefactoringElementImplementation element) {
    synchronized (this.elements) {
      this.elements.add(element);
    }
  }

  protected Collection<FileObject> allMapsInProject(final Project project) {
    final Collection<? extends Scope> scopes = this.refactoring.getRefactoringSource().lookupAll(Scope.class);

    if (!scopes.isEmpty()) {
      final Collection<FileObject> mindMaps = new HashSet<FileObject>();
      for (final Scope s : scopes) {
        for (final NonRecursiveFolder f : s.getFolders()) {
          synchronized (this.cache) {
            Collection<FileObject> found = this.cache.get(f.getFolder());
            if (found == null) {
              found = RefactoringUtils.findAllMindMapsInFolder(f,this);
              this.cache.put(f.getFolder(), found);
            }
            mindMaps.addAll(found);
          }
        }
      }
      return mindMaps;
    }
    else {
      if (project == null) {
        return Collections.<FileObject>emptyList();
      }
      final FileObject projectFolder = project.getProjectDirectory();
      synchronized (this.cache) {
        Collection<FileObject> result = this.cache.get(projectFolder);
        if (result == null) {
          result = RefactoringUtils.findAllMindMapsInProject(project, this);
          this.cache.put(projectFolder, result);
        }
        return result;
      }
    }
  }

  private Collection<? extends FileObject> findFileObjectInLookup(final Lookup lookup) {
    final Collection<? extends FileObject> files = lookup.lookupAll(FileObject.class);
    final Collection<? extends NonRecursiveFolder> folders = lookup.lookupAll(NonRecursiveFolder.class);
    final Set<FileObject> result = new HashSet<FileObject>();
    for (final NonRecursiveFolder f : folders) {
      result.add(f.getFolder());
    }
    result.addAll(files);

    final Collection<? extends TreePathHandle> treePaths = lookup.lookupAll(TreePathHandle.class);
    for (final TreePathHandle h : treePaths) {
      result.add(h.getFileObject());
    }

    return result;
  }

  @Override
  public Problem checkParameters() {
    return null;
  }

  @Override
  public Problem preCheck() {
    return null;
  }

  @Override
  public Problem fastCheckParameters() {
    return null;
  }

  @Override
  public final Problem prepare(final RefactoringElementsBag session) {
    if (isCanceled()) {
      return null;
    }

    final Collection<? extends FileObject> files = findFileObjectInLookup(this.refactoring.getRefactoringSource());

    fireProgressListenerStart(RenameRefactoring.PREPARE, files.size());

    Problem result = null;

    try {
      for (final FileObject fileObject : findFileObjectInLookup(this.refactoring.getRefactoringSource())) {
        if (isCanceled()) {
          return null;
        }
        if (result != null) {
          break;
        }
        final Project project = FileOwnerQuery.getOwner(fileObject);
        result = processFileObject(project, fileObject);
        fireProgressListenerStep(1);
      }
    }
    finally {
      synchronized (this.elements) {
        LOGGER.info("Detected " + this.elements.size() + " elements for refactoring");
        if (!isCanceled()) {
          session.addAll(refactoring, this.elements);
        }
      }

      fireProgressListenerStop();
    }

    return result;
  }

  public Problem processFileObject(final Project project, final FileObject fileObject) {
    return _processFileObject(project, 0, fileObject);
  }
  
  private Problem _processFileObject(final Project project, int level, final FileObject fileObject) {
    final Project theProject;
    if (project == null) {
      theProject = FileOwnerQuery.getOwner(fileObject);
    }
    else {
      theProject = project;
    }
    
    if (theProject == null){
      LOGGER.warn("Request process file object without a project as the owner : "+fileObject);
      return null;
    }
    
    final FileObject projectDirectory = theProject.getProjectDirectory();
    
    if (projectDirectory == null){
      LOGGER.warn("Request process file object in a project which doesn't have folder : " + fileObject+", project : "+project);
      return null;
    }
    
    final File projectFolder = FileUtil.toFile(projectDirectory);

    Problem result = processFile(theProject, level, projectFolder, fileObject);
    level++;
    if (fileObject.isFolder()) {
      for (final FileObject fo : fileObject.getChildren()) {
        if (result != null) {
          break;
        }

        if (fo.isFolder()) {
          result = _processFileObject(theProject, level, fo);
        }
        else {
          result = processFile(theProject, level, projectFolder, fo);
        }
      }
    }

    return result;
  }

  protected abstract Problem processFile(Project project, int level, File projectFolder, FileObject fileObject);

  protected boolean doesMindMapContainFileLink(final Project project, final FileObject mindMap, final MMapURI fileToCheck) throws IOException {
    final FileObject baseFolder = project.getProjectDirectory();
    try {
      final MindMap parsedMap = new MindMap(new StringReader(mindMap.asText("UTF-8"))); //NOI18N
      return parsedMap.doesContainFileLink(FileUtil.toFile(baseFolder), fileToCheck);
    }
    catch (IllegalArgumentException ex) {
      // not mind map
      return false;
    }
  }

  @Override
  public final void cancelRequest() {
    this.canceled.set(true);
  }

  public boolean isCanceled() {
    return this.canceled.get();
  }

}
