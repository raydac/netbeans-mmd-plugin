/*
 * Copyright 2024 Igor Maznitsa.
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

import com.igormaznitsa.ideamindmap.utils.AllIcons;
import com.intellij.openapi.vfs.VirtualFileFilter;
import com.intellij.psi.search.scope.packageSet.FilteredNamedScope;
import java.util.Locale;

public final class MmdFileFilteredScope {

  private static final String TITLE = "SciaReto Mind Maps";
  private static final int PRIORITY = Integer.MAX_VALUE;
  private static final VirtualFileFilter MMD_FILE_FILTER = virtualFile -> virtualFile != null
      && !virtualFile.isDirectory()
      && virtualFile.getName().toLowerCase(Locale.ENGLISH).endsWith(".mmd");

  private MmdFileFilteredScope() {
  }

  public static FilteredNamedScope makeInstance() {
    return new FilteredNamedScope(TITLE, () -> TITLE, AllIcons.Logo.MINDMAP, PRIORITY, MMD_FILE_FILTER);
  }
}
