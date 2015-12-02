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
package com.igormaznitsa.ideamindmap.view;

import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import com.intellij.ui.TreeSpeedSearch;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.util.StringTokenizer;

final class SpeedSearch extends TreeSpeedSearch {
  SpeedSearch(JTree tree) {
    super(tree);
  }

  @Override
  protected boolean isMatchingElement(Object element, String pattern) {
    Object userObject = ((DefaultMutableTreeNode) ((TreePath) element).getLastPathComponent()).getUserObject();
    if (userObject instanceof PsiDirectoryNode) {
      String str = getElementText(element);
      if (str == null)
        return false;
      str = str.toLowerCase();
      if (pattern.indexOf('.') >= 0) {
        return compare(str, pattern);
      }
      StringTokenizer tokenizer = new StringTokenizer(str, ".");
      while (tokenizer.hasMoreTokens()) {
        String token = tokenizer.nextToken();
        if (compare(token, pattern)) {
          return true;
        }
      }
      return false;
    }
    else {
      return super.isMatchingElement(element, pattern);
    }
  }
}
