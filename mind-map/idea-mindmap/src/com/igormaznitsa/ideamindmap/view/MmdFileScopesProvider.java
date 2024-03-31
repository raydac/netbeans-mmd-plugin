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

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.search.scope.packageSet.CustomScopesProviderEx;
import com.intellij.psi.search.scope.packageSet.NamedScope;
import java.util.List;
import com.intellij.openapi.project.Project;
import java.util.Collections;

public class MmdFileScopesProvider extends CustomScopesProviderEx {

  private static final Logger LOGGER = Logger.getInstance(MmdFileScopesProvider.class);

  public MmdFileScopesProvider(Project project) {
    // do nothing
  }

  public static MmdFileScopesProvider getInstance(Project project) {
    return CUSTOM_SCOPES_PROVIDER.findExtension(MmdFileScopesProvider.class, project);
  }

  public List<NamedScope> getCustomScopes() {
    try {
      return Collections.singletonList(MmdFileFilteredScope.makeInstance());
    } catch (Exception ex) {
      LOGGER.warn("Error during custom scope create", ex);
      return Collections.emptyList();
    }
  }

}
