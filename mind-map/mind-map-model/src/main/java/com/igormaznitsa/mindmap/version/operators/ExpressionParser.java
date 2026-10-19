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

import com.igormaznitsa.mindmap.version.VersionValidator;

import java.io.Serializable;

/**
 * Interface to be implemented by an expression parser.
 *
 * @see VersionValidator
 * @see DefaultExpressionParser
 * @since 1.0.0
 */
public interface ExpressionParser extends Serializable {

  /**
   * Parse some text and return root operator for the parsed tree.
   *
   * @param expression text to be parsed as expression, it can be null
   * @return the parsed root tree operator, it can be null
   * @since 1.0.0
   */
  Operator parse(String expression);
}
