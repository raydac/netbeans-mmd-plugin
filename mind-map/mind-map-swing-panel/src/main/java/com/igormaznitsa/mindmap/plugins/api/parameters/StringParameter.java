package com.igormaznitsa.mindmap.plugins.api.parameters;

public class StringParameter extends AbstractParameter<String> {
  public StringParameter(final String id, final String title, final String comment, final String defaultValue) {
    super(id, title, comment, defaultValue);
  }

  @Override
  public void fromString(final String value) {
    this.setValue(value == null ? "" : value);
  }

}
