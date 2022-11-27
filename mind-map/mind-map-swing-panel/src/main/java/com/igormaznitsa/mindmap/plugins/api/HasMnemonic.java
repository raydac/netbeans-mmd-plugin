/*
 * Copyright (C) 2015-2022 Igor A. Maznitsa
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

package com.igormaznitsa.mindmap.plugins.api;

/**
 * Shows that plug-in provides its mnemonic name.
 *
 * @since 1.3.1
 */
public interface HasMnemonic {
  /**
   * Returns mnemocode.
   *
   * @return mnemocode which must be in lower-case, only Latin symbols and can't contain spaces or special chars, it can provide null.
   */
  String getMnemonic();
}
