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

import com.intellij.openapi.vfs.VirtualFileFilter;
import com.igormaznitsa.ideamindmap.utils.AllIcons;
import com.intellij.psi.search.scope.packageSet.FilteredNamedScope;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

public final class MmdFileFilteredScope {

  private static final String TITLE = "SciaReto Mind Maps";
  private static final int PRIORITY = Integer.MAX_VALUE;
  private static final VirtualFileFilter MMD_FILE_FILTER = virtualFile -> virtualFile != null
      && !virtualFile.isDirectory()
      && virtualFile.getName().toLowerCase(Locale.ENGLISH).endsWith(".mmd");

  private MmdFileFilteredScope() {

  }

  public static FilteredNamedScope makeInstance() throws Exception {
    final List<Constructor<?>> constructorList =
        List.of(FilteredNamedScope.class.getConstructors());
    final Constructor<?> constructor = constructorList
        .stream()
        .filter(x -> x.getParameterCount() == 4 || x.getParameterCount() == 5)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException(
            "Can't find FilteredNamedScope constructor with either 4 or 5 parameters"));

    if (constructor.getParameterCount() == 4) {
      return (FilteredNamedScope) constructor.newInstance(TITLE, AllIcons.Logo.MINDMAP, PRIORITY,
          MMD_FILE_FILTER);
    } else {
      return (FilteredNamedScope) constructor.newInstance(TITLE, new Supplier<String>() {
        @Override
        public String get() {
          return TITLE;
        }
      }, AllIcons.Logo.MINDMAP, PRIORITY, MMD_FILE_FILTER);
    }
  }
}
