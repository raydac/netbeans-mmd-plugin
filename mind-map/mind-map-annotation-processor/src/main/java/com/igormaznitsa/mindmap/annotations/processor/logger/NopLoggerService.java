package com.igormaznitsa.mindmap.annotations.processor.logger;

import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerAdapter;
import com.igormaznitsa.mindmap.model.logger.LoggerService;

/**
 * NOP logger service to hide messages from mind map model builder.
 */
public class NopLoggerService implements LoggerService {

  private static final Logger INSTANCE = new LoggerAdapter("NOP");

  @Override
  public Logger getLogger(final Class<?> loggerClass) {
    return INSTANCE;
  }

  @Override
  public Logger getLogger(final String name) {
    return INSTANCE;
  }
}
