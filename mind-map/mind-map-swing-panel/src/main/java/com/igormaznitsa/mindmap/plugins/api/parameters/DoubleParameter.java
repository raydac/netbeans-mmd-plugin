package com.igormaznitsa.mindmap.plugins.api.parameters;

public class DoubleParameter extends AbstractParameter<Double>{

  private final double min;
  private final double max;

  public DoubleParameter(final String id, final String title, final String comment, final double min, final double max, final double defaultValue) {
    super(id, title, comment, defaultValue);
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
    super.setValue(Math.min(this.max, Math.max(this.min, value)));
  }
}
