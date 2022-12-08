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

package com.igormaznitsa.mindmap.plugins.api.parameters;

public class StringParameter extends AbstractParameter<String> {
  public StringParameter(final String id, final String title, final String comment, final String defaultValue, final int order) {
    super(id, title, comment, defaultValue, order);
  }

  public StringParameter(final String id, final String title, final String comment,
                         final String defaultValue) {
    this(id, title, comment, defaultValue, 0);
  }

  @Override
  public void fromString(final String value) {
    this.setValue(value == null ? "" : value);
  }

}
