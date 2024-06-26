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

public class DoubleParameter extends AbstractParameter<Double>{

  private final double min;
  private final double max;

  public DoubleParameter(final String id, final String title, final String comment, final double min, final double max, final double defaultValue) {
    this(id, title, comment, min, max, defaultValue, 0);
  }

  public DoubleParameter(
      final String id,
      final String title,
      final String comment,
      final double min,
      final double max,
      final double defaultValue,
      final int order
  ) {
    this(id, title, comment, defaultValue, order, min, max, Importance.MAIN);
  }

  public DoubleParameter(String id, String title, String comment,
                         Double defaultValue, int order,
                         double min, double max, Importance importance) {
    super(id, title, comment, defaultValue, order, importance);
    this.min = min;
    this.max = max;
  }

  @Override
  public void fromString(final String value) {
    try{
      this.setValue(value == null ? this.min : Double.parseDouble(value.trim()));
    }catch (NumberFormatException ex){
      this.setValue(this.min);
    }
  }

  public double getMin() {
    return this.min;
  }

  public double getMax() {
    return this.max;
  }

  @Override
  public void setValue(final Double value) {
    if (value == null) {
      super.setValue(this.min);
    } else {
      super.setValue(Math.min(this.max, Math.max(this.min, value)));
    }
  }
}
