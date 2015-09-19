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
import com.igormaznitsa.nbmindmap.nb.refactoring.MutableFileLink;
import java.io.File;
import org.netbeans.api.project.Project;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class SafeDeleteFileActionPlugin extends AbstractPlugin<SafeDeleteRefactoring> {

  public SafeDeleteFileActionPlugin(final SafeDeleteRefactoring refactoring) {
    super(refactoring);
  }

  @Override
  public Problem preCheck() {
    return null;
  }

  @Override
  public Problem checkParameters() {
    return null;
  }

  @Override
  public Problem fastCheckParameters() {
    return null;
  }

  @Override
  public void cancelRequest() {
  }

  @Override
  protected Problem processFile(final Project project, final File projectFolder, final RefactoringElementsBag session, final FileObject fileObject) {
    final MMapURI fileAsURI = MMapURI.makeFromFilePath(projectFolder, fileObject.getPath(), null);

    for (final FileObject mmap : allMapsInProject(project)) {
      try {
        if (doesMindMapContainFileLink(project, mmap, fileAsURI)) {
          session.addFileChange(this.refactoring, new DeleteElement(new MutableFileLink(FileUtil.toFile(mmap)), projectFolder, MMapURI.makeFromFilePath(projectFolder, fileObject.getPath(), null)));
        }
      }
      catch (Exception ex) {
        ErrorManager.getDefault().notify(ex);
        return new Problem(true, "Error during mind map processing");
      }
    }

    return null;
  }
}
