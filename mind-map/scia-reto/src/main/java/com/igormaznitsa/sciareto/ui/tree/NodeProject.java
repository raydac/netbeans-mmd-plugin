/*
 * Copyright 2016 Igor Maznitsa.
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
package com.igormaznitsa.sciareto.ui.tree;

import java.io.File;
import java.nio.file.Files;
import javax.annotation.Nonnull;
import com.igormaznitsa.meta.common.utils.Assertions;
import javax.annotation.Nullable;

public class NodeProject  extends NodeFileOrFolder {
  
  private volatile File folder;
  
  public NodeProject(@Nonnull final NodeProjectGroup group, @Nonnull final File folder) {
    super(group, true, folder.getName(), !Files.isWritable(folder.toPath()));
    this.folder = folder;
    reloadSubtree();
  }

  @Override
  public void setName(@Nonnull final String name){
    this.name = name;
    this.folder = new File(folder.getParentFile(),name);
    reloadSubtree();
  }
  
  @Override
  @Nullable
  public File makeFileForNode() {
    return this.folder;
  }

  @Nonnull
  public File getFolder(){
    return this.folder;
  }
  
  public void setFolder(@Nonnull final File folder){
    Assertions.assertTrue("Must be directory", folder.isDirectory());
    this.folder = folder;
    reloadSubtree();
  }
  
  @Nonnull
  public NodeProjectGroup getGroup(){
    return (NodeProjectGroup)this.parent;
  }
  
}
