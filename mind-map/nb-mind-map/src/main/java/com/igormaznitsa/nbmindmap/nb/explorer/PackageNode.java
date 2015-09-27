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
package com.igormaznitsa.nbmindmap.nb.explorer;

import com.igormaznitsa.nbmindmap.utils.BadgeIcons;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.openide.nodes.Node;
import org.openide.util.lookup.ProxyLookup;

final class PackageNode extends AbstractMMFilter {

  private final Project project;

  
  public PackageNode(Project project, final Node originalNode) {
    super(originalNode, new FolderChildren(project, originalNode), new ProxyLookup(originalNode.getLookup()));
    this.project = project;
  }

  @Override
  public Action[] getActions(boolean context) {
//    final List<Action> actions = new ArrayList<>();
//    return actions.toArray(new Action[actions.size()]);
    return EMPTY_ACTION;
  }

  @Override
  public Image getIcon(final int type) {
    return BadgeIcons.getTreeFolderIcon(false);
  }

  @Override
  public Image getOpenedIcon(final int type) {
    return BadgeIcons.getTreeFolderIcon(true);
  }

}
