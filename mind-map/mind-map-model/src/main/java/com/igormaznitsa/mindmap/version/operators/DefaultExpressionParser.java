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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Default expression parser for version validator.
 *
 * @since 1.0.0
 */
public final class DefaultExpressionParser implements ExpressionParser {

  private static final long serialVersionUID = 2676755912472640998L;

  private static final Pattern PATTERN_LEAF = Pattern.compile("(!=|>=|<=|>|<|=)\\s*(.*)");
  private static final Pattern PATTERN_OR = Pattern.compile("(.+);(.+)");
  private static final Pattern PATTERN_AND = Pattern.compile("(.+),(.+)");

  @Override
  public Operator parse(final String text) {
    Operator result = null;
    if (text != null) {
      final String trimmed = text.trim();

      final Matcher orMatcher = PATTERN_OR.matcher(trimmed);
      if (orMatcher.matches()) {
        result = new OperatorOr(parse(orMatcher.group(1)), parse(orMatcher.group(2)));
      } else {
        final Matcher andMatcher = PATTERN_AND.matcher(trimmed);
        if (andMatcher.matches()) {
          result = new OperatorAnd(parse(andMatcher.group(1)), parse(andMatcher.group(2)));
        } else {
          final Matcher leaf = PATTERN_LEAF.matcher(trimmed);
          if (leaf.matches()) {
            result = new OperatorLeaf(Condition.decode(leaf.group(1)), new Version(leaf.group(2)));
          } else {
            result = new OperatorLeaf(Condition.EQU, new Version(trimmed));
          }
        }
      }
    }
    return result;
  }
}
