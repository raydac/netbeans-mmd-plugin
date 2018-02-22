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
package com.igormaznitsa.nbmindmap.nb.explorer;

import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;

class FolderChildren extends FilterNode.Children {

  private final Project project;

  private static final Logger LOGGER = LoggerFactory.getLogger(FolderChildren.class);
  
  FolderChildren(final Project project, final Node originalNode) {
    super(originalNode);
    this.project = project;
  }

  @Override
  protected Node[] createNodes(final Node key) {
    return super.createNodes(key);
  }

  @Override
  protected Node copyNode(final Node originalNode) {
    final FileObject fo = originalNode.getLookup().lookup(FileObject.class);
    if (fo == null) {
      LOGGER.warn(String.format("No fileobject found for node: %s", originalNode));
      return super.copyNode(originalNode);
    }
    if (fo.isFolder()) {
      return new PackageNode(this.project, originalNode);
    }
    
    return new ObjectNode(originalNode);
  }

}
