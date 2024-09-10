/*
 * Copyright (C) 2015-2024 Igor A. Maznitsa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.igormaznitsa.ksrender;

import java.util.Locale;

public enum IdType {
  STORES,
  TOPICS,
  OTHERS;

  public static IdType find(final String elementType) {
    final String normalized = elementType.trim().toLowerCase(Locale.ENGLISH);
    if (normalized.startsWith("topic")) {
      return TOPICS;
    }
    if (normalized.startsWith("store")) {
      return STORES;
    }
    return OTHERS;
  }
}
