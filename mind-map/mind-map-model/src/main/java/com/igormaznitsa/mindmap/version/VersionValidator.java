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

package com.igormaznitsa.mindmap.version;

import com.igormaznitsa.mindmap.version.operators.DefaultExpressionParser;
import com.igormaznitsa.mindmap.version.operators.ExpressionParser;
import com.igormaznitsa.mindmap.version.operators.Operator;

import java.io.Serializable;

/**
 * Class allows to define rules to validate versions. It supports logical AND(,) and OR(;) operators. OR has less priority(!)
 *
 * @since 1.0.0
 */
@SuppressWarnings("GrazieInspection")
public final class VersionValidator implements Serializable {

  private static final long serialVersionUID = 641987018021820537L;
  private static final ExpressionParser DEFAULT_EXPRESSION_PARSER = new DefaultExpressionParser();
  private final Operator expressionRoot;

  /**
   * Make validator based on parsed expression.
   *
   * @param expressionRoot operator to be used as the root of expression tree, it can be null
   * @since 1.0.0
   */
  public VersionValidator(final Operator expressionRoot) {
    this.expressionRoot = expressionRoot;
  }

  /**
   * Make validator from string with default expression parser.
   *
   * @param expression expression for validator, it can be null but in the case the result will be false every time
   * @see DefaultExpressionParser
   * @since 1.0.0
   */
  public VersionValidator(final String expression) {
    this(expression, DEFAULT_EXPRESSION_PARSER);
  }

  /**
   * Make validator with custom expression parser.
   *
   * @param expression text of expression to be parsed, it can be null
   * @param parser     parser to parse expression, it must not be null
   * @see DefaultExpressionParser
   * @since 1.0.0
   */
  public VersionValidator(final String expression, final ExpressionParser parser) {
    this(expression == null ? null : parser.parse(expression));
  }

  /**
   * Get the parsed expression tree root.
   *
   * @return the expression tree root, it can be null
   * @since 1.0.0
   */
  public Operator getExpressionRoot() {
    return this.expressionRoot;
  }

  /**
   * Validate version for the rule.
   *
   * @param version the version to be checked, it can be null
   * @return true if the version is valid or false if the version is null or not valid from point of view the rule.
   * @since 1.0.0
   */
  public boolean isValid(final Version version) {
    boolean result = false;
    if (this.expressionRoot != null) {
      result = this.expressionRoot.isValid(version);
    }
    return result;
  }

  @Override
  public String toString() {
    return VersionValidator.class.getSimpleName() +
        (this.expressionRoot == null ? "[]" : '[' + this.expressionRoot.toString() + ']');
  }

}
