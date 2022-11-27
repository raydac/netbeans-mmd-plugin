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

package com.igormaznitsa.ideamindmap.lang.refactoring;

import com.igormaznitsa.ideamindmap.lang.psi.PsiExtraFile;
import com.igormaznitsa.ideamindmap.utils.IdeaUtils;
import com.igormaznitsa.mindmap.model.MMapURI;
import com.igormaznitsa.mindmap.model.MindMap;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.FileContentUtil;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import javax.annotation.Nonnull;

public final class RefactoringUtils {
  private RefactoringUtils() {
  }

  public static MMapURI makeNewMMapUri(@Nonnull final Project project, @Nonnull final MMapURI oldFile, @Nonnull VirtualFile newFile)
   throws URISyntaxException {
    final File projectFolder = IdeaUtils.findProjectFolder(project);
    if (projectFolder == null) {
      throw new NullPointerException("Project folder is not found for " + project);
    }

    URI baseURI = VfsUtil.toUri(IdeaUtils.vfile2iofile(newFile));
    if (baseURI.isAbsolute()) {
      final URI projectURI = VfsUtil.toUri(projectFolder);
      baseURI = projectURI.relativize(baseURI);
    }

    return MMapURI.makeFromFilePath(projectFolder, baseURI.toString(), oldFile.getParameters());
  }

  @Nonnull
  public static String replaceMMUriToNewFile(@Nonnull final PsiExtraFile mindMapFile, @Nonnull final MMapURI oldFile, @Nonnull final MMapURI newFile) throws IOException {
    final File projectFolder = IdeaUtils.findProjectFolder(mindMapFile);
    if (projectFolder == null) {
      throw new NullPointerException("Project folder is not found for " + mindMapFile);
    }

    final MindMap parsedMap = new MindMap(new StringReader(mindMapFile.getContainingFile().getText()));
    parsedMap.replaceAllLinksToFile(projectFolder, oldFile, newFile);

    return parsedMap.write(new StringWriter(16384)).toString();
  }

  public static void reparseFile(final PsiFile file) {
    ApplicationManager.getApplication().runReadAction(() -> FileContentUtil.reparseFiles(file.getProject(), Collections.singletonList(file.getVirtualFile()), true));
  }
}
