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

package com.igormaznitsa.mindmap.model.nio.impl;

import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.mindmap.model.IgnoreInAnimalSniffer;
import com.igormaznitsa.mindmap.model.nio.Path;
import com.igormaznitsa.mindmap.model.nio.PathService;
import java.io.File;
import javax.annotation.Nonnull;

@IgnoreInAnimalSniffer
public class J7PathService implements PathService {

  @Override
  @Nonnull
  public Path getForFile(@Nonnull final File file) {
    return new J7Path(file);
  }

  @Override
  @Nonnull
  public Path getForPathItems(@Nonnull final String first,
                              @Nonnull @MustNotContainNull final String... items) {
    return new J7Path(first, items);
  }

}
