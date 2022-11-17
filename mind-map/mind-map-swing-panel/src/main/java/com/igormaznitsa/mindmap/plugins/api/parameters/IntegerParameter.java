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
