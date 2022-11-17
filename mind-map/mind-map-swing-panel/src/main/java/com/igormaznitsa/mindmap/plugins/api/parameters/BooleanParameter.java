package com.igormaznitsa.mindmap.plugins.api.parameters;

public class BooleanParameter extends AbstractParameter<Boolean>{
  public BooleanParameter(final String id, final String title, final String comment, final boolean defaultValue) {
    super(id, title, comment, defaultValue);
  }

  @Override
  public void fromString(final String value) {
    this.setValue(value != null && Boolean.parseBoolean(value.trim()));
  }
}
