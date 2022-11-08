package com.igormaznitsa.mindmap.model.logger;

import static java.util.Objects.requireNonNull;

/**
 * Just adapter for abstract logger class.
 */
public class LoggerAdapter extends Logger {

  public LoggerAdapter(final String namme) {
    super(requireNonNull(namme));
  }

  public LoggerAdapter(final Class<?> klazz) {
    super(requireNonNull(klazz));
  }

  @Override
  public void info(final String message) {

  }

  @Override
  public void warn(final String message) {

  }

  @Override
  public void error(final String message) {

  }

  @Override
  public void error(final String message, final Throwable error) {

  }
}
