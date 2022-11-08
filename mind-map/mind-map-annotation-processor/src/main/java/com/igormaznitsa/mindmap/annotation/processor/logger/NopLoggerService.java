package com.igormaznitsa.mindmap.annotation.processor.logger;

import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerService;

public class NopLoggerService implements LoggerService {
  @Override
  public Logger getLogger(final Class<?> klazz) {
    return new Logger("") {
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
    };
  }

  @Override
  public Logger getLogger(final String name) {
    return this.getLogger(String.class);
  }
}
