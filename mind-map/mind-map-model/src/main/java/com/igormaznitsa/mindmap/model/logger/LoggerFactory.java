/*
 * Copyright 2015-2018 Igor Maznitsa.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.igormaznitsa.mindmap.model.logger;

import static java.util.Objects.requireNonNull;

import com.igormaznitsa.mindmap.model.logger.impl.JavaLoggerServiceImpl;
import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Logger factory.
 */
public final class LoggerFactory {
  private static final LoggerService LOGGER_SERVICE;

  static {
    final ServiceLoader<LoggerService> service =
        ServiceLoader.load(LoggerService.class, LoggerFactory.class.getClassLoader());
    service.reload();
    final Iterator<LoggerService> iterator = service.iterator();
    LOGGER_SERVICE = iterator.hasNext() ? iterator.next() : new JavaLoggerServiceImpl();
    LOGGER_SERVICE.getLogger(LoggerFactory.class)
        .info("Detected MindMap Logger Service: " + LOGGER_SERVICE.getClass().getName());
  }

  /**
   * Find or create logger for class.
   *
   * @param klazz class to be used as logger identifier, must not be null
   * @return found or created logger, must not be null
   */
  public static Logger getLogger(final Class<?> klazz) {
    return LOGGER_SERVICE.getLogger(requireNonNull(klazz));
  }

  /**
   * Find or create logger for name.
   *
   * @param name name to be used as logger identifier, must not be null
   * @return found or created logger, must not be null
   */
  public static Logger getLogger(final String name) {
    return LOGGER_SERVICE.getLogger(requireNonNull(name));
  }

}
