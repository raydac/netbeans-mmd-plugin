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

package com.igormaznitsa.mindmap.plugins.api;

import com.igormaznitsa.meta.annotation.MustNotContainNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Interface allows to set and get properties.
 *
 * @since 1.3.1
 */
public interface HasOptions {
  boolean doesSupportKey(@Nonnull final String key);

  @Nonnull
  @MustNotContainNull
  String[] getOptionKeys();

  @Nonnull
  String getOptionKeyDescription(@Nonnull String key);

  void setOption(@Nonnull String key, @Nullable String value);

  @Nullable
  String getOption(@Nonnull String key);
}
