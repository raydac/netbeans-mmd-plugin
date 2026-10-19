/*
 * Copyright (C) 2015-2026 Igor A. Maznitsa
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

package com.igormaznitsa.mindmap.version.operators;

import com.igormaznitsa.mindmap.version.Version;

import java.io.Serializable;

/**
 * Common internal interface for operators.
 *
 * @since 1.0.0
 */
public interface Operator extends Serializable {
  /**
   * Check version by the operator.
   *
   * @param version version to be checked, it can be null.
   * @return true if the version is valid, false otherwise (or if the version is null)
   * @since 1.0.0
   */
  boolean isValid(Version version);
}
