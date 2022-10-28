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
import com.igormaznitsa.nbmindmap.nb.refactoring.MindMapLink;
import java.io.File;
import java.net.URISyntaxException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;

public class WhereUsedActionPlugin extends AbstractPlugin<WhereUsedQuery> {

  public WhereUsedActionPlugin(final WhereUsedQuery refactoring) {
    super(refactoring);
  }

  @Override
  protected Problem processFile(final Project project, final int level, final File projectFolder,
                                final FileObject fileObject) {
    final MMapURI fileAsURI;
    try {
      fileAsURI = MMapURI.makeFromFilePath(projectFolder, fileObject.getPath(), null);
    } catch (URISyntaxException ex) {
      return new Problem(false, "Malformed URI syntax: " + ex.getMessage());
    }

    for (final FileObject mmap : allMapsInProject(project)) {
      if (isCanceled()) {
        break;
      }
      try {
        if (doesMindMapContainFileLink(project, mmap, fileAsURI)) {
          addElement(new WhereUsedElement(new MindMapLink(mmap), projectFolder,
              MMapURI.makeFromFilePath(projectFolder, fileObject.getPath(), null)));
        }
      } catch (Exception ex) {
        ErrorManager.getDefault().notify(ex);
        return new Problem(true, BUNDLE.getString("Refactoring.CantProcessMindMap"));
      }
    }

    return null;
  }
}
