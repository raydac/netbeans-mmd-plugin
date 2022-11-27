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

public class IntegerParameter extends AbstractParameter<Long> {

  private final long min;
  private final long max;

  public IntegerParameter(final String id, final String title, final String comment, final long min, final long max,
                          final long defaultValue) {
    super(id, title, comment, defaultValue);
    this.min = min;
    this.max = max;
  }

  public long getMin() {
    return this.min;
  }

  public long getMax() {
    return this.max;
  }
  @Override
  public void fromString(final String value) {
    try{
      this.setValue(value == null ? this.min : Long.parseLong(value.trim()));
    }catch (NumberFormatException ex){
      this.setValue(this.min);
    }
  }

  @Override
  public void setValue(final Long value) {
    super.setValue(Math.min(this.max, Math.max(this.min, value)));
  }
}
