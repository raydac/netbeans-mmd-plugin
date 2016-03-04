package com.igormaznitsa.ideamindmap.lang.psi;

import com.igormaznitsa.ideamindmap.utils.IdeaUtils;
import com.igormaznitsa.meta.annotation.LazyInited;
import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.MMapURI;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URISyntaxException;
import java.nio.charset.Charset;

public class PsiExtraFile extends AbstractExtraData {

  private static final Logger LOGGER = LoggerFactory.getLogger(PsiExtraFile.class);

  private static final Charset UTF8 = Charset.forName("UTF-8");

  private volatile MMapURI uri;

  public PsiExtraFile(@NotNull final ASTNode node) {
    super(node);

    MMapURI theUri;
    try {
      theUri = new MMapURI(getName());
    }
    catch (URISyntaxException ex) {
      theUri = null;
    }

    this.uri = theUri;
  }

  @Nullable
  public MMapURI getMMapURI(){
    return this.uri;
  }

  public void setMMapURI(@Nullable final MMapURI uri){
    this.uri = uri;
  }

  @NotNull
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

  @Override public Extra.ExtraType getExtraType() {
    return Extra.ExtraType.FILE;
  }
}
