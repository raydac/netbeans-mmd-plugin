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

package com.igormaznitsa.ideamindmap.lang.psi;

import com.igormaznitsa.ideamindmap.utils.IdeaUtils;
import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.MMapURI;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PsiExtraFile extends AbstractExtraData {

  private static final Logger LOGGER = LoggerFactory.getLogger(PsiExtraFile.class);

  private static final Charset UTF8 = StandardCharsets.UTF_8;

  private volatile MMapURI uri;

  public PsiExtraFile(@Nonnull final ASTNode node) {
    super(node);

    MMapURI theUri;
    try {
      theUri = new MMapURI(getName());
    } catch (URISyntaxException ex) {
      theUri = null;
    }

    this.uri = theUri;
  }

  @Nullable
  public MMapURI getMMapURI() {
    return this.uri;
  }

  public void setMMapURI(@Nullable final MMapURI uri) {
    this.uri = uri;
  }

  @Nonnull
  @Override
  public PsiReference[] getReferences() {
    return ReferenceProvidersRegistry.getReferencesFromProviders(this);
  }

  @Nullable
  public VirtualFile findTargetFile() {
    VirtualFile result = null;
    if (this.uri != null) {
      final Project project = getProject();
      final VirtualFile baseDir = project.getBaseDir();
      if (baseDir != null) {
        result = VfsUtil.findFileByIoFile(this.uri.asFile(IdeaUtils.vfile2iofile(baseDir)), true);
      }
    }
    return result;
  }

  @Override
  public Extra.ExtraType getExtraType() {
    return Extra.ExtraType.FILE;
  }
}
