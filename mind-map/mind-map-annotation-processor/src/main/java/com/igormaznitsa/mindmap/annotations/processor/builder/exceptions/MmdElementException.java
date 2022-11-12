package com.igormaznitsa.mindmap.annotations.processor.builder.exceptions;

import static java.util.Objects.requireNonNull;

import javax.lang.model.element.Element;

public class MmdElementException extends Exception {
  private final Element element;

  public MmdElementException(final String message, final Element source) {
    this(message, source, null);
  }

  public MmdElementException(final String message, final Element source, final Throwable cause) {
    super(message, cause);
    this.element = requireNonNull(source);
  }

  public Element getSource() {
    return this.element;
  }

  @Override
  public String toString() {
    return "MmdElementException{" + "element=" + this.element + '}';
  }
}
